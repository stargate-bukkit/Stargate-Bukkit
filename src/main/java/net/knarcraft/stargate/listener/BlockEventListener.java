package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.EconomyHandler;
import net.knarcraft.stargate.Portal;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.event.StargateDestroyEvent;
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

public class BlockEventListener implements Listener {
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (!(block.getBlockData() instanceof WallSign)) {
            return;
        }

        final Portal portal = Portal.createPortal(event, player);
        // Not creating a gate, just placing a sign
        if (portal == null) {
            return;
        }

        Stargate.sendMessage(player, Stargate.getString("createMsg"), false);
        Stargate.debug("onSignChange", "Initialized stargate: " + portal.getName());
        Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, () -> portal.drawSign(), 1);
    }

    // Switch to HIGHEST priority so as to come after block protection plugins (Hopefully)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();

        Portal portal = Portal.getByBlock(block);
        if (portal == null && Stargate.protectEntrance)
            portal = Portal.getByEntrance(block);
        if (portal == null) return;

        boolean deny = false;
        String denyMsg = "";

        if (!Stargate.canDestroy(player, portal)) {
            denyMsg = "Permission Denied"; // TODO: Change to stargate.getString()
            deny = true;
            Stargate.log.info(Stargate.getString("prefix") + player.getName() + " tried to destroy gate");
        }

        int cost = Stargate.getDestroyCost(player, portal.getGate());

        StargateDestroyEvent destroyEvent = new StargateDestroyEvent(portal, player, deny, denyMsg, cost);
        Stargate.server.getPluginManager().callEvent(destroyEvent);
        if (destroyEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        if (destroyEvent.getDeny()) {
            Stargate.sendMessage(player, destroyEvent.getDenyReason());
            event.setCancelled(true);
            return;
        }

        cost = destroyEvent.getCost();

        if (cost != 0) {
            if (!Stargate.chargePlayer(player, cost)) {
                Stargate.debug("onBlockBreak", "Insufficient Funds");
                Stargate.sendMessage(player, Stargate.getString("inFunds"));
                event.setCancelled(true);
                return;
            }

            if (cost > 0) {
                String deductMsg = Stargate.getString("ecoDeduct");
                deductMsg = Stargate.replaceVars(deductMsg, new String[]{"%cost%", "%portal%"}, new String[]{EconomyHandler.format(cost), portal.getName()});
                Stargate.sendMessage(player, deductMsg, false);
            } else {
                String refundMsg = Stargate.getString("ecoRefund");
                refundMsg = Stargate.replaceVars(refundMsg, new String[]{"%cost%", "%portal%"}, new String[]{EconomyHandler.format(-cost), portal.getName()});
                Stargate.sendMessage(player, refundMsg, false);
            }
        }

        portal.unregister(true);
        Stargate.sendMessage(player, Stargate.getString("destroyMsg"), false);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        Portal portal = null;

        // Handle keeping portal material and buttons around
        if (block.getType() == Material.NETHER_PORTAL) {
            portal = Portal.getByEntrance(block);
        } else if (MaterialHelper.isButtonCompatible(block.getType())) {
            portal = Portal.getByControl(block);
        }
        if (portal != null) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Portal portal = Portal.getByEntrance(event.getBlock());

        if (portal != null) {
            event.setCancelled((event.getBlock().getY() == event.getToBlock().getY()));
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        cancelPistonEvent(event, event.getBlocks());
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isSticky()) {
            return;
        }
        cancelPistonEvent(event, event.getBlocks());
    }

    /**
     * Cancels a piston event if it would destroy a portal
     * @param event <p>The event to cancel</p>
     * @param blocks <p>The blocks included in the event</p>
     */
    private void cancelPistonEvent(BlockPistonEvent event, List<Block> blocks) {
        for (Block block : blocks) {
            Portal portal = Portal.getByBlock(block);
            if (portal != null) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
