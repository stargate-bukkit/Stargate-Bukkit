package org.sgrewritten.stargate.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A YAML configuration which retains all comments
 *
 * <p>This configuration converts all comments to YAML values when loaded, which all start with comment_. When saved,
 * those YAML values are converted to normal text comments. This ensures that the comments aren't removed by the
 * YamlConfiguration during its parsing.</p>
 *
 * @author Kristian Knarvik
 * @author Thorin
 */
public class StargateYamlConfiguration extends YamlConfiguration {

    public static final String START_OF_COMMENT_LINE = "[HASHTAG]";
    static public final String END_OF_COMMENT = "_endOfComment_";
    static public final String START_OF_COMMENT = "comment_";

    @Override
    @SuppressWarnings("deprecation")
    protected @NotNull String buildHeader() {
        return "";
    }

    @Override
    public @NotNull String saveToString() {
        // Convert YAML comments to normal comments
        return this.convertYAMLMappingsToComments(super.saveToString());
    }

    @Override
    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        // Convert normal comments to YAML comments to prevent them from disappearing
        super.loadFromString(this.convertCommentsToYAMLMappings(contents));
    }

    /**
     * Reads a file with comments, and recreates them into yaml mappings
     *
     * <p>A mapping follows this format: comment_{CommentNumber}: "The comment"
     * This needs to be done as comments otherwise get removed using
     * the {@link FileConfiguration#save(File)} method. The config
     * needs to be saved if a config value has changed.</p>
     */
    private String convertCommentsToYAMLMappings(String configString) {
        StringBuilder yamlBuilder = new StringBuilder();
        List<String> currentComment = new ArrayList<>();
        int commentId = 0;
        int previousIndentation = 0;

        for (String line : configString.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#")) {
                // Store the indentation of the block
                if (currentComment.isEmpty()) {
                    previousIndentation = getIndentation(line);
                }
                //Temporarily store the comment line
                addComment(currentComment, trimmed);
            } else {
                addYamlString(yamlBuilder, currentComment, line, previousIndentation, commentId);
                commentId++;
                previousIndentation = 0;
            }
        }
        return yamlBuilder.toString();
    }

    /**
     * Adds a YAML string to the given string builder
     *
     * @param yamlBuilder         <p>The string builder used for building YAML</p>
     * @param currentComment      <p>The comment to add as a YAML string</p>
     * @param line                <p>The current line</p>
     * @param previousIndentation <p>The indentation of the current block comment</p>
     * @param commentId           <p>The id of the comment</p>
     */
    private void addYamlString(StringBuilder yamlBuilder, List<String> currentComment, String line,
                               int previousIndentation, int commentId) {
        String trimmed = line.trim();
        //Write the full formatted comment to the StringBuilder
        if (!currentComment.isEmpty()) {
            int indentation = trimmed.isEmpty() ? previousIndentation : getIndentation(line);
            generateCommentYAML(yamlBuilder, currentComment, commentId, indentation);
            currentComment.clear();
        }
        //Add the non-comment line assuming it isn't empty
        if (!trimmed.isEmpty()) {
            yamlBuilder.append(line).append("\n");
        }
    }

    /**
     * Adds the given comment to the given list
     *
     * @param commentParts <p>The list to add to</p>
     * @param comment      <p>The comment to add</p>
     */
    private void addComment(List<String> commentParts, String comment) {
        if (comment.startsWith("# ")) {
            commentParts.add(comment.replaceFirst("# ", START_OF_COMMENT_LINE));
        } else {
            commentParts.add(comment.replaceFirst("#", START_OF_COMMENT_LINE));
        }
    }

    /**
     * Generates a YAML-compatible string for one comment block
     *
     * @param yamlBuilder  <p>The string builder to add the generated YAML to</p>
     * @param commentLines <p>The lines of the comment to convert into YAML</p>
     * @param commentId    <p>The unique id of the comment</p>
     * @param indentation  <p>The indentation to add to every line</p>
     */
    private void generateCommentYAML(StringBuilder yamlBuilder, List<String> commentLines, int commentId, int indentation) {
        String subIndentation = this.addIndentation(indentation + 2);
        //Add the comment start marker
        yamlBuilder.append(this.addIndentation(indentation)).append(START_OF_COMMENT).append(commentId).append(": |\n");
        for (String commentLine : commentLines) {
            //Add each comment line with the proper indentation
            yamlBuilder.append(subIndentation).append(commentLine).append("\n");
        }
        //Add the comment end marker
        yamlBuilder.append(subIndentation).append(subIndentation).append(END_OF_COMMENT).append("\n");
    }

    /**
     * Converts the internal YAML mapping format to a readable config file
     *
     * <p>The internal YAML structure is converted to a string with the same format as a standard configuration file.
     * The internal structure has comments in the format: START_OF_COMMENT + id + multi-line YAML string +
     * END_OF_COMMENT.</p>
     *
     * @param yamlString <p>A string using the YAML format</p>
     * @return <p>The corresponding comment string</p>
     */
    private String convertYAMLMappingsToComments(String yamlString) {
        StringBuilder finalText = new StringBuilder();

        String[] lines = yamlString.split("\n");
        for (int currentIndex = 0; currentIndex < lines.length; currentIndex++) {
            String line = lines[currentIndex];
            String possibleComment = line.trim();

            if (possibleComment.startsWith(START_OF_COMMENT)) {
                //Add an empty line before every comment block
                finalText.append("\n");
                currentIndex = readComment(finalText, lines, currentIndex + 1, getIndentation(line));
            } else {
                //Output the configuration key
                finalText.append(line).append("\n");
            }
        }

        return finalText.toString().trim();
    }

    /**
     * Fully reads a comment
     *
     * @param builder            <p>The string builder to write to</p>
     * @param lines              <p>The lines to read from</p>
     * @param startIndex         <p>The index to start reading from</p>
     * @param commentIndentation <p>The indentation of the read comment</p>
     * @return <p>The index containing the next non-comment line</p>
     */
    private int readComment(StringBuilder builder, String[] lines, int startIndex, int commentIndentation) {
        for (int currentIndex = startIndex; currentIndex < lines.length; currentIndex++) {
            String line = lines[currentIndex];
            String possibleComment = line.trim();
            if (!line.contains(END_OF_COMMENT)) {
                possibleComment = possibleComment.replace(START_OF_COMMENT_LINE, "");
                builder.append(addIndentation(commentIndentation)).append("# ").append(possibleComment).append("\n");
            } else {
                return currentIndex;
            }
        }

        return startIndex;
    }

    /**
     * Gets a string containing the given indentation
     *
     * @param indentationSpaces <p>The number spaces to use for indentation</p>
     * @return <p>A string containing the number of spaces specified</p>
     */
    private String addIndentation(int indentationSpaces) {
        return " ".repeat(Math.max(0, indentationSpaces));
    }


    /**
     * Gets the indentation (number of spaces) of the given line
     *
     * @param line <p>The line to get indentation of</p>
     * @return <p>The number of spaces in the line's indentation</p>
     */
    private int getIndentation(String line) {
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
