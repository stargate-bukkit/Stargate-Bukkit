package org.sgrewritten.stargate.util;

import net.md_5.bungee.api.ChatColor;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.property.StargateConstant;

import java.util.regex.Pattern;

/**
 * A helper class for dealing with portal and network names
 */
public final class NameHelper {

    private static final Pattern WHITE_CHARACTERS = Pattern.compile("\\s\\s+");

    private NameHelper() {

    }

    /**
     * Trims a name and replaces whitespace with normal spaces
     *
     * @param name <p>The name to check</p>
     * @return <p>The trimmed name</p>
     */
    public static String getTrimmedName(String name) {
        if (name == null) {
            return null;
        }
        name = WHITE_CHARACTERS.matcher(name).replaceAll(" ");
        return name.trim();
    }

    /**
     * Gets the normalized version of the given name
     *
     * <p>This basically just lower-cases the name, trims the name and strips color if enabled.
     * This is to make names case-agnostic and optionally color-agnostic.</p>
     *
     * @param name <p>The name to "hash"</p>
     * @return <p>The "hashed" name</p>
     */
    public static String getNormalizedName(String name) {
        String normalizedName = name.toLowerCase();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.DISABLE_CUSTOM_COLORED_NAMES)) {
            normalizedName = ChatColor.stripColor(normalizedName);
        }
        return getTrimmedName(normalizedName);
    }

    /**
     * Checks the length of the name
     *
     * @param name <p> The name to be checked </p>
     * @return <p> If the name has a valid length </p>
     */
    public static boolean isInvalidName(String name) {
        return name == null || name.isEmpty() || name.length() >= StargateConstant.MAX_TEXT_LENGTH;
    }

}
