package net.TheDgtl.Stargate.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A configuration that supports comments
 *
 * @author Thorin
 */
public class StargateConfiguration extends YamlConfiguration {

    static public final String END_OF_COMMENT = "_endOfComment_";
    static public final String START_OF_COMMENT = "comment_";

    @Override
    public @NotNull String saveToString() {
        return this.convertYAMLMappingsToComments(super.saveToString());

    }

    @Override
    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        super.loadFromString(this.convertCommentsToYAMLMappings(contents));
    }

    @Override
    protected @NotNull String buildHeader() {
        return "";
    }


    /**
     * Reads a file with comments, and recreates them into yaml mappings.
     * A mapping follows this format: comment_{CommentNumber}: "The comment"
     * <p>
     * This needs to be done as comments otherwise get removed using
     * the {@link FileConfiguration#save(File)} method. The config
     * needs to be saved if a config value has changed.
     *
     * @throws IOException
     */
    public String convertCommentsToYAMLMappings(String yamlString) {
        StringBuilder newText = new StringBuilder();
        /*
         * A list of each stored comment (which is an list of comment lines) A comment
         * is defined as a set of lines that start with #, this set can not contain an
         * empty line.
         */
        List<List<String>> comments = new ArrayList<>();
        int counter = 0;
        int commentNameCounter = 0;
        int indent;
        List<String> currentComment;
        for (String line : yamlString.split("\n")) {
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
                    newText.append(compileCommentMapping(comment, commentNameCounter++, indent));
                newText.append(line).append("\n");
                comments.clear();
                counter = 0;
                continue;
            }
            newText.append(line).append("\n");
        }
        return newText.toString();
    }

    private String compileCommentMapping(List<String> commentLines, int counter, int indent) {
        StringBuilder commentYamlMapping = new StringBuilder(this.repeat(" ", indent) + START_OF_COMMENT + counter + ": |\n");
        commentLines.add(this.repeat(" ", indent + 2) + END_OF_COMMENT);
        for (String commentLine : commentLines) {
            commentYamlMapping.append(this.repeat(" ", indent + 2)).append(commentLine).append("\n");
        }
        return commentYamlMapping.toString();
    }

    public String convertYAMLMappingsToComments(String yamlString) {
        StringBuilder finalText = new StringBuilder();
        boolean isSkippingComment = false;
        for (String line : yamlString.split("\n")) {
            if (isSkippingComment) {
                if (line.contains(END_OF_COMMENT))
                    isSkippingComment = false;
                continue;
            }
            // TODO: Create a custom method with Java 11's String.strip() if necessary
            String possibleComment = line.trim();
            if (possibleComment.startsWith(START_OF_COMMENT)) {
                int indent = countSpaces(line);
                String lastKeyName = possibleComment.split(":")[0];
                String key = "";
                for (String possibleKey : getKeys(true)) {
                    if (possibleKey.contains(lastKeyName))
                        key = possibleKey;
                }
                String comment = getString(key);
                String[] commentLines = comment.split("\n");
                /*
                 * Go through every line, except the last one, which is just going to be a
                 * END_OF_COMMENT identifier
                 */
                StringBuilder lineBuilder = new StringBuilder();
                for (int i = 0; i < commentLines.length - 1; i++) {
                    lineBuilder.append("\n").append(repeat(" ", indent)).append("# ").append(commentLines[i]);
                }
                line = lineBuilder.toString();
                isSkippingComment = true;
            }
            finalText.append(line).append("\n");
        }
        return finalText.toString();
    }

    /**
     * Repeats the given string the given amount of times
     *
     * @param repeatingString <p>The string to repeat</p>
     * @param repetitions     <p>The number of times to repeat the string</p>
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
}
