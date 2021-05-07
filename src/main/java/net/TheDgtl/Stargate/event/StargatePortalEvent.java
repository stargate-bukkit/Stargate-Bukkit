/*
 * Stargate - A portal plugin for Bukkit
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.TheDgtl.Stargate.event;

import java.util.Objects;
import net.TheDgtl.Stargate.Portal;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StargatePortalEvent extends StargateEvent {
	/*
	 * An event which occurs every time players teleport? 
	 * (should be called stargateteleportevent)
	 */
	
	
    private final Player player;
    private final Portal destination;
    private Location exit;

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public StargatePortalEvent(@NotNull Player player, @NotNull Portal portal, @NotNull Portal dest, @NotNull Location exit) {
        super(Objects.requireNonNull(portal));

        this.player = Objects.requireNonNull(player);
        this.destination = Objects.requireNonNull(dest);
        this.exit = Objects.requireNonNull(exit);
    }

    /**
     * @return player that went through the gate
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * @return destination gate
     */
    @NotNull
    public Portal getDestination() {
        return destination;
    }

    /**
     * @return org.bukkit.Location Location players exit point
     */
    @NotNull
    public Location getExit() {
        return exit;
    }

    /**
     * 
     * @param exitLocation
     */
    public void setExit(@NotNull Location exitLocation) {
        this.exit = Objects.requireNonNull(exitLocation);
    }
}
