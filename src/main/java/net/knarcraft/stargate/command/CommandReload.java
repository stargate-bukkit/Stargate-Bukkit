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

    private final Stargate plugin;

    /**
     * Instantiates the reload command
     *
     * @param plugin <p>A reference to the calling plugin object</p>
     */
    public CommandReload(Stargate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (!player.hasPermission("stargate.reload")) {
                Stargate.sendMessage(commandSender, "Permission Denied");
                return true;
            }
        }
        plugin.reload(commandSender);
        return true;
    }

}
