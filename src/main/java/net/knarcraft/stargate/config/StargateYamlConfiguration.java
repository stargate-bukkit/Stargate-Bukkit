package net.knarcraft.stargate.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A YAML configuration which keeps all comments
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

        for (String line : configString.split("\n")) {
            if (line.trim().startsWith("#")) {
                //Temporarily store the comment line
                currentComment.add(line.trim().replaceFirst("#", ""));
            } else {
                //Write the full formatted comment to the StringBuilder
                if (!currentComment.isEmpty()) {
                    int indentation = getIndentation(line);
                    generateCommentYAML(yamlBuilder, currentComment, commentId++, indentation);
                    currentComment = new ArrayList<>();
                }
                //Add the non-comment line assuming it isn't empty
                if (!line.trim().isEmpty()) {
                    yamlBuilder.append(line).append("\n");
                }
            }
        }
        return yamlBuilder.toString();
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
        boolean isReadingCommentBlock = false;
        int commentIndentation = 0;
        for (String line : yamlString.split("\n")) {
            String possibleComment = line.trim();

            if (isReadingCommentBlock && line.contains(END_OF_COMMENT)) {
                //Skip the line signifying the end of a comment
                isReadingCommentBlock = false;
            } else if (possibleComment.startsWith(START_OF_COMMENT)) {
                //Skip the comment start line, and start comment parsing
                isReadingCommentBlock = true;
                //Get the indentation to use for the comment block
                commentIndentation = getIndentation(line);
                //Add an empty line before every comment block
                finalText.append("\n");
            } else if (line.isEmpty() && !isReadingCommentBlock) {
                //Output the empty line as-is, as it's not part of a comment
                finalText.append("\n");
            } else if (isReadingCommentBlock) {
                //Output the comment with correct indentation
                finalText.append(addIndentation(commentIndentation)).append("# ").append(possibleComment).append("\n");
            } else {
                //Output the configuration key
                finalText.append(line).append("\n");
            }
        }
        return finalText.toString().trim();
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
