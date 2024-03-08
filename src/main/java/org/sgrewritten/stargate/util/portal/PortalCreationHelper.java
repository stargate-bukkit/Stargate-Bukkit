package org.sgrewritten.stargate.util.portal;

import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.StargatePortal;
import org.sgrewritten.stargate.network.portal.behavior.FixedBehavior;
import org.sgrewritten.stargate.network.portal.behavior.LegacyBungeeBehavior;
import org.sgrewritten.stargate.network.portal.behavior.NetworkedBehavior;
import org.sgrewritten.stargate.network.portal.behavior.PortalBehavior;
import org.sgrewritten.stargate.network.portal.behavior.RandomBehavior;
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
        PortalBehavior portalBehavior;
        if (flags.contains(PortalFlag.LEGACY_INTERSERVER)) {
            flags.add(PortalFlag.FIXED);
            // Override whatever network that was going to be used, as it should only be on the bungee network
            network = stargateAPI.getNetworkManager().selectNetwork(ConfigurationHelper.getString(ConfigurationOption.LEGACY_BUNGEE_NETWORK), NetworkType.CUSTOM, StorageType.LOCAL);
            portalBehavior = new LegacyBungeeBehavior(stargateAPI.getLanguageManager(), destination, targetServer);
        } else if (flags.contains(PortalFlag.RANDOM)) {
            portalBehavior = new RandomBehavior(stargateAPI.getLanguageManager());
        } else if (flags.contains(PortalFlag.NETWORKED)) {
            portalBehavior = new NetworkedBehavior(stargateAPI.getLanguageManager());
        } else {
            flags.add(PortalFlag.FIXED);
            portalBehavior = new FixedBehavior(stargateAPI.getLanguageManager(),destination);
        }
        RealPortal portal = new StargatePortal(network, name, flags, unrecognisedFlags, gate, ownerUUID, stargateAPI.getLanguageManager(), stargateAPI.getEconomyManager(),metaData);
        portal.setBehavior(portalBehavior);
        return portal;
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
