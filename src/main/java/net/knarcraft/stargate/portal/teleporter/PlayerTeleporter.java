package net.knarcraft.stargate.portal.teleporter;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.event.StargatePlayerPortalEvent;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.utility.DirectionHelper;
import net.knarcraft.stargate.utility.TeleportHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * The portal teleporter takes care of the actual portal teleportation for any players
 */
public class PlayerTeleporter extends Teleporter {

    private final Player player;

    /**
     * Instantiates a new player teleporter
     *
     * @param targetPortal <p>The portal which is the target of the teleportation</p>
     * @param player       <p>The teleporting player</p>
     */
    public PlayerTeleporter(Portal targetPortal, Player player) {
        super(targetPortal, player);
        this.player = player;
    }

    /**
     * Teleports a player to this teleporter's portal
     *
     * @param origin <p>The portal the player teleports from</p>
     * @param event  <p>The player move event triggering the event</p>
     */
    public void teleportPlayer(Portal origin, PlayerMoveEvent event) {
        double velocity = player.getVelocity().length();
        List<Entity> passengers = player.getPassengers();

        //Call the StargatePlayerPortalEvent to allow plugins to change destination
        if (!origin.equals(portal)) {
            exit = triggerPortalEvent(origin, new StargatePlayerPortalEvent(player, origin, portal, exit));
            if (exit == null) {
                return;
            }
        }

        //Load chunks to make sure not to teleport to the void
        loadChunks();

        //Teleport any creatures leashed by the player in a 15-block range
        TeleportHelper.teleportLeashedCreatures(player, origin, portal);

        if (player.eject()) {
            TeleportHelper.handleEntityPassengers(passengers, player, origin, portal, exit.getDirection());
        }

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

}
