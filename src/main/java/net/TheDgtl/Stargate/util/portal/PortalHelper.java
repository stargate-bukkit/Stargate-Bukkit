package net.TheDgtl.Stargate.util.portal;

import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;

import java.util.Map;
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
    
    

    /**
     * Close all portals in specified map
     * @param networkMap <p> The map with all portals to close </p>
     */
    public static void closeAllPortals(Map<String, Network> networkMap) {
        for (Network network : networkMap.values()) {
            for (Portal portal : network.getAllPortals()) {
                if (portal.hasFlag(PortalFlag.ALWAYS_ON) && !portal.hasFlag(PortalFlag.FIXED) &&
                        portal instanceof RealPortal) {
                    ((RealPortal) portal).getGate().close();
                }
            }
        }
    }

}
