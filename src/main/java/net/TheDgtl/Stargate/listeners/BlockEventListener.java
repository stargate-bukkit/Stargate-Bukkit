package net.TheDgtl.Stargate.listeners;

import net.TheDgtl.Stargate.BypassPermission;
import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.actions.SupplierAction;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.event.StargateCreateEvent;
import net.TheDgtl.Stargate.event.StargateDestroyEvent;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.util.PortalCreationHelper;
import net.TheDgtl.Stargate.util.TranslatableMessageFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * A listener for detecting any relevant block events
 */
public class BlockEventListener implements Listener {

    /**
     * Detects relevant block break events
     *
     * <p>Protects a portal's control blocks and iris from destruction and destroys the attached portal if the entity
     * is allowed.</p>
     *
     * @param event <p>The triggered block break event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        Portal portal = Stargate.factory.getPortal(location, GateStructureType.FRAME);
        if (portal != null) {
            Supplier<Boolean> destroyAction = () -> {
                String msg = Stargate.languageManager.getErrorMessage(TranslatableMessage.DESTROY);
                event.getPlayer().sendMessage(msg);

                portal.destroy();
                Stargate.log(Level.FINE, "Broke portal " + portal.getName());
                return true;
            };

            destroyPortalIfHasPermissionAndCanPay(event, portal, destroyAction);
            return;
        }
        if (Stargate.factory.getPortal(location, GateStructureType.CONTROL_BLOCK) != null) {
            event.setCancelled(true);
            return;
        }
        if (Stargate.factory.getPortal(location, GateStructureType.IRIS) != null && Settings.getBoolean(Setting.PROTECT_ENTRANCE)) {
            event.setCancelled(true);
        }
    }

    /**
     * Destroys a portal if the entity has permission and can pay any fees
     *
     * @param event         <p>The block break event triggering the destruction</p>
     * @param portal        <p>The portal to destroy</p>
     * @param destroyAction <p>The action to run when destroying a portal</p>
     */
    private void destroyPortalIfHasPermissionAndCanPay(BlockBreakEvent event, Portal portal,
                                                       Supplier<Boolean> destroyAction) {
        int cost = Settings.getInteger(Setting.DESTROY_COST);
        StargateDestroyEvent stargateDestroyEvent = new StargateDestroyEvent(portal, event.getPlayer(), cost);
        Bukkit.getPluginManager().callEvent(stargateDestroyEvent);
        PermissionManager permissionManager = new PermissionManager(event.getPlayer());
        if (permissionManager.hasPermission(stargateDestroyEvent) && !stargateDestroyEvent.isCancelled()) {
            /*
             * If setting charge free destination is false, destination portal is PortalFlag.Free and portal is of Fixed type
             * or if player has override cost permission, do not collect money
             */
            if (shouldChargePlayer(event.getPlayer(), portal, BypassPermission.COST_DESTROY) &&
                    !Stargate.economyManager.chargeAndTax(event.getPlayer(),
                            stargateDestroyEvent.getCost())) {
                event.getPlayer().sendMessage(
                        Stargate.languageManager.getErrorMessage(TranslatableMessage.LACKING_FUNDS));
                event.setCancelled(true);
                return;
            }
            Stargate.syncTickPopulator.addAction(new SupplierAction(destroyAction));
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * Checks whether the given player should be charged for destroying a portal
     *
     * @param player           <p>The player to check</p>
     * @param portal           <p>The portal the player is trying to do something with</p>
     * @param bypassPermission <p>The bypass permission that would let the player avoid payment</p>
     * @return <p>True if the player should be charged</p>
     */
    private boolean shouldChargePlayer(Player player, Portal portal, BypassPermission bypassPermission) {
        if (player.hasPermission(bypassPermission.getPermissionString())) {
            return false;
        }

        return Settings.getBoolean(Setting.CHARGE_FREE_DESTINATION) ||
                !portal.hasFlag(PortalFlag.FIXED) ||
                !portal.loadDestination().hasFlag(PortalFlag.FREE);
    }

    /**
     * Checks for and blocks any block placement in a stargate's iris
     *
     * @param event <p>The triggered block place event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();
        Portal portal = Stargate.factory.getPortal(loc, GateStructureType.IRIS);
        if (portal != null && Settings.getBoolean(Setting.PROTECT_ENTRANCE)) {
            event.setCancelled(true);
        }
    }

    /**
     * Checks for any sign change events that may result in the creation of a stargate
     *
     * @param event <p>The triggered sign change event</p>
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Block block = event.getBlock();
        if (!(block.getBlockData() instanceof WallSign)) {
            return;
        }

        String[] lines = event.getLines();
        String network = lines[2];
        int cost = Settings.getInteger(Setting.CREATION_COST);
        Player player = event.getPlayer();
        Set<PortalFlag> flags = PortalFlag.parseFlags(lines[3]);
        PermissionManager permissionManager = new PermissionManager(player);
        TranslatableMessage errorMessage = null;

        if (lines[1].trim().isEmpty()) {
            flags.add(PortalFlag.NETWORKED);
        }

        Set<PortalFlag> disallowedFlags = permissionManager.returnDissallowedFlags(flags);

        if (disallowedFlags.size() > 0) {
            String unformattedMessage = Stargate.languageManager.getErrorMessage(TranslatableMessage.LACKING_FLAGS_PERMISSION);
            player.sendMessage(TranslatableMessageFormatter.compileFlags(unformattedMessage, disallowedFlags));
        }
        flags.removeAll(disallowedFlags);

        String finalNetworkName;
        Network selectedNetwork = null;
        try {
            finalNetworkName = interpretNetworkName(network, flags, player, permissionManager);
            selectedNetwork = selectNetwork(finalNetworkName, flags);
        } catch (NameErrorException nameErrorException) {
            errorMessage = nameErrorException.getErrorMessage();
        }
        


        try {
            tryPortalCreation(selectedNetwork, lines, block, flags, event.getPlayer(), cost, permissionManager, errorMessage);
        } catch (NoFormatFoundException noFormatFoundException) {
            Stargate.log(Level.FINER, "No Gate format matches");
        } catch (GateConflictException gateConflictException) {
            player.sendMessage(Stargate.languageManager.getErrorMessage(TranslatableMessage.GATE_CONFLICT));
        } catch (NameErrorException nameErrorException) {
            player.sendMessage(Stargate.languageManager.getErrorMessage(nameErrorException.getErrorMessage()));
        }
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
    private void tryPortalCreation(Network selectedNetwork, String[] lines, Block signLocation, Set<PortalFlag> flags,
                                   Player player, int cost, PermissionManager permissionManager, TranslatableMessage errorMessage)
            throws NameErrorException, GateConflictException, NoFormatFoundException {

        UUID ownerUUID = flags.contains(PortalFlag.PERSONAL_NETWORK) ? UUID.fromString(selectedNetwork.getName()) : player.getUniqueId();
        Portal portal = PortalCreationHelper.createPortalFromSign(selectedNetwork, lines, signLocation, flags, ownerUUID);
        StargateCreateEvent sEvent = new StargateCreateEvent(player, portal, lines, cost);


        Bukkit.getPluginManager().callEvent(sEvent);

        boolean hasPerm = permissionManager.hasPermission(sEvent);
        Stargate.log(Level.CONFIG, " player has perm = " + hasPerm);

        if (errorMessage != null) {
            player.sendMessage(Stargate.languageManager.getErrorMessage(errorMessage));
            return;
        }

        if (!hasPerm) {
            Stargate.log(Level.CONFIG, " Event was cancelled due to lack of permission");
            player.sendMessage(permissionManager.getDenyMessage());
            return;
        }
        if (sEvent.isCancelled()) {
            Stargate.log(Level.CONFIG, " Event was cancelled due an external cancellation");
            player.sendMessage(sEvent.getDenyReason());
            return;
        }

        if (shouldChargePlayer(player, portal, BypassPermission.COST_CREATE) &&
                !Stargate.economyManager.chargeAndTax(player, sEvent.getCost())) {
            player.sendMessage(Stargate.languageManager.getErrorMessage(TranslatableMessage.LACKING_FUNDS));
            return;
        }
        
        if ((flags.contains(PortalFlag.BUNGEE) || flags.contains(PortalFlag.FANCY_INTER_SERVER))
                && !Settings.getBoolean(Setting.USING_BUNGEE)) {
            player.sendMessage(Stargate.languageManager.getErrorMessage(TranslatableMessage.BUNGEE_DISABLED));
            return;
        }
        if (flags.contains(PortalFlag.FANCY_INTER_SERVER) && !Settings.getBoolean(Setting.USING_REMOTE_DATABASE)) {
            player.sendMessage(Stargate.languageManager.getErrorMessage(TranslatableMessage.INTER_SERVER_DISABLED));
            return;
        }

        if (isInSpawn(signLocation.getLocation())) {
            player.sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.SPAWN_CHUNKS_CONFLICTING));
        }
        
        selectedNetwork.addPortal(portal, true);
        selectedNetwork.updatePortals();
        Stargate.log(Level.FINE, "A Gate format matches");
        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            player.sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.CREATE_PERSONAL));
        } else {
            String unformattedMessage = Stargate.languageManager.getMessage(TranslatableMessage.CREATE);
            player.sendMessage(TranslatableMessageFormatter.compileNetwork(unformattedMessage, selectedNetwork.getName()));
        }
    }

    /**
     * Checks whether the given location is located within the location's world's spawn
     *
     * @param location <p>The location to check</p>
     * @return <p>True if the location is located within the spawn area</p>
     */
    private boolean isInSpawn(Location location) {
        Location spawnPoint = Objects.requireNonNull(location.getWorld()).getSpawnLocation();
        Vector vec = location.subtract(spawnPoint).toVector();
        int spawnProtectionWidth = Bukkit.getServer().getSpawnRadius();
        return (Math.abs(vec.getBlockX()) < spawnProtectionWidth && Math.abs(vec.getBlockZ()) < spawnProtectionWidth);
    }

    /**
     * Interprets a network name and removes any characters with special behavior
     *
     * <p>Goes through some scenarios where the initial network name would need to be changed, and returns the
     * modified network name. Some special characters given in a network name may be interpreted as special flags.</p>
     *
     * @param initialNetworkName <p>The initial network name written on the sign</p>
     * @param flags              <p>All the flags of the portal</p>
     * @param player             <p>The player that wrote the network name</p>
     * @param permissionManager  <p>The permission manager to use for checking the player's permissions</p>
     * @return <p>The interpreted network name</p>
     * @throws NameErrorException <p>If the network name does not follow all rules</p>
     */
    private String interpretNetworkName(String initialNetworkName, Set<PortalFlag> flags, Player player,
                                        PermissionManager permissionManager) throws NameErrorException {
        //Force a network name surrounded by square brackets to force an inter-server portal
        //TODO: This bypasses network permission checks. Is this intentional?
        if (initialNetworkName.endsWith("]") && initialNetworkName.startsWith("[")) {
            flags.add(PortalFlag.FANCY_INTER_SERVER);
            return initialNetworkName.substring(1, initialNetworkName.length() - 1);
        }

        //Force a network name surrounded by curly braces to be treated as a personal network
        if (initialNetworkName.endsWith("}") && initialNetworkName.startsWith("{")) {
            String possiblePlayerName = initialNetworkName.substring(1, initialNetworkName.length() - 1);
            flags.add(PortalFlag.PERSONAL_NETWORK);
            Player possiblePlayer = Bukkit.getPlayer(possiblePlayerName);
            if (possiblePlayer != null) {
                return possiblePlayer.getUniqueId().toString();
            }
            throw new NameErrorException(TranslatableMessage.INVALID_NAME);
        }

        /* Try to fall back to the default or a personal network if no network is given, or the player is missing
         * the necessary permissions */
        if (!permissionManager.canCreateInNetwork(initialNetworkName) || initialNetworkName.trim().isEmpty()) {
            Stargate.log(Level.CONFIG, " Player does not have perms to create on current network. Replacing to default...");
            String defaultNetwork = Settings.getString(Setting.DEFAULT_NETWORK);
            if (!permissionManager.canCreateInNetwork(defaultNetwork)) {
                Stargate.log(Level.CONFIG, " Player does not have perms to create on current network. Replacing to private...");
                flags.add(PortalFlag.PERSONAL_NETWORK);
                return player.getUniqueId().toString();
            }
            return defaultNetwork;
        }

        //Moves any private stargates to the player's personal network
        @SuppressWarnings("deprecation")
        OfflinePlayer possiblePersonalNetworkTarget = Bukkit.getOfflinePlayer(initialNetworkName);
        if (flags.contains(PortalFlag.PRIVATE)) {
            flags.add(PortalFlag.PERSONAL_NETWORK);
            return possiblePersonalNetworkTarget.getUniqueId().toString();
        }

        //Move the legacy bungee stargates to their own network
        if (flags.contains(PortalFlag.BUNGEE)) {
            return "§§§§§§#BUNGEE#§§§§§§";
        }
        return initialNetworkName;
    }

    /**
     * Gets the network with the given name, and creates it if it doesn't already exist
     *
     * @param name  <p>The name of the network to get</p>
     * @param flags <p>The flags of the portal that should belong to this network</p>
     * @return <p>The network the portal should be connected to</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    private Network selectNetwork(String name, Set<PortalFlag> flags) throws NameErrorException {
        try {
            Stargate.factory.createNetwork(name, flags);
        } catch (NameErrorException nameErrorException) {
            TranslatableMessage translatableMessage = nameErrorException.getErrorMessage();
            if (translatableMessage != null) {
                throw nameErrorException;
            }
        }
        return Stargate.factory.getNetwork(name, flags.contains(PortalFlag.FANCY_INTER_SERVER));
    }

    /**
     * Listens to and cancels any piston extend events that may break a stargate
     *
     * @param event <p>The triggered piston extend event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (Stargate.factory.isInPortal(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    /**
     * Listens to and cancels any piston retract events that may break a stargate
     *
     * @param event <p>The triggered piston retract event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (Stargate.factory.isInPortal(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    /**
     * Listens to and cancels any explosion events that may break a stargate
     *
     * @param event <p>The triggered explosion event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Portal portal = Stargate.factory.getPortal(event.getLocation(), new GateStructureType[]{GateStructureType.FRAME, GateStructureType.CONTROL_BLOCK});
        if (portal != null) {
            if (Settings.getBoolean(Setting.DESTROY_ON_EXPLOSION)) {
                portal.destroy();
                Supplier<Boolean> destroyAction = () -> {
                    portal.destroy();
                    Stargate.log(Level.FINEST, "Broke the portal from explosion");
                    return true;
                };
                Stargate.syncTickPopulator.addAction(new SupplierAction(destroyAction));
                return;
            }
            event.setCancelled(true);
        }
    }

    /**
     * Listens to and cancels any water or lava flowing from or into a stargate's entrance
     *
     * @param event <p>The triggered block from to event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block toBlock = event.getToBlock();
        Block fromBlock = event.getBlock();
        if ((Stargate.factory.getPortal(toBlock.getLocation(), GateStructureType.IRIS) != null)
                || (Stargate.factory.getPortal(fromBlock.getLocation(), GateStructureType.IRIS) != null)) {
            event.setCancelled(true);
        }
    }

    /**
     * Listens to and cancels any blocks from forming in a stargate's entrance
     *
     * @param event <p>The triggered block form event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFormEvent event) {
        if (!Settings.getBoolean(Setting.PROTECT_ENTRANCE))
            return;

        Location location = event.getBlock().getLocation();
        Portal portal = Stargate.factory.getPortal(location, GateStructureType.IRIS);
        if (portal != null) {
            event.setCancelled(true);
        }
    }

}
