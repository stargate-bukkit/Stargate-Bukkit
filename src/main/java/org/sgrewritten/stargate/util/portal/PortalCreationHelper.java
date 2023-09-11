package org.sgrewritten.stargate.util.portal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.portal.StargateCreatePortalEvent;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.gate.GateFormat;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.*;
import org.sgrewritten.stargate.network.portal.portaldata.PortalData;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.property.BypassPermission;
import org.sgrewritten.stargate.util.EconomyHelper;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.NetworkCreationHelper;
import org.sgrewritten.stargate.util.SpawnDetectionHelper;
import org.sgrewritten.stargate.util.TranslatableMessageFormatter;
import org.sgrewritten.stargate.vectorlogic.VectorUtils;

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
                                          Set<PortalFlag> flags, Set<Character> unrecognisedFlags, Gate gate, UUID ownerUUID,
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
     * Tries to create a new stargate
     *
     * @param selectedNetwork   <p>The network selected on the stargate's sign</p>
     * @param lines             <p>The lines on the stargate's sign</p>
     * @param signLocation      <p>The location of the changed sign</p>
     * @param flags             <p>The flags selected by the player</p>
     * @param player            <p>The player that changed the sign</p>
     * @param cost              <p>The cost of creating the new stargate</p>
     * @param permissionManager <p>The permission manager to use for checking the player's permissions</p>
     * @param errorMessage      <p>The error message to display to the player</p>
     * @param stargateAPI       <p>Stargate API</p>
     * @throws GateConflictException  <p>If the gate's physical structure is in conflict with another</p>
     * @throws NoFormatFoundException <p>If no known format matches the built stargate</p>
     * @throws TranslatableException  <p>If the name of the stargate does not follow set rules</p>
     */
    public static void tryPortalCreation(Network selectedNetwork, String[] lines, Block signLocation,
                                         Set<PortalFlag> flags, Set<Character> unrecognisedFlags, Player player, int cost,
                                         StargatePermissionManager permissionManager, String errorMessage,
                                         StargateAPI stargateAPI)
            throws GateConflictException, NoFormatFoundException, TranslatableException {


        Gate gate = createGate(signLocation, flags.contains(PortalFlag.ALWAYS_ON), stargateAPI.getRegistry());
        if (errorMessage != null) {
            player.sendMessage(errorMessage);
            return;
        }
        UUID ownerUUID = getOwnerUUID(selectedNetwork, player, flags);
        RealPortal portal = createPortalFromSign(selectedNetwork, lines, flags, unrecognisedFlags, gate, ownerUUID, stargateAPI);


        boolean hasPermission = permissionManager.hasCreatePermissions(portal);
        StargateCreatePortalEvent portalCreateEvent = new StargateCreatePortalEvent(player, portal, lines, !hasPermission,
                permissionManager.getDenyMessage(), cost);
        Bukkit.getPluginManager().callEvent(portalCreateEvent);
        Stargate.log(Level.CONFIG, " player has permission = " + hasPermission);

        //If the create event has been denied, tell the user and abort
        if (portalCreateEvent.getDeny()) {
            Stargate.log(Level.CONFIG, " Event was denied due to lack of permission or an add-on");
            if (portalCreateEvent.getDenyReason() == null) {
                player.sendMessage(stargateAPI.getLanguageManager().getErrorMessage(TranslatableMessage.ADDON_INTERFERE));
            } else if (!portalCreateEvent.getDenyReason().isEmpty()) {
                player.sendMessage(portalCreateEvent.getDenyReason());
            }
            return;
        }

        if (selectedNetwork.isPortalNameTaken(portal.getName())) {
            throw new NameConflictException(String.format("portal %s in network %s already exists", portal.getName(),
                    selectedNetwork.getId()), true);
        }

        //Display an error if trying to create portals across servers while the feature is disabled
        if ((flags.contains(PortalFlag.BUNGEE) || flags.contains(PortalFlag.FANCY_INTER_SERVER))
                && !ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            player.sendMessage(stargateAPI.getLanguageManager().getErrorMessage(TranslatableMessage.BUNGEE_DISABLED));
            return;
        }
        if (flags.contains(PortalFlag.FANCY_INTER_SERVER) && !ConfigurationHelper.getBoolean(
                ConfigurationOption.USING_REMOTE_DATABASE)) {
            player.sendMessage(stargateAPI.getLanguageManager().getErrorMessage(TranslatableMessage.INTER_SERVER_DISABLED));
            return;
        }

        //Charge the player as necessary for the portal creation
        if (EconomyHelper.shouldChargePlayer(player, portal, BypassPermission.COST_CREATE) &&
                !stargateAPI.getEconomyManager().chargePlayer(player, null, portalCreateEvent.getCost())) {
            player.sendMessage(stargateAPI.getLanguageManager().getErrorMessage(TranslatableMessage.LACKING_FUNDS));
            return;
        }

        //Warn the player if their portal is interfering with spawn protection
        if (SpawnDetectionHelper.isInterferingWithSpawnProtection(gate, signLocation.getLocation())) {
            player.sendMessage(stargateAPI.getLanguageManager().getWarningMessage(TranslatableMessage.SPAWN_CHUNKS_CONFLICTING));
        }

        if (flags.contains(PortalFlag.FANCY_INTER_SERVER)) {
            Network inflictingNetwork = NetworkCreationHelper.getInterserverLocalConflict(selectedNetwork, stargateAPI.getRegistry());
            player.sendMessage(TranslatableMessageFormatter.formatUnimplementedConflictMessage(selectedNetwork,
                    inflictingNetwork, stargateAPI.getLanguageManager()));
        }
        //Save the portal and inform the user
        selectedNetwork.addPortal(portal, true);
        //Make sure that the portal sign text formats according the default sign dye color
        getLocationsAdjacentToPortal(gate).forEach((position) -> stargateAPI.getMaterialHandlerResolver().registerPlacement(stargateAPI.getRegistry(),position,List.of(portal),position.getBlock().getType(),player));
        if(Tag.WALL_SIGNS.isTagged(signLocation.getType())) {
            Sign sign = (Sign) signLocation.getState();
            sign.setColor(Stargate.getDefaultSignDyeColor(signLocation.getType()));
            sign.update();
        }
        selectedNetwork.updatePortals();
        Stargate.log(Level.FINE, "Successfully created a new portal");
        String msg;
        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            msg = stargateAPI.getLanguageManager().getMessage(TranslatableMessage.CREATE_PERSONAL);
        } else {
            String unformattedMessage = stargateAPI.getLanguageManager().getMessage(TranslatableMessage.CREATE);
            msg = TranslatableMessageFormatter.formatNetwork(unformattedMessage, selectedNetwork.getName());
        }
        if (flags.contains(PortalFlag.FANCY_INTER_SERVER)) {
            msg = msg + " " + stargateAPI.getLanguageManager().getString(TranslatableMessage.UNIMPLEMENTED_INTER_SERVER);
        }
        player.sendMessage(msg);
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
     * Creates a new gate from the given sign
     *
     * @param sign     <p>The sign containing necessary sign data</p>
     * @param alwaysOn <p>Whether the new gate should be always on</p>
     * @return <p>A new Gate</p>
     * @throws NoFormatFoundException <p>If no gate format is found that matches the physical gate</p>
     * @throws GateConflictException  <p>If a registered gate conflicts with the new gate</p>
     */
    public static Gate createGate(Block sign, boolean alwaysOn, RegistryAPI registry) throws NoFormatFoundException, GateConflictException {
        if (!(Tag.WALL_SIGNS.isTagged(sign.getType()))) {
            throw new NoFormatFoundException();
        }
        //Get the block behind the sign; the material of that block is stored in a register with available gateFormats
        Directional signDirection = (Directional) sign.getBlockData();
        Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
        List<GateFormat> gateFormats = GateFormatHandler.getPossibleGateFormatsFromControlBlockMaterial(behind.getType());
        return findMatchingGate(gateFormats, sign.getLocation(), signDirection.getFacing(), alwaysOn, registry);
    }

    /**
     * Tries to find a gate at the given location matching one of the given gate formats
     *
     * @param gateFormats  <p>The gate formats to look for</p>
     * @param signLocation <p>The location of the sign of the portal to look for</p>
     * @param signFacing   <p>The direction the sign is facing</p>
     * @param alwaysOn     <p>Whether the portal is always on</p>
     * @return <p>A gate if found, otherwise throws an {@link NoFormatFoundException}</p>
     * @throws NoFormatFoundException <p>If no gate was found at the given location matching any of the given formats</p>
     * @throws GateConflictException  <p>If the found gate conflicts with another gate</p>
     */
    private static Gate findMatchingGate(List<GateFormat> gateFormats, Location signLocation, BlockFace signFacing,
                                         boolean alwaysOn, RegistryAPI registry)
            throws NoFormatFoundException, GateConflictException {
        Stargate.log(Level.FINE, "Amount of GateFormats: " + gateFormats.size());
        for (GateFormat gateFormat : gateFormats) {
            Stargate.log(Level.FINE, "--------- " + gateFormat.getFileName() + " ---------");
            try {
                return new Gate(gateFormat, signLocation, signFacing, alwaysOn, registry);
            } catch (InvalidStructureException ignored) {
            }
        }
        throw new NoFormatFoundException();
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

    private static List<Location> getLocationsAdjacentToPortal(GateAPI gate){
        Set<BlockLocation> adjacentLocations = new HashSet<>();
        for(BlockLocation blockLocation : gate.getLocations(GateStructureType.FRAME)){
            for(BlockVector adjacentVector : VectorUtils.getAdjacentRelativePositions()){
                BlockLocation adjacentLocation = new BlockLocation(blockLocation.getLocation().add(adjacentVector));
                adjacentLocations.add(adjacentLocation);
            }
        }
        List<Location> output = new ArrayList<>();
        for(BlockLocation blockLocation : adjacentLocations){
            output.add(blockLocation.getLocation());
        }
        return output;
    }
}
