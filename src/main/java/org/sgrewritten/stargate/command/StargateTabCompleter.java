package org.sgrewritten.stargate.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.database.property.StoredPropertiesAPI;
import org.sgrewritten.stargate.database.property.StoredProperty;
import org.sgrewritten.stargate.property.CommandPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is the tab completer for the /stargate (/sg) command
 */
public class StargateTabCompleter implements TabCompleter {
    
    private @NotNull StoredPropertiesAPI properties;

    public StargateTabCompleter(@NotNull StoredPropertiesAPI properties){
        this.properties = Objects.requireNonNull(properties);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> commands = getAvailableCommands(commandSender);
            List<String> matchingCommands = new ArrayList<>();
            for (String availableCommand : commands) {
                if (availableCommand.startsWith(args[0])) {
                    matchingCommands.add(availableCommand);
                }
            }
            return matchingCommands;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Gets the available commands
     *
     * @param commandSender <p>The command sender to get available commands for</p>
     * @return <p>The commands available to the command sender</p>
     */
    private List<String> getAvailableCommands(CommandSender commandSender) {
        List<String> commands = new ArrayList<>();
        if (commandSender.hasPermission(CommandPermission.RELOAD.getPermissionNode())) {
            commands.add("reload");
        }
        if (commandSender.hasPermission(CommandPermission.TRACE.getPermissionNode())) {
            commands.add("trace");
        }
        if (commandSender.hasPermission(CommandPermission.ABOUT.getPermissionNode())) {
            commands.add("about");
        }
        if (commandSender.hasPermission(CommandPermission.VERSION.getPermissionNode())) {
            commands.add("version");
        }
        if (commandSender instanceof ConsoleCommandSender && "true".equals(properties.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE))) {
            commands.add("parityconfirm");
            commands.add("parityreject");
        }
        return commands;
    }

}