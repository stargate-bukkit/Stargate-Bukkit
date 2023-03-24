package net.knarcraft.stargate.command;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.ConfigOption;
import net.knarcraft.stargate.config.ConfigTag;
import net.knarcraft.stargate.config.DynmapManager;
import net.knarcraft.stargate.config.OptionDataType;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalRegistry;
import net.knarcraft.stargate.portal.PortalSignDrawer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This command represents the config command for changing config values
 */
public class CommandConfig implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (commandSender instanceof Player player) {
            if (!player.hasPermission("stargate.admin.config")) {
                Stargate.getMessageSender().sendErrorMessage(commandSender, "Permission Denied");
                return true;
            }
        }

        if (args.length > 0) {
            ConfigOption selectedOption = ConfigOption.getByName(args[0]);
            if (selectedOption == null) {
                return false;
            }
            if (args.length > 1) {
                if (selectedOption.getDataType() == OptionDataType.STRING_LIST) {
                    updateListConfigValue(selectedOption, commandSender, args);
                } else {
                    updateConfigValue(selectedOption, commandSender, args[1]);
                }
            } else {
                //Display info and the current value of the given config value
                printConfigOptionValue(commandSender, selectedOption);
            }
            return true;
        } else {
            //Display all config options
            displayConfigValues(commandSender);
        }
        return true;
    }

    /**
     * Updates a config value
     *
     * @param selectedOption <p>The option which should be updated</p>
     * @param commandSender  <p>The command sender that changed the value</p>
     * @param value          <p>The new value of the config option</p>
     */
    private void updateConfigValue(ConfigOption selectedOption, CommandSender commandSender, String value) {
        FileConfiguration configuration = Stargate.getInstance().getConfig();

        //Validate any sign colors
        if (ConfigTag.COLOR.isTagged(selectedOption)) {
            try {
                ChatColor.of(value.toUpperCase());
            } catch (IllegalArgumentException | NullPointerException ignored) {
                commandSender.sendMessage(ChatColor.RED + "Invalid color given");
                return;
            }
        }

        //Store the config values, accounting for the data type
        switch (selectedOption.getDataType()) {
            case BOOLEAN -> updateBooleanConfigValue(selectedOption, value, configuration);
            case INTEGER -> {
                Integer intValue = getInteger(commandSender, selectedOption, value);
                if (intValue == null) {
                    return;
                } else {
                    Stargate.getStargateConfig().getConfigOptionsReference().put(selectedOption, intValue);
                    configuration.set(selectedOption.getConfigNode(), intValue);
                }
            }
            case DOUBLE -> {
                Double doubleValue = getDouble(commandSender, selectedOption, value);
                if (doubleValue == null) {
                    return;
                } else {
                    Stargate.getStargateConfig().getConfigOptionsReference().put(selectedOption, doubleValue);
                    configuration.set(selectedOption.getConfigNode(), doubleValue);
                }
            }
            case STRING -> {
                updateStringConfigValue(selectedOption, commandSender, value);
                configuration.set(selectedOption.getConfigNode(), value);
            }
            default -> {
                Stargate.getStargateConfig().getConfigOptionsReference().put(selectedOption, value);
                configuration.set(selectedOption.getConfigNode(), value);
            }
        }

        saveAndReload(selectedOption, commandSender);
    }

    /**
     * Updates a boolean config value
     *
     * @param selectedOption <p>The option which should be updated</p>
     * @param value          <p>The new value of the config option</p>
     * @param configuration  <p>The configuration file to save to</p>
     */
    private void updateBooleanConfigValue(ConfigOption selectedOption, String value, FileConfiguration configuration) {
        boolean newValue = Boolean.parseBoolean(value);
        if (selectedOption == ConfigOption.ENABLE_BUNGEE && newValue != Stargate.getGateConfig().enableBungee()) {
            Stargate.getStargateConfig().startStopBungeeListener(newValue);
        }
        Stargate.getStargateConfig().getConfigOptionsReference().put(selectedOption, newValue);
        configuration.set(selectedOption.getConfigNode(), newValue);
    }

    /**
     * Updates a string config value
     *
     * @param selectedOption <p>The option which should be updated</p>
     * @param commandSender  <p>The command sender that changed the value</p>
     * @param value          <p>The new value of the config option</p>
     */
    private void updateStringConfigValue(ConfigOption selectedOption, CommandSender commandSender, String value) {
        if (selectedOption == ConfigOption.GATE_FOLDER || selectedOption == ConfigOption.PORTAL_FOLDER ||
                selectedOption == ConfigOption.DEFAULT_GATE_NETWORK) {
            if (value.contains("../") || value.contains("..\\")) {
                commandSender.sendMessage(ChatColor.RED + "Path traversal characters cannot be used");
                return;
            }
        }
        if (ConfigTag.COLOR.isTagged(selectedOption)) {
            if (!registerColor(selectedOption, value, commandSender)) {
                return;
            }
        }
        if (selectedOption == ConfigOption.LANGUAGE) {
            Stargate.getStargateConfig().getLanguageLoader().setChosenLanguage(value);
        }
        Stargate.getStargateConfig().getConfigOptionsReference().put(selectedOption, value);
    }

    /**
     * Updates a config value
     *
     * @param selectedOption <p>The option which should be updated</p>
     * @param commandSender  <p>The command sender that changed the value</p>
     * @param arguments      <p>The arguments for the new config option</p>
     */
    private void updateListConfigValue(ConfigOption selectedOption, CommandSender commandSender, String[] arguments) {
        FileConfiguration configuration = Stargate.getInstance().getConfig();

        if (selectedOption == ConfigOption.PER_SIGN_COLORS) {
            if (arguments.length < 4) {
                Stargate.getMessageSender().sendErrorMessage(commandSender, "Usage: /sg config perSignColors " +
                        "<SIGN_TYPE> <MAIN_COLOR> <HIGHLIGHTING_COLOR>");
                return;
            }

            String colorString = parsePerSignColorInput(commandSender, arguments);
            if (colorString == null) {
                return;
            }

            //Update the per-sign colors according to input
            updatePerSignColors(arguments[1], colorString, configuration);
        }

        saveAndReload(selectedOption, commandSender);
    }

    /**
     * Parses the input given for changing the per-color string
     *
     * @param commandSender <p>The command sender that triggered the command</p>
     * @param arguments     <p>The arguments given by the user</p>
     * @return <p>The per-sign color string to update with, or null if the input was invalid</p>
     */
    private String parsePerSignColorInput(CommandSender commandSender, String[] arguments) {
        //Make sure the sign type is an actual sign
        if (Material.matchMaterial(arguments[1] + "_SIGN") == null) {
            Stargate.getMessageSender().sendErrorMessage(commandSender, "The given sign type is invalid");
            return null;
        }
        String colorString = arguments[1] + ":";

        //Validate the colors given by the user
        String[] errorMessage = new String[]{"The given main sign color is invalid!", "The given highlight sign color is invalid!"};
        String[] newColors = new String[2];
        for (int i = 0; i < 2; i++) {
            if (validatePerSignColor(arguments[i + 2])) {
                newColors[i] = arguments[i + 2];
            } else {
                Stargate.getMessageSender().sendErrorMessage(commandSender, errorMessage[i]);
                return null;
            }
        }
        colorString += String.join(",", newColors);
        return colorString;
    }

    /**
     * Updates the per-sign colors with the given input
     *
     * @param signType      <p>The sign type that is updated</p>
     * @param colorString   <p>The new color string to replace any previous value with</p>
     * @param configuration <p>The file configuration to update with the new per-sign colors</p>
     */
    private void updatePerSignColors(String signType, String colorString, FileConfiguration configuration) {
        List<String> newColorStrings = new ArrayList<>();
        List<?> oldColors = (List<?>) Stargate.getStargateConfig().getConfigOptionsReference().get(ConfigOption.PER_SIGN_COLORS);
        for (Object object : oldColors) {
            newColorStrings.add(String.valueOf(object));
        }
        newColorStrings.removeIf((item) -> item.startsWith(signType));
        newColorStrings.add(colorString);

        Stargate.getStargateConfig().getConfigOptionsReference().put(ConfigOption.PER_SIGN_COLORS, newColorStrings);
        configuration.set(ConfigOption.PER_SIGN_COLORS.getConfigNode(), newColorStrings);
    }

    /**
     * Tries to validate one of the colors given when changing per-sign colors
     *
     * @param color <p>The color chosen by the user</p>
     * @return <p>True if the given color is valid</p>
     */
    private boolean validatePerSignColor(String color) {
        ChatColor newHighlightColor = parseColor(color);
        return newHighlightColor != null || color.equalsIgnoreCase("default") ||
                color.equalsIgnoreCase("inverted");
    }

    /**
     * Saves the configuration file and reloads as necessary
     *
     * @param selectedOption <p>The config option that was changed</p>
     * @param commandSender  <p>The command sender that executed the config command</p>
     */
    private void saveAndReload(ConfigOption selectedOption, CommandSender commandSender) {
        //Save the config file and reload if necessary
        Stargate.getInstance().saveConfig();

        Stargate.getMessageSender().sendSuccessMessage(commandSender, "Config updated");

        //Reload whatever is necessary
        reloadIfNecessary(commandSender, selectedOption);
    }

    /**
     * Registers the chat color if
     *
     * @param selectedOption <p>The option to change</p>
     * @param commandSender  <p>The command sender to alert if the color is invalid</p>
     * @param value          <p>The new option value</p>
     */
    private boolean registerColor(ConfigOption selectedOption, String value, CommandSender commandSender) {
        ChatColor parsedColor = parseColor(value);
        if (parsedColor == null) {
            commandSender.sendMessage(ChatColor.RED + "Invalid color given");
            return false;
        }

        if (selectedOption == ConfigOption.FREE_GATES_COLOR) {
            PortalSignDrawer.setFreeColor(parsedColor);
        } else if (selectedOption == ConfigOption.MAIN_SIGN_COLOR) {
            PortalSignDrawer.setMainColor(parsedColor);
        } else if (selectedOption == ConfigOption.HIGHLIGHT_SIGN_COLOR) {
            PortalSignDrawer.setHighlightColor(parsedColor);
        }
        return true;
    }

    /**
     * Parses a chat color
     *
     * @param value <p>The value to parse</p>
     * @return <p>The parsed color or null</p>
     */
    private ChatColor parseColor(String value) {
        try {
            return ChatColor.of(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return null;
        }
    }

    /**
     * Gets an integer from a string
     *
     * @param commandSender  <p>The command sender that sent the config command</p>
     * @param selectedOption <p>The option the command sender is trying to change</p>
     * @param value          <p>The value given</p>
     * @return <p>An integer, or null if it was invalid</p>
     */
    private Integer getInteger(CommandSender commandSender, ConfigOption selectedOption, String value) {
        try {
            int intValue = Integer.parseInt(value);

            if ((selectedOption == ConfigOption.USE_COST || selectedOption == ConfigOption.CREATE_COST) && intValue < 0) {
                commandSender.sendMessage(ChatColor.RED + "This config option cannot be negative.");
                return null;
            }

            return intValue;
        } catch (NumberFormatException exception) {
            commandSender.sendMessage(ChatColor.RED + "Invalid number given");
            return null;
        }
    }

    /**
     * Gets a double from a string
     *
     * @param commandSender  <p>The command sender that sent the config command</p>
     * @param selectedOption <p>The option the command sender is trying to change</p>
     * @param value          <p>The value given</p>
     * @return <p>A double, or null if it was invalid</p>
     */
    private Double getDouble(CommandSender commandSender, ConfigOption selectedOption, String value) {
        try {
            double doubleValue = Double.parseDouble(value);

            if (selectedOption == ConfigOption.EXIT_VELOCITY && doubleValue < 0) {
                commandSender.sendMessage(ChatColor.RED + "This config option cannot be negative.");
                return null;
            }

            return doubleValue;
        } catch (NumberFormatException exception) {
            commandSender.sendMessage(ChatColor.RED + "Invalid number given");
            return null;
        }
    }

    /**
     * Reloads the config if necessary
     *
     * @param commandSender <p>The command sender initiating the reload</p>
     * @param configOption  <p>The changed config option</p>
     */
    private void reloadIfNecessary(CommandSender commandSender, ConfigOption configOption) {
        if (ConfigTag.requiresFullReload(configOption)) {
            //Reload everything
            Stargate.getStargateConfig().reload(commandSender);
        } else {
            if (ConfigTag.requiresColorReload(configOption)) {
                Stargate.getStargateConfig().getStargateGateConfig().loadPerSignColors();
            }
            if (ConfigTag.requiresPortalReload(configOption)) {
                //Just unload and reload the portals
                Stargate.getStargateConfig().unloadAllPortals();
                Stargate.getStargateConfig().loadAllPortals();
            }
            if (ConfigTag.requiresLanguageReload(configOption)) {
                //Reload the language loader
                Stargate.getStargateConfig().getLanguageLoader().reload();
                //Re-draw all portal signs
                for (Portal portal : PortalRegistry.getAllPortals()) {
                    portal.drawSign();
                }
            }
            if (ConfigTag.requiresEconomyReload(configOption)) {
                //Load or unload Vault and Economy as necessary
                Stargate.getStargateConfig().reloadEconomy();
            }
            if (ConfigTag.requiresDynmapReload(configOption)) {
                //Regenerate all Dynmap markers
                DynmapManager.addAllPortalMarkers();
            }
        }
    }

    /**
     * Prints information about a config option and its current value
     *
     * @param sender <p>The command sender that sent the command</p>
     * @param option <p>The config option to print information about</p>
     */
    private void printConfigOptionValue(CommandSender sender, ConfigOption option) {
        Object value = Stargate.getStargateConfig().getConfigOptions().get(option);
        sender.sendMessage(getOptionDescription(option));
        sender.sendMessage(ChatColor.GREEN + "Current value: " + ChatColor.GOLD + value);
    }

    /**
     * Displays the name and a small description of every config value
     *
     * @param sender <p>The command sender to display the config list to</p>
     */
    private void displayConfigValues(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + Stargate.getBackupString("prefix") + ChatColor.GOLD +
                "Config values:");
        for (ConfigOption option : ConfigOption.values()) {
            sender.sendMessage(getOptionDescription(option));
        }
    }

    /**
     * Gets the description of a single config option
     *
     * @param option <p>The option to describe</p>
     * @return <p>A string describing the config option</p>
     */
    private String getOptionDescription(ConfigOption option) {
        Object defaultValue = option.getDefaultValue();
        String stringValue = String.valueOf(defaultValue);
        if (option.getDataType() == OptionDataType.STRING_LIST) {
            stringValue = "[" + String.join(",", (String[]) defaultValue) + "]";
        }
        return ChatColor.GOLD + option.getName() + ChatColor.WHITE + " - " + ChatColor.GREEN + option.getDescription() +
                ChatColor.DARK_GRAY + " (Default: " + ChatColor.GRAY + stringValue + ChatColor.DARK_GRAY + ")";
    }

}
