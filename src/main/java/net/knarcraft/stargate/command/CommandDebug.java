package net.knarcraft.stargate.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class CommandDebug implements CommandExecutor {
    private final Plugin plugin;

    public CommandDebug(Plugin stargate){
        this.plugin = stargate;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ChatColor textColor = ChatColor.GOLD;
        ChatColor highlightColor = ChatColor.GREEN;
        commandSender.sendMessage(textColor + "Stargate version " + highlightColor + plugin.getDescription().getVersion()
                + textColor + "running on " + highlightColor + Bukkit.getServer().getVersion());
        return true;
    }
}
