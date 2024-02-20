package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.portal.PortalHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This listener listens to teleportation-related events
 */
@SuppressWarnings("unused")
public class TeleportEventListener implements Listener {

    /**
     * This event handler handles some special teleportation events
     *
     * <p>This event cancels nether portal, end gateway and end portal teleportation if the user teleported from a
     * stargate entrance. This prevents the user from just teleporting to the nether or the end with portals using
     * the special teleportation blocks.</p>
     *
     * @param event <p>The event to check and possibly cancel</p>
     */
    @EventHandler
    public void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        PlayerTeleportEvent.TeleportCause cause = event.getCause();

        //Block normal portal teleportation if teleporting from a stargate
        if (!event.isCancelled() && (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ||
                cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY ||
                cause == PlayerTeleportEvent.TeleportCause.END_PORTAL)
                && PortalHandler.getByAdjacentEntrance(event.getFrom()) != null) {
            event.setCancelled(true);
        }
    }

}
