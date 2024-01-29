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


}
