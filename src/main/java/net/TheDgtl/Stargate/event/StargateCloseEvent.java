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

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import net.TheDgtl.portal.Portal;

public class StargateCloseEvent extends StargateEvent {
    private boolean force;
    //TODO add player?

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public StargateCloseEvent(@NotNull Portal portal, boolean force) {
        super(Objects.requireNonNull(portal));
        this.force = force;
    }

    public boolean getForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}