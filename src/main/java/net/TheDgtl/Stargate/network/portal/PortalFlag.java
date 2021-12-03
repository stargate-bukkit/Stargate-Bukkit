package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.exception.NoFlagFound;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a portal flag which defines an enabled behavior for a stargate
 */
public enum PortalFlag {

    /**
     * A random stargate which teleports to a random destination in its network
     */
    RANDOM('R'),

    /**
     * An across-server stargate using the new and better protocol
     */
    FANCY_INTER_SERVER('I'),

    /**
     * A stargate which is always on and always open
     */
    ALWAYS_ON('A'),

    /**
     * A stargate where the exit is on the opposite side of the sign
     */
    BACKWARDS('B'),

    /**
     * A stargate which is hidden on the network for unprivileged users, except for the owner
     */
    HIDDEN('H'),

    /**
     * A stargate which can only be used by the owner and unprivileged users
     */
    PRIVATE('P'),

    /**
     * A stargate displaying all available destinations, regardless of whether they are available to the player
     */
    FORCE_SHOW('S'),

    /**
     * A stargate with its network name hidden
     * TODO: This is unused
     */
    HIDE_NETWORK('N'),

    /**
     * A free stargate, regardless of all set prices
     */
    FREE('F'),

    /**
     * An across-server stargate using the legacy BungeeCord protocol
     */
    BUNGEE('U'),

    /**
     * A silent stargate which doesn't output a teleportation message
     * TODO: This is unused
     */
    SILENT('Q'),

    /* ***************
     * Internal flags *
     ******************/

    /**
     * A normal networked stargate
     */
    NETWORKED('1'),

    /**
     * A fixed stargate with only one destination
     */
    FIXED('2'),

    /**
     * A stargate whose opening can be entirely blocked by an iron door
     */
    IRON_DOOR('3'),

    /**
     * A stargate part of a personal network
     */
    PERSONAL_NETWORK('4');

    private final char characterRepresentation;
    private final static Map<Character, PortalFlag> map = new HashMap<>();

    /**
     * Instantiates a new portal flag
     *
     * @param characterRepresentation <p>The character used to identify this portal flag in flag strings</p>
     */
    PortalFlag(char characterRepresentation) {
        this.characterRepresentation = characterRepresentation;
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
     * Gets all portal flags present in the given string
     *
     * @param line <p>The string to search for portal flags</p>
     * @return <p>A set of all found portal flags</p>
     */
    public static EnumSet<PortalFlag> parseFlags(String line) {
        EnumSet<PortalFlag> foundFlags = EnumSet.noneOf(PortalFlag.class);
        char[] charArray = line.toUpperCase().toCharArray();
        for (char character : charArray) {
            try {
                foundFlags.add(PortalFlag.valueOf(character));
            } catch (NoFlagFound ignored) {
            }
        }
        return foundFlags;
    }

    /**
     * Gets the portal flag corresponding to the given character
     *
     * @param label <p>A character representing a portal flag</p>
     * @return <p>The portal flag represented by the character</p>
     * @throws NoFlagFound <p>If unable to find a matching flag</p>
     */
    public static PortalFlag valueOf(char label) throws NoFlagFound {
        if (map.isEmpty()) {
            for (PortalFlag flag : values()) {
                map.put(flag.characterRepresentation, flag);
            }
        }

        PortalFlag flag = map.get(label);
        if (flag != null) {
            return flag;
        } else {
            throw new NoFlagFound();
        }
    }

}