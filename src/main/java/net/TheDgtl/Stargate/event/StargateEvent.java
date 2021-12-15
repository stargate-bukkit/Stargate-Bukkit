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

import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class StargateEvent extends Event implements Cancellable {
    // old name = StargateEvent
    protected final Portal portal;
    protected boolean cancelled;
    private final PluginManager pm;


    public StargateEvent(@NotNull Portal portal) {
        this.portal = Objects.requireNonNull(portal);
        this.cancelled = false;
        this.pm = Bukkit.getPluginManager();
    }

    public Portal getPortal() {
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

    public abstract List<Permission> getRelatedPerms();

    protected List<Permission> compileFlagPerms(String permIdentifier) {
        List<Permission> permList = new ArrayList<>();
        Set<PortalFlag> flags = PortalFlag.parseFlags(portal.getAllFlagsString());
        for (PortalFlag flag : flags) {
            String identifier;
            switch (flag) {
                case FIXED:
                case NETWORKED:
                case PERSONAL_NETWORK:
                case IRON_DOOR:
                    continue;
                default:
                    identifier = String.valueOf(flag.getCharacterRepresentation()).toLowerCase();
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
        if (portal.getNetwork().getName().equals(Settings.getString(Setting.DEFAULT_NETWORK)))
            return pm.getPermission(permIdentifier + ".network.default");
        Permission custom = new Permission(permIdentifier + ".network.custom." + portal.getNetwork().getName());
        Permission parent = pm.getPermission(permIdentifier + ".network.custom");
        if (parent != null) {
            custom.addParent(parent, true);
        }
        return custom;
    }

    protected Permission compileWorldPerm(String permissionIdentifier, Location loc) {
        Permission parent = pm.getPermission(permissionIdentifier + ".world");
        World world = loc.getWorld();
        if (world == null) {
            return null;
        }
        String permissionNode = permissionIdentifier + ".world." + world.getName();
        Permission worldPermission = new Permission(permissionNode);
        if (parent != null) {
            worldPermission.addParent(parent, true);
        }
        return worldPermission;
    }

    protected Permission compileDesignPerm(String permIdentifier) {
        Permission parent = pm.getPermission(permIdentifier + ".design");
        String permNode = permIdentifier + ".design." + portal.getDesignName();
        Permission design = new Permission(permNode);
        if (parent != null) {
            design.addParent(parent, true);
        }
        return design;
    }

    protected List<Permission> defaultPermCompile(String permIdentifier, String activatorUUID) {
        List<Permission> permList = new ArrayList<>(compileFlagPerms(permIdentifier));
        permList.add(compileWorldPerm(permIdentifier, portal.getSignLocation()));
        permList.add(compileNetworkPerm(permIdentifier, activatorUUID));
        permList.add(compileDesignPerm(permIdentifier));
        return permList;
    }
}
