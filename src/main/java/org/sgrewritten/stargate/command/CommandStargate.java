package org.sgrewritten.stargate.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.property.CommandPermission;
import org.sgrewritten.stargate.util.TranslatableMessageFormatter;

/**
 * This command represents any command which starts with stargate
 *
 * <p>This prefix command should only be used for commands which are certain to collide with others and which relate to
 * the plugin itself, not commands for functions of the plugin.</p>
 */
public class CommandStargate implements CommandExecutor {

    private LanguageManager languageManager;
    private @NotNull Stargate stargate;

    public CommandStargate(@NotNull Stargate stargate) {
        this.languageManager = stargate.getLanguageManager();
        this.stargate = stargate;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "about":
                    return new CommandAbout(languageManager).onCommand(commandSender, command, s, args);
                case "reload":
                    return new CommandReload(languageManager).onCommand(commandSender, command, s, args);
                case "trace":
                    return new CommandTrace(stargate).onCommand(commandSender, command, s, args);
                case "version":
                    break;
                case "parityconfirm":
                    return new CommandParity(stargate.getStoredPropertiesAPI(),stargate.getDataFolder(),true).onCommand(commandSender, command, s, args);
                case "parityreject":
                    return new CommandParity(stargate.getStoredPropertiesAPI(),stargate.getDataFolder(),false).onCommand(commandSender, command, s, args);
                default:
                    return false;
            }
        }
        if (!commandSender.hasPermission(CommandPermission.VERSION.getPermissionNode())) {
            commandSender.sendMessage(languageManager.getErrorMessage(TranslatableMessage.DENY));
            return true;
        }
        String unformattedMessage = languageManager.getMessage(TranslatableMessage.COMMAND_INFO);
        commandSender.sendMessage(TranslatableMessageFormatter.formatVersion(unformattedMessage,
                Stargate.getInstance().getDescription().getVersion()));
        return true;

    }

}
