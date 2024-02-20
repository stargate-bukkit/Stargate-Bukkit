package net.knarcraft.stargate.command;

import de.themoep.minedown.MineDown;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.Message;
import net.knarcraft.stargate.utility.FileHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * This command represents the plugin's about command
 */
public class CommandAbout implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {

        ChatColor textColor = ChatColor.GOLD;
        ChatColor highlightColor = ChatColor.GREEN;

        try (InputStream inputStream = Stargate.class.getResourceAsStream("/messages/about.md")) {
            if (inputStream != null) {
                String aboutMessageString = FileHelper.readStreamToString(inputStream);
                BaseComponent[] component = MineDown.parse(aboutMessageString);
                commandSender.spigot().sendMessage(component);
            }
        } catch (IOException ioException) {
            commandSender.sendMessage("Internal error");
        }
        String author = Stargate.getStargateConfig().getLanguageLoader().getString(Message.AUTHOR);
        if (!author.isEmpty()) {
            commandSender.sendMessage(textColor + "Language created by " + highlightColor + author);
        }
        return true;
    }

}
