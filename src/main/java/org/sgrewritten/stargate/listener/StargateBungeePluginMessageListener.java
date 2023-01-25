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

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.manager.BungeeManager;
import org.sgrewritten.stargate.property.PluginChannel;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Listens for and handles any received plugin messages
 *
 * <p>Sends plugin enable message to all servers. Sends all loaded bungee-portals to all servers.</p>
 *
 * @author Thorin
 */
public class StargateBungeePluginMessageListener implements PluginMessageListener {

    private final StargateLogger stargateLogger;
    private final BungeeManager bungeeManager;

    /**
     * Instantiates a new stargate bungee plugin message listener
     *
     * @param stargateAPI    <p>Something implementing the Stargate API</p>
     * @param stargateLogger <p>Something implementing the Stargate logger</p>
     */
    public StargateBungeePluginMessageListener(BungeeManager bungeeManager, StargateLogger stargateLogger) {
        this.stargateLogger = Objects.requireNonNull(stargateLogger);
        this.bungeeManager = Objects.requireNonNull(bungeeManager);
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
     *
     * @param channel The channel being used to send the message.
     * @param unused  A player object.
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
                    bungeeManager.updateNetwork(in.readUTF());
                    break;
                case PLAYER_TELEPORT:
                    stargateLogger.logMessage(Level.FINEST, "trying to read player join json msg");
                    bungeeManager.playerConnect(in.readUTF());
                    break;
                case LEGACY_BUNGEE:
                    bungeeManager.legacyPlayerConnect(in.readUTF());
                    break;
                default:
                    stargateLogger.logMessage(Level.FINEST, "Received unknown message with a sub-channel: " + subChannel);
                    break;
            }
        } catch (IOException e) {
            stargateLogger.logMessage(Level.WARNING, "[Stargate] Error receiving BungeeCord message");
            Stargate.log(e);
        }
    }
}

