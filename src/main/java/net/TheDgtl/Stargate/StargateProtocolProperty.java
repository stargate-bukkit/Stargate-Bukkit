package net.TheDgtl.Stargate;

/**
 * An enum containing all actions in the StarGate plugin message protocol
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
    TYPE,

    /**
     * The enabled portal flags property of a request
     */
    PORTAL_FLAG,

    /**
     * The portal owner property of a request
     */
    OWNER

}
