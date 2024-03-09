package org.sgrewritten.stargate.util.portal;

import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.property.PortalValidity;

import java.util.Set;

/**
 * A helper class for dealing with portals
 */
public final class PortalHelper {

    private PortalHelper() {
        throw new IllegalStateException("Utility class");
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
     *
     * @param portal <p>The portal to check for</p>
     * @param networkManager <p>A stargate network manager</p>
     * @return <p>True if the portal is valid</p>
     */
    public static boolean portalValidityCheck(RealPortal portal, NetworkManager networkManager) {
        PortalValidity portalValidity = PortalValidity.valueOf(ConfigurationHelper.getString(ConfigurationOption.PORTAL_VALIDITY).toUpperCase());
        try {
            boolean isValid = portal.getGate().isValid();
            return switch (portalValidity){
                case IGNORE -> isValid;
                case REMOVE -> {
                    if(!isValid){
                        networkManager.destroyPortal(portal);
                    }
                    yield isValid;
                }
                case REPAIR -> {
                    if(!isValid){
                        portal.getGate().forceGenerateStructure();
                    }
                    yield true;
                }
            };
        } catch (GateConflictException e) {
            return false;
        }
    }

}
