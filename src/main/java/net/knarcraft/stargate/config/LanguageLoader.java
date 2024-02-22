package net.knarcraft.stargate.config;

import net.knarcraft.knarlib.property.ColorConversion;
import net.knarcraft.knarlib.util.FileHelper;
import net.knarcraft.stargate.Stargate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * This class is responsible for loading all strings which are translated into several languages
 */
public final class LanguageLoader {

    private final String languageFolder;
    private final Map<Message, String> loadedBackupStrings;
    private String chosenLanguage;
    private Map<Message, String> loadedStringTranslations;

    /**
     * Instantiates a new language loader
     *
     * <p>This will only load the backup language. Set the chosen language and reload afterwards.</p>
     *
     * @param languageFolder <p>The folder containing the language files</p>
     */
    public LanguageLoader(@NotNull String languageFolder) {
        this.languageFolder = languageFolder;
        File testFile = new File(languageFolder, "en.txt");
        if (!testFile.exists()) {
            if (testFile.getParentFile().mkdirs()) {
                Stargate.debug("LanguageLoader", "Created language folder");
            }
        }

        //Load english as backup language in case the chosen language is missing newly added text strings
        InputStream inputStream = FileHelper.getInputStreamForInternalFile("/lang/en.txt");
        if (inputStream != null) {
            loadedBackupStrings = load("en", inputStream);
        } else {
            loadedBackupStrings = null;
            Stargate.logSevere("Error loading backup language. " +
                    "There may be missing text in-game");
        }
    }

    /**
     * Reloads languages from the files on disk
     */
    public void reload() {
        //Extracts/Updates the language as needed
        updateLanguage(chosenLanguage);
        loadedStringTranslations = load(chosenLanguage);
    }

    /**
     * Gets the string to display given its message key
     *
     * @param message <p>The message to display</p>
     * @return <p>The message in the user's preferred language</p>
     */
    @NotNull
    public String getString(@NotNull Message message) {
        String value = null;
        if (loadedStringTranslations != null) {
            value = loadedStringTranslations.get(message);
        }
        if (value == null) {
            value = getBackupString(message);
        }
        return value;
    }

    /**
     * Gets the string to display given its message key
     *
     * @param message <p>The message to display</p>
     * @return <p>The string in the backup language (English)</p>
     */
    @NotNull
    public String getBackupString(@NotNull Message message) {
        String value = null;
        if (loadedBackupStrings != null) {
            value = loadedBackupStrings.get(message);
        }
        if (value == null) {
            return "";
        }
        return value;
    }

    /**
     * Sets the chosen plugin language
     *
     * @param chosenLanguage <p>The new plugin language</p>
     */
    public void setChosenLanguage(@NotNull String chosenLanguage) {
        this.chosenLanguage = chosenLanguage;
    }

    /**
     * Updates files in the plugin directory with contents from the compiled .jar
     *
     * @param language <p>The language to update</p>
     */
    private void updateLanguage(@NotNull String language) {
        Map<Message, String> currentLanguageValues = load(language);

        InputStream inputStream = getClass().getResourceAsStream("/lang/" + language + ".txt");
        if (inputStream == null) {
            Stargate.logInfo(String.format("The language %s is not available. Falling back to english, You can add a " +
                    "custom language by creating a new text file in the lang directory.", language));
            Stargate.debug("LanguageLoader::updateLanguage", String.format("Unable to load /lang/%s.txt", language));
            return;
        }

        try {
            readChangedLanguageStrings(inputStream, language, currentLanguageValues);
        } catch (IOException exception) {
            Stargate.logSevere("Unable to read language strings! Message: " + exception.getMessage());
        }
    }

