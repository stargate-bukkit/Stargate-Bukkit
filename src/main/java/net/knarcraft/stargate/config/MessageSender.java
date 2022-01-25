package net.knarcraft.stargate.config;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * The message sender is responsible sending messages to players with correct coloring and formatting
 */
public final class MessageSender {

    private final LanguageLoader languageLoader;

    /**
     * Instantiates a new message sender
     *
     * @param languageLoader <p>The language loader to get translated strings from</p>
     */
    public MessageSender(LanguageLoader languageLoader) {
        this.languageLoader = languageLoader;
    }

    /**
     * Sends an error message to a player
     *
     * @param player  <p>The player to send the message to</p>
     * @param message <p>The message to send</p>
     */
    public void sendErrorMessage(CommandSender player, String message) {
        sendMessage(player, message, true);
    }

    /**
     * Sends a success message to a player
     *
     * @param player  <p>The player to send the message to</p>
     * @param message <p>The message to send</p>
     */
    public void sendSuccessMessage(CommandSender player, String message) {
        sendMessage(player, message, false);
    }

    /**
     * Sends a message to a player
     *
     * @param sender  <p>The player to send the message to</p>
     * @param message <p>The message to send</p>
     * @param error   <p>Whether the message sent is an error</p>
     */
    private void sendMessage(CommandSender sender, String message, boolean error) {
        if (message.isEmpty()) {
            return;
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        if (error) {
            sender.sendMessage(ChatColor.RED + languageLoader.getString("prefix") + ChatColor.WHITE + message);
        } else {
            sender.sendMessage(ChatColor.GREEN + languageLoader.getString("prefix") + ChatColor.WHITE + message);
        }
    }

}
