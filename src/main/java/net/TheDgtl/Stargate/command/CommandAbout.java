package net.TheDgtl.Stargate.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;

/**
 * This command represents the plugin's about command
 */
public class CommandAbout implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {
        if(!commandSender.hasPermission(CommandPermission.INFO_ABOUT.getPermissionNode())) {
            commandSender.sendMessage(Stargate.languageManager.getErrorMessage(TranslatableMessage.DENY));
            return true;
        }
        commandSender.sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.COMMAND_HELP));
        return true;
    }
    

}
