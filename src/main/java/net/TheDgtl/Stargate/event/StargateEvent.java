package net.TheDgtl.Stargate.event;
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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;

public abstract class StargateEvent extends Event implements Cancellable {
	// oldname = StargateEvent
    protected final IPortal portal;
    protected boolean cancelled;
    
    public StargateEvent(@NotNull IPortal portal) {
        this.portal = Objects.requireNonNull(portal);
        this.cancelled = false;
    }

    public IPortal getPortal() {
        return portal;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    //TODO temporary, this method should be abstract
	public abstract List<Permission> getRelatedPerms();
	
	protected List<Permission> compileFlagPerms(String permIdentifier){
		List<Permission> permList = new ArrayList<>();
		EnumSet<PortalFlag> flags = PortalFlag.parseFlags(portal.getAllFlagsString());
		for(PortalFlag flag : flags) {
			String identifier;
			switch(flag) {
			case FIXED:
				identifier = "fixed";
				break;
			case NETWORKED:
				identifier = "non-fixed";
				break;
			case PERSONAL_NETWORK:
			case IRON_DOOR:
				continue;
			default:
				identifier = String.valueOf(flag.label).toLowerCase();
				break;
			}
			permList.add( Permission.loadPermission(permIdentifier + ".type." + flag.label, null));
		}
		return permList;
	}
	
	protected Permission compileNetworkPerm(String permIdentifier) {
		if(portal.hasFlag(PortalFlag.PERSONAL_NETWORK))
			return Permission.loadPermission( permIdentifier + ".network.personal", null );
		if(portal.getNetwork().getName().equals((String)Stargate.getSetting(Setting.DEFAULT_NET)))
			return Permission.loadPermission( permIdentifier + ".network.default", null );
		Permission custom = new Permission( permIdentifier + ".network.custom." + portal.getNetwork().getName());
		Permission parrent = Permission.loadPermission(permIdentifier + ".network.custom", null);
		custom.addParent(parrent, true);
		return custom;
	}
	
	protected Permission compileWorldPerm(String permIdentifier) {
		Permission parrent = Permission.loadPermission(permIdentifier + ".world", null);
		String permNode = permIdentifier + ".world." + portal.getSignPos().getWorld().getName();
		Permission world = new Permission(permNode);
		world.addParent(parrent, true);
		return world;
	}
	
	protected Permission compileDesignPerm(String permIdentifier) {
		Permission parrent = Permission.loadPermission(permIdentifier + ".design", null);
		String permNode = permIdentifier + ".design." + portal.getDesignName();
		Permission design = new Permission(permNode);
		design.addParent(parrent, true);
		return design;
	}
	
	protected List<Permission> defaultPermCompile(String permIdentifier){
		List<Permission> permList = new ArrayList<>();
		permList.addAll(compileFlagPerms(permIdentifier));
		permList.add(compileWorldPerm(permIdentifier));
		permList.add(compileNetworkPerm(permIdentifier));
		permList.add(compileDesignPerm(permIdentifier));
		return permList;
	}
}
