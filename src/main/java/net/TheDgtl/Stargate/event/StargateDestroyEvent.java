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

import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Stargate - A portal plugin for Bukkit
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class StargateDestroyEvent extends StargateEvent {
    // old name = StargateDestroyEvent
    private final Player player;
    private boolean deny;
    private String denyReason;
    private int cost;

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public StargateDestroyEvent(@NotNull IPortal portal, @NotNull Player player, int cost) {
        super(Objects.requireNonNull(portal));
        this.player = Objects.requireNonNull(player);
        this.cost = cost;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    public boolean getDeny() {
        return deny;
    }

    public void setDeny(boolean deny) {
        this.deny = deny;
    }

    @NotNull
    public String getDenyReason() {
        return denyReason;
    }

    public void setDenyReason(@NotNull String denyReason) {
        this.denyReason = Objects.requireNonNull(denyReason);
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    @Override
    public List<Permission> getRelatedPerms() {
        String identifier = "sg.destroy";
        List<Permission> permList = super.defaultPermCompile(identifier, player.getUniqueId().toString());
        if(portal.hasFlag(PortalFlag.PERSONAL_NETWORK) && !player.getUniqueId().equals(portal.getOwnerUUID())) {
            permList.add(Bukkit.getPluginManager().getPermission("sg.admin.bypass.private"));
        }
        return permList;
    }

}
