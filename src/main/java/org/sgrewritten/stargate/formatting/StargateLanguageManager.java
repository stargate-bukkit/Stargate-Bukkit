package org.sgrewritten.stargate.formatting;

import org.bukkit.ChatColor;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.util.FileHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * The language manager is responsible for translating various messages
 */
public class StargateLanguageManager implements LanguageManager {

    private final File languageFolder;
    private String language;
    private Map<TranslatableMessage, String> translatedStrings;
    private final Map<TranslatableMessage, String> backupStrings;
    private static final Map<String, String> LANGUAGE_SHORTHANDS = new HashMap<>();
    private static final Pattern COLOR_CODE = Pattern.compile("(&([a-f0-9]))");

    static {
        FileHelper.readInternalFileToMap("/language-edge-cases.properties", LANGUAGE_SHORTHANDS);
    }

    /**
     * Instantiates a new language manager
     *
     * @param languageFolder <p>The folder containing all language files</p>
     */
    public StargateLanguageManager(File languageFolder) {
        this.languageFolder = languageFolder;

        backupStrings = loadBackupLanguage();
    }

    /**
     * Load the backup language from the plugin file
     *
     * @return <p>A map containing all backup translations</p>
     */
    private Map<TranslatableMessage, String> loadBackupLanguage() {
        Map<String, String> translations = new HashMap<>();
        Map<TranslatableMessage, String> output = new EnumMap<>(TranslatableMessage.class);
        FileHelper.readInternalFileToMap("/lang/en-GB/en-GB.txt", translations);
        for (TranslatableMessage translatableMessage : TranslatableMessage.values()) {
            output.put(translatableMessage, translations.get(translatableMessage.getMessageKey()));
        }
        return output;
    }

    @Override
    public String getErrorMessage(TranslatableMessage translatableMessage) {
        return formatMessage(translatableMessage, ChatColor.RED);
    }

    @Override
    public String getWarningMessage(TranslatableMessage translatableMessage) {
        return formatMessage(translatableMessage, ChatColor.YELLOW);
    }

    @Override
    public String getMessage(TranslatableMessage translatableMessage) {
        return formatMessage(translatableMessage, ChatColor.GREEN);
    }

