package net.knarcraft.stargate.command;

import net.knarcraft.stargate.Stargate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This command represents the plugin's reload command
 */
public class CommandReload implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (commandSender instanceof Player player) {
            if (!player.hasPermission("stargate.reload")) {
                Stargate.getMessageSender().sendErrorMessage(commandSender, "Permission Denied");
                return true;
            }
        }
        Stargate.getStargateConfig().reload(commandSender);
        return true;
    }

}
