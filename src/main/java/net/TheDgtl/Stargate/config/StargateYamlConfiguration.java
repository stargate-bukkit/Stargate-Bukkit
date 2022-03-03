package net.TheDgtl.Stargate.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A configuration that supports comments
 *
 * @author Thorin
 */
public class StargateYamlConfiguration extends YamlConfiguration {

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
     * Reads a file with comments, and recreates them into yaml mappings
     *
     * <p>
     * A mapping follows this format: comment_{CommentNumber}: "The comment"
     * <p>
     * This needs to be done as comments otherwise get removed using
     * the {@link FileConfiguration#save(File)} method. The config
     * needs to be saved if a config value has changed.</p>
     */
    public String convertCommentsToYAMLMappings(String yamlString) {
        StringBuilder newText = new StringBuilder();
        /*
         * A list of each stored comment (which is a list of comment lines) A comment
         * is defined as a set of lines that start with #, this set can not contain an
         * empty line.
         */
        List<List<String>> comments = new ArrayList<>();
        int counter = 0;
        int commentNameCounter = 0;
        int indentation;
        List<String> currentComment;
        for (String line : yamlString.split("\n")) {
            if (line.trim().isEmpty()) {
                //A cheesy way to move to the next comment
                counter = comments.size();
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
                indentation = this.countSpaces(line);
                for (List<String> comment : comments)
                    newText.append(compileCommentMapping(comment, commentNameCounter++, indentation));
                newText.append(line).append("\n");
                comments.clear();
                counter = 0;
                continue;
            }
            newText.append(line).append("\n");
        }
        return newText.toString();
    }

    private String compileCommentMapping(List<String> commentLines, int counter, int indentation) {
        StringBuilder commentYamlMapping = new StringBuilder(this.addIndentation(indentation) + START_OF_COMMENT +
                counter + ": |\n");
        commentLines.add(this.addIndentation(indentation + 2) + END_OF_COMMENT);
        for (String commentLine : commentLines) {
            commentYamlMapping.append(this.addIndentation(indentation + 2)).append(commentLine).append("\n");
        }
        return commentYamlMapping.toString();
    }

    public String convertYAMLMappingsToComments(String yamlString) {
        StringBuilder finalText = new StringBuilder();
        boolean isSkippingComment = false;
        for (String line : yamlString.split("\n")) {
            if (isSkippingComment) {
                if (line.contains(END_OF_COMMENT)) {
                    isSkippingComment = false;
                }
                continue;
            }
            // TODO: Create a custom method with Java 11's String.strip() if necessary
            String possibleComment = line.trim();
            if (possibleComment.startsWith(START_OF_COMMENT)) {
                int indentation = countSpaces(line);
                String lastKeyName = possibleComment.split(":")[0];
                String key = "";
                for (String possibleKey : getKeys(true)) {
                    if (possibleKey.contains(lastKeyName)) {
                        key = possibleKey;
                    }
                }
                String comment = getString(key);
                if (comment == null) {
                    continue;
                }
                String[] commentLines = comment.split("\n");
                //Go through every line, except the last one, which is just going to be an END_OF_COMMENT identifier
                StringBuilder lineBuilder = new StringBuilder();
                for (int i = 0; i < commentLines.length - 1; i++) {
                    lineBuilder.append("\n").append(addIndentation(indentation)).append("# ").append(commentLines[i]);
                }
                line = lineBuilder.toString();
                isSkippingComment = true;
            }
            finalText.append(line).append("\n");
        }
        return finalText.toString();
    }

    /**
     * Gets a string containing the given indentation
     *
     * @param indentationSpaces <p>The number spaces to use for indentation</p>
     * @return <p>A string containing the number of spaces specified</p>
     */
    private String addIndentation(int indentationSpaces) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indentationSpaces; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }


    /**
     * Counts the spaces at the start of a line
     *
     * @param line <p>The line to count spaces for</p>
     * @return <p>The number of found spaces</p>
     */
    private int countSpaces(String line) {
        int spacesFound = 0;
        for (char aCharacter : line.toCharArray()) {
            if (aCharacter == ' ') {
                spacesFound++;
            } else {
                break;
            }
        }
        return spacesFound;
    }

}
