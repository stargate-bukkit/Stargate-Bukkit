package org.sgrewritten.stargate.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
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
import org.bukkit.event.block.TNTPrimeEvent;
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
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.StargatePortalBuilderEvent;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.gate.GateBuilder;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.gate.ImplicitGateBuilder;
import org.sgrewritten.stargate.api.network.PortalBuilder;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.LocalisedMessageException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.property.BlockEventType;
import org.sgrewritten.stargate.util.BlockEventHelper;
import org.sgrewritten.stargate.util.MessageUtils;
import org.sgrewritten.stargate.util.portal.PortalDestructionHelper;

import java.util.List;
import java.util.Objects;
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
                MessageUtils.sendMessageFromPortal(portal, event.getPlayer(), msg, MessageType.DESTROY);

                stargateAPI.getNetworkManager().destroyPortal(portal);
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
                MessageUtils.sendMessageFromPortal(portalFromIris, event.getPlayer(), msg, MessageType.DESTROY);

                event.getPlayer().sendMessage(msg);
                portalFromIris.destroy();
                try {
                    stargateAPI.getStorageAPI().removePortalFromStorage(null);
                } catch (StorageWriteException e) {
                    Stargate.log(e);
                }
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
        if (BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_PLACE, event.getBlock().getLocation(), stargateAPI)) {
            event.getPlayer().sendMessage(languageManager.getErrorMessage(TranslatableMessage.DESTROY));
        }
        if (!addonRegistry.hasRegisteredBlockHandler(event.getBlock().getType())) {
            return;
        }
        List<RealPortal> portals = registry.getPortalsFromTouchingBlock(event.getBlock().getLocation(), GateStructureType.FRAME);
        if (portals.isEmpty()) {
            Stargate.log(Level.FINEST, "Could not find any portals next to placed block");
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
        if (registry.getPortal(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
            return;
        }
        Player player = event.getPlayer();
        String portalName = event.getLine(0);
        String destinationName = event.getLine(1);
        String networkOrServerName = event.getLine(2);
        String flagsString = event.getLine(3);
        flagsString = flagsString == null ? "" : flagsString;

        if (portalName == null || networkOrServerName == null) {
            return;
        }

        try {
            GateBuilder gateBuilder = new ImplicitGateBuilder(event.getBlock().getLocation(), registry);
            PortalBuilder portalBuilder = new PortalBuilder(stargateAPI, player, portalName).setFlags(flagsString);
            portalBuilder.setNetwork(networkOrServerName).setGateBuilder(gateBuilder);
            portalBuilder.addEventHandling(player).addMessageReceiver(player).addPermissionCheck(player).setCost(ConfigurationHelper.getDouble(ConfigurationOption.CREATION_COST), player);
            portalBuilder.setDestination(destinationName).setAdaptiveGatePositionGeneration(true).setDestinationServerName(networkOrServerName);
            StargatePortalBuilderEvent builderEvent = new StargatePortalBuilderEvent(portalBuilder, gateBuilder, event.getLines(), player);
            builderEvent.callEvent();
            portalBuilder.build();
        } catch (NoFormatFoundException noFormatFoundException) {
            Stargate.log(Level.FINER, "No Gate format matches");
        } catch (GateConflictException gateConflictException) {
            event.getPlayer().sendMessage(languageManager.getErrorMessage(TranslatableMessage.GATE_CONFLICT));
        } catch (LocalisedMessageException e) {
            if (e.getPortal() != null) {
                MessageUtils.sendMessageFromPortal(e.getPortal(), event.getPlayer(), e.getLocalisedMessage(languageManager), e.getMessageType());
            } else {
                MessageUtils.sendMessage(event.getPlayer(), e.getLocalisedMessage(languageManager));
            }
        } catch (TranslatableException e) {
            event.getPlayer().sendMessage(e.getLocalisedMessage(languageManager));
        } catch (InvalidStructureException ignored) {
        }
    }

    /**
     * Listens to and cancels any piston extend events that may break a stargate
     *
     * @param event <p>The triggered piston extend event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.BLOCK_PISTON_EXTEND, event.getBlocks(), stargateAPI);
    }

    /**
     * Listens to and cancels any piston retract events that may break a stargate
     *
     * @param event <p>The triggered piston retract event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.BLOCK_PISTON_RETRACT, event.getBlocks(), stargateAPI);
    }

    /**
     * Listens to and cancels any explosion events that may break a stargate
     *
     * @param event <p>The triggered explosion event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.ENTITY_EXPLODE, event.blockList(), stargateAPI);
    }

    /**
     * Listens to and cancels any explosion events that may break a stargate
     *
     * @param event <p>The triggered explosion event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.BLOCK_EXPLODE, event.blockList(), stargateAPI);
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
        if ((registry.getPortal(toBlock.getLocation(), GateStructureType.IRIS) != null) || (registry.getPortal(fromBlock.getLocation(), GateStructureType.IRIS) != null)) {
            event.setCancelled(true);
            return;
        }
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FROM_TO, toBlock.getLocation(), stargateAPI);
    }

    /**
     * Listens to and cancels any blocks from forming in a stargate's entrance
     *
     * @param event <p>The triggered block form event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFormEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FORM, event.getBlock().getLocation(), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_PHYSICS, event.getBlock().getLocation(), stargateAPI);
    }

    /**
     * Listens to and cancels any block burn events that may break a stargate
     *
     * @param event <p>The triggered burn event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_BURN, event.getBlock().getLocation(), stargateAPI);
    }

    /**
     * Listens to and cancels any fire ignition events touching portal (avoids infinite fires)
     *
     * @param event <p>The triggered ignition event</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!BlockEventType.BLOCK_BURN.canDestroyPortal() && registry.isNextToPortal(event.getBlock().getLocation(), GateStructureType.FRAME)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FADE, event.getBlock().getLocation(), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_FERTILIZE, event.getBlock().getLocation(), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockMultiPlace(BlockMultiPlaceEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.BLOCK_MULTI_PLACE, BlockEventHelper.getBlockList(event.getReplacedBlockStates()), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_BLOCK_FORM, event.getBlock().getLocation(), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.LEAVES_DECAY, event.getBlock().getLocation(), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.SPONGE_ABSORB, BlockEventHelper.getBlockList(event.getBlocks()), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_CHANGE_EVENT_BLOCK, event.getBlock().getLocation(), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_BREAK_DOOR, event.getBlock().getLocation(), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        BlockEventHelper.onAnyMultiBlockChangeEvent(event, BlockEventType.PORTAL_CREATE, BlockEventHelper.getBlockList(event.getBlocks()), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.ENTITY_PLACE, event.getBlock().getLocation(), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.PLAYER_BUCKET_EMPTY, event.getBlock().getLocation(), stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (!(event.getBlock().getBlockData() instanceof Directional dispenser)) {
            return;
        }
        Location dispensedLocation = event.getBlock().getLocation().clone().add(dispenser.getFacing().getDirection());
        BlockEventHelper.onAnyBlockChangeEvent(event, BlockEventType.BLOCK_DISPENSE, dispensedLocation, stargateAPI);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTNTPrime(TNTPrimeEvent tntPrimeEvent) {
        BlockEventHelper.onAnyBlockChangeEvent(tntPrimeEvent, BlockEventType.TNT_PRIME, tntPrimeEvent.getBlock().getLocation(), stargateAPI);
    }
}
