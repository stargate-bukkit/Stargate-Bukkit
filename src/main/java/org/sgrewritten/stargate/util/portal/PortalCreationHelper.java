package org.sgrewritten.stargate.util.portal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.event.portal.StargateSendMessagePortalEvent;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.portal.StargateCreatePortalEvent;
import org.sgrewritten.stargate.exception.*;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.portal.*;
import org.sgrewritten.stargate.network.portal.portaldata.PortalData;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.permission.BypassPermission;
import org.sgrewritten.stargate.util.*;
import org.sgrewritten.stargate.util.VectorUtils;

import java.util.*;
import java.util.logging.Level;

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
                                          StargateAPI stargateAPI) throws TranslatableException {
        name = NameHelper.getTrimmedName(name);

        if (flags.contains(PortalFlag.BUNGEE)) {
            flags.add(PortalFlag.FIXED);
            Network bungeeNetwork = NetworkCreationHelper.selectNetwork(BungeePortal.getLegacyNetworkName(), NetworkType.CUSTOM, false, stargateAPI.getRegistry());
            return new BungeePortal(bungeeNetwork, name, destination, targetServer, flags,unrecognisedFlags, gate, ownerUUID, stargateAPI.getLanguageManager(), stargateAPI.getEconomyManager());
        } else if (flags.contains(PortalFlag.RANDOM)) {
            return new RandomPortal(network, name, flags, unrecognisedFlags, gate, ownerUUID, stargateAPI.getLanguageManager(), stargateAPI.getEconomyManager());
        } else if (flags.contains(PortalFlag.NETWORKED)) {
            return new NetworkedPortal(network, name, flags, unrecognisedFlags, gate, ownerUUID, stargateAPI.getLanguageManager(), stargateAPI.getEconomyManager());
        } else {
            flags.add(PortalFlag.FIXED);
            return new FixedPortal(network, name, destination, flags, unrecognisedFlags, gate, ownerUUID, stargateAPI.getLanguageManager(), stargateAPI.getEconomyManager());
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
        return createPortal(network, portalData.name(), portalData.destination(), portalData.networkName(), portalData.flags(), portalData.unrecognisedFlags(), gate, portalData.ownerUUID(), stargateAPI);
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
        return createPortal(network, lines[0], lines[1], lines[2], flags, unrecognisedFlags, gate, ownerUUID, stargateAPI);
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
