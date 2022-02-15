package net.knarcraft.stargate.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;

/**
 * A generic teleportation event
 */
public interface StargateTeleportEvent extends Cancellable {

    /**
     * Return the location of the players exit point
     *
     * @return <p>Location of the exit point</p>
     */
    Location getExit();

}
