package net.knarcraft.stargate.command;

import net.knarcraft.stargate.Stargate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * This command represents any command which starts with stargate
 *
 * <p>This prefix command should only be used for commands which are certain to collide with others and which relate to
 * the plugin itself, not commands for functions of the plugin.</p>
 */
public class CommandStarGate implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("about")) {
                return new CommandAbout().onCommand(commandSender, command, s, args);
            } else if (args[0].equalsIgnoreCase("reload")) {
                return new CommandReload().onCommand(commandSender, command, s, args);
            } else if (args[0].equalsIgnoreCase("config")) {
                return new CommandConfig().onCommand(commandSender, command, s, args);
            }
            return false;
        } else {
            commandSender.sendMessage(ChatColor.GOLD + "Stargate version " +
                    ChatColor.GREEN + Stargate.getPluginVersion());
            return true;
        }
    }
}
