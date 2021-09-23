package net.knarcraft.stargate;

import net.knarcraft.stargate.utility.FileHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible for loading all strings which are translated into several languages
 */
public class LanguageLoader {

    // Variables
    private final String languageFolder;
    private final Map<String, String> loadedBackupStrings;
    private String chosenLanguage;
    private Map<String, String> loadedStringTranslations;

    /**
     * Instantiates a new language loader
     *
     * @param languageFolder <p>The folder containing the language files</p>
     * @param chosenLanguage <p>The chosen plugin language</p>
     */
    public LanguageLoader(String languageFolder, String chosenLanguage) {
        this.chosenLanguage = chosenLanguage;
        this.languageFolder = languageFolder;

        File tmp = new File(languageFolder, chosenLanguage + ".txt");
        if (!tmp.exists()) {
            if (tmp.getParentFile().mkdirs() && Stargate.debuggingEnabled) {
                Stargate.logger.info("[stargate] Created language folder");
            }
        }
        updateLanguage(chosenLanguage);

        loadedStringTranslations = load(chosenLanguage);
        //Load english as backup language in case the chosen language is missing newly added text strings
        InputStream inputStream = FileHelper.getInputStreamForInternalFile("/lang/en.txt");
        if (inputStream != null) {
            loadedBackupStrings = load("en", inputStream);
        } else {
            loadedBackupStrings = null;
            Stargate.logger.severe("[stargate] Error loading backup language. There may be missing text in-game");
        }
    }

    /**
     * Reloads languages from the files on disk
     */
    public void reload() {
        // This extracts/updates the language as needed
        updateLanguage(chosenLanguage);
        loadedStringTranslations = load(chosenLanguage);
    }

    /**
     * Gets the string to display given its name/key
     *
     * @param name <p>The name/key of the string to display</p>
     * @return <p>The string in the user's preferred language</p>
     */
    public String getString(String name) {
        String value = null;
        if (loadedStringTranslations != null) {
            value = loadedStringTranslations.get(name);
        }
        if (value == null && loadedBackupStrings != null) {
            value = loadedBackupStrings.get(name);
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
    public void setChosenLanguage(String chosenLanguage) {
        this.chosenLanguage = chosenLanguage;
    }

    /**
     * Updates files in the plugin directory with contents from the compiled .jar
     *
     * @param language <p>The language to update</p>
     */
    private void updateLanguage(String language) {
        // Load the current language file

        Map<String, String> currentLanguageValues = load(language);

        InputStream inputStream = getClass().getResourceAsStream("/lang/" + language + ".txt");
        if (inputStream == null) {
            Stargate.logger.info("[stargate] The language " + language + " is not available. Falling back to " +
                    "english, You can add a custom language by creating a new text file in the lang directory.");
            if (Stargate.debuggingEnabled) {
                Stargate.logger.info("[stargate] Unable to load /lang/" + language + ".txt");
            }
            return;
        }

        try {
            readChangedLanguageStrings(inputStream, language, currentLanguageValues);
        } catch (IOException ex) {
            ex.printStackTrace();
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
    private void readChangedLanguageStrings(InputStream inputStream, String language, Map<String,
            String> currentLanguageValues) throws IOException {

        //Get language values
        BufferedReader bufferedReader = FileHelper.getBufferedReaderFromInputStream(inputStream);
        Map<String, String> internalLanguageValues = FileHelper.readKeyValuePairs(bufferedReader);

        //If currentLanguageValues is null; the chosen language is invalid, use the internal strings instead
        if (currentLanguageValues == null) {
            return;
        }

        //If a key is not found in the language file, add the one in the internal file. Must update the external file
        if (!internalLanguageValues.keySet().equals(currentLanguageValues.keySet())) {
            Map<String, String> newLanguageValues = new HashMap<>();
            boolean updateNecessary = false;
            for (String key : internalLanguageValues.keySet()) {
                if (currentLanguageValues.get(key) == null) {
                    newLanguageValues.put(key, internalLanguageValues.get(key));
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
                Stargate.logger.info("[stargate] Your language file (" + language + ".txt) has been updated");
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
    private void updateLanguageFile(String language, Map<String, String> languageStrings,
                                    Map<String, String> customLanguageStrings) throws IOException {
        BufferedWriter bufferedWriter = FileHelper.getBufferedWriterFromString(languageFolder + language + ".txt");

        //Output normal Language data
        for (String key : languageStrings.keySet()) {
            bufferedWriter.write(key + "=" + languageStrings.get(key));
            bufferedWriter.newLine();
        }
        bufferedWriter.newLine();
        //Output any custom language strings the user had
        if (customLanguageStrings != null) {
            for (String key : customLanguageStrings.keySet()) {
                bufferedWriter.write(key + "=" + customLanguageStrings.get(key));
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
    private Map<String, String> load(String lang) {
        return load(lang, null);
    }

    /**
     * Loads the given language
     *
     * @param lang        <p>The language to load</p>
     * @param inputStream <p>An optional input stream to use. Defaults to using a file input stream</p>
     * @return <p>A mapping between loaded string indexes and the strings to display</p>
     */
    private Map<String, String> load(String lang, InputStream inputStream) {
        Map<String, String> strings;
        BufferedReader bufferedReader;
        try {
            if (inputStream == null) {
                bufferedReader = FileHelper.getBufferedReaderFromString(languageFolder + lang + ".txt");
            } else {
                bufferedReader = FileHelper.getBufferedReaderFromInputStream(inputStream);
            }
            strings = FileHelper.readKeyValuePairs(bufferedReader);
        } catch (Exception e) {
            if (Stargate.debuggingEnabled) {
                Stargate.logger.info("Unable to load chosen language");
            }
            return null;
        }
        return strings;
    }

    /**
     * Prints debug output to the console for checking of loading language strings/translations
     */
    public void debug() {
        Set<String> keys = loadedStringTranslations.keySet();
        for (String key : keys) {
            Stargate.debug("LanguageLoader::Debug::loadedStringTranslations", key + " => " + loadedStringTranslations.get(key));
        }
        if (loadedBackupStrings == null) return;
        keys = loadedBackupStrings.keySet();
        for (String key : keys) {
            Stargate.debug("LanguageLoader::Debug::loadedBackupStrings", key + " => " + loadedBackupStrings.get(key));
        }
    }

}
