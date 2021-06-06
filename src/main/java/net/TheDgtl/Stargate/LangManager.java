package net.TheDgtl.Stargate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.bukkit.ChatColor;

public class LangManager {
	
	private final String dataFolder;
    private String lang;
    private HashMap<String, String> strList;
    private final HashMap<String, String> defList;
    
    private Stargate stargate;
    
    public LangManager(Stargate stargate, String datFolder, String lang) {
        this.lang = lang;
        this.dataFolder = datFolder;
        String defaultLang = "en";
        
        
        this.stargate = stargate;
        
        
        strList = loadLanguage(lang);
        defList = loadLanguage(defaultLang);
    }
    
    public String getString(String textAlias) {
    	String output = strList.get(textAlias);
    	if(output == null)
    		output = defList.get(textAlias);
    	return output;
    }
    
    
    private HashMap<String,String> loadLanguage(String language) {
    	
    	LangLoader loader = new LangLoader(language);
    	try {
			loader.open();
			return loader.load();
		} catch (FileNotFoundException e) {
			//TODO proper error printing
			e.printStackTrace();
		} catch (IOException e) {
			// TODO proper error printing
			e.printStackTrace();
		} finally {
			loader.close();
		}
    	
    	// This code only gets triggered if an error was thrown in the try clause
    	return new HashMap<String,String>();
    	// TODO show error message
    }
    
	
	
	private class LangLoader{
		private InputStreamReader isr;
		private InputStream  fis;
		private BufferedReader br;
		
		private final String language;
		
		private static final String UTF8_BOM = "\uFEFF";
		
		LangLoader(String language){
			this.language = language;
		}
		/**
		 * If not exist create language file from resources, then establish a datastream connection
		 * @throws FileNotFoundException
		 */
		void open() throws FileNotFoundException {
			File langFile = new File(dataFolder, language + ".txt");
			if(!langFile.exists()) {
				langFile.mkdirs();
				stargate.saveResource(language + ".txt", true);
			}
				
			fis = new FileInputStream(langFile);
			isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
			br = new BufferedReader(isr);
		}
		
		HashMap<String,String> load() throws IOException {
			HashMap<String, String> output = new HashMap<>();
			
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
                output.put(key, val);
                line = br.readLine();
            }
			
			return output;
		}
		void close() {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				isr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private String removeUTF8BOM(String text) {
	        if (text.startsWith(UTF8_BOM)) {
	        	text = text.substring(1);
	        }
	        return text;
	    }
		
	}
}
