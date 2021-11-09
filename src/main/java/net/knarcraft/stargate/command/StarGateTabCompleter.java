package net.knarcraft.stargate.command;

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
        List<String> commands = new ArrayList<>();
        commands.add("about");
        if (!(commandSender instanceof Player player) || player.hasPermission("stargate.admin.reload")) {
            commands.add("reload");
        }
        if (!(commandSender instanceof Player player) || player.hasPermission("stargate.admin")) {
            commands.add("config");
        }


        if (args.length == 1) {
            return commands;
        } else {
            return new ArrayList<>();
        }
    }

}
