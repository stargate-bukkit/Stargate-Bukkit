package net.TheDgtl.Stargate.listener;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.action.SupplierAction;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.manager.PermissionManager;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.util.NetworkCreationHelper;
import net.TheDgtl.Stargate.util.TranslatableMessageFormatter;
import net.TheDgtl.Stargate.util.portal.PortalCreationHelper;
import net.TheDgtl.Stargate.util.portal.PortalDestructionHelper;
import org.bukkit.Location;
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

import java.util.HashSet;
import java.util.Set;
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
        RealPortal portal = Stargate.getRegistryStatic().getPortal(location, GateStructureType.FRAME);
        if (portal != null) {
            Supplier<Boolean> destroyAction = () -> {
                String msg = Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.DESTROY);
                event.getPlayer().sendMessage(msg);

                portal.destroy();
                Stargate.log(Level.FINE, "Broke portal " + portal.getName());
                return true;
            };

            boolean shouldCancel = PortalDestructionHelper.destroyPortalIfHasPermissionAndCanPay(event.getPlayer(), portal, destroyAction);
            if (shouldCancel) {
                event.setCancelled(true);
            }
            return;
        }
        if (Stargate.getRegistryStatic().getPortal(location, GateStructureType.CONTROL_BLOCK) != null) {
            event.setCancelled(true);
            return;
        }
        if (Stargate.getRegistryStatic().getPortal(location, GateStructureType.IRIS) != null && ConfigurationHelper.getBoolean(ConfigurationOption.PROTECT_ENTRANCE)) {
            event.setCancelled(true);
        }
    }

    /**
     * Checks for and blocks any block placement in a stargate's iris
     *
     * @param event <p>The triggered block place event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();
        Portal portal = Stargate.getRegistryStatic().getPortal(loc, GateStructureType.IRIS);
        if (portal != null && ConfigurationHelper.getBoolean(ConfigurationOption.PROTECT_ENTRANCE)) {
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
        int cost = ConfigurationHelper.getInteger(ConfigurationOption.CREATION_COST);
        Player player = event.getPlayer();
        Set<PortalFlag> flags = PortalFlag.parseFlags(lines[3]);

        PermissionManager permissionManager = new PermissionManager(player);
        TranslatableMessage errorMessage = null;

        if (lines[1].trim().isEmpty()) {
            flags.add(PortalFlag.NETWORKED);
        }

        if (flags.contains(PortalFlag.PRIVATE)) {
            flags.add(PortalFlag.PERSONAL_NETWORK);
        }

        Set<PortalFlag> disallowedFlags = permissionManager.returnDisallowedFlags(flags);

        if (disallowedFlags.size() > 0) {
            String unformattedMessage = Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.LACKING_FLAGS_PERMISSION);
            player.sendMessage(TranslatableMessageFormatter.formatFlags(unformattedMessage, disallowedFlags));
        }
        flags.removeAll(disallowedFlags);

        String finalNetworkName;
        Network selectedNetwork = null;
        try {
            Stargate.log(Level.FINER, "....Choosing network name....");
            Stargate.log(Level.FINER, "initial name is " + network);
            finalNetworkName = NetworkCreationHelper.interpretNetworkName(network, flags, player, Stargate.getRegistryStatic());
            Stargate.log(Level.FINER, "Took format " + finalNetworkName);
            finalNetworkName = NetworkCreationHelper.getAllowedNetworkName(finalNetworkName, permissionManager, player);
            Stargate.log(Level.FINER, "From allowed permissions took " + finalNetworkName);
            flags.addAll(NetworkCreationHelper.getNameRelatedFlags(finalNetworkName));
            finalNetworkName = NetworkCreationHelper.parseNetworkNameName(finalNetworkName);
            Stargate.log(Level.FINER, "Ended upp with name " + finalNetworkName);
            selectedNetwork = NetworkCreationHelper.selectNetwork(finalNetworkName, flags);
        } catch (NameErrorException nameErrorException) {
            errorMessage = nameErrorException.getErrorMessage();
        }


        try {
            PortalCreationHelper.tryPortalCreation(selectedNetwork, lines, block, flags, event.getPlayer(), cost, permissionManager, errorMessage);
        } catch (NoFormatFoundException noFormatFoundException) {
            Stargate.log(Level.FINER, "No Gate format matches");
        } catch (GateConflictException gateConflictException) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.GATE_CONFLICT));
        } catch (NameErrorException nameErrorException) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(nameErrorException.getErrorMessage()));
        }
    }

    /**
     * Listens to and cancels any piston extend events that may break a stargate
     *
     * @param event <p>The triggered piston extend event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (Stargate.getRegistryStatic().isPartOfPortal(event.getBlocks())) {
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
        if (Stargate.getRegistryStatic().isPartOfPortal(event.getBlocks())) {
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
        Set<Portal> explodedPortals = new HashSet<>();

        for (Block block : event.blockList()) {
            Portal portal = Stargate.getRegistryStatic().getPortal(block.getLocation(),
                    new GateStructureType[]{GateStructureType.FRAME, GateStructureType.CONTROL_BLOCK});
            if (portal != null) {
                if (!ConfigurationHelper.getBoolean(ConfigurationOption.DESTROY_ON_EXPLOSION)) {
                    event.setCancelled(true);
                    return;
                }
                explodedPortals.add(portal);
            }
        }

        for (Portal portal : explodedPortals) {
            Supplier<Boolean> destroyAction = () -> {
                portal.destroy();
                Stargate.log(Level.FINEST, "Broke the portal from explosion");
                return true;
            };
            Stargate.syncTickPopulator.addAction(new SupplierAction(destroyAction));
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
        if ((Stargate.getRegistryStatic().getPortal(toBlock.getLocation(), GateStructureType.IRIS) != null)
                || (Stargate.getRegistryStatic().getPortal(fromBlock.getLocation(), GateStructureType.IRIS) != null)) {
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
        if (!ConfigurationHelper.getBoolean(ConfigurationOption.PROTECT_ENTRANCE)) {
            return;
        }

        Location location = event.getBlock().getLocation();
        Portal portal = Stargate.getRegistryStatic().getPortal(location, GateStructureType.IRIS);
        if (portal != null) {
            event.setCancelled(true);
        }
    }

}
