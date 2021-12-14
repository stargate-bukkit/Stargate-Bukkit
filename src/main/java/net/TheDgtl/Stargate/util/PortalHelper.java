package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.network.portal.PortalFlag;

import java.util.Set;

/**
 * A helper class for dealing with portals
 */
public class PortalHelper {

    /**
     * Gets the string representation of a set of portal flags
     *
     * @param flags <p>The flags to convert to a string</p>
     * @return <p>A string representing the portal flags</p>
     */
    public static String flagsToString(Set<PortalFlag> flags) {
        StringBuilder flagsStringBuilder = new StringBuilder();
        for (PortalFlag flag : flags) {
            flagsStringBuilder.append(flag.getCharacterRepresentation());
        }
        return flagsStringBuilder.toString();
    }

}
