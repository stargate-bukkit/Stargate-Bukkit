package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.BlockLocation;
import net.knarcraft.stargate.Portal;
import net.knarcraft.stargate.PortalHandler;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.utility.BungeeHelper;
import net.knarcraft.stargate.utility.EconomyHelper;
import net.knarcraft.stargate.utility.MaterialHelper;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

public class PlayerEventsListener implements Listener {

    private static long eventTime;
    private static PlayerInteractEvent previousEvent;

    /**
     * This event handler handles detection of any player teleporting through a bungee gate
     *
     * @param event <p>The event to check for a teleporting player</p>
     */
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

    /**
     * This event handler handles some special teleportation events
     *
     * <p>This event cancels nether portal and end gateway teleportation if the user teleported from a stargate
     * entrance. This prevents the user from just teleporting to the nether with the default portal design.
     * Additionally, this event teleports any vehicles not detected by the VehicleMove event together with the player.</p>
     *
     * @param event <p>The event to check and possibly cancel</p>
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // cancel portal and end gateway teleportation if it's from a stargate entrance
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (!event.isCancelled() && (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
                || cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY && World.Environment.THE_END ==
                Objects.requireNonNull(event.getFrom().getWorld()).getEnvironment())
                && PortalHandler.getByAdjacentEntrance(event.getFrom()) != null) {
            event.setCancelled(true);
        }
        if (event.isCancelled()) {
            return;
        }

        Entity playerVehicle = event.getPlayer().getVehicle();
        Portal portal = PortalHandler.getByEntrance(event.getFrom());
        if (playerVehicle != null && PortalHandler.getByEntrance(event.getFrom()) != null) {
            Portal destinationPortal = portal.getDestination();
            if (destinationPortal != null) {
                VehicleEventListener.teleportVehicleAfterPlayer((Vehicle) playerVehicle, destinationPortal, event.getPlayer());
                Stargate.log.info("Player was driving  " + playerVehicle.getName());
            }
        }
    }

    /**
     * This event handler detects if a player moves into a portal
     *
     * @param event <p>Player move event which was triggered</p>
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        //Check to see if the player moved to another block
        BlockLocation fromLocation = (BlockLocation) event.getFrom();
        BlockLocation toLocation = (BlockLocation) event.getTo();
        if (toLocation == null || fromLocation.equals(toLocation)) {
            return;
        }

        Player player = event.getPlayer();
        Portal entrancePortal = PortalHandler.getByEntrance(toLocation);
        Portal destination = entrancePortal.getDestination(player);

        //Decide if the anything stops the player from teleport
        if (!playerCanTeleport(entrancePortal, destination, player, event)) {
            return;
        }

        Stargate.sendMessage(player, Stargate.getString("teleportMsg"), false);

        //Decide if the user should be teleported to another bungee server
        if (entrancePortal.isBungee() && bungeeTeleport(player, entrancePortal, event)) {
            return;
        }
        destination.teleport(player, entrancePortal, event);
        entrancePortal.close(false);
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

    /**
     * Teleports a player to a bungee gate
     *
     * @param player         <p>The player to teleport</p>
     * @param entrancePortal <p>The gate the player is entering from</p>
     * @param event          <p>The event causing the teleportation</p>
     * @return <p>True if the teleportation was successful</p>
     */
    private boolean bungeeTeleport(Player player, Portal entrancePortal, PlayerMoveEvent event) {
        //Check if bungee is actually enabled
        if (!Stargate.enableBungee) {
            player.sendMessage(Stargate.getString("bungeeDisabled"));
            entrancePortal.close(false);
            return false;
        }

        //Teleport the player back to this gate, for sanity's sake
        entrancePortal.teleport(player, entrancePortal, event);

        //Send the SGBungee packet first, it will be queued by BC if required
        if (!BungeeHelper.sendTeleportationMessage(player, entrancePortal)) {
            return false;
        }

        // Connect player to new server
        if (!BungeeHelper.changeServer(player, entrancePortal)) {
            return false;
        }

        // Close portal if required (Should never be)
        entrancePortal.close(false);
        return true;
    }

    /**
     * Decide of the player can teleport through a portal
     *
     * @param entrancePortal <p>The portal the player is entering from</p>
     * @param destination    <p>The destination of the portal the player is inside</p>
     * @param player         <p>The player wanting to teleport</p>
     * @param event          <p>The move event causing the teleportation</p>
     * @return <p>True if the player can teleport. False otherwise</p>
     */
    private boolean playerCanTeleport(Portal entrancePortal, Portal destination, Player player, PlayerMoveEvent event) {
        // No portal or not open
        if (entrancePortal == null || !entrancePortal.isOpen()) {
            return false;
        }

        // Not open for this player
        if (!entrancePortal.isOpenFor(player)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            entrancePortal.teleport(player, entrancePortal, event);
            return false;
        }

        //No destination
        if (!entrancePortal.isBungee() && destination == null) {
            return false;
        }

        //Player cannot access portal
        if (!Stargate.canAccessPortal(player, entrancePortal, destination)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            entrancePortal.teleport(player, entrancePortal, event);
            entrancePortal.close(false);
            return false;
        }

        //Player cannot pay for teleportation
        int cost = Stargate.getUseCost(player, entrancePortal, destination);
        if (cost > 0) {
            if (!EconomyHelper.payTeleportFee(entrancePortal, player, cost)) {
                return false;
            }
        }
        return true;
    }

}
