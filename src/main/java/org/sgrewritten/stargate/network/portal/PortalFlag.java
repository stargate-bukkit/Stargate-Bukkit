package org.sgrewritten.stargate.network.portal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a portal flag which defines an enabled behavior for a stargate
 */
public enum PortalFlag {

    /**
     * A random stargate which teleports to a random destination in its network
     */
    RANDOM('R', true),

    /**
     * An across-server stargate using the new and better protocol
     */
    FANCY_INTER_SERVER('I', true),

    /**
     * A stargate which is always on and always open
     */
    ALWAYS_ON('A', true),

    /**
     * A stargate where the exit is on the opposite side of the sign
     */
    BACKWARDS('B', true),

    /**
     * A stargate which is hidden on the network for unprivileged users, except for the owner
     */
    HIDDEN('H', true),

    /**
     * A stargate which can only be used by the owner and unprivileged users
     */
    PRIVATE('P', true),

    /**
     * A stargate displaying all available destinations, regardless of whether they are available to the player
     */
    FORCE_SHOW('S', true),

    /**
     * A stargate with its network name hidden
     */
    HIDE_NETWORK('N', true),

    /**
     * A free stargate, regardless of all set prices
     */
    FREE('F', true),

    /**
     * An across-server stargate using the legacy BungeeCord protocol
     */
    BUNGEE('U', true),

    /**
     * A silent stargate which doesn't output a teleportation message
     */
    SILENT('Q', true),

    /**
     * A stargate without a sign
     */
    NO_SIGN('V', true),

    /* ***************
     * Internal flags *
     ******************/

    /**
     * A normal networked stargate
     */
    NETWORKED('1', false),

    /**
     * A fixed stargate with only one destination
     */
    FIXED('2', false),

    /**
     * A stargate whose opening can be entirely blocked by an iron door
     */
    IRON_DOOR('3', false),

    /**
     * A stargate part of a personal network
     */
    PERSONAL_NETWORK('4', false),

    /**
     * A stargate on the default network
     */
    DEFAULT_NETWORK('5', false),

    /**
     * A stargate custom network
     */
    CUSTOM_NETWORK('6', false),

    /**
     * A stargate on the terminal network
     * NOT IMPLEMENTED AT THE MOMENT, temporary an internal flag (inaccessible)
     */
    TERMINAL_NETWORK('T', false);

    private final boolean isUserSpecifiable;
    private final char characterRepresentation;
    private final static Map<Character, PortalFlag> map = new HashMap<>();

    /**
     * Instantiates a new portal flag
     *
     * @param characterRepresentation <p>The character used to identify this portal flag in flag strings</p>
     * @param isUserSpecifiable       <p>Whether a user can specify whether this flag is enabled for a portal</p>
     */
    PortalFlag(char characterRepresentation, boolean isUserSpecifiable) {
        this.characterRepresentation = characterRepresentation;
        this.isUserSpecifiable = isUserSpecifiable;
    }

    /**
     * Gets the character representing this portal flag
     *
     * @return <p>The character representing this portal flag</p>
     */
    public Character getCharacterRepresentation() {
        return this.characterRepresentation;
    }

    /**
     * Gets whether this flag is an internal flag
     *
     * <p>User specifiable flags are flags like backwards or always on, which the user specifies upon creation.
     * Internal flags are the flags that are implicitly set to some value based on the Stargate created.</p>
     *
     * @return <p>True if this flag is user specifiable</p>
     */
    public boolean isInternalFlag() {
        return !isUserSpecifiable;
    }

    /**
     * Gets all portal flags present in the given string
     *
     * @param line <p>The string to search for portal flags</p>
     * @return <p>A set of all found portal flags</p>
     */
    public static Set<PortalFlag> parseFlags(String line) {
        Set<PortalFlag> foundFlags = EnumSet.noneOf(PortalFlag.class);
        char[] charArray = line.toUpperCase().toCharArray();
        for (char character : charArray) {
            try {
                foundFlags.add(PortalFlag.valueOf(character));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return foundFlags;
    }

    /**
     * Gets the portal flag corresponding to the given character
     *
     * @param label <p>A character representing a portal flag</p>
     * @return <p>The portal flag represented by the character</p>
     * @throws IllegalArgumentException <p>If unable to find a matching flag</p>
     */
    public static PortalFlag valueOf(char label) throws IllegalArgumentException {
        if (map.isEmpty()) {
            for (PortalFlag flag : values()) {
                map.put(flag.characterRepresentation, flag);
            }
        }

        PortalFlag flag = map.get(label);
        if (flag != null) {
            return flag;
        } else {
            throw new IllegalArgumentException();
        }
    }

}