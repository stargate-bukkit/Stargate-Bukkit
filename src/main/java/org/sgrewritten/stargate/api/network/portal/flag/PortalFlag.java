package org.sgrewritten.stargate.api.network.portal.flag;

import java.util.HashSet;
import java.util.Set;

public interface PortalFlag {
    /**
     * A random stargate which teleports to a random destination in its network
     */
    PortalFlag RANDOM = StargateFlag.RANDOM;

    /**
     * An across-server stargate using the new and better protocol
     */
    PortalFlag INTERSERVER = StargateFlag.INTERSERVER;

    /**
     * A stargate which is always on and always open
     */
    PortalFlag ALWAYS_ON = StargateFlag.ALWAYS_ON;

    /**
     * A stargate where the exit is on the opposite side of the sign
     */
    PortalFlag BACKWARDS = StargateFlag.BACKWARDS;

    /**
     * A stargate which is hidden on the network for unprivileged users, except for the owner
     */
    PortalFlag HIDDEN = StargateFlag.HIDDEN;

    /**
     * A stargate which can only be used by the owner and unprivileged users
     */
    PortalFlag PRIVATE = StargateFlag.PRIVATE;

    /**
     * A stargate displaying all available destinations, regardless of whether they are available to the player
     */
    PortalFlag FORCE_SHOW = StargateFlag.FORCE_SHOW;

    /**
     * A stargate with its network name hidden
     */
    PortalFlag HIDE_NETWORK = StargateFlag.HIDE_NETWORK;

    /**
     * A free stargate, regardless of all set prices
     */
    PortalFlag FREE = StargateFlag.FREE;

    /**
     * An across-server stargate using the legacy BungeeCord protocol
     */
    PortalFlag LEGACY_INTERSERVER = StargateFlag.LEGACY_INTERSERVER;

    /**
     * A silent stargate which doesn't output a teleportation message
     */
    PortalFlag SILENT = StargateFlag.SILENT;

    /**
     * A stargate without a sign.
     * <p>This is used by an official module, no implementation in Stargate-Core has been done </p>
     */
    PortalFlag NO_SIGN = StargateFlag.NO_SIGN;

    /* ***************
     * Internal flags *
     ******************/

    /**
     * A normal networked stargate
     */
    PortalFlag NETWORKED = StargateFlag.NETWORKED;

    /**
     * A fixed stargate with only one destination
     */
    PortalFlag FIXED = StargateFlag.FIXED;

    /**
     * A stargate whose opening can be entirely blocked by an iron door
     */
    PortalFlag IRON_DOOR = StargateFlag.IRON_DOOR;

    /**
     * A stargate part of a personal network
     */
    PortalFlag PERSONAL_NETWORK = StargateFlag.PERSONAL_NETWORK;

    /**
     * A stargate on the default network
     */
    PortalFlag DEFAULT_NETWORK = StargateFlag.DEFAULT_NETWORK;

    /**
     * A stargate custom network
     */
    PortalFlag CUSTOM_NETWORK = StargateFlag.CUSTOM_NETWORK;

    /**
     * A stargate on the terminal network
     * NOT IMPLEMENTED AT THE MOMENT, temporary an internal flag (inaccessible)
     */
    PortalFlag TERMINAL_NETWORK = StargateFlag.TERMINAL_NETWORK;

    /**
     * @return <p>Whether this flag relates to how a portal selects destinations</p>
     */
    boolean isBehaviorFlag();

    /**
     * Gets whether this flag is an internal flag
     *
     * <p>User specifiable flags are flags like backwards or always on, which the user specifies upon creation.
     * Internal flags are the flags that are implicitly set to some value based on the Stargate created.</p>
     *
     * @return <p>True if this flag is user specifiable</p>
     */
    boolean isInternalFlag();

    /**
     * Gets the character representing this portal flag
     *
     * @return <p>The character representing this portal flag</p>
     */
    char getCharacterRepresentation();

    /**
     * @return <p>True if this flag is custom</p>
     */
    boolean isCustom();
    /**
     *
     * @param flagString <p>The flag string to parse</p>
     * @return <p>A mutable set of PortalFlags</p>
     */
    static Set<PortalFlag> parseFlags(String flagString){
        Set<PortalFlag> outPut = new HashSet<>(StargateFlag.parseFlags(flagString));
        StargateFlag.getUnrecognisedFlags(flagString).forEach(character -> outPut.add(CustomFlag.getOrCreate(character)));
        return outPut;
    }
}
