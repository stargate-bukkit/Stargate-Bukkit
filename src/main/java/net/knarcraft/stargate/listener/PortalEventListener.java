package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.container.FromTheEndTeleportation;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.PortalTeleporter;
import net.knarcraft.stargate.utility.PermissionHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Listens for and cancels relevant portal events
 */
public class PortalEventListener implements Listener {

    private static final List<FromTheEndTeleportation> playersFromTheEnd = new ArrayList<>();

    /**
     * Listens for and aborts vanilla portal creation caused by stargate creation
     *
     * @param event <p>The triggered event</p>
     */
    @EventHandler
    public void onPortalCreation(PortalCreateEvent event) {
        if (event.isCancelled()) {
            return;
        }
        //Cancel nether portal creation when the portal is a StarGate portal
        for (BlockState block : event.getBlocks()) {
            if (PortalHandler.getByBlock(block.getBlock()) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Listen for entities entering an artificial end portal
     *
     * @param event <p>The triggered event</p>
     */
    @EventHandler
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        Location location = event.getLocation();
        World world = location.getWorld();
        Entity entity = event.getEntity();
        //Hijack normal portal teleportation if teleporting from a stargate
        if (entity instanceof Player player && location.getBlock().getType() == Material.END_PORTAL && world != null &&
                world.getEnvironment() == World.Environment.THE_END) {
            Portal portal = PortalHandler.getByAdjacentEntrance(location);

            //Remove any old player teleportations in case weird things happen
            playersFromTheEnd.removeIf((teleportation -> teleportation.getPlayer() == player));
            //Decide if the anything stops the player from teleporting
            if (PermissionHelper.playerCannotTeleport(portal, portal.getDestination(), player, null)) {
                //Teleport the player back to the portal they came in, just in case
                playersFromTheEnd.add(new FromTheEndTeleportation(player, portal));
            }
            playersFromTheEnd.add(new FromTheEndTeleportation(player, portal.getDestination()));
        }
    }

    /**
     * Listen for the respawn event to catch players teleporting from the end in an artificial end portal
     *
     * @param event <p>The triggered event</p>
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player respawningPlayer = event.getPlayer();
        int playerIndex = playersFromTheEnd.indexOf(new FromTheEndTeleportation(respawningPlayer, null));
        if (playerIndex == -1) {
            return;
        }
        FromTheEndTeleportation teleportation = playersFromTheEnd.get(playerIndex);
        playersFromTheEnd.remove(playerIndex);

        Portal exitPortal = teleportation.getExit();
        //Overwrite respawn location to respawn in front of the portal
        event.setRespawnLocation(new PortalTeleporter(exitPortal).getExit(respawningPlayer,
                respawningPlayer.getLocation()));
        //Properly close the portal to prevent it from staying in a locked state until it times out
        exitPortal.close(false);
    }

}