    /**
     * Reads language strings
     *
     * @param inputStream           <p>The input stream to read from</p>
     * @param language              <p>The selected language</p>
     * @param currentLanguageValues <p>The current values of the loaded/processed language</p>
     * @throws IOException <p>if unable to read a language file</p>
     */
    private void readChangedLanguageStrings(@NotNull InputStream inputStream, @NotNull String language,
                                            @Nullable Map<Message, String> currentLanguageValues) throws IOException {
        //Get language values
        BufferedReader bufferedReader = FileHelper.getBufferedReaderFromInputStream(inputStream);
        Map<Message, String> internalLanguageValues = fromStringMap(FileHelper.readKeyValuePairs(bufferedReader,
                "=", ColorConversion.NORMAL));

        //If currentLanguageValues is null; the chosen language has not been used before
        if (currentLanguageValues == null) {
            updateLanguageFile(language, internalLanguageValues, null);
            Stargate.logInfo(String.format("Language (%s) has been loaded", language));
            return;
        }

        //If a key is not found in the language file, add the one in the internal file. Must update the external file
        if (!internalLanguageValues.keySet().equals(currentLanguageValues.keySet())) {
            Map<Message, String> newLanguageValues = new EnumMap<>(Message.class);
            boolean updateNecessary = false;
            for (Map.Entry<Message, String> entry : internalLanguageValues.entrySet()) {
                Message key = entry.getKey();
                String value = entry.getValue();

                if (currentLanguageValues.get(key) == null) {
                    newLanguageValues.put(key, value);
                    //Found at least one value in the internal file not in the external file. Need to update
                    updateNecessary = true;
                } else {
                    newLanguageValues.put(key, currentLanguageValues.get(key));
                    currentLanguageValues.remove(key);
                }
            }
            //Update the file itself
            if (updateNecessary) {
                updateLanguageFile(language, newLanguageValues, currentLanguageValues);
                Stargate.logInfo(String.format("Your language file (%s.txt) has been updated", language));
            }
        }
    }

    /**
     * Updates the language file for a given language
     *
     * @param language              <p>The language to update</p>
     * @param languageStrings       <p>The updated language strings</p>
     * @param customLanguageStrings <p>Any custom language strings not recognized</p>
     * @throws IOException <p>If unable to write to the language file</p>
     */
    private void updateLanguageFile(@NotNull String language, @NotNull Map<Message, String> languageStrings,
                                    @Nullable Map<Message, String> customLanguageStrings) throws IOException {
        BufferedWriter bufferedWriter = FileHelper.getBufferedWriterFromString(languageFolder + language + ".txt");

        //Output normal Language data
        for (Map.Entry<Message, String> entry : languageStrings.entrySet()) {
            bufferedWriter.write(entry.getKey() + "=" + entry.getValue());
            bufferedWriter.newLine();
        }
        bufferedWriter.newLine();
        //Output any custom language strings the user had
        if (customLanguageStrings != null) {
            for (Map.Entry<Message, String> entry : customLanguageStrings.entrySet()) {
                bufferedWriter.write(entry.getKey() + "=" + entry.getValue());
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.close();
    }

    /**
     * Loads the given language
     *
     * @param lang <p>The language to load</p>
     * @return <p>A mapping between loaded string indexes and the strings to display</p>
     */
    private Map<Message, String> load(@NotNull String lang) {
        return load(lang, null);
    }

    /**
     * Loads the given language
     *
     * @param lang        <p>The language to load</p>
     * @param inputStream <p>An optional input stream to use. Defaults to using a file input stream</p>
     * @return <p>A mapping between loaded string indexes and the strings to display</p>
     */
    private Map<Message, String> load(@NotNull String lang, @Nullable InputStream inputStream) {
        BufferedReader bufferedReader;
        try {
            if (inputStream == null) {
                bufferedReader = FileHelper.getBufferedReaderFromString(languageFolder + lang + ".txt");
            } else {
                bufferedReader = FileHelper.getBufferedReaderFromInputStream(inputStream);
            }
            return fromStringMap(FileHelper.readKeyValuePairs(bufferedReader, "=", ColorConversion.NORMAL));
        } catch (Exception exception) {
            if (Stargate.getStargateConfig().isDebuggingEnabled()) {
                Stargate.logInfo("Unable to load language " + lang);
            }
            return null;
        }
    }

    /**
     * Prints debug output to the console for checking loaded language strings/translations
     */
    public void debug() {
        if (loadedStringTranslations != null) {
            for (Map.Entry<Message, String> entry : loadedStringTranslations.entrySet()) {
                Stargate.debug("LanguageLoader::Debug::loadedStringTranslations", entry.getKey() +
                        " => " + entry.getValue());
            }
        }
        if (loadedBackupStrings == null) {
            return;
        }

        for (Map.Entry<Message, String> entry : loadedBackupStrings.entrySet()) {
            Stargate.debug("LanguageLoader::Debug::loadedBackupStrings", entry.getKey() + " => " +
                    entry.getValue());
        }
    }

    @NotNull
    private Map<Message, String> fromStringMap(@NotNull Map<String, String> configurationStrings) {
        Map<Message, String> output = new EnumMap<>(Message.class);
        for (Map.Entry<String, String> entry : configurationStrings.entrySet()) {
            Message message = Message.getFromKey(entry.getKey());
            if (message == null) {
                Stargate.logWarning("Found unrecognized language key " + entry.getKey());
                continue;
            }

            output.put(message, entry.getValue());
        }

        return output;
    }

}
