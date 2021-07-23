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
package net.TheDgtl.Stargate.listeners;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.portal.Network;
import net.TheDgtl.Stargate.portal.Portal;

public class StargateBungeePluginMessageListener implements PluginMessageListener {
	
	static boolean IS_ENABLE_BUNGEE = true;
	
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player unused, byte[] message) {
        if (!IS_ENABLE_BUNGEE || !channel.equals("BungeeCord")) return;

        // Get data from message
        String inChannel;
        byte[] data;
        Stargate stargate = JavaPlugin.getPlugin(Stargate.class);
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            inChannel = in.readUTF();
            short len = in.readShort();
            data = new byte[len];
            in.readFully(data);
        } catch (IOException ex) {
            Stargate.log(Level.SEVERE, "Error receiving BungeeCord message");
            ex.printStackTrace();
            return;
        }

        // Verify that it's an SGBungee packet
        if (!inChannel.equals("SGBungee")) {
            return;
        }

        // Data should be player name, and destination gate name
        String msg = new String(data);
        String[] parts = msg.split("#@#");

        String playerName = parts[0];
        String destination = parts[1];

        // Check if the player is online, if so, teleport, otherwise, queue
        Player player = stargate.getServer().getPlayer(playerName);
        if (player == null) {
        	Stargate.addItemToBungeeQueue(playerName.toLowerCase(), destination);
        } else {
            Portal dest = Network.getBungeePortal(destination);
            // Specified an invalid gate. For now we'll just let them connect at their current location
            if (dest == null) {
                Stargate.log(Level.INFO,"Bungee gate " + destination + " does not exist");
                return;
            }
            dest.teleportToExit(player);
        }
    }
}
