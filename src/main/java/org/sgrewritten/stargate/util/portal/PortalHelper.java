package org.sgrewritten.stargate.util.portal;

import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

import java.util.Map;
import java.util.Set;

/**
 * A helper class for dealing with portals
 */
public final class PortalHelper {

    private PortalHelper() {

    }

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


    /**
     * Close all portals in specified map
     *
     * @param networkMap <p> The map with all portals to close </p>
     */
    public static void closeAllPortals(Map<String, Network> networkMap) {
        for (Network network : networkMap.values()) {
            for (Portal portal : network.getAllPortals()) {
                if (portal.hasFlag(PortalFlag.ALWAYS_ON) && portal instanceof RealPortal) {
                    ((RealPortal) portal).getGate().close();
                }
            }
        }
    }

}
