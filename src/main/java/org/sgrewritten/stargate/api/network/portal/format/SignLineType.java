package org.sgrewritten.stargate.api.network.portal.format;

public enum SignLineType {

    /**
     * Sign line with error message
     */
    ERROR,

    /**
     * Sign line with any text on it
     */
    TEXT,

    /**
     * Sign line with network name on it
     */
    NETWORK,

    /**
     * Sign line with the current selected portal name
     */
    DESTINATION_PORTAL,

    /**
     * Sign line with current portal name
     */
    THIS_PORTAL,

    /**
     * Sign line with any portal
     */
    PORTAL,
}
