package net.TheDgtl.Stargate.listeners;

import net.TheDgtl.Stargate.Portal;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.event.StargateDestroyEvent;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;

public class BlockEventsListener extends StargateListener {

    public BlockEventsListener(@NotNull Stargate stargate) {
        super(stargate);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (!(block.getBlockData() instanceof WallSign)) return;

        final Portal portal = Portal.createPortal(stargate, event, player);
        // Not creating a gate, just placing a sign
        if (portal == null) return;

        stargate.sendMessage(player, stargate.getString("createMsg"), false);
        Stargate.debug("onSignChange", "Initialized stargate: " + portal.getName());
        stargate.getServer().getScheduler().scheduleSyncDelayedTask(stargate, portal::drawSign, 1L);
    }

    // Switch to HIGHEST priority so as to come after block protection plugins (Hopefully)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        Player player = event.getPlayer();
        Portal portal = Portal.getByBlock(block);

        if (portal == null && stargate.isProtectEntrance())
            portal = Portal.getByEntrance(block);
        if (portal == null) return;

        boolean deny = false;
        String denyMsg = "";

        if (!stargate.canDestroy(player, portal)) {
            denyMsg = "Permission Denied"; // TODO: Change to stargate.getString()
            deny = true;
            stargate.getStargateLogger().info("[Stargate] " + player.getName() + " tried to destroy gate");
        }

        int cost = stargate.getDestroyCost(player, portal.getGate());

        StargateDestroyEvent dEvent = new StargateDestroyEvent(portal, player, deny, denyMsg, cost);
        stargate.getServer().getPluginManager().callEvent(dEvent);

        boolean denied = dEvent.getDeny();
        boolean cancelled = denied || dEvent.isCancelled();

        if (cancelled) {
            if (denied) {
                stargate.sendMessage(player, dEvent.getDenyReason());
            }

            event.setCancelled(true);
            return;
        }

        cost = dEvent.getCost();

        if (cost != 0) {
            if (!stargate.chargePlayer(player, cost)) {
                Stargate.debug("onBlockBreak", "Insufficient Funds");
                stargate.sendMessage(player, stargate.getString("inFunds"));

                event.setCancelled(true);
                return;
            }

            boolean deduct = cost > 0;
            int fCost = deduct ? cost : -cost;

            String msg = deduct ? stargate.getString("ecoDeduct") : stargate.getString("ecoRefund");
            msg = stargate.replaceVars(msg, new String[]{"%cost%", "%portal%"}, new String[]{stargate.getEconomyHandler().format(fCost), portal.getName()});

            stargate.sendMessage(player, msg, false);
        }

        portal.unregister(true);
        stargate.sendMessage(player, stargate.getString("destroyMsg"), false);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        Portal portal = null;

        // Handle keeping portal material and buttons around
        switch (block.getType()) {
            case NETHER_PORTAL:
                portal = Portal.getByEntrance(block);
                break;
            case STONE_BUTTON:
            case DEAD_TUBE_CORAL_WALL_FAN:
                portal = Portal.getByControl(block);
                break;
        }

        if (portal != null) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Portal portal = Portal.getByEntrance(event.getBlock());

        if (portal == null) {
            return;
        }

        boolean cancelled = event.getBlock().getY() == event.getToBlock().getY();
        event.setCancelled(cancelled);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            Portal portal = Portal.getByBlock(block);

            if (portal != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isSticky()) return;

        for (Block block : event.getBlocks()) {
            Portal portal = Portal.getByBlock(block);

            if (portal != null) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
