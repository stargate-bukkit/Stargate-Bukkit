package net.TheDgtl.Stargate.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;

/**
 * This command represents the plugin's reload command
 */
public class CommandReload implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
            @NotNull String[] args) {
        if (!commandSender.hasPermission(CommandPermission.RELOAD.getPermissionNode())) {
            commandSender.sendMessage(Stargate.languageManager.getErrorMessage(TranslatableMessage.DENY));
            return true;
        }
        Stargate.getInstance().load();
        commandSender.sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.COMMAND_RELOAD));
        return true;
    }

}
