package net.knarcraft.stargate.container;

import net.knarcraft.stargate.portal.Portal;
import org.jetbrains.annotations.NotNull;

/**
 * A request for updating a portal's control blocks
 *
 * @param portal <p>The portal to update the control blocks for</p>
 */
public record ControlBlockUpdateRequest(@NotNull Portal portal) {
}
