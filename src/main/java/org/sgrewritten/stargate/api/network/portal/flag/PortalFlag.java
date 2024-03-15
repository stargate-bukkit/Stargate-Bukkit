package org.sgrewritten.stargate.api.network.portal.flag;

import java.util.HashSet;
import java.util.Set;

public interface PortalFlag {
    PortalFlag RANDOM = StargateFlag.RANDOM;
    PortalFlag INTERSERVER = StargateFlag.INTERSERVER;
    PortalFlag ALWAYS_ON = StargateFlag.ALWAYS_ON;
    PortalFlag BACKWARDS = StargateFlag.BACKWARDS;
    PortalFlag HIDDEN = StargateFlag.HIDDEN;
    PortalFlag PRIVATE = StargateFlag.PRIVATE;
    PortalFlag FORCE_SHOW = StargateFlag.FORCE_SHOW;
    PortalFlag HIDE_NETWORK = StargateFlag.HIDE_NETWORK;
    PortalFlag FREE = StargateFlag.FREE;
    PortalFlag LEGACY_INTERSERVER = StargateFlag.LEGACY_INTERSERVER;
    PortalFlag SILENT = StargateFlag.SILENT;
    PortalFlag NO_SIGN = StargateFlag.NO_SIGN;
    PortalFlag NETWORKED = StargateFlag.NETWORKED;
    PortalFlag FIXED = StargateFlag.FIXED;
    PortalFlag IRON_DOOR = StargateFlag.IRON_DOOR;

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
