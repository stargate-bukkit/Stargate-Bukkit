package net.knarcraft.stargate;

import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * stargate - A portal plugin for Bukkit
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 * Copyright (C) 2021 Kristian Knarvik
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class is responsible for loading all strings which are translated into several languages
 */
public class LanguageLoader {

    // Variables
    private final String languageFolder;
    private String chosenLanguage;
    private Map<String, String> loadedStringTranslations;
    private final Map<String, String> loadedBackupStrings;

    /**
     * Instantiates a new language loader
     * @param languageFolder <p>The folder containing the language files</p>
     * @param chosenLanguage <p>The chosen plugin language</p>
     */
    public LanguageLoader(String languageFolder, String chosenLanguage) {
        this.chosenLanguage = chosenLanguage;
        this.languageFolder = languageFolder;

        File tmp = new File(languageFolder, chosenLanguage + ".txt");
        if (!tmp.exists()) {
            if (tmp.getParentFile().mkdirs() && Stargate.debug) {
                Stargate.log.info("[stargate] Created language folder");
            }
        }
        updateLanguage(chosenLanguage);

        loadedStringTranslations = load(chosenLanguage);
        // We have a default hashMap used for when new text is added.
        InputStream inputStream = getClass().getResourceAsStream("/lang/en.txt");
        if (inputStream != null) {
            loadedBackupStrings = load("en", inputStream);
        } else {
            loadedBackupStrings = null;
            Stargate.log.severe("[stargate] Error loading backup language. There may be missing text in-game");
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
     * @param chosenLanguage <p>The new plugin language</p>
     */
    public void setChosenLanguage(String chosenLanguage) {
        this.chosenLanguage = chosenLanguage;
    }

    /**
     * Updates files in the plugin directory with contents from the compiled .jar
     * @param language <p>The language to update</p>
     */
    private void updateLanguage(String language) {
        // Load the current language file
        ArrayList<String> keyList = new ArrayList<>();
        ArrayList<String> valueList = new ArrayList<>();

        Map<String, String> currentLanguageValues = load(language);

        InputStream inputStream = getClass().getResourceAsStream("/lang/" + language + ".txt");
        if (inputStream == null) {
            Stargate.log.info("[stargate] The language " + language + " is not available. Falling back to " +
                    "english, You can add a custom language by creating a new text file in the lang directory.");
            if (Stargate.debug) {
                Stargate.log.info("[stargate] Unable to load /lang/" + language + ".txt");
            }
            return;
        }

        boolean updated = false;
        FileOutputStream fileOutputStream = null;
        try {
            if (readChangedLanguageStrings(inputStream, keyList, valueList, currentLanguageValues)) {
                updated = true;
            }

            // Save file
            fileOutputStream = new FileOutputStream(languageFolder + language + ".txt");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            // Output normal Language data
            for (int i = 0; i < keyList.size(); i++) {
                bufferedWriter.write(keyList.get(i) + valueList.get(i));
                bufferedWriter.newLine();
            }
            bufferedWriter.newLine();
            // Output any custom language strings the user had
            if (currentLanguageValues != null) {
                for (String key : currentLanguageValues.keySet()) {
                    bufferedWriter.write(key + "=" + currentLanguageValues.get(key));
                    bufferedWriter.newLine();
                }
            }

            bufferedWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    //Ignored
                }
            }
        }
        if (updated) {
            Stargate.log.info("[stargate] Your language file (" + language + ".txt) has been updated");
        }
    }

    /**
     * Reads language strings
     * @param inputStream <p>The input stream to read from</p>
     * @param keyList <p>The key list to add keys to</p>
     * @param valueList <p>The value list to add values to</p>
     * @param currentLanguageValues <p>The current values of the loaded/processed language</p>
     * @return <p>True if at least one line was updated</p>
     * @throws IOException <p>if unable to read a language file</p>
     */
    private boolean readChangedLanguageStrings(InputStream inputStream, List<String> keyList, List<String> valueList,
                                            Map<String, String> currentLanguageValues) throws IOException {
        boolean updated = false;
        // Input stuff
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line = bufferedReader.readLine();
        boolean firstLine = true;
        while (line != null) {
            // Strip UTF BOM
            if (firstLine) {
                line = removeUTF8BOM(line);
                firstLine = false;
            }
            // Split at first "="
            int equalSignIndex = line.indexOf('=');
            if (equalSignIndex == -1) {
                keyList.add("");
                valueList.add("");
                line = bufferedReader.readLine();
                continue;
            }
            String key = line.substring(0, equalSignIndex);
            String value = line.substring(equalSignIndex);

            if (currentLanguageValues == null || currentLanguageValues.get(key) == null) {
                keyList.add(key);
                valueList.add(value);
                updated = true;
            } else {
                keyList.add(key);
                valueList.add("=" + currentLanguageValues.get(key).replace('\u00A7', '&'));
                currentLanguageValues.remove(key);
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return updated;
    }

    /**
     * Loads the given language
     * @param lang <p>The language to load</p>
     * @return <p>A mapping between loaded string indexes and the strings to display</p>
     */
    private Map<String, String> load(String lang) {
        return load(lang, null);
    }

    /**
     * Loads the given language
     * @param lang <p>The language to load</p>
     * @param inputStream <p>An optional input stream to use. Defaults to using a file input stream</p>
     * @return <p>A mapping between loaded string indexes and the strings to display</p>
     */
    private Map<String, String> load(String lang, InputStream inputStream) {
        Map<String, String> strings = new HashMap<>();
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader;
        try {
            if (inputStream == null) {
                fileInputStream = new FileInputStream(languageFolder + lang + ".txt");
                inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            } else {
                inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            }
            readLanguageFile(inputStreamReader, strings);
        } catch (Exception e) {
            if (Stargate.debug) {
                Stargate.log.info("Unable to load chosen language");
            }
            return null;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    //Ignored
                }
            }
        }
        return strings;
    }

    /**
     * Reads a language file given its input stream
     * @param inputStreamReader <p>The input stream reader to read from</p>
     * @param strings <p>The loaded string pairs</p>
     * @throws IOException <p>If unable to read the file</p>
     */
    private void readLanguageFile(InputStreamReader inputStreamReader, Map<String, String> strings) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();
        boolean firstLine = true;
        while (line != null) {
            // Strip UTF BOM
            if (firstLine) {
                line = removeUTF8BOM(line);
                firstLine = false;
            }
            // Split at first "="
            int equalSignIndex = line.indexOf('=');
            if (equalSignIndex == -1) {
                line = bufferedReader.readLine();
                continue;
            }
            String key = line.substring(0, equalSignIndex);
            String val = ChatColor.translateAlternateColorCodes('&', line.substring(equalSignIndex + 1));
            strings.put(key, val);
            line = bufferedReader.readLine();
        }
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

    /**
     * Removes the UTF-8 Byte Order Mark if present
     * @param string <p>The string to remove the BOM from</p>
     * @return <p>A string guaranteed without a BOM</p>
     */
    private String removeUTF8BOM(String string) {
        String UTF8_BOM = "\uFEFF";
        if (string.startsWith(UTF8_BOM)) {
            string = string.substring(1);
        }
        return string;
    }

}
