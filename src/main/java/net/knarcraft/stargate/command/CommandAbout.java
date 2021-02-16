package net.knarcraft.stargate.command;

import net.knarcraft.stargate.Stargate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * This command represents the plugin's about command
 */
public class CommandAbout implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {

        commandSender.sendMessage(ChatColor.GOLD + "Stargate Plugin created by " + ChatColor.GREEN + "Drakia");
        String author = Stargate.languageLoader.getString("author");
        if (!author.isEmpty())
            commandSender.sendMessage(ChatColor.GOLD + "Language created by " + ChatColor.GREEN + author);
        return true;
    }

}
