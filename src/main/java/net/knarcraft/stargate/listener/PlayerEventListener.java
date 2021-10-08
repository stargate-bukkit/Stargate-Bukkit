package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.utility.BungeeHelper;
import net.knarcraft.stargate.utility.EconomyHandler;
import net.knarcraft.stargate.utility.EconomyHelper;
import net.knarcraft.stargate.utility.MaterialHelper;
import net.knarcraft.stargate.utility.PermissionHelper;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

/**
 * This listener listens to any player-related events related to stargates
 */
@SuppressWarnings("unused")
public class PlayerEventListener implements Listener {

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

        Portal portal = PortalHandler.getBungeePortal(destination);
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
    }

    /**
     * This event handler detects if a player moves into a portal
     *
     * @param event <p>The player move event which was triggered</p>
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled() || event.getTo() == null) {
            return;
        }

        BlockLocation fromLocation = new BlockLocation(event.getFrom().getBlock());
        BlockLocation toLocation = new BlockLocation(event.getTo().getBlock());
        Player player = event.getPlayer();

        //Check whether the event needs to be considered
        if (!isRelevantMoveEvent(event, player, fromLocation, toLocation)) {
            return;
        }
        Portal entrancePortal = PortalHandler.getByEntrance(toLocation);
        Portal destination = entrancePortal.getDestination(player);

        //Teleport the vehicle to the player
        Entity playerVehicle = player.getVehicle();
        if (playerVehicle != null && !(playerVehicle instanceof Boat) && !(playerVehicle instanceof RideableMinecart)) {

            //Make sure the horse can be sat on
            if (playerVehicle instanceof AbstractHorse) {
                AbstractHorse horse = ((AbstractHorse) playerVehicle);
                if (!horse.isTamed()) {
                    horse.setOwner(player);
                }
            }
            destination.teleport((Vehicle) playerVehicle, entrancePortal);
        } else {
            destination.teleport(player, entrancePortal, event);
        }
        Stargate.sendSuccessMessage(player, Stargate.getString("teleportMsg"));
        entrancePortal.close(false);
    }

    /**
     * Checks whether a player move event is relevant for this plugin
     *
     * @param event        <p>The player move event to check</p>
     * @param player       <p>The player which moved</p>
     * @param fromLocation <p>The location the player is moving from</p>
     * @param toLocation   <p>The location the player is moving to</p>
     * @return <p>True if the event is relevant</p>
     */
    private boolean isRelevantMoveEvent(PlayerMoveEvent event, Player player, BlockLocation fromLocation, BlockLocation toLocation) {
        //Check to see if the player moved to another block
        if (fromLocation.equals(toLocation)) {
            return false;
        }

        //Check if the player moved from a portal
        Portal entrancePortal = PortalHandler.getByEntrance(toLocation);
        if (entrancePortal == null) {
            return false;
        }

        Portal destination = entrancePortal.getDestination(player);

        //Decide if the anything stops the player from teleport
        if (!playerCanTeleport(entrancePortal, destination, player, event)) {
            return false;
        }

        //Decide if the user should be teleported to another bungee server
        if (entrancePortal.getOptions().isBungee()) {
            if (bungeeTeleport(player, entrancePortal, event)) {
                Stargate.sendSuccessMessage(player, Stargate.getString("teleportMsg"));
            }
            return false;
        }
        return true;
    }

    /**
     * This event handler detects if a player clicks a button or a sign
     *
     * @param event <p>The player interact event which was triggered</p>
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleRightClickBlock(event, player, block);
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && block.getBlockData() instanceof WallSign) {
            //Handle left click of a wall sign
            handleSignClick(event, player, block, true);
        }
    }

    /**
     * This method handles left- or right-clicking of a sign
     *
     * @param event     <p>The event causing the click</p>
     * @param player    <p>The player clicking the sign</p>
     * @param block     <p>The block that was clicked</p>
     * @param leftClick <p>Whether the player performed a left click as opposed to a right click</p>
     */
    private void handleSignClick(PlayerInteractEvent event, Player player, Block block, boolean leftClick) {
        Portal portal = PortalHandler.getByBlock(block);
        if (portal == null) {
            return;
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        if (leftClick) {
            //Cancel event in creative mode to prevent breaking the sign
            if (player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        } else {
            //Prevent usage of item in the player's hand (placing block and such)
            event.setUseItemInHand(Event.Result.DENY);
        }

        //Check if the user can use the portal
        if (cannotAccessPortal(player, portal)) {
            return;
        }

        //Cycle portal destination
        if ((!portal.isOpen()) && (!portal.getOptions().isFixed())) {
            if (leftClick) {
                portal.cycleDestination(player, -1);
            } else {
                portal.cycleDestination(player);
            }
        }
    }

    /**
     * Check if a player should be denied from accessing (using) a portal
     *
     * @param player <p>The player trying to access the portal</p>
     * @param portal <p>The portal the player is trying to use</p>
     * @return <p>True if the player should be denied</p>
     */
    private boolean cannotAccessPortal(Player player, Portal portal) {
        boolean deny = PermissionHelper.cannotAccessNetwork(player, portal.getNetwork());

        if (PermissionHelper.cannotAccessPortal(player, portal, deny)) {
            Stargate.sendErrorMessage(player, Stargate.getString("denyMsg"));
            return true;
        }
        return false;
    }

    /**
     * This method handles right clicking of a sign or button belonging to a stargate
     *
     * @param event  <p>The event triggering the right-click</p>
     * @param player <p>The player doing the right-click</p>
     * @param block  <p>The block the player clicked</p>
     */
    private void handleRightClickBlock(PlayerInteractEvent event, Player player, Block block) {
        if (block.getBlockData() instanceof WallSign) {
            handleSignClick(event, player, block, false);
            return;
        }

        // Implement right-click to toggle a stargate, gets around spawn protection problem.
        if (MaterialHelper.isButtonCompatible(block.getType())) {
            //Prevent a double click caused by a Spigot bug
            if (clickIsBug(event, block)) {
                return;
            }

            Portal portal = PortalHandler.getByBlock(block);
            if (portal == null) {
                return;
            }

            // Cancel item use
            event.setUseItemInHand(Event.Result.DENY);
            event.setUseInteractedBlock(Event.Result.DENY);

            //Check if the user can use the portal
            if (cannotAccessPortal(player, portal)) {
                return;
            }

            PermissionHelper.openPortal(player, portal);
            if (portal.isOpenFor(player)) {
                event.setUseInteractedBlock(Event.Result.ALLOW);
            }
        }
    }

    /**
     * This function decides if a right click of a coral is caused by a Spigot bug
     *
     * <p>The Spigot bug currently makes every right click of a coral trigger twice, causing the portal to close
     * immediately. This fix should detect the bug without breaking wall coral buttons once the bug is fixed.</p>
     *
     * @param event <p>The event causing the right click</p>
     * @param block <p>The block to check</p>
     * @return <p>True if the click is a bug and should be cancelled</p>
     */
    private boolean clickIsBug(PlayerInteractEvent event, Block block) {
        if (MaterialHelper.isWallCoral(block.getType())) {
            if (previousEvent != null &&
                    event.getPlayer() == previousEvent.getPlayer() && eventTime + 15 > System.currentTimeMillis()) {
                previousEvent = null;
                eventTime = 0;
                return true;
            }
            previousEvent = event;
            eventTime = System.currentTimeMillis();
        }
        return false;
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
            Stargate.sendErrorMessage(player, Stargate.getString("bungeeDisabled"));
            entrancePortal.close(false);
            return false;
        }

        //Teleport the player back to this gate, for sanity's sake
        entrancePortal.teleport(player, entrancePortal, event);

        //Send the SGBungee packet first, it will be queued by BC if required
        if (!BungeeHelper.sendTeleportationMessage(player, entrancePortal)) {
            Stargate.debug("bungeeTeleport", "Unable to send teleportation message");
            return false;
        }

        // Connect player to new server
        if (!BungeeHelper.changeServer(player, entrancePortal)) {
            Stargate.debug("bungeeTeleport", "Unable to change server");
            return false;
        }

        // Close portal if required (Should never be)
        Stargate.debug("bungeeTeleport", "Teleported player to another server");
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
            Stargate.sendErrorMessage(player, Stargate.getString("denyMsg"));
            entrancePortal.teleport(player, entrancePortal, event);
            return false;
        }

        //No destination
        if (!entrancePortal.getOptions().isBungee() && destination == null) {
            return false;
        }

        //Player cannot access portal
        if (PermissionHelper.cannotAccessPortal(player, entrancePortal, destination)) {
            Stargate.sendErrorMessage(player, Stargate.getString("denyMsg"));
            entrancePortal.teleport(player, entrancePortal, event);
            entrancePortal.close(false);
            return false;
        }

        //Player cannot pay for teleportation
        int cost = EconomyHandler.getUseCost(player, entrancePortal, destination);
        if (cost > 0) {
            return EconomyHelper.payTeleportFee(entrancePortal, player, cost);
        }
        return true;
    }

}
