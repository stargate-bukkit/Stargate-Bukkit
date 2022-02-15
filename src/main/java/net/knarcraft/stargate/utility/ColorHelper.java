package net.knarcraft.stargate.utility;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class for dealing with colors
 */
public final class ColorHelper {

    private ColorHelper() {

    }

    /**
     * Inverts the given color
     *
     * @param color <p>The color to invert</p>
     * @return <p>The inverted color</p>
     */
    public static Color invert(Color color) {
        return color.setRed(255 - color.getRed()).setGreen(255 - color.getGreen()).setBlue(255 - color.getBlue());
    }

    /**
     * Gets the chat color corresponding to the given color
     *
     * @param color <p>The color to convert into a chat color</p>
     * @return <p>The resulting chat color</p>
     */
    public static ChatColor fromColor(Color color) {
        return ChatColor.of(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
    }

    /**
     * Translates all found color codes to formatting in a string
     *
     * @param message <p>The string to search for color codes</p>
     * @return <p>The message with color codes translated</p>
     */
    public static String translateAllColorCodes(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        Pattern pattern = Pattern.compile("(#[a-fA-F0-9]{6})");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            message = message.replace(matcher.group(), "" + ChatColor.of(matcher.group()));
        }
        return message;
    }

}
