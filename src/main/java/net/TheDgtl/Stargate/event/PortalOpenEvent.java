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
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PortalOpenEvent extends PortalEvent {
    private final Player player;
    private boolean isForced;

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PortalOpenEvent(Player player, @NotNull Portal portal, boolean isForced) {
        super(Objects.requireNonNull(portal));
        this.player = player;
        this.isForced = isForced;
    }

    /**
     * @return player that opened the gate
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    public boolean getIsForced() {
        return isForced;
    }

    public void setForce(boolean isForced) {
        this.isForced = isForced;
    }
}
