package org.sgrewritten.stargate.manager;

import org.sgrewritten.stargate.network.portal.Portal;

/**
 * A interface for handling anything related to bungee
 */
public interface BungeeManager {

    /**
     * Updates a network according to a "network changed" message
     *
     * @param message <p>The network change message to parse and handle</p>
     */
    void updateNetwork(String message);

    /**
     * Handles a player teleport message
     *
     * @param message <p>The player teleport message to parse and handle</p>
     */
    void playerConnect(String message);

    /**
     * Handle the connection of a player using the legacy Stargate method
     *
     * <p>This is done to let servers on any of the old Stargate forks connect to this version.</p>
     *
     * @param message <p>The legacy connect message to parse and handle</p>
     */
    void legacyPlayerConnect(String message);

    /**
     * Gets a portal from the BungeeCord teleportation queue
     *
     * @param playerName <p>The player to pull from the queue</p>
     * @return <p>The portal the player should be teleported to</p>
     */
    Portal pullFromQueue(String playerName);
}
