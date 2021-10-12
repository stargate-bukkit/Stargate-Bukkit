package net.knarcraft.stargate.container;

import net.knarcraft.stargate.portal.Portal;
import org.bukkit.entity.Player;

/**
 * This class represents a player teleporting from the end to the over-world using an artificial end portal
 */
public class FromTheEndTeleportation {

    private final Player teleportingPlayer;
    private final Portal exitPortal;

    /**
     * Instantiates a new teleportation from the end
     *
     * @param teleportingPlayer <p>The teleporting player</p>
     * @param exitPortal        <p>The portal to exit from</p>
     */
    public FromTheEndTeleportation(Player teleportingPlayer, Portal exitPortal) {
        this.teleportingPlayer = teleportingPlayer;
        this.exitPortal = exitPortal;
    }

    /**
     * Gets the teleporting player
     *
     * @return <p>The teleporting player</p>
     */
    public Player getPlayer() {
        return this.teleportingPlayer;
    }

    /**
     * Gets the portal to exit from
     *
     * @return <p>The portal to exit from</p>
     */
    public Portal getExit() {
        return this.exitPortal;
    }

}
