package net.TheDgtl.Stargate.listener;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.manager.StargatePermissionManager;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.RegistryAPI;
import net.TheDgtl.Stargate.network.portal.BungeePortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.property.BlockEventType;
import net.TheDgtl.Stargate.util.BlockEventHelper;
import net.TheDgtl.Stargate.util.NetworkCreationHelper;
import net.TheDgtl.Stargate.util.TranslatableMessageFormatter;
import net.TheDgtl.Stargate.util.portal.PortalCreationHelper;
import net.TheDgtl.Stargate.util.portal.PortalDestructionHelper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.Set;
import java.util.logging.Level;

/**
 * A listener for detecting any relevant block events
 */
public class BlockEventListener implements Listener {

    private final RegistryAPI registry;

    /**
     * Instantiates a new block event listener
     *
     * @param registry <p>The registry to use for looking up portals</p>
     */
    public BlockEventListener(RegistryAPI registry) {
        this.registry = registry;
    }

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
        RealPortal portal = registry.getPortal(location, GateStructureType.FRAME);
        if (portal != null) {
            Runnable destroyAction = () -> {
                String msg = Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.DESTROY);
                event.getPlayer().sendMessage(msg);

                portal.destroy();
                Stargate.log(Level.FINE, "Broke portal " + portal.getName());
            };

            boolean shouldCancel = PortalDestructionHelper.destroyPortalIfHasPermissionAndCanPay(event.getPlayer(), portal, destroyAction);
            if (shouldCancel) {
                event.setCancelled(true);
            }
            return;
        }
        if (registry.getPortal(location, GateStructureType.CONTROL_BLOCK) != null) {
            event.setCancelled(true);
            return;
        }
        if (registry.getPortal(location, GateStructureType.IRIS) != null && ConfigurationHelper.getBoolean(ConfigurationOption.PROTECT_ENTRANCE)) {
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
        Portal portal = registry.getPortal(event.getBlock().getLocation());
        if (portal == null) {
            return;
        }
        if (registry.getPortal(event.getBlock().getLocation(), GateStructureType.IRIS) != null) {
            if (ConfigurationHelper.getBoolean(ConfigurationOption.PROTECT_ENTRANCE)) {
                event.setCancelled(true);
            }
            return;
        }
        if (!BlockEventType.BLOCK_PLACE.canDestroyPortal()) {
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

        StargatePermissionManager permissionManager = new StargatePermissionManager(player);
        TranslatableMessage errorMessage = null;

        if (lines[1].trim().isEmpty()) {
            flags.add(PortalFlag.NETWORKED);
        }

        Set<PortalFlag> disallowedFlags = permissionManager.returnDisallowedFlags(flags);

        if (disallowedFlags.size() > 0) {
            String unformattedMessage = Stargate.getLanguageManagerStatic().getWarningMessage(TranslatableMessage.LACKING_FLAGS_PERMISSION);
            player.sendMessage(TranslatableMessageFormatter.formatFlags(unformattedMessage, disallowedFlags));
        }
        flags.removeAll(disallowedFlags);

        String finalNetworkName;
        Network selectedNetwork = null;
        try {
            if (flags.contains(PortalFlag.BUNGEE)) {
                selectedNetwork = NetworkCreationHelper.selectNetwork(BungeePortal.getLegacyNetworkName(), flags);
            } else {
                Stargate.log(Level.FINER, "....Choosing network name....");
                Stargate.log(Level.FINER, "initial name is " + network);
                boolean shouldShowFallBackMessage = !network.isEmpty();
                finalNetworkName = NetworkCreationHelper.interpretNetworkName(network, flags, player, registry);
                Stargate.log(Level.FINER, "Took format " + finalNetworkName);
                finalNetworkName = NetworkCreationHelper.getAllowedNetworkName(finalNetworkName, permissionManager, player, shouldShowFallBackMessage);
                Stargate.log(Level.FINER, "From allowed permissions took " + finalNetworkName);
                flags.addAll(NetworkCreationHelper.getNameRelatedFlags(finalNetworkName));
                finalNetworkName = NetworkCreationHelper.parseNetworkNameName(finalNetworkName);
                Stargate.log(Level.FINER, "Ended upp with name " + finalNetworkName);
                selectedNetwork = NetworkCreationHelper.selectNetwork(finalNetworkName, flags);
            }
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
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.BLOCK_PISTON_EXTEND, event.getBlocks());
    }

    /**
     * Listens to and cancels any piston retract events that may break a stargate
     *
     * @param event <p>The triggered piston retract event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.BLOCK_PISTON_RETRACT, event.getBlocks());
    }

    /**
     * Listens to and cancels any explosion events that may break a stargate
     *
     * @param event <p>The triggered explosion event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, ConfigurationHelper.getBoolean(ConfigurationOption.DESTROY_ON_EXPLOSION),
                event.blockList());
    }
    
    /**
     * Listens to and cancels any explosion events that may break a stargate
     *
     * @param event <p>The triggered explosion event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, ConfigurationHelper.getBoolean(ConfigurationOption.DESTROY_ON_EXPLOSION),
                event.blockList());
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
        if ((registry.getPortal(toBlock.getLocation(), GateStructureType.IRIS) != null)
                || (registry.getPortal(fromBlock.getLocation(), GateStructureType.IRIS) != null)) {
            event.setCancelled(true);
            return;
        }
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FROM_TO, toBlock.getLocation());
    }

    /**
     * Listens to and cancels any blocks from forming in a stargate's entrance
     *
     * @param event <p>The triggered block form event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFormEvent event) {
        Location location = event.getBlock().getLocation();
        Portal portalFromIris = registry.getPortal(location, GateStructureType.IRIS);
        if (portalFromIris != null) {
            if (ConfigurationHelper.getBoolean(ConfigurationOption.PROTECT_ENTRANCE)) {
                event.setCancelled(true);
            }
            return;
        }
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FORM, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_PHYSICS, event.getBlock().getLocation());
    }

    /**
     * Listens to and cancels any block burn events that may break a stargate
     *
     * @param event <p>The triggered burn event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_BURN, event.getBlock().getLocation());
    }

    /**
     * Listens to and cancels any fire ignition events touching portal (avoids infinite fires)
     *
     * @param event <p>The triggered ignition event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!BlockEventType.BLOCK_BURN.canDestroyPortal()
                && registry.isNextToPortal(event.getBlock().getLocation(), GateStructureType.FRAME)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FADE, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FERTILIZE, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockMultiPlace(BlockMultiPlaceEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.BLOCK_MULTI_PLACE,
                BlockEventHelper.getBlockList(event.getReplacedBlockStates()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_BLOCK_FORM, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.LEAVES_DECAY, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.SPONGE_ABSORB, BlockEventHelper.getBlockList(event.getBlocks()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_CHANGE_EVENT_BLOCK, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_BREAK_DOOR, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.PORTAL_CREATE, BlockEventHelper.getBlockList(event.getBlocks()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_PLACE, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.PLAYER_BUCKET_EMPTY, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (!(event.getBlock().getBlockData() instanceof Directional)) {
            return;
        }
        Directional dispenser = (Directional) event.getBlock().getBlockData();
        Location dispensedLocation = event.getBlock().getLocation().clone().add(dispenser.getFacing().getDirection());
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_DISPENSE, dispensedLocation);
    }
}
