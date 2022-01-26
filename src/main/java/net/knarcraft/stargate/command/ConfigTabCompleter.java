package net.knarcraft.stargate.command;

import net.knarcraft.stargate.config.ConfigOption;
import net.knarcraft.stargate.config.OptionDataType;
import net.md_5.bungee.api.ChatColor;
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

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                                      @NotNull String[] args) {

        if (args.length > 1) {
            ConfigOption selectedOption = ConfigOption.getByName(args[0]);
            if (selectedOption == null) {
                return new ArrayList<>();
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
        List<String> booleans = new ArrayList<>();
        booleans.add("true");
        booleans.add("false");

        List<String> numbers = new ArrayList<>();
        numbers.add("0");
        numbers.add("5");

        switch (selectedOption) {
            case LANGUAGE:
                //Return available languages
                return filterMatching(getLanguages(), typedText);
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
                return filterMatching(getColors(), typedText);
        }

        //If the config value is a boolean, show the two boolean values
        if (selectedOption.getDataType() == OptionDataType.BOOLEAN) {
            return filterMatching(booleans, typedText);
        }

        //If the config value is an integer, display some valid numbers
        if (selectedOption.getDataType() == OptionDataType.INTEGER) {
            if (typedText.trim().isEmpty()) {
                return numbers;
            } else {
                return new ArrayList<>();
            }
        }

        //TODO: What to do with per-sign colors?

        return null;
    }

    /**
     * Gets all available languages
     *
     * @return <p>The available languages</p>
     */
    private List<String> getLanguages() {
        List<String> languages = new ArrayList<>();
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
        return languages;
    }

    /**
     * Gets all available colors
     *
     * @return <p>All available colors</p>
     */
    private List<String> getColors() {
        List<String> colors = new ArrayList<>();
        for (ChatColor color : getChatColors()) {
            colors.add(color.getName());
        }
        return colors;
    }

    /**
     * Gets a list of all available chat colors
     *
     * @return <p>A list of chat colors</p>
     */
    private List<ChatColor> getChatColors() {
        List<ChatColor> chatColors = new ArrayList<>();
        chatColors.add(ChatColor.WHITE);
        chatColors.add(ChatColor.BLUE);
        chatColors.add(ChatColor.DARK_BLUE);
        chatColors.add(ChatColor.DARK_PURPLE);
        chatColors.add(ChatColor.LIGHT_PURPLE);
        chatColors.add(ChatColor.GOLD);
        chatColors.add(ChatColor.GREEN);
        chatColors.add(ChatColor.BLACK);
        chatColors.add(ChatColor.DARK_GREEN);
        chatColors.add(ChatColor.DARK_RED);
        chatColors.add(ChatColor.RED);
        chatColors.add(ChatColor.AQUA);
        chatColors.add(ChatColor.DARK_AQUA);
        chatColors.add(ChatColor.DARK_GRAY);
        chatColors.add(ChatColor.GRAY);
        chatColors.add(ChatColor.YELLOW);
        return chatColors;
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

}
