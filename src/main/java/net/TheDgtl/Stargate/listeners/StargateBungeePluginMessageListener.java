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


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.TheDgtl.Stargate.Channel;
import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateProtocolProperty;
import net.TheDgtl.Stargate.StargateProtocolRequestType;
import net.TheDgtl.Stargate.network.InterserverNetwork;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Deals with bungee plugin messages
 *
 * @author Thorin
 */
public class StargateBungeePluginMessageListener implements PluginMessageListener {

    Stargate stargate;

    /**
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

        boolean usingBungee = Setting.getBoolean(Setting.USING_BUNGEE);
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
                case PLUGIN_ENABLE:
                case PLUGIN_DISABLE:
                    break;
                case NETWORK_CHANGED:
                    String msg = in.readUTF();
                    updateNetwork(msg);
                    break;
                case PLAYER_TELEPORT:
                    Stargate.log(Level.FINEST, "trying to read player join json msg");
                    String msg1 = in.readUTF();
                    playerConnect(msg1);
                    break;
                case LEGACY_BUNGEE:
                    String msg2 = in.readUTF();
                    legacyPlayerConnect(msg2);
                    break;
                default:
                    Stargate.log(Level.FINEST, "Recieved unknown message with a subchannel: " + subChannel);
                    break;
            }
        } catch (IOException ex) {
            Stargate.log(Level.SEVERE, "[Stargate] Error receiving BungeeCord message");
            ex.printStackTrace();
        }

    }

    private void legacyPlayerConnect(String msg) {
        String[] parts = msg.split("#@#");

        String playerName = parts[0];
        String destination = parts[1];

        Stargate.log(Level.FINEST, "desti=" + destination + ",player=" + playerName);

        // Check if the player is online, if so, teleport, otherwise, queue
        Player player = stargate.getServer().getPlayer(playerName);
        if (player == null) {
            Stargate.log(Level.FINEST, "Player was null; adding to queue");
            Stargate.addToQueue(playerName, destination, "§§§§§§#BUNGEE#§§§§§§", false);
        } else {
            Network net = Stargate.factory.getNetwork("§§§§§§#BUNGEE#§§§§§§", false);
            IPortal dest = net.getPortal(destination);
            dest.teleportHere(player, null);
        }
    }

    private void updateNetwork(String msg) {
        JsonParser parser = new JsonParser();
        Stargate.log(Level.FINEST, msg);
        JsonObject json = (JsonObject) parser.parse(msg);
        StargateProtocolRequestType type = StargateProtocolRequestType.valueOf(json.get(StargateProtocolProperty.TYPE.toString()).getAsString());
        String portalName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
        String network = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();
        String server = json.get(StargateProtocolProperty.SERVER.toString()).getAsString();
        String flags = json.get(StargateProtocolProperty.PORTAL_FLAG.toString()).getAsString();
        UUID ownerUUID = UUID.fromString(json.get(StargateProtocolProperty.OWNER.toString()).getAsString());
        InterserverNetwork targetNet = (InterserverNetwork) Stargate.factory.getNetwork(network, true);
        VirtualPortal portal = new VirtualPortal(server, portalName, targetNet, PortalFlag.parseFlags(flags), ownerUUID);

        switch (type) {
            case TYPE_PORTAL_ADD:
                targetNet.addPortal(portal, false);
                break;
            case TYPE_PORTAL_REMOVE:
                targetNet.removePortal(portal, false);
                break;
        }

    }

    private void playerConnect(String msg) {
        JsonParser parser = new JsonParser();
        Stargate.log(Level.FINEST, msg);
        JsonObject json = (JsonObject) parser.parse(msg);
        String playerName = json.get(StargateProtocolProperty.PLAYER.toString()).getAsString();
        String portalName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
        String network = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();

        Player player = stargate.getServer().getPlayer(playerName);
        if (player == null) {
            Stargate.log(Level.FINEST, "Player was null; adding to queue");
            Stargate.addToQueue(playerName, portalName, network, true);
        } else {
            try {
                Stargate.log(Level.FINEST, "Player was not null; trying to teleport");
                Network net = Stargate.factory.getNetwork(network, true);

                IPortal dest = net.getPortal(portalName);
                dest.teleportHere(player, null);
            } catch (NullPointerException e) {
                player.sendMessage(Stargate.langManager.getMessage(LangMsg.BUNGEE_EMPTY, true));
            }
        }
    }
}

