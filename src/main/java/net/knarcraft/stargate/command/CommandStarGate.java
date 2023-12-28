package net.knarcraft.stargate.command;

import net.knarcraft.stargate.Stargate;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * This command represents any command which starts with stargate
 *
 * <p>This prefix command should only be used for commands which are certain to collide with others and which relate to
 * the plugin itself, not commands for functions of the plugin.</p>
 */
public class CommandStarGate implements CommandExecutor {
    private final Plugin stargate;

    public CommandStarGate(Plugin stargate) {
        this.stargate = stargate;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("about")) {
                return new CommandAbout().onCommand(commandSender, command, s, args);
            } else if (args[0].equalsIgnoreCase("reload")) {
                return new CommandReload().onCommand(commandSender, command, s, args);
            } else if (args[0].equalsIgnoreCase("config")) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return new CommandConfig().onCommand(commandSender, command, s, subArgs);
            } else if (args[0].equalsIgnoreCase("debug")) {
                return new CommandDebug(this.stargate).onCommand(commandSender, command, s, args);
            }
            return false;
        } else {
            commandSender.sendMessage(ChatColor.GOLD + "Stargate version " +
                    ChatColor.GREEN + Stargate.getPluginVersion());
            return true;
        }
    }

}
