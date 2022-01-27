package net.knarcraft.stargate.portal.teleporter;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.event.StargatePlayerPortalEvent;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.utility.DirectionHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

/**
 * The portal teleporter takes care of the actual portal teleportation for any players
 */
public class PlayerTeleporter extends Teleporter {

    private final Player player;

    /**
     * Instantiates a new player teleporter
     *
     * @param portal <p>The portal which is the target of the teleportation</p>
     * @param player <p>The teleporting player</p>
     */
    public PlayerTeleporter(Portal portal, Player player) {
        super(portal);
        this.player = player;
    }

    /**
     * Teleports a player to this teleporter's portal
     *
     * @param origin <p>The portal the player teleports from</p>
     * @param event  <p>The player move event triggering the event</p>
     */
    public void teleport(Portal origin, PlayerMoveEvent event) {
        double velocity = player.getVelocity().length();
        Location traveller = player.getLocation();
        Location exit = getExit(player, traveller);

        //Rotate the player to face out from the portal
        adjustRotation(exit);

        //Call the StargatePlayerPortalEvent to allow plugins to change destination
        if (!origin.equals(portal)) {
            exit = triggerPlayerPortalEvent(origin, exit, event);
            if (exit == null) {
                return;
            }
        }

        //Load chunks to make sure not to teleport to the void
        loadChunks();

        //Teleport any creatures leashed by the player in a 15-block range
        teleportLeashedCreatures(player, origin);

        //If no event is passed in, assume it's a teleport, and act as such
        if (event == null) {
            player.teleport(exit);
        } else {
            //Set the exit location of the event
            event.setTo(exit);
        }

        //Set the velocity of the teleported player after the teleportation is finished
        Bukkit.getScheduler().scheduleSyncDelayedTask(Stargate.getInstance(), () -> {
            Vector newVelocityDirection = DirectionHelper.getDirectionVectorFromYaw(portal.getYaw());
            Vector newVelocity = newVelocityDirection.multiply(velocity * Stargate.getGateConfig().getExitVelocity());
            player.setVelocity(newVelocity);
        }, 1);
    }

    /**
     * Triggers the player portal event to allow plugins to change the exit location
     *
     * @param origin <p>The origin portal teleported from</p>
     * @param exit   <p>The exit location to teleport the player to</p>
     * @param event  <p>The player move event which triggered the teleportation</p>
     * @return <p>The location the player should be teleported to, or null if the event was cancelled</p>
     */
    private Location triggerPlayerPortalEvent(Portal origin, Location exit, PlayerMoveEvent event) {
        StargatePlayerPortalEvent stargatePlayerPortalEvent = new StargatePlayerPortalEvent(player, origin, portal, exit);
        Stargate.getInstance().getServer().getPluginManager().callEvent(stargatePlayerPortalEvent);
        //Teleport is cancelled. Teleport the player back to where it came from
        if (stargatePlayerPortalEvent.isCancelled()) {
            new PlayerTeleporter(origin, player).teleport(origin, event);
            return null;
        }
        return stargatePlayerPortalEvent.getExit();
    }

}
