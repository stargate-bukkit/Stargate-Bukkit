package net.TheDgtl.Stargate.util.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.event.StargateCreateEvent;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.InvalidStructureException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.gate.GateFormatHandler;
import net.TheDgtl.Stargate.manager.PermissionManager;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.BungeePortal;
import net.TheDgtl.Stargate.network.portal.FixedPortal;
import net.TheDgtl.Stargate.network.portal.NetworkedPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RandomPortal;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.property.BypassPermission;
import net.TheDgtl.Stargate.util.EconomyHelper;
import net.TheDgtl.Stargate.util.NameHelper;
import net.TheDgtl.Stargate.util.SpawnDetectionHelper;
import net.TheDgtl.Stargate.util.TranslatableMessageFormatter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class PortalCreationHelper {

    /**
     * Creates a new portal of the correct type from the given sing lines
     *
     * @param network   <p>The network the portal belongs to</p>
     * @param lines     <p>The lines written on a stargate sign</p>
     * @param flags     <p>The flags enabled for the portal</p>
     * @param gate      <p>The gate belonging to the portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @return <p>A new portal</p>
     * @throws NameErrorException <p>If the portal's name is invalid</p>
     */
    public static RealPortal createPortalFromSign(Network network, String[] lines, Set<PortalFlag> flags, Gate gate,
                                                  UUID ownerUUID, StargateLogger logger) throws NameErrorException {
        return createPortal(network, lines[0], lines[1], lines[2], flags, gate, ownerUUID, logger);
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
     * @throws NameErrorException <p>If the portal's name is invalid</p>
     */
    public static RealPortal createPortal(Network network, String name, String destination, String targetServer,
                                          Set<PortalFlag> flags, Gate gate, UUID ownerUUID, StargateLogger logger) throws NameErrorException {
        name = NameHelper.getTrimmedName(name);

        if (flags.contains(PortalFlag.BUNGEE)) {
            return new BungeePortal(network, name, destination, targetServer, flags, gate, ownerUUID, logger);
        } else if (flags.contains(PortalFlag.RANDOM)) {
            return new RandomPortal(network, name, flags, gate, ownerUUID, logger);
        } else if (flags.contains(PortalFlag.NETWORKED)) {
            return new NetworkedPortal(network, name, flags, gate, ownerUUID, logger);
        } else {
            return new FixedPortal(network, name, destination, flags, gate, ownerUUID, logger);
        }
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
    private static Gate findMatchingGate(List<GateFormat> gateFormats, Location signLocation, BlockFace signFacing, boolean alwaysOn, StargateLogger logger)
            throws NoFormatFoundException, GateConflictException {
        Stargate.log(Level.FINE, "Amount of GateFormats: " + gateFormats.size());
        for (GateFormat gateFormat : gateFormats) {
            logger.logMessage(Level.FINE, "--------- " + gateFormat.getFileName() + " ---------");
            try {
                return new Gate(gateFormat, signLocation, signFacing, alwaysOn, logger);
            } catch (InvalidStructureException ignored) {
            }
        }
        throw new NoFormatFoundException();
    }

    public static Gate createGate(Block sign, boolean alwaysOn, StargateLogger logger) throws NoFormatFoundException, GateConflictException {
        if (!(Tag.WALL_SIGNS.isTagged(sign.getType()))) {
            throw new NoFormatFoundException();
        }
        //Get the block behind the sign; the material of that block is stored in a register with available gateFormats
        Directional signDirection = (Directional) sign.getBlockData();
        Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
        List<GateFormat> gateFormats = GateFormatHandler.getPossibleGateFormatsFromControlBlockMaterial(behind.getType());
        return findMatchingGate(gateFormats, sign.getLocation(), signDirection.getFacing(), alwaysOn, logger);
    }

    

    
    /**
     * Determine the uuid of the owner of this portal
     * @param network 
     * @param player
     * @param flags
     * @return
     */
    public static UUID getOwnerUUID(Network network, Player player, Set<PortalFlag> flags) {
        if(network != null) {
            return flags.contains(PortalFlag.PERSONAL_NETWORK) ? UUID.fromString(network.getName()) : player.getUniqueId();
        }
        return player.getUniqueId();
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
     * @throws NameErrorException     <p>If the name of the stargate does not follow set rules</p>
     * @throws GateConflictException  <p>If the gate's physical structure is in conflict with another</p>
     * @throws NoFormatFoundException <p>If no known format matches the built stargate</p>
     */
    public static void tryPortalCreation(Network selectedNetwork, String[] lines, Block signLocation, Set<PortalFlag> flags,
                                   Player player, int cost, PermissionManager permissionManager, TranslatableMessage errorMessage)
            throws NameErrorException, GateConflictException, NoFormatFoundException {

        UUID ownerUUID = PortalCreationHelper.getOwnerUUID(selectedNetwork,player,flags);
        Gate gate = PortalCreationHelper.createGate(signLocation, flags.contains(PortalFlag.ALWAYS_ON), Stargate.getInstance());
        RealPortal portal = PortalCreationHelper.createPortalFromSign(selectedNetwork, lines, flags, gate, ownerUUID, Stargate.getInstance());
        StargateCreateEvent stargateCreateEvent = new StargateCreateEvent(player, portal, lines, cost);

        Bukkit.getPluginManager().callEvent(stargateCreateEvent);

        boolean hasPermission = permissionManager.hasCreatePermissions(portal);
        Stargate.log(Level.CONFIG, " player has perm = " + hasPermission);

        if (errorMessage != null) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(errorMessage));
            return;
        }

        if (!hasPermission) {
            Stargate.log(Level.CONFIG, " Event was cancelled due to lack of permission");
            player.sendMessage(permissionManager.getDenyMessage());
            return;
        }
        if (stargateCreateEvent.isCancelled()) {
            Stargate.log(Level.CONFIG, " Event was cancelled due an external cancellation");
            player.sendMessage(stargateCreateEvent.getDenyReason());
            return;
        }

        if (EconomyHelper.shouldChargePlayer(player, portal, BypassPermission.COST_CREATE) &&
                !Stargate.economyManager.chargeAndTax(player, stargateCreateEvent.getCost())) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.LACKING_FUNDS));
            return;
        }

        if ((flags.contains(PortalFlag.BUNGEE) || flags.contains(PortalFlag.FANCY_INTER_SERVER))
                && !ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.BUNGEE_DISABLED));
            return;
        }
        if (flags.contains(PortalFlag.FANCY_INTER_SERVER) && !ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE)) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.INTER_SERVER_DISABLED));
            return;
        }

        if (SpawnDetectionHelper.isInterferingWithSpawnProtection(gate, signLocation.getLocation())) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.SPAWN_CHUNKS_CONFLICTING));
        }

        selectedNetwork.addPortal(portal, true);
        selectedNetwork.updatePortals();
        Stargate.log(Level.FINE, "A Gate format matches");
        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getMessage(TranslatableMessage.CREATE_PERSONAL));
        } else {
            String unformattedMessage = Stargate.getLanguageManagerStatic().getMessage(TranslatableMessage.CREATE);
            player.sendMessage(TranslatableMessageFormatter.formatNetwork(unformattedMessage, selectedNetwork.getName()));
        }
    }
}
