package net.TheDgtl.Stargate.refactoring;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.refactoring.retcons.Modificator;
import net.TheDgtl.Stargate.refactoring.retcons.RetCon1_0_0;
import net.TheDgtl.Stargate.util.FileHelper;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Refactorer {
    /*
     * This name stays
     * NOT USED CURRENTLY
     */

    private int configVersion;
    private FileConfiguration defaultConfig;
    private File configFile;
    private Map<String, Object> config;
    private StargateLogger logger;
    private FileConfiguration fileConfig;
    private static final Modificator[] RETCONS;

    static private String END_OF_COMMENT = "_endOfComment_";
    static private String START_OF_COMMENT = "comment_";

    static {
        RETCONS = new Modificator[]{
                new RetCon1_0_0()
        };
    }

    public Refactorer(File configFile, StargateLogger logger) throws FileNotFoundException, IOException, InvalidConfigurationException {
        FileConfiguration fileConfig = new YamlConfiguration();
        fileConfig.load(configFile);
        this.fileConfig = fileConfig;
        this.config = fileConfig.getValues(true);
        this.configVersion = fileConfig.getInt("configVersion");
        this.configFile = configFile;
        this.logger = logger;
    }

    public Map<String, Object> calculateNewConfig() {
        for (Modificator retCon : RETCONS) {
            int retConConfigNumber = retCon.getConfigNumber();
            if (retConConfigNumber >= configVersion) {
                config = retCon.run(config);
                configVersion = retConConfigNumber;
            }
        }
        return config;
    }
    
    /**
     * Reads a file with comments, and recreates them into yaml mappings.
     * A mapping follows this format: comment_{CommentNumber}: "The comment" 
     * <p>
     * This needs to be done as comments otherwise get removed using
     * the {@link FileConfiguration#save(File)} method. The config
     * needs to be saved if a config value has changed.
     * @throws IOException
     */
    public void convertCommentsToYAMLMappings() throws IOException {
        BufferedReader bReader = FileHelper.getBufferedReader(configFile);
        String line;
        String newText = "";
        /*
         * A list of each stored comment (which is an list of comment lines)
         * A comment is defined as a set of lines that start with #, this set 
         * can not contain an empty line.
         */
        List<List<String>> comments = new ArrayList<>();
        int counter = 0;
        int commentNameCounter = 0;
        int indent = 0;
        List<String> currentComment;
        try {
            while ((line = bReader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    counter = comments.size(); // a cheesy way to move to the next comment
                    continue;
                }
                if (line.trim().startsWith("#")) {
                    if (counter >= comments.size()) {
                        comments.add(new ArrayList<>());
                    }
                    currentComment = comments.get(counter);
                    currentComment.add(line.trim().replaceFirst("#", ""));
                    continue;
                }
                if (!comments.isEmpty()) {
                    indent = this.countSpaces(line);
                    for (List<String> comment : comments)
                        newText = newText + compileCommentMapping(comment, commentNameCounter++, indent);
                    newText = newText + line + "\n";
                    comments.clear();
                    counter = 0;
                    continue;
                }
                newText = newText + line + "\n";
            }
        } finally {
            bReader.close();
        }
        
        File oldFile = new File(configFile.getAbsolutePath() + ".old");
        if(oldFile.exists())
            oldFile.delete();
        configFile.renameTo(oldFile);
        configFile.createNewFile();
        BufferedWriter bWriter = FileHelper.getBufferedWriter(configFile);
        try {
            bWriter.write(newText);
        } finally {
            bWriter.close();
        }
        
    }
    
    private String compileCommentMapping(List<String> commentLines, int counter, int indent) {
        String commentYamlMapping = this.repeat(" ", indent) + START_OF_COMMENT + counter + ": |\n";
        commentLines.add(this.repeat(" ", indent + 2) + END_OF_COMMENT);
        for(String commentLine : commentLines) {
            commentYamlMapping = commentYamlMapping + this.repeat(" ", indent + 2) + commentLine + "\n";
        }
        return commentYamlMapping;
    }

    public void insertNewValues(Map<String, Object> config) throws IOException, InvalidConfigurationException {
        fileConfig.load(configFile);
        for (String settingKey : config.keySet()) {
            fileConfig.set(settingKey, config.get(settingKey));
        }
        fileConfig.set("configVersion", Stargate.CURRENT_CONFIG_VERSION);
        fileConfig.save(configFile);
    }

    public void convertYAMLMappingsToComments() throws IOException, InvalidConfigurationException {
        BufferedReader bReader = FileHelper.getBufferedReader(configFile);
        fileConfig.load(configFile);

        String finalText = "";
        String line;
        boolean isSkippingComment = false;
        try {
            while ((line = bReader.readLine()) != null) {
                if (isSkippingComment) {
                    if (line.contains(END_OF_COMMENT))
                        isSkippingComment = false;
                    continue;
                }
                // TODO: Create a custom method with Java 11's String.strip() if necessary
                String possibleComment = line.trim();
                if (possibleComment.startsWith(START_OF_COMMENT)) {
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
        } finally {
            bReader.close();
        }

        BufferedWriter writer = FileHelper.getBufferedWriter(configFile);
        try {
            writer.write(finalText);
        } finally {
            writer.close();
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
     * @throws IOException
     */
    public void dispConfig() throws IOException {
        BufferedReader bReader;
        bReader = FileHelper.getBufferedReader(configFile);

        String line;
        try {
            line = bReader.readLine();
            while (line != null) {
                line = bReader.readLine();
            }
        } finally {
            bReader.close();
        }
    }
}