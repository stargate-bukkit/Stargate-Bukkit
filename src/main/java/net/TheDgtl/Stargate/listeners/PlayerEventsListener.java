package net.TheDgtl.Stargate.listeners;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Portal;
import net.TheDgtl.Stargate.Stargate;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerEventsListener extends StargateListener {
    //Var used for temp workaround for a spigot bug.
    private static boolean antiDoubleActivate;
    public PlayerEventsListener(@NotNull Stargate stargate) {
        super(stargate);
        //Var used for temp workaround for a spigot bug.
        antiDoubleActivate = false;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!stargate.isEnableBungee()) return;

        Player player = event.getPlayer();
        String destination = stargate.getBungeeQueue().remove(player.getName().toLowerCase());
        if (destination == null) return;

        Portal portal = Portal.getBungeeGate(destination);
        if (portal == null) {
            Stargate.debug("PlayerJoin", "Error fetching destination portal: " + destination);
            return;
        }
        portal.teleport(player, portal, null);
    }

    @EventHandler
    public void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        // cancel portal and endgateway teleportation if it's from a Stargate entrance
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (!event.isCancelled()
                && (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
                || cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY && World.Environment.THE_END == event.getFrom().getWorld().getEnvironment())
                && Portal.getByAdjacentEntrance(event.getFrom()) != null) {
            event.setCancelled(true);
        }
    }
    /**
     * TODO A method should never be 150 lines long and have this many indentations
     * @param event
     */
    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }
        // Check to see if the player actually moved
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Portal portal = Portal.getByEntrance(event.getTo());
        // No portal or not open
        if (portal == null || !portal.isOpen()) return;

        // Not open for this player
        if (!portal.isOpenFor(player)) {
            stargate.sendMessage(player, stargate.getString("denyMsg"));
            portal.teleport(player, portal, event);
            return;
        }

        Portal destination = portal.getDestination(player);
        if (!portal.isBungee() && destination == null) return;

        boolean deny = false;
        // Check if player has access to this server for Bungee gates
        if (portal.isBungee()) {
            if (!PermissionManager.canAccessServer(player, portal.getNetwork())) {
                deny = true;
            }
        } else {
            // Check if player has access to this network
            if (!PermissionManager.canAccessNetwork(player, portal.getNetwork())) {
                deny = true;
            }

            // Check if player has access to destination world
            if (!PermissionManager.canAccessWorld(player, destination.getWorld().getName())) {
                deny = true;
            }
        }

        if (!PermissionManager.canAccessPortal(player, portal, deny)) {
            stargate.sendMessage(player, stargate.getString("denyMsg"));
            portal.teleport(player, portal, event);
            portal.close(false);
            return;
        }

        int cost = stargate.getUseCost(player, portal, destination);
        if (cost > 0) {
            boolean success;
            if (portal.getGate().getToOwner()) {
                if (portal.getOwnerUUID() == null) {
                    success = stargate.chargePlayer(player, portal.getOwnerUUID(), cost);
                } else {
                    //noinspection deprecation
                    success = stargate.chargePlayer(player, portal.getOwnerName(), cost);
                }
            } else {
                success = stargate.chargePlayer(player, cost);
            }
            if (!success) {
                // Insufficient Funds
                stargate.sendMessage(player, stargate.getString("inFunds"));
                portal.close(false);
                return;
            }

            String deductMsg = stargate.getString("ecoDeduct");
            deductMsg = stargate.replaceVars(deductMsg, new String[]{"%cost%", "%portal%"}, new String[]{stargate.getEconomyHandler().format(cost), portal.getName()});
            stargate.sendMessage(player, deductMsg, false);

            if (portal.getGate().getToOwner() && portal.getOwnerUUID() != null) {
                Player p;
                if (portal.getOwnerUUID() != null) {
                    p = stargate.getServer().getPlayer(portal.getOwnerUUID());
                } else {
                    p = stargate.getServer().getPlayer(portal.getOwnerName());
                }
                if (p != null) {
                    String obtainedMsg = stargate.getString("ecoObtain");
                    obtainedMsg = stargate.replaceVars(obtainedMsg, new String[]{"%cost%", "%portal%"}, new String[]{stargate.getEconomyHandler().format(cost), portal.getName()});
                    stargate.sendMessage(p, obtainedMsg, false);
                }
            }
        }

        stargate.sendMessage(player, stargate.getString("teleportMsg"), false);

        // BungeeCord Support
        if (portal.isBungee()) {
            if (!stargate.isEnableBungee()) {
                player.sendMessage(stargate.getString("bungeeDisabled"));
                portal.close(false);
                return;
            }

            // Teleport the player back to this gate, for sanity's sake
            portal.teleport(player, portal, event);

            // Send the SGBungee packet first, it will be queued by BC if required
            try {
                // Build the message, format is <player>#@#<destination>
                String msg = event.getPlayer().getName() + "#@#" + portal.getDestinationName();
                // Build the message data, sent over the SGBungee bungeecord channel
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                DataOutputStream msgData = new DataOutputStream(bao);
                msgData.writeUTF("Forward");
                msgData.writeUTF(portal.getNetwork());    // Server
                msgData.writeUTF("SGBungee");            // Channel
                msgData.writeShort(msg.length());    // Data Length
                msgData.writeBytes(msg);            // Data
                player.sendPluginMessage(stargate, "BungeeCord", bao.toByteArray());
            } catch (IOException ex) {
                stargate.getStargateLogger().severe("[Stargate] Error sending BungeeCord teleport packet");
                ex.printStackTrace();
                return;
            }

            // Connect player to new server
            try {
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                DataOutputStream msgData = new DataOutputStream(bao);
                msgData.writeUTF("Connect");
                msgData.writeUTF(portal.getNetwork());

                player.sendPluginMessage(stargate, "BungeeCord", bao.toByteArray());
                bao.reset();
            } catch (IOException ex) {
                stargate.getStargateLogger().severe("[Stargate] Error sending BungeeCord connect packet");
                ex.printStackTrace();
                return;
            }

            // Close portal if required (Should never be)
            portal.close(false);
            return;
        }

        destination.teleport(player, portal, event);
        portal.close(false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        BlockData blockData = block.getBlockData();
        Action action = event.getAction();
        Material blockMat = block.getType();

        if (action == Action.RIGHT_CLICK_BLOCK
                && (blockMat == Material.STONE_BUTTON || blockMat == Material.DEAD_TUBE_CORAL_WALL_FAN)) {
            
            if (blockMat == Material.DEAD_TUBE_CORAL_WALL_FAN) {
                if (antiDoubleActivate == true) {
                    antiDoubleActivate = false;
                    //stargate.debug("Debug", "Correcting for an issue with underwater portals.");
                    return;
                }
                antiDoubleActivate = true;
            }

            Portal portal = Portal.getByBlock(block);
            if (portal == null) return;

            // Cancel item use
            event.setUseItemInHand(Event.Result.DENY);
            event.setUseInteractedBlock(Event.Result.DENY);

            boolean deny = false;
            if (!PermissionManager.canAccessNetwork(player, portal.getNetwork())) {
                deny = true;
            }

            if (!PermissionManager.canAccessPortal(player, portal, deny)) {
                stargate.sendMessage(player, stargate.getString("denyMsg"));
                return;
            }

            portal.innitateOpen(player, false);
            if (portal.isOpenFor(player)) {
                event.setUseInteractedBlock(Event.Result.ALLOW);
            }
        }
        
        if (blockData instanceof WallSign
                && (action == Action.LEFT_CLICK_BLOCK
                || action == Action.RIGHT_CLICK_BLOCK)) {
                
            Portal portal = Portal.getByBlock(block);
            if (portal == null) return;

            event.setUseInteractedBlock(Event.Result.DENY);

            // Only cancel event in creative mode
            if (player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }

            boolean deny = false;
            if (!PermissionManager.canAccessNetwork(player, portal.getNetwork())) {
                deny = true;
            }

            if (!PermissionManager.canAccessPortal(player, portal, deny)) {
                stargate.sendMessage(player, stargate.getString("denyMsg"));
                return;
            }

            if ((!portal.isOpen()) && (!portal.isFixed())) {
                portal.cycleDestination(player, action == Action.RIGHT_CLICK_BLOCK ? 1 : -1);
            }
        }
    }
}
