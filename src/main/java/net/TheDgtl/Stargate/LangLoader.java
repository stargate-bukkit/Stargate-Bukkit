/*
 * Stargate - A portal plugin for Bukkit
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.TheDgtl.Stargate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.bukkit.ChatColor;

/**
 * Enables multi lingual support for Stargate
 * @author Thorin
 * @author Someone who don't know what a comment is
 *
 */
public class LangLoader {
    private static final String UTF8_BOM = "\uFEFF";
    
    private final String dataFolder;
    private String lang;
    
    private HashMap<String, String> strList;
    private final HashMap<String, String> defList;

    // di
    private final Stargate stargate;

    public LangLoader(Stargate stargate, String datFolder, String lang) {
        this.stargate = stargate;
        this.lang = lang;
        this.dataFolder = datFolder;
        
        //checks if the language file exist, if not creates it
        File tmp = new File(datFolder, lang + ".txt");
        if (!tmp.exists()) {
            tmp.getParentFile().mkdirs();
        }
        updateLanguage(lang);

        strList = load(lang);
        // We have a default hashMap used for when new text is added.
        InputStream is = Stargate.class.getResourceAsStream("/"+lang+".txt");
        if (is != null) {
            defList = load(is);
        } else {
            defList = null;
            stargate.getStargateLogger().severe("[Stargate] Error loading backup language. There may be missing text ingame");
        }
    }

    public boolean reload() {
        // This extracts/updates the language as needed
        updateLanguage(lang);
        strList = load(lang);
        return true;
    }

    public String getString(String name) {
        String val = strList.get(name);
        if (val == null && defList != null) val = defList.get(name);
        if (val == null) return "";
        return val;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
    
    /**
     * Reads all the lines from the lang.txt file, checks for issues with it
     * then modifies it using the resource file.
     * 
     * This could in essence be replaced by creating a new lang.txt file from
     * resources if not exists (one line of code). 
     * 
     * @param lang
     */
    private void updateLanguage(String language) {
        ArrayList<String> keyList = new ArrayList<>();
        ArrayList<String> valList = new ArrayList<>();
        
        // Load the current language file
        HashMap<String, String> currentLang = load(language);
        
        //get inputstream from resource folder
        InputStream is = Stargate.class.getResourceAsStream("/" + language + ".txt");
        if (is == null) return;

        boolean updated = false;
        try {
            BufferedReader br = new BufferedReader(   new InputStreamReader(is, StandardCharsets.UTF_8)  );

            String line = br.readLine();
            line = removeUTF8BOM(line);
            while (line != null) {
                // Position of "="
                int eq = line.indexOf('=');
                
                //No '=' was found from resources
                if (eq == -1) {
                    keyList.add("");
                    valList.add("");
                    line = br.readLine();
                    continue;
                }
                String key = line.substring(0, eq);
                String val = line.substring(eq);
                
                //checks if key does not exist in currentlang
                if (currentLang == null || currentLang.get(key) == null) {
                    keyList.add(key);
                    valList.add(val);
                    updated = true;
                }else {
                    keyList.add(key);
                    valList.add("=" + currentLang.get(key).replace('\u00A7', '&'));
                    currentLang.remove(key);
                }
                line = br.readLine();
            }
            br.close();

            // Establishes connection to file
            File langFile = new File(dataFolder, language + ".txt");
            BufferedWriter bw = new BufferedWriter(   new OutputStreamWriter(  new FileOutputStream(langFile)  , StandardCharsets.UTF_8)   );

            // Write normal Language data
            for (int i = 0; i < keyList.size(); i++) {
                bw.write(keyList.get(i) + valList.get(i));
                bw.newLine();
            }
            bw.newLine();
            // Write any custom language strings the user had
            if (currentLang != null) {
                for (String key : currentLang.keySet()) {
                    bw.write(key + "=" + currentLang.get(key));
                    bw.newLine();
                }
            }

            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
        if (updated)
            stargate.getStargateLogger().info("[Stargate] Your language file (" + language + ".txt) has been updated");
    }
    
    /**
     * Creates a more usable hashmap from the lang file
     * @param language
     * @return A hashmap with a text identifier as a key and the text to display as values
     */
    private HashMap<String, String> load(String language) {
    	
    	File langFile = new File(dataFolder, language + ".txt");
    	InputStream fis = null;
    	try {
    		 fis = new FileInputStream(langFile);
    	}catch(FileNotFoundException e) {return null;}
    	
    	
        return load( fis );
    }
    /**
     * Creates a more usable hashmap from the lang file
     * @param is
     * @return A hashmap with a text identifier as a key and the text to display as values
     */
    private HashMap<String, String> load(InputStream is) {
        HashMap<String, String> strings = new HashMap<>();
        try {
        	InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            line = removeUTF8BOM(line);
            while (line != null) {
                // Split at first "="
                int eq = line.indexOf('=');
                if (eq == -1) {
                    line = br.readLine();
                    continue;
                }
                String key = line.substring(0, eq);
                String val = ChatColor.translateAlternateColorCodes('&', line.substring(eq + 1));
                strings.put(key, val);
                line = br.readLine();
            }
            br.close(); isr.close();
        } catch (Exception ex) {
            return null;
        }
        
        return strings;
    }

    public void debug() {
        Set<String> keys = strList.keySet();
        for (String key : keys) {
            Stargate.debug("LangLoader::Debug::strList", key + " => " + strList.get(key));
        }
        if (defList == null) return;
        keys = defList.keySet();
        for (String key : keys) {
            Stargate.debug("LangLoader::Debug::defList", key + " => " + defList.get(key));
        }
    }
    /**
     * @param text
     * @return text without utfBOM
     */
    private String removeUTF8BOM(String text) {
        if (text.startsWith(UTF8_BOM)) {
        	text = text.substring(1);
        }
        return text;
    }
}
