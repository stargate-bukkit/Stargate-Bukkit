package org.sgrewritten.stargate.listener;

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
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.BlockHandlerResolver;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.BungeePortal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.property.BlockEventType;
import org.sgrewritten.stargate.util.BlockEventHelper;
import org.sgrewritten.stargate.util.NetworkCreationHelper;
import org.sgrewritten.stargate.util.TranslatableMessageFormatter;
import org.sgrewritten.stargate.util.portal.PortalCreationHelper;
import org.sgrewritten.stargate.util.portal.PortalDestructionHelper;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

/**
 * A listener for detecting any relevant block events
 */
public class BlockEventListener implements Listener {

    private final @NotNull RegistryAPI registry;
    private final @NotNull LanguageManager languageManager;
    private final @NotNull StargateEconomyAPI economyManager;
    private final @NotNull BlockHandlerResolver addonRegistry;
    private final @NotNull StargateAPI stargateAPI;

    /**
     * Instantiates a new block event listener
     *
     * @param stargateAPI <p>The stargate API</p>
     */
    public BlockEventListener(@NotNull StargateAPI stargateAPI) {
        this.stargateAPI = Objects.requireNonNull(stargateAPI);
        this.registry = stargateAPI.getRegistry();
        this.languageManager = stargateAPI.getLanguageManager();
        this.economyManager = stargateAPI.getEconomyManager();
        this.addonRegistry = stargateAPI.getMaterialHandlerResolver();
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
                String msg = languageManager.getErrorMessage(TranslatableMessage.DESTROY);
                event.getPlayer().sendMessage(msg);

                portal.destroy();
                Stargate.log(Level.FINE, "Broke portal " + portal.getName());
            };

