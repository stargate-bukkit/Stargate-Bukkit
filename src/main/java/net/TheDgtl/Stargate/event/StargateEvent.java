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

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public abstract class StargateEvent extends Event implements Cancellable {
    // old name = StargateEvent
    protected final IPortal portal;
    protected boolean cancelled;
    private final PluginManager pm;


    public StargateEvent(@NotNull IPortal portal) {
        this.portal = Objects.requireNonNull(portal);
        this.cancelled = false;
        this.pm = Bukkit.getPluginManager();
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

    protected List<Permission> compileFlagPerms(String permIdentifier) {
        List<Permission> permList = new ArrayList<>();
        EnumSet<PortalFlag> flags = PortalFlag.parseFlags(portal.getAllFlagsString());
        for (PortalFlag flag : flags) {
            String identifier;
            switch (flag) {
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
            permList.add(pm.getPermission(permIdentifier + ".type." + identifier));
        }
        return permList;
    }

    protected Permission compileNetworkPerm(String permIdentifier, String activator) {
        if (portal.hasFlag(PortalFlag.PERSONAL_NETWORK)) {
            return pm.getPermission(permIdentifier + ".network.personal");
        }
        if (portal.getNetwork().getName().equals(Setting.getString(Setting.DEFAULT_NET)))
            return pm.getPermission(permIdentifier + ".network.default");
        Permission custom = new Permission(permIdentifier + ".network.custom." + portal.getNetwork().getName());
        Permission parent = pm.getPermission(permIdentifier + ".network.custom");
        custom.addParent(parent, true);
        return custom;
    }

    protected Permission compileWorldPerm(String permIdentifier) {
        Permission parent = pm.getPermission(permIdentifier + ".world");
        String permNode = permIdentifier + ".world." + portal.getSignPos().getWorld().getName();
        Permission world = new Permission(permNode);
        world.addParent(parent, true);
        return world;
    }

    protected Permission compileDesignPerm(String permIdentifier) {
        Permission parent = pm.getPermission(permIdentifier + ".design");
        String permNode = permIdentifier + ".design." + portal.getDesignName();
        Permission design = new Permission(permNode);
        design.addParent(parent, true);
        return design;
    }

    protected List<Permission> defaultPermCompile(String permIdentifier, String activatorUUID) {
        List<Permission> permList = new ArrayList<>(compileFlagPerms(permIdentifier));
        permList.add(compileWorldPerm(permIdentifier));
        permList.add(compileNetworkPerm(permIdentifier, activatorUUID));
        permList.add(compileDesignPerm(permIdentifier));
        return permList;
    }
}
