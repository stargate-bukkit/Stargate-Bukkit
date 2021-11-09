package net.knarcraft.stargate.command;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.ConfigOption;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

        if (args.length > 1) {
            //TODO: Do stuff
        } else {
            //TODO: Display list of config values
            displayConfigValues(commandSender);
        }
        return true;
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
            sender.sendMessage(ChatColor.GOLD + option.getName() + ChatColor.WHITE + " - " + ChatColor.GREEN +
                    option.getDescription() + " (" + option.getDefaultValue() + ")");
        }
    }

}
