package net.knarcraft.stargate.command;

import net.knarcraft.stargate.config.ConfigOption;
import net.knarcraft.stargate.config.OptionDataType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the completer for stargates config sub-command (/sg config)
 */
public class ConfigTabCompleter implements TabCompleter {

    private List<String> signTypes;
    private List<String> booleans;
    private List<String> integers;
    private List<String> chatColors;
    private List<String> languages;
    private List<String> extendedColors;
    private List<String> doubles;

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                                      @NotNull String[] args) {
        if (signTypes == null || booleans == null || integers == null || chatColors == null || languages == null) {
            initializeAutoCompleteLists();
        }
        if (args.length > 1) {
            ConfigOption selectedOption = ConfigOption.getByName(args[0]);
            if (selectedOption == null) {
                return new ArrayList<>();
            } else if (selectedOption.getDataType() == OptionDataType.STRING_LIST) {
                return getPossibleStringListOptionValues(selectedOption, args);
            } else {
                return getPossibleOptionValues(selectedOption, args[1]);
            }
        } else {
            List<String> configOptionNames = new ArrayList<>();
            for (ConfigOption option : ConfigOption.values()) {
                configOptionNames.add(option.getName());
            }
            return filterMatching(configOptionNames, args[0]);
        }
    }

    /**
     * Find completable strings which match the text typed by the command's sender
     *
     * @param values    <p>The values to filter</p>
     * @param typedText <p>The text the player has started typing</p>
     * @return <p>The given string values which start with the player's typed text</p>
     */
    private List<String> filterMatching(List<String> values, String typedText) {
        List<String> configValues = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase().startsWith(typedText.toLowerCase())) {
                configValues.add(value);
            }
        }
        return configValues;
    }

    /**
     * Get possible values for the selected option
     *
     * @param selectedOption <p>The selected option</p>
     * @param typedText      <p>The beginning of the typed text, for filtering matching results</p>
     * @return <p>Some or all of the valid values for the option</p>
     */
    private List<String> getPossibleOptionValues(ConfigOption selectedOption, String typedText) {
        switch (selectedOption) {
            case LANGUAGE:
                //Return available languages
                return filterMatching(languages, typedText);
            case GATE_FOLDER:
            case PORTAL_FOLDER:
            case DEFAULT_GATE_NETWORK:
                //Just return the default value as most values should be possible
                if (typedText.trim().isEmpty()) {
                    return putStringInList((String) selectedOption.getDefaultValue());
                } else {
                    return new ArrayList<>();
                }
            case MAIN_SIGN_COLOR:
            case HIGHLIGHT_SIGN_COLOR:
            case FREE_GATES_COLOR:
                //Return all colors
                return filterMatching(chatColors, typedText);
        }

        //If the config value is a boolean, show the two boolean values
        if (selectedOption.getDataType() == OptionDataType.BOOLEAN) {
            return filterMatching(booleans, typedText);
        }

        //If the config value is an integer, display some valid numbers
        if (selectedOption.getDataType() == OptionDataType.INTEGER) {
            if (typedText.trim().isEmpty()) {
                return integers;
            } else {
                return new ArrayList<>();
            }
        }

        //If the config value is a double, display some valid numbers
        if (selectedOption.getDataType() == OptionDataType.DOUBLE) {
            if (typedText.trim().isEmpty()) {
                return doubles;
            } else {
                return new ArrayList<>();
            }
        }
        return null;
    }

    /**
     * Get possible values for the selected string list option
     *
     * @param selectedOption <p>The selected option</p>
     * @param args           <p>The arguments given by the user</p>
     * @return <p>Some or all of the valid values for the option</p>
     */
    private List<String> getPossibleStringListOptionValues(ConfigOption selectedOption, String[] args) {
        if (selectedOption == ConfigOption.PER_SIGN_COLORS) {
            return getPerSignColorCompletion(args);
        } else {
            return null;
        }
    }

    /**
     * Gets the tab completion values for completing the per-sign color text
     *
     * @param args <p>The arguments given by the user</p>
     * @return <p>The options to give the user</p>
     */
    private List<String> getPerSignColorCompletion(String[] args) {
        if (args.length < 3) {
            return filterMatching(signTypes, args[1]);
        } else if (args.length < 4) {
            return filterMatching(extendedColors, args[2]);
        } else if (args.length < 5) {
            return filterMatching(extendedColors, args[3]);
        }
        return new ArrayList<>();
    }

    /**
     * Puts a single string value into a string list
     *
     * @param value <p>The string to make into a list</p>
     * @return <p>A list containing the string value</p>
     */
    private List<String> putStringInList(String value) {
        List<String> list = new ArrayList<>();
        list.add(value);
        return list;
    }

    /**
     * Initializes all lists of auto-completable values
     */
    private void initializeAutoCompleteLists() {
        booleans = new ArrayList<>();
        booleans.add("true");
        booleans.add("false");

        integers = new ArrayList<>();
        integers.add("0");
        integers.add("5");

        signTypes = new ArrayList<>();
        for (Material material : Material.values()) {
            if (Tag.STANDING_SIGNS.isTagged(material)) {
                signTypes.add(material.toString().replace("_SIGN", ""));
            }
        }

        getColors();
        initializeLanguages();

        extendedColors = new ArrayList<>(chatColors);
        extendedColors.add("default");
        extendedColors.add("inverted");

        doubles = new ArrayList<>();
        doubles.add("5");
        doubles.add("1");
        doubles.add("0.5");
        doubles.add("0.1");
    }


    /**
     * Initializes the list of chat colors
     */
    private void getColors() {
        chatColors = new ArrayList<>();
        for (ChatColor color : getChatColors()) {
            chatColors.add(color.getName());
        }
    }

    /**
     * Gets available chat colors
     *
     * @return <p>The available chat colors</p>
     */
    private List<ChatColor> getChatColors() {
        List<ChatColor> chatColors = new ArrayList<>();
        char[] colors = new char[]{'a', 'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        for (char color : colors) {
            chatColors.add(ChatColor.getByChar(color));
        }
        chatColors.add(ChatColor.of("#ed76d9"));
        chatColors.add(ChatColor.of("#ffecb7"));
        return chatColors;
    }

    /**
     * Initializes the list of all available languages
     */
    private void initializeLanguages() {
        languages = new ArrayList<>();
        languages.add("de");
        languages.add("en");
        languages.add("es");
        languages.add("fr");
        languages.add("hu");
        languages.add("it");
        languages.add("nb-no");
        languages.add("nl");
        languages.add("nn-no");
        languages.add("pt-br");
        languages.add("ru");
        languages.add("zh_cn");
        //TODO: Generate this list dynamically by listing the language files in the jar and adding the user's custom 
        // language files
    }

}
