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
package net.TheDgtl.Stargate.listener;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.network.InterServerNetwork;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;
import net.TheDgtl.Stargate.property.PluginChannel;
import net.TheDgtl.Stargate.property.StargateProtocolProperty;
import net.TheDgtl.Stargate.property.StargateProtocolRequestType;
import net.TheDgtl.Stargate.util.BungeeHelper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

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

    final Stargate stargate;

    /**
     * Instantiates a new stargate bungee plugin message listener
     *
     * @param stargate <p>A stargate instance</p>
     */
    public StargateBungeePluginMessageListener(Stargate stargate) {
        this.stargate = stargate;
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
     * </ul></p>
     */
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player unused, byte[] message) {
        Stargate.log(Level.FINEST, "Received plugin-message");

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
                Stargate.log(Level.FINEST, "Received unknown message on unknown sub-channel: " + subChannel);
                return;
            }
            switch (subPluginChannel) {
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
                    BungeeHelper.playerConnect(in.readUTF());
                    break;
                case LEGACY_BUNGEE:
                    BungeeHelper.legacyPlayerConnect(in.readUTF());
                    break;
                default:
                    Stargate.log(Level.FINEST, "Received unknown message with a sub-channel: " + subChannel);
                    break;
            }
        } catch (IOException ex) {
            Stargate.log(Level.WARNING, "[Stargate] Error receiving BungeeCord message");
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
        Stargate.log(Level.FINEST, message);
        JsonObject json = (JsonObject) parser.parse(message);

        String requestTypeString = json.get(StargateProtocolProperty.REQUEST_TYPE.toString()).getAsString();
        StargateProtocolRequestType requestType = StargateProtocolRequestType.valueOf(requestTypeString);

        String portalName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
        String network = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();
        String server = json.get(StargateProtocolProperty.SERVER.toString()).getAsString();
        Set<PortalFlag> flags = PortalFlag.parseFlags(json.get(StargateProtocolProperty.PORTAL_FLAG.toString()).getAsString());
        UUID ownerUUID = UUID.fromString(json.get(StargateProtocolProperty.OWNER.toString()).getAsString());

        try {
            Stargate.getRegistryStatic().createNetwork(network, flags);
            InterServerNetwork targetNetwork = (InterServerNetwork) Stargate.getRegistryStatic().getNetwork(network, true);
            VirtualPortal portal = new VirtualPortal(server, portalName, targetNetwork, flags, ownerUUID);

            switch (requestType) {
                case PORTAL_ADD:
                    targetNetwork.addPortal(portal, false);
                    Stargate.log(Level.FINE, String.format("Adding virtual portal %s in inter-server network %s", portalName, network));
                    break;
                case PORTAL_REMOVE:
                    Stargate.log(Level.FINE, String.format("Removing virtual portal %s in inter-server network %s", portalName, network));
                    targetNetwork.removePortal(portal, false);
                    break;
            }
        } catch (NameErrorException ignored) {
        }
    }

}

