package net.TheDgtl.Stargate.refactoring;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.refactoring.retcons.Modificator;
import net.TheDgtl.Stargate.refactoring.retcons.RetCon1_0_0;
import net.TheDgtl.Stargate.util.FileHelper;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

public class Refactorer {
    /*
     * This name stays
     * NOT USED CURRENTLY
     */

    private int configVersion;
    private FileConfiguration defaultConfig;
    private Map<String, Object> config;
    private Stargate stargate;
    private static final Modificator[] RETCONS;

    static {
        RETCONS = new Modificator[]{
                new RetCon1_0_0()
        };
    }

    public Refactorer(FileConfiguration config, Stargate stargate) {
        this.config = config.getValues(true);
        this.stargate = stargate;
        configVersion = config.getInt("configVersion");
        stargate.saveResource("config.yml", true);
        stargate.reloadConfig();
        defaultConfig = stargate.getConfig();
    }

    public void run() {
        for (Modificator retCon : RETCONS) {
            int retConConfigNumber = retCon.getConfigNumber();
            if (retConConfigNumber >= configVersion) {
                config = retCon.run(config);
                configVersion = retConConfigNumber;
            }
        }
        for(String settingKey : config.keySet()) {
            defaultConfig.set(settingKey, config.get(settingKey));
        }
        
        defaultConfig.set("configVersion", Stargate.CURRENT_CONFIG_VERSION);
        try {
            defaultConfig.save(new File(stargate.getDataFolder(), "config.yml"));
            addComments();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ;
    }

    static private String ENDOFCOMMENT = "_endOfComment_";
    static private String STARTOFCOMMENT = "comment_";

    void addComments() throws FileNotFoundException {
        File configFile = new File(stargate.getDataFolder(), "config.yml");
        BufferedReader bReader = FileHelper.getBufferedReader(configFile);

        String finalText = "";
        try {
            String line;
            boolean isSkippingComment = false;
            while ((line = bReader.readLine()) != null) {
                if (isSkippingComment) {
                    if (line.contains(ENDOFCOMMENT))
                        isSkippingComment = false;
                    continue;
                }
                //TODO: Create a custom method with Java 11's String.strip() if necessary
                String possibleComment = line.trim();
                if (possibleComment.startsWith(STARTOFCOMMENT)) {
                    int indent = countSpaces(line);
                    String key = possibleComment.split(":")[0];
                    String comment = defaultConfig.getString(key);
                    String[] commentLines = comment.split("\n");
                    line = "";
                    /*
                     * Go through every line, except the last one, which is just going to be a
                     * ENDOFCOMMENT identifier
                     */
                    for (int i = 0; i < commentLines.length - 1; i++) {
                        line = line + "\n" + repeat(" ", indent) + "# " + commentLines[i];
                    }
                    isSkippingComment = true;
                }
                finalText = finalText + line + "\n";
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            OutputStream writerStream = new FileOutputStream(configFile);
            OutputStreamWriter writer = new OutputStreamWriter(writerStream);
            writer.write(finalText);
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Repeats the given string the given amount of times
     * 
     * @param repeatingString <p>The string to repeat</p>
     * @param repetitions <p>The number of times to repeat the string</p>
     * @return <p>The string repeated the given amount of times</p>
     */
    private String repeat(String repeatingString, int repetitions) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < repetitions; i++) {
            builder.append(repeatingString);
        }
        return builder.toString();
    }

    /**
     * Count the spaces at the start of a line. there's probably an already existing
     * method for this, but meh
     *
     * @param line
     * @return
     */
    private int countSpaces(String line) {
        int spaceAmount = 0;
        for (char aChar : line.toCharArray()) {
            if (aChar == ' ')
                spaceAmount++;
            else
                break;
        }
        return spaceAmount;
    }

    /**
     * Used in debug, when you want to see the state of the currently stored
     * configuration
     *
     * @throws FileNotFoundException
     */
    public void dispConfig() throws IOException {
        BufferedReader bReader;
        bReader = FileHelper.getBufferedReader(new File(stargate.getDataFolder(), "config.yml"));

        String line;
        try {
            line = bReader.readLine();
            while (line != null) {
                line = bReader.readLine();
            }
        } finally {
            try {
                bReader.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}