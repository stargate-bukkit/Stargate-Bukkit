package org.sgrewritten.stargate.util.portal;

import org.bukkit.entity.Player;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.BungeePortal;
import org.sgrewritten.stargate.network.portal.FixedPortal;
import org.sgrewritten.stargate.network.portal.NetworkedPortal;
import org.sgrewritten.stargate.network.portal.RandomPortal;
import org.sgrewritten.stargate.network.portal.portaldata.PortalData;
import org.sgrewritten.stargate.util.NameHelper;

import java.util.Set;
import java.util.UUID;

/**
 * A helper class for creating new portals
 */
public final class PortalCreationHelper {

    private PortalCreationHelper() {

    }

    /**
     * Creates a new portal of the correct type
     *
     * @param network      <p>The network the portal belongs to</p>
     * @param name         <p>The name of the portal</p>
     * @param destination  <p>The destination of the portal</p>
     * @param targetServer <p>The portal's target server (if bungee)</p>
     * @param flags        <p>The flags enabled for the portal</p>
     * @param gate         <p>The gate belonging to the portal</p>
     * @param ownerUUID    <p>The UUID of the portal's owner</p>
     * @return <p>A new portal</p>
     * @throws TranslatableException <p>If the portal's name is invalid</p>
     */
    public static RealPortal createPortal(Network network, String name, String destination, String targetServer,
                                          Set<PortalFlag> flags, Set<Character> unrecognisedFlags, GateAPI gate, UUID ownerUUID,
                                          StargateAPI stargateAPI, String metaData) throws TranslatableException {
        name = NameHelper.getTrimmedName(name);

        if (flags.contains(PortalFlag.BUNGEE)) {
            flags.add(PortalFlag.FIXED);
            Network bungeeNetwork = stargateAPI.getNetworkManager().selectNetwork(BungeePortal.getLegacyNetworkName(), NetworkType.CUSTOM, StorageType.LOCAL);
            return new BungeePortal(bungeeNetwork, name, destination, targetServer, flags, unrecognisedFlags, gate, ownerUUID, stargateAPI.getLanguageManager(), stargateAPI.getEconomyManager(), metaData);
        } else if (flags.contains(PortalFlag.RANDOM)) {
            return new RandomPortal(network, name, flags, unrecognisedFlags, gate, ownerUUID, stargateAPI.getLanguageManager(), stargateAPI.getEconomyManager(), metaData);
        } else if (flags.contains(PortalFlag.NETWORKED)) {
            return new NetworkedPortal(network, name, flags, unrecognisedFlags, gate, ownerUUID, stargateAPI.getLanguageManager(), stargateAPI.getEconomyManager(), metaData);
        } else {
            flags.add(PortalFlag.FIXED);
            return new FixedPortal(network, name, destination, flags, unrecognisedFlags, gate, ownerUUID, stargateAPI.getLanguageManager(), stargateAPI.getEconomyManager(), metaData);
        }
    }

    /**
     * Creates a new portal of the correct type
     *
     * @param network    <p>The network the portal belongs to</p>
     * @param portalData <p>Data of the portal </p>
     * @param gate       <p>The gate belonging to the portal</p>
     * @return <p>A new portal</p>
     * @throws TranslatableException <p>If the portal's name is invalid</p>
     */
    public static RealPortal createPortal(Network network, PortalData portalData, Gate gate,
                                          StargateAPI stargateAPI) throws TranslatableException {
        return createPortal(network, portalData.name(), portalData.destination(), portalData.networkName(), portalData.flags(), portalData.unrecognisedFlags(), gate, portalData.ownerUUID(), stargateAPI, portalData.metaData());
    }

    /**
     * Creates a new portal of the correct type from the given sing lines
     *
     * @param network   <p>The network the portal belongs to</p>
     * @param lines     <p>The lines written on a stargate sign</p>
     * @param flags     <p>The flags enabled for the portal</p>
     * @param gate      <p>The gate belonging to the portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @return <p>A new portal</p>
     * @throws TranslatableException <p>If the portal's name is invalid</p>
     */
    private static RealPortal createPortalFromSign(Network network, String[] lines, Set<PortalFlag> flags, Set<Character> unrecognisedFlags, Gate gate,
                                                   UUID ownerUUID, StargateAPI stargateAPI) throws TranslatableException {
        return createPortal(network, lines[0], lines[1], lines[2], flags, unrecognisedFlags, gate, ownerUUID, stargateAPI, null);
    }


    /**
     * Gets the owner of the new portal
     *
     * <p>For a personal network, the owner is defined as the network name, but normally the owner is defined as the
     * portal creator.</p>
     *
     * @param network <p>The network of the new portal</p>
     * @param player  <p>The player creating the new portal</p>
     * @param flags   <p>The flags specified for the new portal</p>
     * @return <p>The UUID of the new portal's owner</p>
     */
    private static UUID getOwnerUUID(Network network, Player player, Set<PortalFlag> flags) {
        if (network != null && flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            return UUID.fromString(network.getId());
        } else {
            return player.getUniqueId();
        }
    }


}
