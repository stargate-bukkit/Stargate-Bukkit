package org.sgrewritten.stargate.network;

/**
 * Represents the different portal types considered in the database
 */
public enum PortalType {

    /**
     * Local portals are the portals on this server
     */
    LOCAL,

    /**
     * Inter-server portals are portals from all servers, and available from all servers
     */
    INTER_SERVER

}
