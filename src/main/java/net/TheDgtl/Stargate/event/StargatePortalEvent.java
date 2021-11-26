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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;

/**
 * Gets thrown whenever a player teleports. Should honestly be called StargateTeleportEvent, but unfortunatly
 * is not because of legacy.
 * @author Thorin
 *
 */
public class StargatePortalEvent extends StargateEvent {
	/*
	 * An event which occurs every time players teleport? 
	 */
	
	
    private final Entity target;
    private final IPortal destination;
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

    public StargatePortalEvent(@NotNull Entity target, @NotNull IPortal portal) {
        super(Objects.requireNonNull(portal));

        this.target = Objects.requireNonNull(target);
        this.destination = ((Portal)portal).loadDestination();
        if(destination instanceof Portal)
        	this.exit = ((Portal)destination).getExit();
    }

    /**
     * @return player that went through the gate
     */
    @NotNull
    @Deprecated
    public Player getPlayer() {
    	if(target instanceof Player)
    		return (Player)target;
    	return null;
    }

    public Entity getEntity() {
    	return target;
    }
    
    /**
     * @return destination gate
     */
    @NotNull
    public IPortal getDestination() {
        return destination;
    }

    /**
     * @return org.bukkit.Location Location players exit point
     */
    @NotNull
    public Location getExit() {
    	if(destination instanceof Portal)
    		return ((Portal)destination).getExit();
        return null;
    }

    /**
     * 
     * @param exitLocation
     */
    public void setExit(@NotNull Location exitLocation) {
        this.exit = Objects.requireNonNull(exitLocation);
    }

	@Override
	public List<Permission> getRelatedPerms() {
		String identifier = "sg.use";
		List<Permission> permList = new ArrayList<>();
		if(target instanceof Player && !portal.isOpenFor(target)) {
			permList.add(Permission.loadPermission(identifier + ".follow", null));
		}
		return permList;
	}
}