    @Override
    public String getString(TranslatableMessage translatableMessage) {
        if (translatedStrings == null) {
            return null;
        }

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

    @Override
    public void setLanguage(String languageSpecification) {
        //Replace any shorthands with the full language code
        for (Map.Entry<String,String> entry : LANGUAGE_SHORTHANDS.entrySet()) {
            if (languageSpecification.equalsIgnoreCase(entry.getKey())) {
                languageSpecification = entry.getValue();
            }
        }

        //Find the specified language if possible
        Language chosenLanguage = getLanguage(languageSpecification);
        if (chosenLanguage != null) {
            Stargate.log(Level.FINE, String.format("Found supported language %s", chosenLanguage.getLanguageCode()));
            languageSpecification = chosenLanguage.getLanguageCode();
        }

        // Only update language if it has actually changed
        if (!languageSpecification.equals(this.language)) {
            this.language = languageSpecification;
            translatedStrings = loadLanguage(chosenLanguage, languageSpecification);
            //Update the external language if it's not a custom language
            if (chosenLanguage != null) {
                updateLanguage(chosenLanguage, translatedStrings);
            }
        }
    }

    /**
     * Loads the given language
     *
     * @param language              <p>The language parsed from the language specification</p>
     * @param languageSpecification <p>The language to load</p>
     * @return <p>The map of translatable messages to translated strings for the loaded language</p>
     */
    private Map<TranslatableMessage, String> loadLanguage(Language language, String languageSpecification) {
        try {
            return loadLanguageFile(language, languageSpecification);
        } catch (IOException exception) {
            if (language == null) {
                Stargate.log(Level.WARNING, String.format("Unable to load the language file for %s",
                        languageSpecification));
            } else {
                Stargate.log(Level.FINER, String.format("Unable to load the language file for %s. This is " +
                        "expected if the file has not been copied to disk yet", languageSpecification));
            }
            return new EnumMap<>(TranslatableMessage.class);
        }
    }

    /**
     * Gets the language matching the given language specification
     *
     * @param languageSpecification <p>A language specification</p>
     * @return <p>The matching language, or null if no language matches</p>
     */
    private Language getLanguage(String languageSpecification) {
        //Allow "_" as a language-country separator
        languageSpecification = languageSpecification.replace("_", "-");
        for (Language aLanguage : Language.values()) {
            if (aLanguage.matches(languageSpecification)) {
                return aLanguage;
            }
        }
        return null;
    }

    /**
     * Gets a formatted translated message
     *
     * <p>The stargate prefix and the appropriate color is added to the returned translated message.</p>
     *
     * @param translatableMessage <p>The translatable message to display</p>
     * @param prefixColor         <p>The color of the prefix</p>
     * @return <p>A translated and formatted message</p>
     */
    private String formatMessage(TranslatableMessage translatableMessage, ChatColor prefixColor) {
        String prefix = prefixColor + getString(TranslatableMessage.PREFIX);
        String message = COLOR_CODE.matcher(getString(translatableMessage)).replaceAll("§$2");
        Stargate.log(Level.FINE, String.format("Formatted TranslatableMessage '%s' to '%s'",
                translatableMessage.toString(), message));
        return prefix + ChatColor.WHITE + message;
    }

    /**
     * Loads the language file of the given language
     *
     * @param language              <p>The language parsed from the language specification</p>
     * @param languageSpecification <p>The string specifying the language to load</p>
     * @return <p>The translatable messages found in the language file</p>
     * @throws IOException <p>If unable to read the language file</p>
     */
    private Map<TranslatableMessage, String> loadLanguageFile(Language language, String languageSpecification) throws IOException {
        File languageFile;
        if (language != null) {
            //For a known language file, we know the correct path
            languageFile = getLanguageFile(this.languageFolder, language);
            Stargate.log(Level.FINER, String.format("Loading known language %s from file %s",
                    language.getLanguageCode(), languageFile));
        } else {
            //For a custom language file, try all possible paths
            languageFile = findCustomLanguageFile(languageSpecification);
            Stargate.log(Level.FINER, String.format("Loading custom language %s from file %s",
                    languageSpecification, languageFile));
        }

        //If no satisfactory language file exists, give up
        if (languageFile == null) {
            return new EnumMap<>(TranslatableMessage.class);
        }

        //Get the contents of the language file
        try (BufferedReader bufferedReader = FileHelper.getBufferedReader(languageFile)) {
            return readLanguageReader(bufferedReader);
        }
    }

    /**
     * Finds the language file of a custom language
     *
     * @param languageSpecification <p>The string specifying the language to load</p>
     * @return <p>The language file, or null if no such file was found</p>
     */
    private File findCustomLanguageFile(String languageSpecification) {
        List<File> possibleLanguageFiles = findTargetFiles(languageSpecification, this.languageFolder);
        File foundMatchingFile = null;

        //Check through the possible language files for any matches
        for (File possibleLanguageFile : possibleLanguageFiles) {
            if (possibleLanguageFile.exists()) {
                foundMatchingFile = possibleLanguageFile;
                break;
            }
        }

        if (foundMatchingFile == null) {
            Stargate.log(Level.WARNING, String.format("The selected language, \"%s\", is not supported, and no "
                    + "custom language file exists. Falling back to English.", languageSpecification));
        }
        return foundMatchingFile;
    }

    /**
     * Finds all relevant language files in the given location
     *
     * @param language <p>The selected language to find relevant files for</p>
     * @param path     <p>The path to search for language files</p>
     * @return <p>The found language files</p>
     */
    private List<File> findTargetFiles(String language, File path) {
        //Find the relevant language and country codes from the language string
        List<String> possibleNames = new ArrayList<>(2);
        possibleNames.add(language);
        if (language.contains("-")) {
            possibleNames.add(language.split("-")[0]);
        }

        //Find all possible file and folder paths a user might create
        List<File> possibleFiles = new ArrayList<>();
        for (String pathName : possibleNames) {
            //Add the files languageCode.txt and language.txt
            possibleFiles.add(new File(path, pathName + ".txt"));

            File directory = new File(path, pathName);
            for (String fileName : possibleNames) {
                /* Add the files languageCode/languageCode.txt, language/language.txt, language/languageCode.txt and 
                   languageCode/language.txt */
                possibleFiles.add(new File(directory, fileName + ".txt"));
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
        Map<String, String> translations = FileHelper.readKeyValuePairs(bufferedReader);
        Map<TranslatableMessage, String> output = new EnumMap<>(TranslatableMessage.class);

        //Parse each line to its appropriate translatable message
        for (Map.Entry<String,String> entry : translations.entrySet()) {
            TranslatableMessage translatableMessage = TranslatableMessage.parse(entry.getKey());
            if (translatableMessage == null) {
                Stargate.log(Level.FINER, String.format("Skipping language prompt: %s = %s", entry.getKey(),
                        entry.getValue()));
                continue;
            }
            String value = ChatColor.translateAlternateColorCodes('&', entry.getValue());
            output.put(translatableMessage, value);
        }

        return output;
    }

    /**
     * Gets the language file for the given folder
     *
     * @param folder   <p>The folder containing language files</p>
     * @param language <p>The language to get the file of</p>
     * @return <p>The file containing the given language's translations</p>
     */
    private File getLanguageFile(File folder, Language language) {
        return new File(new File(folder, language.getLanguageFolder()), language.getLanguageCode() + ".txt");
    }

    /**
     * Updates files in the plugin directory with contents from the compiled .jar
     *
     * @param language          <p>The language to update</p>
     * @param translatedStrings <p> The already set strings </p>
     */
    private void updateLanguage(Language language, Map<TranslatableMessage, String> translatedStrings) {
        Map<String, String> internalFileTranslations = new HashMap<>();
        File internalFile = getLanguageFile(new File("lang"), language);
        File externalFile = getLanguageFile(this.languageFolder, language);

        FileHelper.readInternalFileToMap("/" + internalFile.getPath().replace("\\", "/"),
                internalFileTranslations);
        Stargate.log(Level.FINE, String.format("Checking internal language file '%s'", internalFile.getPath()));
        if (internalFileTranslations.isEmpty()) {
            Stargate.log(Level.FINE, "Could not find any strings in the internal language file. It could" +
                    " be that it's missing or that it has no translations");
            return;
        }

        //Find all translations in the internal file
        Map<TranslatableMessage, String> internalTranslatedValues = new EnumMap<>(TranslatableMessage.class);
        for (Map.Entry<String,String> entry : internalFileTranslations.entrySet()) {
            TranslatableMessage translatableMessageKey = TranslatableMessage.parse(entry.getKey());
            if (translatableMessageKey != null) {
                internalTranslatedValues.put(translatableMessageKey, entry.getValue());
            }
        }

        //Skip if no new translations are available
        if (translatedStrings.size() >= internalTranslatedValues.size()) {
            return;
        }
        addMissingInternalTranslations(externalFile, translatedStrings, internalTranslatedValues);
    }

    /**
     * Adds the missing translations into the external language file
     *
     * @param languageFile             <p> The file to add missing translations to</p>
     * @param translatedStrings        <p> The strings that already has been translated</p>
     * @param internalTranslatedValues <p> The translated strings from internal file </p>
     */
    private void addMissingInternalTranslations(File languageFile, Map<TranslatableMessage, String> translatedStrings,
                                                Map<TranslatableMessage, String> internalTranslatedValues) {
        File folder = languageFile.getParentFile();
        if (folder != null && !folder.exists() && !folder.mkdirs()) {
            Stargate.log(Level.WARNING, "Unable to create folders required for copying language file");
            return;
        }
        try {
            BufferedWriter writer = FileHelper.getBufferedWriter(languageFile, true);
            for (Map.Entry<TranslatableMessage, String> entry : internalTranslatedValues.entrySet()) {
                if (translatedStrings.containsKey(entry.getKey())) {
                    continue;
                }
                translatedStrings.put(entry.getKey(), entry.getValue());
                Stargate.log(Level.FINE, String.format("%n Adding a line of translations of key %s to language file '%s'",
                        entry.getKey().toString(), languageFile));
                writer.newLine();
                writer.write(String.format("%s=%s", entry.getKey().getMessageKey(), entry.getValue()));
            }
            writer.close();
        } catch (IOException e) {
            Stargate.log(e);
        }
    }

}
