package org.sgrewritten.stargate.network.portal.portaldata;

import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;

import java.util.Set;
import java.util.UUID;

/**
 * The data contained within a Portal.
 *
 * @param name              The name of the portal.
 * @param networkName       The name of the network that the portal is associated with.
 * @param destination       The name of the portal stored as this portal's destination.
 * @param flagString        A string containing all the flags associated with this portal.
 * @param flags             A set containing all the flags associated with this portal.
 * @param unrecognisedFlags A set containing all unrecognised flags associated with this portal
 * @param ownerUUID         The UUID of the player who owns this portal.
 * @param serverUUID        The UUID of the server this portal was constructed on.
 * @param serverName        The name of the server this portal was constructed on.
 * @param portalType        The type associated with this portal.
 */
public record PortalData(GateData gateData, String name, String networkName, String destination,
                         Set<PortalFlag> flags, Set<Character> unrecognisedFlags, UUID ownerUUID,
                         String serverUUID, String serverName, StorageType portalType) {

    public String flagString() {
        StringBuilder builder = new StringBuilder();
        flags().forEach((flag) -> builder.append(flag.getCharacterRepresentation()));
        unrecognisedFlags().forEach(builder::append);
        return builder.toString();
    }
}
