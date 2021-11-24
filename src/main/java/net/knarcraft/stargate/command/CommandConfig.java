package net.knarcraft.stargate.command;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.ConfigOption;
import net.knarcraft.stargate.config.ConfigTag;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalRegistry;
import net.knarcraft.stargate.portal.PortalSignDrawer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
                updateConfigValue(selectedOption, commandSender, args[1]);
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
                ChatColor.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException | NullPointerException ignored) {
                commandSender.sendMessage(ChatColor.RED + "Invalid color given");
                return;
            }
        }

        //Store the config values, accounting for the data type
        switch (selectedOption.getDataType()) {
            case BOOLEAN -> {
                boolean newValue = Boolean.parseBoolean(value);
                if (selectedOption == ConfigOption.ENABLE_BUNGEE && newValue != Stargate.getGateConfig().enableBungee()) {
                    Stargate.getStargateConfig().startStopBungeeListener(newValue);
                }
                Stargate.getStargateConfig().getConfigOptionsReference().put(selectedOption, newValue);
                configuration.set(selectedOption.getConfigNode(), newValue);
            }
            case INTEGER -> {
                Integer intValue = getInteger(commandSender, selectedOption, value);
                if (intValue == null) {
                    return;
                } else {
                    Stargate.getStargateConfig().getConfigOptionsReference().put(selectedOption, intValue);
                    configuration.set(selectedOption.getConfigNode(), intValue);
                }
            }
            case STRING -> {
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
                configuration.set(selectedOption.getConfigNode(), value);
            }
            default -> {
                Stargate.getStargateConfig().getConfigOptionsReference().put(selectedOption, value);
                configuration.set(selectedOption.getConfigNode(), value);
            }
        }

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
            return ChatColor.valueOf(value.toUpperCase());
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
     * Reloads the config if necessary
     *
     * @param commandSender <p>The command sender initiating the reload</p>
     * @param configOption  <p>The changed config option</p>
     */
    private void reloadIfNecessary(CommandSender commandSender, ConfigOption configOption) {
        if (ConfigTag.requiresFullReload(configOption)) {
            //Reload everything
            Stargate.getStargateConfig().reload(commandSender);
        } else if (ConfigTag.requiresPortalReload(configOption)) {
            //Just unload and reload the portals
            Stargate.getStargateConfig().unloadAllPortals();
            Stargate.getStargateConfig().loadAllPortals();
        } else if (ConfigTag.requiresLanguageReload(configOption)) {
            //Reload the language loader
            Stargate.getStargateConfig().getLanguageLoader().reload();
            //Re-draw all portal signs
            for (Portal portal : PortalRegistry.getAllPortals()) {
                portal.drawSign();
            }
        } else if (ConfigTag.requiresEconomyReload(configOption)) {
            //Load or unload Vault and Economy as necessary
            Stargate.getStargateConfig().reloadEconomy();
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
        return ChatColor.GOLD + option.getName() + ChatColor.WHITE + " - " + ChatColor.GREEN + option.getDescription() +
                ChatColor.DARK_GRAY + " (Default: " + ChatColor.GRAY + option.getDefaultValue() + ChatColor.DARK_GRAY + ")";
    }

}
