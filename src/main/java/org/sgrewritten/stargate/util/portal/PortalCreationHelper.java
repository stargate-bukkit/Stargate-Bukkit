package org.sgrewritten.stargate.util.portal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.event.StargateCreateEvent;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.name.BungeeNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.gate.GateFormat;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.BungeePortal;
import org.sgrewritten.stargate.network.portal.FixedPortal;
import org.sgrewritten.stargate.network.portal.NetworkedPortal;
import org.sgrewritten.stargate.network.portal.PortalData;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.RandomPortal;
import org.sgrewritten.stargate.network.portal.RealPortal;
import org.sgrewritten.stargate.property.BypassPermission;
import org.sgrewritten.stargate.util.EconomyHelper;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.NetworkCreationHelper;
import org.sgrewritten.stargate.util.SpawnDetectionHelper;
import org.sgrewritten.stargate.util.TranslatableMessageFormatter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
     * @param logger
     * @return <p>A new portal</p>
     * @throws InvalidNameException <p>If the portal's name is invalid</p>
     * @throws NameLengthException 
     * @throws BungeeNameException 
     */
    public static RealPortal createPortal(Network network, String name, String destination, String targetServer,
                                          Set<PortalFlag> flags, Gate gate, UUID ownerUUID,
                                          LanguageManager languageManager, RegistryAPI registry,StargateEconomyAPI economyAPI) throws InvalidNameException, NameLengthException, BungeeNameException {
        name = NameHelper.getTrimmedName(name);

        if (flags.contains(PortalFlag.BUNGEE)) {
            flags.add(PortalFlag.FIXED);
            Network bungeeNetwork = NetworkCreationHelper.selectNetwork(BungeePortal.getLegacyNetworkName(), NetworkType.CUSTOM, false, registry);
            return new BungeePortal(bungeeNetwork, name, destination, targetServer, flags, gate, ownerUUID,languageManager,economyAPI);
        } else if (flags.contains(PortalFlag.RANDOM)) {
            return new RandomPortal(network, name, flags, gate, ownerUUID,languageManager,economyAPI);
        } else if (flags.contains(PortalFlag.NETWORKED)) {
            return new NetworkedPortal(network, name, flags, gate, ownerUUID,languageManager,economyAPI);
        } else {
            flags.add(PortalFlag.FIXED);
            return new FixedPortal(network, name, destination, flags, gate, ownerUUID,languageManager,economyAPI);
        }
    }

    /**
     * Creates a new portal of the correct type
     *
     * @param network    <p>The network the portal belongs to</p>
     * @param portalData <p>Data of the portal </p>
     * @param gate       <p>The gate belonging to the portal</p>
     * @param logger
     * @return <p>A new portal</p>
     * @throws InvalidNameException <p>If the portal's name is invalid</p>
     * @throws BungeeNameException 
     * @throws NameLengthException 
     */
    public static RealPortal createPortal(Network network, PortalData portalData, Gate gate,LanguageManager languageManager, RegistryAPI registry,StargateEconomyAPI economyAPI)
            throws InvalidNameException, NameLengthException, BungeeNameException {
        return createPortal(network, portalData.name, portalData.destination, portalData.networkName, portalData.flags, gate, portalData.ownerUUID,languageManager,registry,economyAPI);
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
     * @param registry          <p>Where the new stargate will be registered</p>
     * @throws InvalidNameException     <p>If the name of the stargate does not follow set rules</p>
     * @throws GateConflictException  <p>If the gate's physical structure is in conflict with another</p>
     * @throws NoFormatFoundException <p>If no known format matches the built stargate</p>
     * @throws TranslatableException 
     */
    public static void tryPortalCreation(Network selectedNetwork, String[] lines, Block signLocation,
            Set<PortalFlag> flags, Player player, int cost, StargatePermissionManager permissionManager,
            TranslatableMessage errorMessage, RegistryAPI registry,LanguageManager languageManager,StargateEconomyAPI economyAPI)
            throws GateConflictException, NoFormatFoundException, TranslatableException, InvalidNameException {
        if (errorMessage != null) {
            player.sendMessage(languageManager.getErrorMessage(errorMessage));
            return;
        }

        UUID ownerUUID = getOwnerUUID(selectedNetwork, player, flags);
        Gate gate = createGate(signLocation, flags.contains(PortalFlag.ALWAYS_ON),registry);
        RealPortal portal = createPortalFromSign(selectedNetwork, lines, flags, gate, ownerUUID ,languageManager,registry,economyAPI);

        
        boolean hasPermission = permissionManager.hasCreatePermissions(portal);
        StargateCreateEvent stargateCreateEvent = new StargateCreateEvent(player, portal, lines, !hasPermission,
                permissionManager.getDenyMessage(), cost);
        Bukkit.getPluginManager().callEvent(stargateCreateEvent);
        Stargate.log(Level.CONFIG, " player has permission = " + hasPermission);

        //If the create event has been denied, tell the user and abort
        if (stargateCreateEvent.getDeny()) {
            Stargate.log(Level.CONFIG, " Event was denied due to lack of permission or an add-on");
            if (stargateCreateEvent.getDenyReason() == null) {
                player.sendMessage(languageManager.getErrorMessage(TranslatableMessage.ADDON_INTERFERE));
            } else if (!stargateCreateEvent.getDenyReason().isEmpty()) {
                player.sendMessage(stargateCreateEvent.getDenyReason());
            }
            return;
        }

        if (selectedNetwork.isPortalNameTaken(portal.getName())) {
            throw new NameConflictException(String.format("portal %s in network %s already exists", portal.getName(), selectedNetwork.getId()));
        }

        //Display an error if trying to create portals across servers while the feature is disabled
        if ((flags.contains(PortalFlag.BUNGEE) || flags.contains(PortalFlag.FANCY_INTER_SERVER))
                && !ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            player.sendMessage(languageManager.getErrorMessage(TranslatableMessage.BUNGEE_DISABLED));
            return;
        }
        if (flags.contains(PortalFlag.FANCY_INTER_SERVER) && !ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE)) {
            player.sendMessage(languageManager.getErrorMessage(TranslatableMessage.INTER_SERVER_DISABLED));
            return;
        }

        //Charge the player as necessary for the portal creation
        if (EconomyHelper.shouldChargePlayer(player, portal, BypassPermission.COST_CREATE) &&
                !economyAPI.chargePlayer(player, null, stargateCreateEvent.getCost())) {
            player.sendMessage(languageManager.getErrorMessage(TranslatableMessage.LACKING_FUNDS));
            return;
        }

        //Warn the player if their portal is interfering with spawn protection
        if (SpawnDetectionHelper.isInterferingWithSpawnProtection(gate, signLocation.getLocation())) {
            player.sendMessage(languageManager.getWarningMessage(TranslatableMessage.SPAWN_CHUNKS_CONFLICTING));
        }
        
        if(flags.contains(PortalFlag.FANCY_INTER_SERVER)) {
            Network inflictingNetwork = NetworkCreationHelper.getInterserverLocalConflict(selectedNetwork, registry);
            player.sendMessage(TranslatableMessageFormatter.formatUnimplementedConflictMessage(selectedNetwork, inflictingNetwork, languageManager));
        }

        //Save the portal and inform the user
        selectedNetwork.addPortal(portal, true);
        //Make sure that the portal sign text formats according the default sign dye color
        Sign sign = (Sign) signLocation.getState();
        sign.setColor(Stargate.getDefaultSignDyeColor(signLocation.getType()));
        sign.update();
        selectedNetwork.updatePortals();
        Stargate.log(Level.FINE, "Successfully created a new portal");
        String msg;
        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            msg = languageManager.getMessage(TranslatableMessage.CREATE_PERSONAL);
        } else {
            String unformattedMessage = languageManager.getMessage(TranslatableMessage.CREATE);
            msg = TranslatableMessageFormatter.formatNetwork(unformattedMessage, selectedNetwork.getName());
        }
        if(flags.contains(PortalFlag.FANCY_INTER_SERVER)) {
            msg = msg + languageManager.getMessage(TranslatableMessage.UNIMPLEMENTED_INTERSERVER);
        }
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
     * @throws InvalidNameException <p>If the portal's name is invalid</p>
     * @throws BungeeNameException 
     * @throws NameLengthException 
     */
    private static RealPortal createPortalFromSign(Network network, String[] lines, Set<PortalFlag> flags, Gate gate,
                                                   UUID ownerUUID, LanguageManager languageManager,RegistryAPI registry,StargateEconomyAPI economyAPI) throws InvalidNameException, NameLengthException, BungeeNameException {
        return createPortal(network, lines[0], lines[1], lines[2], flags, gate, ownerUUID,languageManager,registry,economyAPI);
    }

    /**
     * Creates a new gate from the given sign
     *
     * @param sign     <p>The sign containing necessary sign data</p>
     * @param alwaysOn <p>Whether the new gate should be always on</p>
     * @param logger   <p>The logger to use for logging</p>
     * @return <p>A new Gate</p>
     * @throws NoFormatFoundException <p>If no gate format is found that matches the physical gate</p>
     * @throws GateConflictException  <p>If a registered gate conflicts with the new gate</p>
     */
    public static Gate createGate(Block sign, boolean alwaysOn,RegistryAPI registry) throws NoFormatFoundException, GateConflictException {
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
                return new Gate(gateFormat, signLocation, signFacing, alwaysOn,registry);
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

}
