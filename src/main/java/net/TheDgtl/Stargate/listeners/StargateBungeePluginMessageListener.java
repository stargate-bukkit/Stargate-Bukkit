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
import java.io.InvalidClassException;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.Channel;

public class StargateBungeePluginMessageListener implements PluginMessageListener {

	Stargate stargate;
	/**
	 * TODO
	 * - Send plugin enable message to all servers
	 * - Send all loaded bungeeportals to all servers
	 */
	public StargateBungeePluginMessageListener(Stargate stargate) {
		this.stargate = stargate;
	}
	
	/**
	 * Types of messages that can be received and their response
	 * - All loadeded portals messages - add all loaded portals as "virtual portals"
	 * - Plugin enabled message - send all loaded portals message to specific server
	 * - portal destroyed message - remove virtual portal from specific network
	 * - portal added message - add virtual portal from specific network
	 * - plugin disable message - remove all virtual portals given in message
	 * - portal open message - open selected portal. Too much ?
	 */
	@Override
	public void onPluginMessageReceived(@NotNull String channel, @NotNull Player unused, byte[] message) {
		Stargate.log(Level.FINEST, "Recieved pluginmessage");
		
		boolean usingBungee = (boolean) Stargate.getSetting(Setting.USING_BUNGEE);
		if (!usingBungee || !channel.equals("BungeeCord"))
			return;

		try {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
			String subChannel = in.readUTF();
			switch (Channel.parse(subChannel)) {
			case GET_SERVER:
				Stargate.serverName = in.readUTF();
				Stargate.knowsServerName = (Stargate.serverName != null) && (!Stargate.serverName.isEmpty());
				break;
			case PLAYER_CONNECT:
				break;
			case PLUGIN_ENABLE:
				break;
			case PLUGIN_DISABLE:
				break;
			case PLAYER_TELEPORT:
				Stargate.log(Level.FINEST, "trying to read player join json msg");
				String msg = in.readUTF();
				playerConnect(msg);
				break;
			default:
				Stargate.log(Level.FINEST, "Recieved unknown message with a subchannel: " + subChannel);
				break;
			}
		} catch (IOException ex) {
			Stargate.log(Level.SEVERE,"[Stargate] Error receiving BungeeCord message");
            ex.printStackTrace();
            return;
        }
		
    }
	
	private void playerConnect(String msg) {
		JsonParser parser = new JsonParser();
		Stargate.log(Level.FINEST, msg);
		JsonObject json = (JsonObject) parser.parse(msg);
		String playerName = json.get("playerName").getAsString();
		Stargate.log(Level.FINEST, "playerName= " + playerName);
		
		String portalName = json.get("portalName").getAsString();
		Stargate.log(Level.FINEST, "portalName= " + portalName);
		
		String network = json.get("network").getAsString();
		Stargate.log(Level.FINEST, "network= " + network);
		
		Player player = stargate.getServer().getPlayer(playerName);
		if(player == null) {
			Stargate.log(Level.FINEST, "Player was null; adding to queue");
			Stargate.addToQueue(playerName, portalName, network);
		}
		else {
			Stargate.log(Level.FINEST, "Player was not null; trying to teleport");
			IPortal dest = Stargate.factory.getNetwork(network, true).getPortal(portalName);
			dest.teleportHere(player);
		}
	}
}

