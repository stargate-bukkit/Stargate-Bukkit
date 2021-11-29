package net.TheDgtl.Stargate.network;

/**
 * Represents the different portal types considered in the database
 */
public enum PortalType {

    /**
     * Local portals are the portals on this server
     */
    LOCAL,

    /**
     * Bungee portals are the bungee portals on this server
     */
    BUNGEE,

    /**
     * Inter-server portals are portals from all servers, and available from all servers
     */
    INTER_SERVER

}
