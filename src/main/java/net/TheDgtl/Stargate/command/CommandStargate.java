package net.TheDgtl.Stargate.command;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.util.TranslatableMessageFormatter;
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
public class CommandStargate implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "about":
                    return new CommandAbout().onCommand(commandSender, command, s, args);
                case "reload":
                    return new CommandReload().onCommand(commandSender, command, s, args);
                case "trace":
                    return new CommandTrace().onCommand(commandSender, command, s, args);
                default:
                    return false;
            }
        } else {
            String unformattedMessage = Stargate.getLanguageManagerStatic().getMessage(TranslatableMessage.COMMAND_INFO);
            commandSender.sendMessage(TranslatableMessageFormatter.formatVersion(unformattedMessage,
                    Stargate.getInstance().getDescription().getVersion()));
            return true;
        }
    }

}
