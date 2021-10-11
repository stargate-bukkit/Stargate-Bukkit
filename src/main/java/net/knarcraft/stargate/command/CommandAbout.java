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

        ChatColor textColor = ChatColor.GOLD;
        ChatColor highlightColor = ChatColor.GREEN;
        commandSender.sendMessage(textColor + "Stargate Plugin originally created by " + highlightColor +
                "Drakia" + textColor + ", and revived by " + highlightColor + "EpicKnarvik97");
        commandSender.sendMessage(textColor + "Go to " + highlightColor +
                "https://git.knarcraft.net/EpicKnarvik97/Stargate " + textColor + "for the official repository");
        String author = Stargate.languageLoader.getString("author");
        if (!author.isEmpty())
            commandSender.sendMessage(textColor + "Language created by " + highlightColor + author);
        return true;
    }

}
