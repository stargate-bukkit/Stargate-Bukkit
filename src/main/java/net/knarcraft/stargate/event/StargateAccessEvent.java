package net.knarcraft.stargate.event;

import net.knarcraft.stargate.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/*
 * stargate - A portal plugin for Bukkit
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 * Copyright (C) 2021 Kristian Knarvik
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


@SuppressWarnings("unused")
public class StargateAccessEvent extends StargateEvent {

	private final Player player;
	private boolean deny;
	
	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public StargateAccessEvent(Player player, Portal portal, boolean deny) {
		super("StargateAccessEvent", portal);
		
		this.player = player;
		this.deny = deny;
	}

    /**
     * Gets whether the player should be denied access
     * @return <p>Whether the player should be denied access</p>
     */
	public boolean getDeny() {
		return this.deny;
	}

    /**
     * Sets whether to deny the player
     * @param deny <p>Whether to deny the player</p>
     */
	public void setDeny(boolean deny) {
		this.deny = deny;
	}
	
	public Player getPlayer() {
		return this.player;
	}

}
