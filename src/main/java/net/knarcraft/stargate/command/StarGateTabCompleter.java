package net.knarcraft.stargate.command;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the tab completer for the /stargate (/sg) command
 */
public class StarGateTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> commands = getAvailableCommands(commandSender);
            List<String> matchingCommands = new ArrayList<>();
            for (String availableCommand : commands) {
                if (availableCommand.startsWith(args[0])) {
                    matchingCommands.add(availableCommand);
                }
            }
            return matchingCommands;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("config")) {
            String[] subArgs = (String[]) ArrayUtils.remove(args, 0);
            return new ConfigTabCompleter().onTabComplete(commandSender, command, s, subArgs);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Gets the available commands
     *
     * @param commandSender <p>The command sender to get available commands for</p>
     * @return <p>The commands available to the command sender</p>
     */
    private List<String> getAvailableCommands(CommandSender commandSender) {
        List<String> commands = new ArrayList<>();
        commands.add("about");
        if (!(commandSender instanceof Player player) || player.hasPermission("stargate.admin.reload")) {
            commands.add("reload");
        }
        if (!(commandSender instanceof Player player) || player.hasPermission("stargate.admin")) {
            commands.add("config");
        }
        return commands;
    }

}
