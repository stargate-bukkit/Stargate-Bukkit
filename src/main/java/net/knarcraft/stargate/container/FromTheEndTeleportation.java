package net.knarcraft.stargate.container;

import net.knarcraft.stargate.portal.Portal;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents a teleportation from the end to the over-world using an artificial end portal
 *
 * <p>This is necessary because a player entering an end portal in the end is a special case. Instead of being
 * teleported, the player is respawned. Because of this, the teleportation needs to be saved and later used to hijack
 * the position of where the player is to respawn.</p>
 *
 * @param exitPortal <p>The portal the player should exit from when arriving in the over-world</p>
 */
public record FromTheEndTeleportation(@NotNull Portal exitPortal) {
}
