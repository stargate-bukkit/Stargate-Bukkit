package net.TheDgtl.Stargate;

import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.logging.Level;

/**
 * The language manager is responsible for translating various messages
 */
public class LanguageManager {

    private final String languageFolder;
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
        this.languageFolder = languageFolder;
        String defaultLanguage = "en";

        this.stargate = stargate;

        translatedStrings = loadLanguage(language);
        backupStrings = loadLanguage(defaultLanguage);
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
    public String getMessage(TranslatableMessage key, boolean isError) {
        String prefix = (isError ? ChatColor.RED : ChatColor.GREEN) + translatedStrings.get(TranslatableMessage.PREFIX);
        String message = getString(key).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        return prefix + ChatColor.WHITE + message;
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
            return key.getString();
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
     * @param language <p>The language to load</p>
     * @return <p>The translatable messages found in the language file</p>
     * @throws IOException <p>If unable to read the language file</p>
     */
    private EnumMap<TranslatableMessage, String> loadLanguageFile(String language) throws IOException {
        File languageFile = new File(languageFolder, language + ".txt");
        if (!languageFile.exists()) {
            stargate.saveResource("lang/" + language + ".txt", false);
        }

        BufferedReader bufferedReader = getBufferedReader(languageFile);
        EnumMap<TranslatableMessage, String> output = readLanguageReader(bufferedReader);
        try {
            bufferedReader.close();
        } catch (IOException ignored) {
        }

        return output;
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
     * Gets a buffered reader for reading the given language file
     *
     * @param languageFile <p>The language file to read</p>
     * @return <p>A buffered reader for reading the language file</p>
     * @throws FileNotFoundException <p></p>
     */
    private BufferedReader getBufferedReader(File languageFile) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(languageFile);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return new BufferedReader(inputStreamReader);
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
