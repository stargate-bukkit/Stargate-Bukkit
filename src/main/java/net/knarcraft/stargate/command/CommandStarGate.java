package net.knarcraft.stargate.command;

import net.knarcraft.stargate.Stargate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * This command represents any command which starts with stargate
 *
 * <p>This prefix command should only be used for commands which are certain to collide with others and which relate to
 * the plugin itself, not commands for functions of the plugin.</p>
 */
public class CommandStarGate implements CommandExecutor {

    private final Stargate plugin;

    /**
     * Instantiates the stargate command
     *
     * @param plugin <p>A reference to the calling plugin object</p>
     */
    public CommandStarGate(Stargate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {
        if (strings.length > 0) {
            if (strings[0].equalsIgnoreCase("about")) {
                return new CommandAbout().onCommand(commandSender, command, s, strings);
            } else if (strings[0].equalsIgnoreCase("reload")) {
                return new CommandReload(plugin).onCommand(commandSender, command, s, strings);
            }
            return false;
        } else {
            commandSender.sendMessage(ChatColor.GOLD + "Stargate version " + ChatColor.GREEN + Stargate.getPluginVersion());
            return true;
        }
    }
}
