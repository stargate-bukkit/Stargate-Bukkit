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
package org.sgrewritten.stargate.listener;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.InterServerNetwork;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.VirtualPortal;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.property.StargateProtocolProperty;
import org.sgrewritten.stargate.property.StargateProtocolRequestType;
import org.sgrewritten.stargate.util.BungeeHelper;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Listens for and handles any received plugin messages
 *
 * <p>Sends plugin enable message to all servers. Sends all loaded bungee-portals to all servers.</p>
 *
 * @author Thorin
 */
public class StargateBungeePluginMessageListener implements PluginMessageListener {

    private final StargateAPI stargateAPI;
    private final StargateLogger stargateLogger;
    private RegistryAPI registry;

    /**
     * Instantiates a new stargate bungee plugin message listener
     *
     * @param stargateAPI    <p>Something implementing the Stargate API</p>
     * @param stargateLogger <p>Something implementing the Stargate logger</p>
     */
    public StargateBungeePluginMessageListener(StargateAPI stargateAPI, StargateLogger stargateLogger, RegistryAPI registry) {
        this.stargateAPI = stargateAPI;
        this.stargateLogger = stargateLogger;
        this.registry = registry;
    }

    /**
     * Handles relevant received plugin messages
     *
     * <p>Types of messages that can be received and their response:
     * <ul>
     *  <li>All loaded portals messages - add all loaded portals as "virtual portals"</li>
     *  <li>Plugin enabled message - send all loaded portals message to specific server</li>
     *  <li>portal destroyed message - remove virtual portal from specific network</li>
     *  <li>portal added message - add virtual portal from specific network</li>
     *  <li>plugin disable message - remove all virtual portals given in message</li>
     *  <li>portal open message - open selected portal. Too much ?</li>
     * </ul>
     * @param channel The channel being used to send the message.
     * @param unused A player object.
     * @param message The message being processed.
     */
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player unused, byte[] message) {
        stargateLogger.logMessage(Level.FINEST, "Received plugin-message");

        boolean usingBungee = ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE);
        if (!usingBungee || !channel.equals("BungeeCord")) {
            return;
        }

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subChannel = in.readUTF();
            //Ignore any unknown sub-channels to prevent an exception caused by converting null into ordinal
            PluginChannel subPluginChannel = PluginChannel.parse(subChannel);
            if (subPluginChannel == null) {
                stargateLogger.logMessage(Level.FINEST, "Received unknown message on unknown sub-channel: " + subChannel);
                return;
            }
            switch (subPluginChannel) {
                case GET_SERVER:
                    Stargate.setServerName(in.readUTF());
                    Stargate.setKnowsServerName(!Stargate.getServerName().isEmpty());
                    break;
                case PLAYER_CONNECT:
                case PLUGIN_ENABLE:
                case PLUGIN_DISABLE:
                    break;
                case NETWORK_CHANGED:
                    updateNetwork(in.readUTF());
                    break;
                case PLAYER_TELEPORT:
                    stargateLogger.logMessage(Level.FINEST, "trying to read player join json msg");
                    BungeeHelper.playerConnect(in.readUTF(),registry);
                    break;
                case LEGACY_BUNGEE:
                    BungeeHelper.legacyPlayerConnect(in.readUTF());
                    break;
                default:
                    stargateLogger.logMessage(Level.FINEST, "Received unknown message with a sub-channel: " + subChannel);
                    break;
            }
        } catch (IOException ex) {
            stargateLogger.logMessage(Level.WARNING, "[Stargate] Error receiving BungeeCord message");
            ex.printStackTrace();
        }
    }

    /**
     * Updates a network according to a "network changed" message
     *
     * @param message <p>The network change message to parse and handle</p>
     */
    private void updateNetwork(String message) {
        JsonParser parser = new JsonParser();
        stargateLogger.logMessage(Level.FINEST, message);
        JsonObject json = (JsonObject) parser.parse(message);

        String requestTypeString = json.get(StargateProtocolProperty.REQUEST_TYPE.toString()).getAsString();
        StargateProtocolRequestType requestType = StargateProtocolRequestType.valueOf(requestTypeString);

        String portalName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
        String network = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();
        String server = json.get(StargateProtocolProperty.SERVER.toString()).getAsString();
        Set<PortalFlag> flags = PortalFlag.parseFlags(json.get(StargateProtocolProperty.PORTAL_FLAG.toString()).getAsString());
        UUID ownerUUID = UUID.fromString(json.get(StargateProtocolProperty.OWNER.toString()).getAsString());

        try {
            stargateAPI.getRegistry().createNetwork(network, flags, false);
        } catch (InvalidNameException | NameLengthException | NameConflictException  e) {
            e.printStackTrace();
        }
        try {
            InterServerNetwork targetNetwork = (InterServerNetwork) stargateAPI.getRegistry().getNetwork(network, true);
            VirtualPortal portal = new VirtualPortal(server, portalName, targetNetwork, flags, ownerUUID);
            switch (requestType) {
                case PORTAL_ADD:
                    targetNetwork.addPortal(portal, false);
                    stargateLogger.logMessage(Level.FINE, String.format("Adding virtual portal %s in inter-server network %s", portalName, network));
                    break;
                case PORTAL_REMOVE:
                    stargateLogger.logMessage(Level.FINE, String.format("Removing virtual portal %s in inter-server network %s", portalName, network));
                    targetNetwork.removePortal(portal, false);
                    break;
            }
            targetNetwork.updatePortals();
        } catch (NameConflictException ignored) {
        }
    }

}