            boolean shouldCancel = PortalDestructionHelper.destroyPortalIfHasPermissionAndCanPay(event.getPlayer(), portal, destroyAction, languageManager, economyManager);
            if (shouldCancel) {
                event.setCancelled(true);
            }
            return;
        }
        if (registry.getPortalPosition(location) != null) {
            event.setCancelled(true);
            return;
        }
        RealPortal portalFromIris = registry.getPortal(location, GateStructureType.IRIS);
        if (portalFromIris != null) {
            if (BlockEventType.BLOCK_BREAK.canDestroyPortal()) {
                String msg = languageManager.getErrorMessage(TranslatableMessage.DESTROY);
                event.getPlayer().sendMessage(msg);
                portalFromIris.destroy();
                return;
            }
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
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_PLACE, event.getBlock().getLocation(),
                registry,
                () -> event.getPlayer().sendMessage(languageManager.getErrorMessage(TranslatableMessage.DESTROY)));
        if(event.isCancelled() || !addonRegistry.hasRegisteredBlockHandler(event.getBlock().getType())) {
            return;
        }
        List<RealPortal> portals = registry.getPortalsFromTouchingBlock(event.getBlock().getLocation(), GateStructureType.FRAME);
        if(portals.isEmpty()) {
            return;
        }
        addonRegistry.registerPlacement(registry, event.getBlock().getLocation(), portals, event.getBlock().getType(), event.getPlayer());
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
        Set<Character> unrecognisedFlags = PortalFlag.getUnrecognisedFlags(lines[3]);
        //Prevent the player from explicitly setting any internal flags
        flags.removeIf(PortalFlag::isInternalFlag);

        StargatePermissionManager permissionManager = new StargatePermissionManager(player, languageManager);
        String errorMessage = null;

        if (lines[1].trim().isEmpty()) {
            flags.add(PortalFlag.NETWORKED);
        }

        Set<PortalFlag> disallowedFlags = permissionManager.returnDisallowedFlags(flags);

        if (disallowedFlags.size() > 0) {
            String unformattedMessage = languageManager.getWarningMessage(TranslatableMessage.LACKING_FLAGS_PERMISSION);
            player.sendMessage(TranslatableMessageFormatter.formatFlags(unformattedMessage, disallowedFlags));
        }
        flags.removeAll(disallowedFlags);

        Network selectedNetwork = null;
        try {
            if (flags.contains(PortalFlag.BUNGEE)) {
                selectedNetwork = NetworkCreationHelper.selectNetwork(BungeePortal.getLegacyNetworkName(), permissionManager, player, flags, registry);
            } else {
                selectedNetwork = NetworkCreationHelper.selectNetwork(network, permissionManager, player, flags, registry);
            }
            //NetworkType-flags are incompatible with each other, this makes sure that only the flag of the portals network is in use
            NetworkType.removeNetworkTypeRelatedFlags(flags);
            flags.add(selectedNetwork.getType().getRelatedFlag());
        } catch (TranslatableException e) {
            errorMessage = e.getLocalisedMessage(languageManager);
        }
        try {
            PortalCreationHelper.tryPortalCreation(selectedNetwork, lines, block, flags, unrecognisedFlags, event.getPlayer(), cost,
                    permissionManager, errorMessage, stargateAPI);
        } catch (NoFormatFoundException noFormatFoundException) {
            Stargate.log(Level.FINER, "No Gate format matches");
        } catch (GateConflictException gateConflictException) {
            player.sendMessage(languageManager.getErrorMessage(TranslatableMessage.GATE_CONFLICT));
        } catch (TranslatableException e) {
            player.sendMessage(e.getLocalisedMessage(languageManager));
        }
    }

    /**
     * Listens to and cancels any piston extend events that may break a stargate
     *
     * @param event <p>The triggered piston extend event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.BLOCK_PISTON_EXTEND, event.getBlocks(), registry);
    }

    /**
     * Listens to and cancels any piston retract events that may break a stargate
     *
     * @param event <p>The triggered piston retract event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.BLOCK_PISTON_RETRACT, event.getBlocks(), registry);
    }

    /**
     * Listens to and cancels any explosion events that may break a stargate
     *
     * @param event <p>The triggered explosion event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, ConfigurationHelper.getBoolean(ConfigurationOption.DESTROY_ON_EXPLOSION),
                event.blockList(), registry);
    }

    /**
     * Listens to and cancels any explosion events that may break a stargate
     *
     * @param event <p>The triggered explosion event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, ConfigurationHelper.getBoolean(ConfigurationOption.DESTROY_ON_EXPLOSION),
                event.blockList(), registry);
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
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FROM_TO, toBlock.getLocation(), registry);
    }

    /**
     * Listens to and cancels any blocks from forming in a stargate's entrance
     *
     * @param event <p>The triggered block form event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFormEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FORM, event.getBlock().getLocation(), registry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_PHYSICS, event.getBlock().getLocation(), registry);
    }

    /**
     * Listens to and cancels any block burn events that may break a stargate
     *
     * @param event <p>The triggered burn event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_BURN, event.getBlock().getLocation(), registry);
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
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FADE, event.getBlock().getLocation(), registry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FERTILIZE, event.getBlock().getLocation(), registry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockMultiPlace(BlockMultiPlaceEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.BLOCK_MULTI_PLACE,
                BlockEventHelper.getBlockList(event.getReplacedBlockStates()), registry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_BLOCK_FORM, event.getBlock().getLocation(), registry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.LEAVES_DECAY, event.getBlock().getLocation(), registry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.SPONGE_ABSORB, BlockEventHelper.getBlockList(event.getBlocks()), registry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_CHANGE_EVENT_BLOCK, event.getBlock().getLocation(), registry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_BREAK_DOOR, event.getBlock().getLocation(), registry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.PORTAL_CREATE, BlockEventHelper.getBlockList(event.getBlocks()), registry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_PLACE, event.getBlock().getLocation(), registry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.PLAYER_BUCKET_EMPTY, event.getBlock().getLocation(), registry);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (!(event.getBlock().getBlockData() instanceof Directional dispenser)) {
            return;
        }
        Location dispensedLocation = event.getBlock().getLocation().clone().add(dispenser.getFacing().getDirection());
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_DISPENSE, dispensedLocation, registry);
    }
}
