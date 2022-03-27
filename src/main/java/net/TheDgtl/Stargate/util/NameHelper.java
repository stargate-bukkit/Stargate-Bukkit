package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.md_5.bungee.api.ChatColor;

public class NameHelper {

    /**
     * @param name <p> The name to check </p>
     * @return <p> The allowed name </P>
     */
    public static String getAllowedName(String name) {
        name = name.replaceAll("\s\s+", " ");
        return name.trim();
    }

    /**
     * Gets the id of this name
     *
     * <p>This basically just lower-cases the name, and strips color if enabled. This is to make names
     * case-agnostic and optionally color-agnostic.</p>
     *
     * @param name <p>The name to "hash"</p>
     * @return <p>The "hashed" name</p>
     */
    public static String getID(String name) {
        String nameHash = name.toLowerCase();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.DISABLE_CUSTOM_COLORED_NAMES)) {
            nameHash = ChatColor.stripColor(nameHash);
        }
        return nameHash;
    }

}
