package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.PortalHandler;
import net.knarcraft.stargate.utility.EconomyHelper;
import net.knarcraft.stargate.utility.MaterialHelper;
import net.knarcraft.stargate.Portal;
import net.knarcraft.stargate.Stargate;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerEventsListener implements Listener {

    private static long eventTime;
    private static PlayerInteractEvent previousEvent;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Stargate.enableBungee) {
            return;
        }

        Player player = event.getPlayer();
        String destination = Stargate.bungeeQueue.remove(player.getName().toLowerCase());
        if (destination == null) {
            return;
        }

        Portal portal = PortalHandler.getBungeeGate(destination);
        if (portal == null) {
            Stargate.debug("PlayerJoin", "Error fetching destination portal: " + destination);
            return;
        }
        portal.teleport(player, portal, null);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // cancel portal and endgateway teleportation if it's from a stargate entrance
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (!event.isCancelled()
                && (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
                || cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY && World.Environment.THE_END == event.getFrom().getWorld().getEnvironment())
                && PortalHandler.getByAdjacentEntrance(event.getFrom()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;

        // Check to see if the player actually moved
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Portal entracePortal = PortalHandler.getByEntrance(event.getTo());
        // No portal or not open
        if (entracePortal == null || !entracePortal.isOpen()) return;

        // Not open for this player
        if (!entracePortal.isOpenFor(player)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            entracePortal.teleport(player, entracePortal, event);
            return;
        }

        Portal destination = entracePortal.getDestination(player);
        if (!entracePortal.isBungee() && destination == null) return;

        boolean deny = false;
        // Check if player has access to this server for Bungee gates
        if (entracePortal.isBungee()) {
            if (!Stargate.canAccessServer(player, entracePortal.getNetwork())) {
                deny = true;
            }
        } else {
            // Check if player has access to this network
            if (!Stargate.canAccessNetwork(player, entracePortal.getNetwork())) {
                deny = true;
            }

            // Check if player has access to destination world
            if (!Stargate.canAccessWorld(player, destination.getWorld().getName())) {
                deny = true;
            }
        }

        if (!Stargate.canAccessPortal(player, entracePortal, deny)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            entracePortal.teleport(player, entracePortal, event);
            entracePortal.close(false);
            return;
        }

        int cost = Stargate.getUseCost(player, entracePortal, destination);
        if (cost > 0) {
            if (!EconomyHelper.payTeleportFee(entracePortal, player, cost)) {
                return;
            }
        }

        Stargate.sendMessage(player, Stargate.getString("teleportMsg"), false);

        // BungeeCord Support
        if (entracePortal.isBungee()) {
            if (!Stargate.enableBungee) {
                player.sendMessage(Stargate.getString("bungeeDisabled"));
                entracePortal.close(false);
                return;
            }

            // Teleport the player back to this gate, for sanity's sake
            entracePortal.teleport(player, entracePortal, event);

            // Send the SGBungee packet first, it will be queued by BC if required
            try {
                // Build the message, format is <player>#@#<destination>
                String msg = event.getPlayer().getName() + "#@#" + entracePortal.getDestinationName();
                // Build the message data, sent over the SGBungee bungeecord channel
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                DataOutputStream msgData = new DataOutputStream(bao);
                msgData.writeUTF("Forward");
                msgData.writeUTF(entracePortal.getNetwork());    // Server
                msgData.writeUTF("SGBungee");            // Channel
                msgData.writeShort(msg.length());    // Data Length
                msgData.writeBytes(msg);            // Data
                player.sendPluginMessage(Stargate.stargate, "BungeeCord", bao.toByteArray());
            } catch (IOException ex) {
                Stargate.log.severe(Stargate.getString("prefix") + "Error sending BungeeCord teleport packet");
                ex.printStackTrace();
                return;
            }

            // Connect player to new server
            try {
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                DataOutputStream msgData = new DataOutputStream(bao);
                msgData.writeUTF("Connect");
                msgData.writeUTF(entracePortal.getNetwork());

                player.sendPluginMessage(Stargate.stargate, "BungeeCord", bao.toByteArray());
                bao.reset();
            } catch (IOException ex) {
                Stargate.log.severe(Stargate.getString("prefix") + "Error sending BungeeCord connect packet");
                ex.printStackTrace();
                return;
            }

            // Close portal if required (Should never be)
            entracePortal.close(false);
            return;
        }

        destination.teleport(player, entracePortal, event);
        entracePortal.close(false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        // Right click
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (block.getBlockData() instanceof WallSign) {
                Portal portal = PortalHandler.getByBlock(block);
                if (portal == null) {
                    return;
                }
                // Cancel item use
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);

                boolean deny = false;
                if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                    deny = true;
                }

                if (!Stargate.canAccessPortal(player, portal, deny)) {
                    Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                    return;
                }

                if ((!portal.isOpen()) && (!portal.isFixed())) {
                    portal.cycleDestination(player);
                }
                return;
            }

            // Implement right-click to toggle a stargate, gets around spawn protection problem.
            if (MaterialHelper.isButtonCompatible(block.getType())) {

                if (MaterialHelper.isWallCoral(block.getType())) {
                    if (previousEvent != null &&
                            event.getPlayer() == previousEvent.getPlayer() && eventTime + 10 > System.currentTimeMillis()) {
                        previousEvent = null;
                        eventTime = 0;
                        return;
                    }
                    previousEvent = event;
                    eventTime = System.currentTimeMillis();
                }

                Portal portal = PortalHandler.getByBlock(block);
                if (portal == null) {
                    return;
                }

                // Cancel item use
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);

                boolean deny = false;
                if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                    deny = true;
                }

                if (!Stargate.canAccessPortal(player, portal, deny)) {
                    Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                    return;
                }

                Stargate.openPortal(player, portal);
                if (portal.isOpenFor(player)) {
                    event.setUseInteractedBlock(Event.Result.ALLOW);
                }
            }
            return;
        }

        // Left click
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Check if we're scrolling a sign
            if (block.getBlockData() instanceof WallSign) {
                Portal portal = PortalHandler.getByBlock(block);
                if (portal == null) {
                    return;
                }

                event.setUseInteractedBlock(Event.Result.DENY);
                // Only cancel event in creative mode
                if (player.getGameMode().equals(GameMode.CREATIVE)) {
                    event.setCancelled(true);
                }

                boolean deny = false;
                if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                    deny = true;
                }

                if (!Stargate.canAccessPortal(player, portal, deny)) {
                    Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                    return;
                }

                if ((!portal.isOpen()) && (!portal.isFixed())) {
                    portal.cycleDestination(player, -1);
                }
            }
        }
    }

}
