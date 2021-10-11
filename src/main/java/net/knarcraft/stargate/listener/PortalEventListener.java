package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.container.TwoTuple;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
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

    private static final List<TwoTuple<Player, Portal>> playersFromTheEnd = new ArrayList<>();

    /**
     * Listen for and abort vanilla portal creation caused by stargate creation
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
        //Block normal portal teleportation if teleporting from a stargate
        if (entity instanceof Player && location.getBlock().getType() == Material.END_PORTAL && world != null &&
                world.getEnvironment() == World.Environment.THE_END) {
            Portal portal = PortalHandler.getByAdjacentEntrance(location);
            if (portal != null) {
                playersFromTheEnd.add(new TwoTuple<>((Player) entity, portal.getDestination()));
            }
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
        playersFromTheEnd.forEach((tuple) -> {
            //Check if player is actually teleporting from the end
            if (tuple.getFirstValue() == respawningPlayer) {
                Portal exitPortal = tuple.getSecondValue();
                //Need to make sure the player is allowed to exit from the portal
                if (!exitPortal.isOpenFor(respawningPlayer)) {
                    return;
                }
                //Overwrite respawn location to respawn in front of the portal
                event.setRespawnLocation(exitPortal.getExit(respawningPlayer, respawningPlayer.getLocation()));
                //Properly close the portal to prevent it from staying in a locked state until it times out
                exitPortal.close(false);
            }
        });
    }

}
