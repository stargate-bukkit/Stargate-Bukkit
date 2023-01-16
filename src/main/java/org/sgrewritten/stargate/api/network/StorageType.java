package org.sgrewritten.stargate.api.network;

/**
 * Represents the different portal types considered in the database
 */
public enum StorageType {

    /**
     * Local portals are the portals on this server
     */
    LOCAL,

    /**
     * Inter-server portals are portals from all servers, and available from all servers
     */
    INTER_SERVER

}
