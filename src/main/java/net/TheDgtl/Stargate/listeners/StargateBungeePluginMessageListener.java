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
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateProtocolProperty;
import net.TheDgtl.Stargate.StargateProtocolRequestType;
import net.TheDgtl.Stargate.TranslatableMessage;
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
 * Listens for and handles any received plugin messages
 *
 * @author Thorin
 */
public class StargateBungeePluginMessageListener implements PluginMessageListener {

    Stargate stargate;

    /**
     * Instantiates a new stargate bungee plugin message listener
     *
     * <p>- Send plugin enable message to all servers
     * - Send all loaded bungee-portals to all servers</p>
     *
     * @param stargate <p>A stargate instance</p>
     */
    public StargateBungeePluginMessageListener(Stargate stargate) {
        this.stargate = stargate;
    }

    /**
     * Handles relevant received plugin messages
     *
     * <p>Types of messages that can be received and their response
     * - All loaded portals messages - add all loaded portals as "virtual portals"
     * - Plugin enabled message - send all loaded portals message to specific server
     * - portal destroyed message - remove virtual portal from specific network
     * - portal added message - add virtual portal from specific network
     * - plugin disable message - remove all virtual portals given in message
     * - portal open message - open selected portal. Too much ?</p>
     */
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player unused, byte[] message) {
        Stargate.log(Level.FINEST, "Received plugin-message");

        boolean usingBungee = Setting.getBoolean(Setting.USING_BUNGEE);
        if (!usingBungee || !channel.equals("BungeeCord"))
            return;

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subChannel = in.readUTF();
            switch (Channel.parse(subChannel)) {
                case GET_SERVER:
                    Stargate.serverName = in.readUTF();
                    Stargate.knowsServerName = !Stargate.serverName.isEmpty();
                    break;
                case PLAYER_CONNECT:
                case PLUGIN_ENABLE:
                case PLUGIN_DISABLE:
                    break;
                case NETWORK_CHANGED:
                    updateNetwork(in.readUTF());
                    break;
                case PLAYER_TELEPORT:
                    Stargate.log(Level.FINEST, "trying to read player join json msg");
                    playerConnect(in.readUTF());
                    break;
                case LEGACY_BUNGEE:
                    legacyPlayerConnect(in.readUTF());
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

    /**
     * Handle the connection of a player using the legacy Stargate method
     *
     * <p>This is done to let servers on any of the old Stargate forks connect to this version.</p>
     *
     * @param message <p>The legacy connect message to parse and handle</p>
     */
    private void legacyPlayerConnect(String message) {
        String bungeeNetwork = "§§§§§§#BUNGEE#§§§§§§";
        String[] parts = message.split("#@#");

        String playerName = parts[0];
        String destination = parts[1];

        Stargate.log(Level.FINEST, "destination=" + destination + ",player=" + playerName);

        // Check if the player is online, if so, teleport, otherwise, queue
        Player player = stargate.getServer().getPlayer(playerName);
        if (player == null) {
            Stargate.log(Level.FINEST, "Player was null; adding to queue");
            Stargate.addToQueue(playerName, destination, bungeeNetwork, false);
        } else {
            Network network = Stargate.factory.getNetwork(bungeeNetwork, false);
            IPortal destinationPortal = network.getPortal(destination);
            destinationPortal.teleportHere(player, null);
        }
    }

    /**
     * Updates a network according to a "network changed" message
     *
     * @param message <p>The network change message to parse and handle</p>
     */
    private void updateNetwork(String message) {
        JsonParser parser = new JsonParser();
        Stargate.log(Level.FINEST, message);
        JsonObject json = (JsonObject) parser.parse(message);

        String requestTypeString = json.get(StargateProtocolProperty.TYPE.toString()).getAsString();
        StargateProtocolRequestType requestType = StargateProtocolRequestType.valueOf(requestTypeString);

        String portalName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
        String network = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();
        String server = json.get(StargateProtocolProperty.SERVER.toString()).getAsString();
        String flags = json.get(StargateProtocolProperty.PORTAL_FLAG.toString()).getAsString();
        UUID ownerUUID = UUID.fromString(json.get(StargateProtocolProperty.OWNER.toString()).getAsString());
        InterserverNetwork targetNetwork = (InterserverNetwork) Stargate.factory.getNetwork(network, true);
        VirtualPortal portal = new VirtualPortal(server, portalName, targetNetwork, PortalFlag.parseFlags(flags), ownerUUID);

        switch (requestType) {
            case TYPE_PORTAL_ADD:
                targetNetwork.addPortal(portal, false);
                break;
            case TYPE_PORTAL_REMOVE:
                targetNetwork.removePortal(portal, false);
                break;
        }

    }

    /**
     * Handles a player teleport message
     *
     * @param message <p>The player teleport message to parse and handle</p>
     */
    private void playerConnect(String message) {
        JsonParser parser = new JsonParser();
        Stargate.log(Level.FINEST, message);

        JsonObject json = (JsonObject) parser.parse(message);
        String playerName = json.get(StargateProtocolProperty.PLAYER.toString()).getAsString();
        String portalName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
        String networkName = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();

        Player player = stargate.getServer().getPlayer(playerName);
        if (player == null) {
            Stargate.log(Level.FINEST, "Player was null; adding to queue");
            Stargate.addToQueue(playerName, portalName, networkName, true);
        } else {
            try {
                Stargate.log(Level.FINEST, "Player was not null; trying to teleport");
                Network network = Stargate.factory.getNetwork(networkName, true);

                IPortal destinationPortal = network.getPortal(portalName);
                destinationPortal.teleportHere(player, null);
            } catch (NullPointerException e) {
                player.sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.BUNGEE_EMPTY, true));
            }
        }
    }

}

