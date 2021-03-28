package net.TheDgtl.Stargate;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import net.TheDgtl.Stargate.event.StargateAccessEvent;
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

/**
 * @author Thorin <-- refactored by
 */
public class PermissionManager {

    static public Server server;
	/**
     * Check whether the player has the given permissions.
     */
    public static boolean hasPerm(Player player, String perm) {
        Stargate.permDebug("hasPerm::SuperPerm(" + player.getName() + ")", perm + " => " + player.hasPermission(perm));
        return player.hasPermission(perm);
    }

    /**
     * Check a deep permission, this will check to see if the permissions is defined for this use
     * If using Permissions it will return the same as hasPerm
     * If using SuperPerms will return true if the node isn't defined
     * Or the value of the node if it is
     */
    public static boolean hasPermDeep(Player player, String perm) {
        if (!player.isPermissionSet(perm)) {
            Stargate.permDebug("hasPermDeep::SuperPerm", perm + " => true");
            return true;
        }
        Stargate.permDebug("hasPermDeep::SuperPerms", perm + " => " + player.hasPermission(perm));
        return player.hasPermission(perm);
    }

    /**
     * Check whether player can teleport to dest world
     */
    public static boolean canAccessWorld(Player player, String world) {
        // Can use all Stargate player features or access all worlds
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.world")) {
            // Do a deep check to see if the player lacks this specific world node
            return hasPermDeep(player, "stargate.world." + world);
        }
        // Can access dest world
        return hasPerm(player, "stargate.world." + world);
    }

    /**
     * Check whether player can use network
     */
    public static boolean canAccessNetwork(Player player, String network) {
        // Can user all Stargate player features, or access all networks
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.network")) {
            // Do a deep check to see if the player lacks this specific network node
            return hasPermDeep(player, "stargate.network." + network);
        }
        // Can access this network
        if (hasPerm(player, "stargate.network." + network)) return true;
        // Is able to create personal gates (Assumption is made they can also access them)
        String playerName = player.getName();
        if (playerName.length() > 11) playerName = playerName.substring(0, 11);
        return network.equals(playerName) && hasPerm(player, "stargate.create.personal");
    }

    /**
     * Check whether the player can access this server
     */
    public static boolean canAccessServer(Player player, String server) {
        // Can user all Stargate player features, or access all servers
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.servers")) {
            // Do a deep check to see if the player lacks this specific server node
            return hasPermDeep(player, "stargate.server." + server);
        }
        // Can access this server
        return hasPerm(player, "stargate.server." + server);
    }

    /**
     * Call the StargateAccessPortal event, used for other plugins to bypass Permissions checks
     */
    public static boolean canAccessPortal(Player player, Portal portal, boolean deny) {
        StargateAccessEvent event = new StargateAccessEvent(player, portal, deny);
        server.getPluginManager().callEvent(event);
        return !event.getDeny();
    }
}
