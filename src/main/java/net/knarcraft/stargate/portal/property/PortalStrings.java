package net.knarcraft.stargate.portal.property;

import org.jetbrains.annotations.NotNull;

/**
 * A record of a portal's string values
 *
 * @param name        <p>The name of the portal</p>
 * @param network     <p>The name of the network the portal belongs to</p>
 * @param destination <p>The name of the portal's destination</p>
 */
public record PortalStrings(@NotNull String name, @NotNull String network, @NotNull String destination) {
}
