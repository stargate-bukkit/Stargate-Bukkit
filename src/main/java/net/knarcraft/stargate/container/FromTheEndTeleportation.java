package net.knarcraft.stargate.container;

import net.knarcraft.stargate.portal.Portal;
import org.bukkit.entity.Player;

/**
 * This class represents a player teleporting from the end to the over-world using an artificial end portal
 *
 * <p>This is necessary because a player entering an end portal in the end is a special case. Instead of being
 * teleported, the player is respawned. Because of this, the teleportation needs to be saved and later used to hijack
 * the position of where the player is to respawn.</p>
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

    @Override
    public int hashCode() {
        return teleportingPlayer.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof FromTheEndTeleportation otherTeleportation)) {
            return false;
        }
        return teleportingPlayer.equals(otherTeleportation.teleportingPlayer);
    }

}
