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

public class LangManager {

    private final String dataFolder;
    private String lang;
    private EnumMap<TranslatableMessage, String> strList;
    private final EnumMap<TranslatableMessage, String> defList;

    private Stargate stargate;

    public LangManager(Stargate stargate, String datFolder, String lang) {
        this.lang = lang;
        this.dataFolder = datFolder;
        String defaultLang = "en";


        this.stargate = stargate;


        strList = loadLanguage(lang);
        defList = loadLanguage(defaultLang);
    }

    public String getMessage(TranslatableMessage key, boolean isError) {
        String prefix = (isError ? ChatColor.RED : ChatColor.GREEN) + strList.get(TranslatableMessage.PREFIX);
        String msg = getString(key).replaceAll("(&([a-f0-9]))", "\u00A7$2");
        return prefix + ChatColor.WHITE + msg;
    }

    public String getString(TranslatableMessage key) {
        String msg = strList.get(key);
        if (msg == null)
            msg = defList.get(key);
        return msg;
    }


    private EnumMap<TranslatableMessage, String> loadLanguage(String language) {

        LangLoader loader = new LangLoader(language);
        try {
            loader.open();
            return loader.load();
        } catch (IOException e) {
            //TODO proper error printing
            e.printStackTrace();
        } finally {
            loader.close();
        }

        // This code only gets triggered if an error was thrown in the try clause
        return new EnumMap<>(TranslatableMessage.class);
        // TODO show error message
    }

    public void setLang(String language) {
        this.lang = language;
        strList = loadLanguage(language);
    }


    private class LangLoader {
        private InputStreamReader isr;
        private InputStream fis;
        private BufferedReader br;

        private final String language;

        private static final String UTF8_BOM = "\uFEFF";

        LangLoader(String language) {
            this.language = language;
        }

        /**
         * If not exist create language file from resources, then establish a datastream connection
         *
         * @throws FileNotFoundException
         */
        void open() throws FileNotFoundException {
            File langFile = new File(dataFolder, language + ".txt");
            if (!langFile.exists()) {
                stargate.saveResource("lang/" + language + ".txt", false);
            }

            fis = new FileInputStream(langFile);
            isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            br = new BufferedReader(isr);
        }

        EnumMap<TranslatableMessage, String> load() throws IOException {
            EnumMap<TranslatableMessage, String> output = new EnumMap<>(TranslatableMessage.class);

            String line = br.readLine();
            line = removeUTF8BOM(line);
            while (line != null) {
                // Split at first "="
                int eq = line.indexOf('=');
                if (eq == -1) {
                    line = br.readLine();
                    continue;
                }
                TranslatableMessage key = TranslatableMessage.parse(line.substring(0, eq));
                if (key == null) {
                    Stargate.log(Level.CONFIG, "Skipping line: " + line);
                    line = br.readLine();
                    continue;
                }
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
