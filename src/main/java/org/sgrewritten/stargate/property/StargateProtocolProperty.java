package org.sgrewritten.stargate.property;

/**
 * An enum containing all actions in the Stargate plugin message protocol
 *
 * @author Thorin
 */
public enum StargateProtocolProperty {

    /**
     * The network name property of a request
     */
    NETWORK,

    /**
     * The portal name property of a request
     */
    PORTAL,

    /**
     * The player name property of a request
     */
    PLAYER,

    /**
     * The server name property of a request
     */
    SERVER,

    /**
     * The protocol message type used for this request
     */
    REQUEST_TYPE,

    /**
     * The enabled portal flags property of a request
     */
    PORTAL_FLAG,

    /**
     * The portal owner property of a request
     */
    OWNER,

    /**
     * The new name of the network
     */
    NEW_NETWORK_NAME,

    /**
     * The new name of the portal
     */
    NEW_PORTAL_NAME

}
