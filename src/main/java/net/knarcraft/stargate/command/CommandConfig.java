package net.knarcraft.stargate.command;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.ConfigOption;
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
            if (!player.hasPermission("stargate.admin")) {
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
        if (selectedOption == ConfigOption.MAIN_SIGN_COLOR || selectedOption == ConfigOption.HIGHLIGHT_SIGN_COLOR) {
            try {
                ChatColor.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException | NullPointerException ignored) {
                commandSender.sendMessage(ChatColor.RED + "Invalid color given");
                return;
            }
        }

        //Store the config values, accounting for the data type
        switch (selectedOption.getDataType()) {
            case BOOLEAN -> configuration.set(selectedOption.getConfigNode(), Boolean.parseBoolean(value));
            case INTEGER -> {
                try {
                    configuration.set(selectedOption.getConfigNode(), Integer.parseInt(value));
                } catch (NumberFormatException exception) {
                    commandSender.sendMessage(ChatColor.RED + "Invalid number given");
                    return;
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
                configuration.set(selectedOption.getConfigNode(), value);
            }
            default -> configuration.set(selectedOption.getConfigNode(), value);
        }

        //Save the config file and reload if necessary
        Stargate.getInstance().saveConfig();
        reloadIfNecessary(commandSender);
    }

    /**
     * Reloads the config if necessary
     *
     * @param commandSender <p>The command sender initiating the reload</p>
     */
    private void reloadIfNecessary(CommandSender commandSender) {
        //TODO: Only update the config values which have changed and do the least amount of work necessary to load the 
        // changes. Only do a full reload if absolutely necessary, or when the partial reloading would be as 
        // inefficient as a full reload.
        Stargate.getStargateConfig().reload(commandSender);
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
