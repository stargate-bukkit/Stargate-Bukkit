package net.TheDgtl.Stargate;

import net.TheDgtl.Stargate.util.FileHelper;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * The language manager is responsible for translating various messages
 */
public class LanguageManager {

    private final File languageFolder;
    private String language;
    private Map<TranslatableMessage, String> translatedStrings;
    private final Map<TranslatableMessage, String> backupStrings;

    private final Stargate stargate;

    private static final Map<String, String> LANGUAGE_EDGE_CASES = new HashMap<>();

    static {
        FileHelper.readInternalFileToMap("/language-edge-cases.properties", LANGUAGE_EDGE_CASES);
    }

    /**
     * Instantiates a new language manager
     *
     * @param stargate       <p>A reference to an instance of the main Stargate class</p>
     * @param languageFolder <p>The folder containing all language files</p>
     */
    public LanguageManager(Stargate stargate, File languageFolder) {
        String defaultLanguage = "en-US";
        this.languageFolder = languageFolder;

        this.stargate = stargate;
        backupStrings = loadLanguage(defaultLanguage);
    }

    /**
     * Gets a formatted error message
     *
     * @param translatableMessage <p>The translatable message to display as an error</p>
     * @return <p>The formatted error message</p>
     */
    public String getErrorMessage(TranslatableMessage translatableMessage) {
        return formatMessage(translatableMessage, true);
    }

    /**
     * Gets a formatted message
     *
     * @param translatableMessage <p>The translatable message to display</p>
     * @return <p>The formatted message</p>
     */
    public String getMessage(TranslatableMessage translatableMessage) {
        return formatMessage(translatableMessage, false);
    }

    /**
     * Gets a translated string
     *
     * @param translatableMessage <p>The translatable message to translate</p>
     * @return <p>The corresponding translated message</p>
     */
    public String getString(TranslatableMessage translatableMessage) {
        String translatedMessage = translatedStrings.get(translatableMessage);
        if (translatedMessage == null) {
            translatedMessage = backupStrings.get(translatableMessage);
        }
        if (translatedMessage == null) {
            Stargate.log(Level.WARNING, String.format("Unable to find %s in the backup language file", translatableMessage));
            return translatableMessage.getMessageKey();
        }
        return translatedMessage;
    }

    /**
     * Loads the given language
     *
     * @param language <p>The language to load</p>
     * @return <p>The map of translatable messages to translated strings for the loaded language</p>
     */
    private Map<TranslatableMessage, String> loadLanguage(String language) {
        try {
            return loadLanguageFile(language);
        } catch (IOException exception) {
            Stargate.log(Level.WARNING, String.format("Unable to load the language file for %s", language));
            return new EnumMap<>(TranslatableMessage.class);
        }
    }

    /**
     * Sets the currently used language
     *
     * <p>Sets the language and loads everything from the language file</p>
     *
     * @param language <p>The language to change to</p>
     */
    public void setLanguage(String language) {
        if (LANGUAGE_EDGE_CASES.get(language) != null) {
            language = LANGUAGE_EDGE_CASES.get(language);
        }
        // Only update language if it has actually changed
        if (!language.equals(this.language)) {
            this.language = language;
            translatedStrings = loadLanguage(language);
        }
    }

    /**
     * Gets a formatted translated message
     *
     * <p>The stargate prefix and the appropriate color is added to the returned translated message.</p>
     *
     * @param translatableMessage <p>The translatable message to display</p>
     * @param isError             <p>Whether the message should be formatted as an error message</p>
     * @return <p>A translated and formatted message</p>
     */
    private String formatMessage(TranslatableMessage translatableMessage, boolean isError) {
        String prefix = (isError ? ChatColor.RED : ChatColor.GREEN) + getString(TranslatableMessage.PREFIX);
        String message = getString(translatableMessage).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        return prefix + ChatColor.WHITE + message;
    }

    /**
     * Loads the language file of the given language
     *
     * @param language <p>The language to load</p>
     * @return <p>The translatable messages found in the language file</p>
     * @throws IOException <p>If unable to read the language file</p>
     */
    private Map<TranslatableMessage, String> loadLanguageFile(String language) throws IOException {
        File[] possibleLanguageFiles = findTargetFiles(language, this.languageFolder);
        File endFile = null;

        for (int i = 0; i < possibleLanguageFiles.length; i++) {
            if (!possibleLanguageFiles[i].exists()) {
                Stargate.log(Level.FINEST, String.format("%s did not exist, checking if there's an internal file with this name", possibleLanguageFiles[i].getPath()));
                try {
                    File path = new File("lang");
                    File internalLanguageFile = findTargetFiles(language, path)[i];
                    Stargate.log(Level.FINE,
                            String.format("Saving language file from internal path %s", internalLanguageFile.getPath()));
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
            Stargate.log(Level.WARNING, String.format("The selected language, \"%s\", is not supported, and no "
                    + "custom language file exists. Falling back to English.", language));
            return new EnumMap<>(TranslatableMessage.class);
        }

        BufferedReader bufferedReader = FileHelper.getBufferedReader(endFile);
        Map<TranslatableMessage, String> output = readLanguageReader(bufferedReader);
        try {
            bufferedReader.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return output;
    }

    /**
     * Finds all relevant language files in the given location
     *
     * @param language <p>The selected language to find relevant files for</p>
     * @param path     <p>The path to search for language files</p>
     * @return <p>The found language files</p>
     */
    private File[] findTargetFiles(String language, File path) {
        String[] splitLanguage = language.split("-");

        String[] possibleNames;
        if (splitLanguage.length > 1) {
            possibleNames = new String[]{language, splitLanguage[0]};
        } else {
            possibleNames = new String[]{language};
        }

        File[] possibleFiles = new File[possibleNames.length * possibleNames.length];
        int i = 0;
        for (String pathName : possibleNames) {
            File dir = new File(path, pathName);
            for (String fileName : possibleNames) {
                possibleFiles[i++] = new File(dir, fileName + ".txt");
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
    private Map<TranslatableMessage, String> readLanguageReader(BufferedReader bufferedReader) throws IOException {
        Map<TranslatableMessage, String> output = new EnumMap<>(TranslatableMessage.class);

        String line = bufferedReader.readLine();
        line = FileHelper.removeUTF8BOM(line);
        while (line != null) {
            // Split at first "="
            int equalsIndex = line.indexOf('=');
            if (equalsIndex == -1) {
                line = bufferedReader.readLine();
                continue;
            }
            TranslatableMessage key = TranslatableMessage.parse(line.substring(0, equalsIndex));
            if (key == null) {
                Stargate.log(Level.FINER, "Skipping language prompt: " + line);
                line = bufferedReader.readLine();
                continue;
            }
            String value = ChatColor.translateAlternateColorCodes('&', line.substring(equalsIndex + 1));
            output.put(key, value);
            line = bufferedReader.readLine();
        }

        return output;
    }

}
