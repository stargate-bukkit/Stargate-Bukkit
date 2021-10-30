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
import net.TheDgtl.Stargate.Channel;

public class StargateBungeePluginMessageListener implements PluginMessageListener {

	/**
	 * TODO
	 * - Send plugin enable message to all servers
	 * - Send all loaded bungeeportals to all servers
	 */
	public StargateBungeePluginMessageListener() {
		
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
		boolean usingBungee = (boolean) Stargate.getSetting(Setting.USING_BUNGEE);
		if (!usingBungee || !channel.equals("BungeeCord"))
			return;

		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subChannel = in.readUTF();
		switch (Channel.parse(subChannel)) {
		case GET_SERVER:
			Stargate.serverName = in.readUTF();
			Stargate.knowsServerName = (Stargate.serverName != null) && (!Stargate.serverName.isEmpty());
			break;
		case PLAYER_CONNECT:
			Stargate.log(Level.FINEST, "trying to read player join json msg");
			short length = in.readShort();
			byte[] buffer = new byte[length];
			in.readFully(buffer);
			JsonParser parser = new JsonParser();
			JsonObject json = (JsonObject) parser.parse(buffer.toString());
			String playerName = json.get("playerName").getAsString();
			String portalName = json.get("portalName").getAsString();
			String network = json.get("network").getAsString();
			boolean isPersonalNet = json.get("isPrivate").getAsBoolean();
			Stargate.addToQueue(playerName, portalName, network);
			break;
		case PLUGIN_ENABLE:
			break;
		case PLUGIN_DISABLE:
			break;
		case PLAYER_TELEPORT:
			break;
		default:
			Stargate.log(Level.FINEST, "Recieved unknown message with a subchannel: " + subChannel);
			break;
		}
    }
}
