package net.TheDgtl.Stargate;

import net.TheDgtl.Stargate.util.FileHelper;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.logging.Level;

/**
 * The language manager is responsible for translating various messages
 */
public class LanguageManager {

    private final File languageFolder;
    private String language;
    private EnumMap<TranslatableMessage, String> translatedStrings;
    private final EnumMap<TranslatableMessage, String> backupStrings;

    private final Stargate stargate;
    private static final String UTF8_BOM = "\uFEFF";

    /**
     * Instantiates a new language loader
     *
     * @param stargate       <p>A reference to an instance of the main Stargate class</p>
     * @param languageFolder <p>The folder containing all language files</p>
     * @param language       <p>The language to use for all strings</p>
     */
    public LanguageManager(Stargate stargate, String languageFolder, String language) {
        String defaultLanguage = "en-US";

        this.stargate = stargate;

        translatedStrings = loadLanguage(language);
        backupStrings = loadLanguage(defaultLanguage);
        this.languageFolder = new File(languageFolder);
    }

    /**
     * Gets a translated message
     *
     * <p>The stargate prefix and the appropriate color is added to the returned translated message.</p>
     *
     * @param key     <p>The translatable message to display</p>
     * @param isError <p>Whether the message should be formatted as an error message</p>
     * @return <p>A translated and formatted message</p>
     */
    private String compileMessage(TranslatableMessage key, boolean isError) {
        String prefix = (isError ? ChatColor.RED : ChatColor.GREEN) + getString(TranslatableMessage.PREFIX);
        String message = getString(key).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        return prefix + ChatColor.WHITE + message;
    }

    public String getErrorMessage(TranslatableMessage key) {
        return compileMessage(key, true);
    }

    public String getMessage(TranslatableMessage key) {
        return compileMessage(key, false);
    }

    /**
     * Gets a translated string
     *
     * @param key <p>The translatable message to translate</p>
     * @return <p>The corresponding translated message</p>
     */
    public String getString(TranslatableMessage key) {
        String translatedMessage = translatedStrings.get(key);
        if (translatedMessage == null) {
            translatedMessage = backupStrings.get(key);
        }
        if (translatedMessage == null) {
            Stargate.log(Level.WARNING, String.format("Unable to find %s in the backup language file", key));
            return key.getMessageKey();
        }
        return translatedMessage;
    }

    /**
     * Loads the given language
     *
     * @param language <p>The language to load</p>
     * @return <p>The map of translatable messages to translated strings for the loaded language</p>
     */
    private EnumMap<TranslatableMessage, String> loadLanguage(String language) {
        try {
            return loadLanguageFile(language);
        } catch (IOException exception) {
            Stargate.log(Level.WARNING, String.format("Unable to load the language file for %s", language));
            return new EnumMap<>(TranslatableMessage.class);
        }
    }

    /**
     * Gets the currently used/loaded language
     *
     * @return <p>The currently used language</p>
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the currently used language
     *
     * <p>Sets the language and loads everything from the language file</p>
     *
     * @param language <p>The language to change to</p>
     */
    public void setLanguage(String language) {
        this.language = language;
        translatedStrings = loadLanguage(language);
    }

    /**
     * Loads the language file of the given language
     *
     * @param language
     *                 <p>
     *                 The language to load
     *                 </p>
     * @return
     *         <p>
     *         The translatable messages found in the language file
     *         </p>
     * @throws IOException
     *                     <p>
     *                     If unable to read the language file
     *                     </p>
     */
    private EnumMap<TranslatableMessage, String> loadLanguageFile(String language) throws IOException {
        File[] possibleLanguageFiles = findTargetFiles(language, this.languageFolder);

        File endFile = null;

        for (int i = 0; i < possibleLanguageFiles.length; i++) {
            if (!possibleLanguageFiles[i].exists()) {
                try {
                    File path = new File("lang");
                    File internalLanguageFile = findTargetFiles(language, path)[i];
                    Stargate.log(Level.FINE,
                            String.format("Saving languagefile from internal path %s", internalLanguageFile.getPath()));
                    stargate.saveResource(internalLanguageFile.getPath(), false);
                    endFile = possibleLanguageFiles[i];
                    break;
                } catch (IllegalArgumentException ignored) {
                    continue;
                }

            }
            endFile = possibleLanguageFiles[i];

        }

        if (endFile == null) {
            Stargate.log(Level.SEVERE, String.format("The selected language, \"%s\", is not supported, and no "
                    + "custom language file exists. Falling back to English.", language));
            return new EnumMap<>(TranslatableMessage.class);
        }

        BufferedReader bufferedReader = FileHelper.getBufferedReader(endFile);
        EnumMap<TranslatableMessage, String> output = readLanguageReader(bufferedReader);
        try {
            bufferedReader.close();
        } catch (IOException ignored) {
        }

        return output;
    }
    
    
    
    private File[] findTargetFiles(String language, File path) {
        String[] langSplited = language.split("-");
        
        String[] possibleNames;
        if(langSplited.length > 1)
            possibleNames = new String[]{language, langSplited[0]};
        else
            possibleNames = new String[]{language};
        
        File[] possibleFiles = new File[possibleNames.length*possibleNames.length];
        int i = 0;
        for(String pathName : possibleNames) {
            File dir = new File(path,pathName);
            for(String fileName : possibleNames) {
                possibleFiles[i++] = new File(dir,fileName);
            }
        }
        return possibleFiles;
    }

    /**
     * Reads from a buffered reader and puts the found key->value pairs in an enum map
     *
     * @param bufferedReader <p>The buffered reader to read</p>
     * @return <p>The found message translations</p>
     * @throws IOException <p>If unable to read from the given buffered reader</p>
     */
    private EnumMap<TranslatableMessage, String> readLanguageReader(BufferedReader bufferedReader) throws IOException {
        EnumMap<TranslatableMessage, String> output = new EnumMap<>(TranslatableMessage.class);

        String line = bufferedReader.readLine();
        line = removeUTF8BOM(line);
        while (line != null) {
            // Split at first "="
            int equalsIndex = line.indexOf('=');
            if (equalsIndex == -1) {
                line = bufferedReader.readLine();
                continue;
            }
            TranslatableMessage key = TranslatableMessage.parse(line.substring(0, equalsIndex));
            if (key == null) {
                Stargate.log(Level.CONFIG, "Skipping line: " + line);
                line = bufferedReader.readLine();
                continue;
            }
            String val = ChatColor.translateAlternateColorCodes('&', line.substring(equalsIndex + 1));
            output.put(key, val);
            line = bufferedReader.readLine();
        }

        return output;
    }

    /**
     * Removes a UTF-8 byte order mark from a text string as it may mess up parsing
     *
     * @param text <p>The text to remove the byte order mark from</p>
     * @return <p>The input string without a byte order mark</p>
     */
    private String removeUTF8BOM(String text) {
        if (text.startsWith(UTF8_BOM)) {
            text = text.substring(1);
        }
        return text;
    }

}
