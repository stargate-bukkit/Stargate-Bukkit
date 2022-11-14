package org.sgrewritten.stargate.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.property.CommandPermission;

/**
 * This command represents the plugin's about command
 */
public class CommandAbout implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {
        if (!commandSender.hasPermission(CommandPermission.ABOUT.getPermissionNode())) {
            commandSender.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.DENY));
            return true;
        }
        commandSender.sendMessage(Stargate.getLanguageManagerStatic().getMessage(TranslatableMessage.COMMAND_HELP));
        return true;
    }

}
