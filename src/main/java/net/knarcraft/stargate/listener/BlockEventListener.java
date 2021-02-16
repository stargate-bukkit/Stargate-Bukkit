package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Portal;
import net.knarcraft.stargate.PortalHandler;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.event.StargateDestroyEvent;
import net.knarcraft.stargate.utility.EconomyHelper;
import net.knarcraft.stargate.utility.MaterialHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;

/**
 * This class is responsible for listening to relevant block events related to creating and breaking portals
 */
@SuppressWarnings("unused")
public class BlockEventListener implements Listener {

    /**
     * Detects sign changes to detect if the user is creating a new gate
     *
     * @param event <p>The triggered event</p>
     */
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        //Ignore normal signs
        if (!(block.getBlockData() instanceof WallSign)) {
            return;
        }

        final Portal portal = PortalHandler.createPortal(event, player);
        // Not creating a gate, just placing a sign
        if (portal == null) {
            return;
        }

        Stargate.sendMessage(player, Stargate.getString("createMsg"), false);
        Stargate.debug("onSignChange", "Initialized stargate: " + portal.getName());
        Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, portal::drawSign, 1);
    }

    // Switch to HIGHEST priority so as to come after block protection plugins (Hopefully)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        Player player = event.getPlayer();

        //Decide if a portal is broken
        Portal portal = PortalHandler.getByBlock(block);
        if (portal == null && Stargate.protectEntrance) {
            portal = PortalHandler.getByEntrance(block);
        }
        if (portal == null) {
            return;
        }

        boolean deny = false;
        String denyMsg = "";

        //Decide if the user can destroy the portal
        if (!Stargate.canDestroy(player, portal)) {
            denyMsg = Stargate.getString("denyMsg");
            deny = true;
            Stargate.log.info(Stargate.getString("prefix") + player.getName() + " tried to destroy gate");
        }

        int cost = Stargate.getDestroyCost(player, portal.getGate());

        //Create and call a StarGateDestroyEvent
        StargateDestroyEvent destroyEvent = new StargateDestroyEvent(portal, player, deny, denyMsg, cost);
        Stargate.server.getPluginManager().callEvent(destroyEvent);
        if (destroyEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        //Destroy denied
        if (destroyEvent.getDeny()) {
            Stargate.sendMessage(player, destroyEvent.getDenyReason());
            event.setCancelled(true);
            return;
        }

        //Take care of payment transactions
        if (!handleEconomyPayment(destroyEvent, player, portal, event)) {
            return;
        }

        PortalHandler.unregisterPortal(portal, true);
        Stargate.sendMessage(player, Stargate.getString("destroyMsg"), false);
    }

    /**
     * Handles economy payment for breaking the portal
     * @param destroyEvent <p>The destroy event</p>
     * @param player <p>The player which triggered the event</p>
     * @param portal <p>The broken portal</p>
     * @param event <p>The break event</p>
     * @return <p>True if the payment was successful. False if the event was cancelled</p>
     */
    private boolean handleEconomyPayment(StargateDestroyEvent destroyEvent, Player player, Portal portal,
                                      BlockBreakEvent event) {
        int cost = destroyEvent.getCost();
        if (cost != 0) {
            if (!Stargate.chargePlayer(player, cost)) {
                Stargate.debug("onBlockBreak", "Insufficient Funds");
                EconomyHelper.sendInsufficientFundsMessage(portal.getName(), player, cost);
                event.setCancelled(true);
                return false;
            }
            if (cost > 0) {
                EconomyHelper.sendDeductMessage(portal.getName(), player, cost);
            } else {
                EconomyHelper.sendRefundMessage(portal.getName(), player, cost);
            }
        }
        return true;
    }

    /**
     * Prevents any block physics events which may damage parts of the portal
     * @param event <p>The event to check and possibly cancel</p>
     */
    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        Portal portal = null;

        // Handle keeping portal material and buttons around
        if (block.getType() == Material.NETHER_PORTAL) {
            portal = PortalHandler.getByEntrance(block);
        } else if (MaterialHelper.isButtonCompatible(block.getType())) {
            portal = PortalHandler.getByControl(block);
        }
        if (portal != null) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels any block move events which may cause a block to enter the opening of a portal
     *
     * @param event <p>The event to check and possibly cancel</p>
     */
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Portal portal = PortalHandler.getByEntrance(event.getBlock());

        if (portal != null) {
            event.setCancelled((event.getBlock().getY() == event.getToBlock().getY()));
        }
    }

    /**
     * Cancels any piston extend events if the target block is part of a portal
     *
     * @param event <p>The event to check and possibly cancel</p>
     */
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        cancelPistonEvent(event, event.getBlocks());
    }

    /**
     * Cancels any piston retract events if the target block is part of a portal
     *
     * @param event <p>The event to check and possibly cancel</p>
     */
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isSticky()) {
            return;
        }
        cancelPistonEvent(event, event.getBlocks());
    }

    /**
     * Cancels a piston event if it would destroy a portal
     *
     * @param event  <p>The event to cancel</p>
     * @param blocks <p>The blocks included in the event</p>
     */
    private void cancelPistonEvent(BlockPistonEvent event, List<Block> blocks) {
        for (Block block : blocks) {
            Portal portal = PortalHandler.getByBlock(block);
            if (portal != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
