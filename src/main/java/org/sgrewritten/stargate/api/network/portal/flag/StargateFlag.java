package org.sgrewritten.stargate.api.network.portal.flag;

import org.sgrewritten.stargate.util.ExceptionHelper;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a portal flag which defines an enabled behavior for a stargate
 */
public enum StargateFlag implements PortalFlag {

    /**
     * A random stargate which teleports to a random destination in its network
     */
    RANDOM('R', true, true),

    /**
     * An across-server stargate using the new and better protocol
     */
    INTERSERVER('I', true, false),

    /**
     * A stargate which is always on and always open
     */
    ALWAYS_ON('A', true, false),

    /**
     * A stargate where the exit is on the opposite side of the sign
     */
    BACKWARDS('B', true, false),

    /**
     * A stargate which is hidden on the network for unprivileged users, except for the owner
     */
    HIDDEN('H', true, false),

    /**
     * A stargate which can only be used by the owner and unprivileged users
     */
    PRIVATE('P', true, false),

    /**
     * A stargate displaying all available destinations, regardless of whether they are available to the player
     */
    FORCE_SHOW('S', true, false),

    /**
     * A stargate with its network name hidden
     */
    HIDE_NETWORK('N', true, false),

    /**
     * A free stargate, regardless of all set prices
     */
    FREE('F', true, false),

    /**
     * An across-server stargate using the legacy BungeeCord protocol
     */
    LEGACY_INTERSERVER('U', true, true),

    /**
     * A silent stargate which doesn't output a teleportation message
     */
    SILENT('Q', true, false),

    /**
     * A stargate without a sign.
     * <p>This is used by an official module, no implementation in Stargate-Core has been done </p>
     */
    NO_SIGN('V', true, false),

    /* ***************
     * Internal flags *
     ******************/

    /**
     * A normal networked stargate
     */
    NETWORKED('1', false, true),

    /**
     * A fixed stargate with only one destination
     */
    FIXED('2', false, true),

    /**
     * A stargate whose opening can be entirely blocked by an iron door
     */
    IRON_DOOR('3', false, false),

    /**
     * A stargate part of a personal network
     */
    PERSONAL_NETWORK('4', false, false),

    /**
     * A stargate on the default network
     */
    DEFAULT_NETWORK('5', false, false),

    /**
     * A stargate custom network
     */
    CUSTOM_NETWORK('6', false, false),

    /**
     * A stargate on the terminal network
     * NOT IMPLEMENTED AT THE MOMENT, temporary an internal flag (inaccessible)
     */
    TERMINAL_NETWORK('T', false, false);

    private final boolean isUserSpecifiable;
    private final char characterRepresentation;
    private static final Map<Character, StargateFlag> map = new HashMap<>();
    private final boolean isSelector;

    private static final Pattern NON_FLAG_STRING = Pattern.compile("(\\{.*?\\})");

    /**
     * Instantiates a new portal flag
     *
     * @param characterRepresentation <p>The character used to identify this portal flag in flag strings</p>
     * @param isUserSpecifiable       <p>Whether a user can specify whether this flag is enabled for a portal</p>
     */
    StargateFlag(char characterRepresentation, boolean isUserSpecifiable, boolean isSelector) {
        this.characterRepresentation = characterRepresentation;
        this.isUserSpecifiable = isUserSpecifiable;
        this.isSelector = isSelector;
    }

    @Override
    public char getCharacterRepresentation() {
        return this.characterRepresentation;
    }

    @Override
    public boolean isInternalFlag() {
        return !isUserSpecifiable;
    }

    /**
     * Gets all portal flags present in the given string
     *
     * @return <p>A set of all found portal flags</p>
     */
    public static Set<StargateFlag> parseFlags(String flagString) {
        Set<StargateFlag> foundFlags = EnumSet.noneOf(StargateFlag.class);
        Matcher matcher = NON_FLAG_STRING.matcher(flagString.toUpperCase());
        char[] charArray = matcher.replaceAll("").toCharArray();
        for (char character : charArray) {
            try {
                foundFlags.add(StargateFlag.valueOf(character));
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
    public static StargateFlag valueOf(char label) throws IllegalArgumentException {
        if (map.isEmpty()) {
            for (StargateFlag flag : values()) {
                map.put(flag.characterRepresentation, flag);
            }
        }

        StargateFlag flag = map.get(label);
        if (flag != null) {
            return flag;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Ignores arguments enveloped within {}
     *
     * @param flagString <p>A string of all flags</p>
     * @return <p>Flags that were unrecognised, but might be addon flags</p>
     */
    public static Set<Character> getUnrecognisedFlags(String flagString) {
        Set<Character> unrecognisedFlags = new HashSet<>();
        Matcher matcher = NON_FLAG_STRING.matcher(flagString.toUpperCase());
        for (char flag : matcher.replaceAll("").toCharArray()) {
            if (!ExceptionHelper.doesNotThrow(() -> StargateFlag.valueOf(flag))) {
                unrecognisedFlags.add(flag);
            }
        }
        return unrecognisedFlags;
    }

    @Override
    public boolean isBehaviorFlag() {
        return this.isSelector;
    }
}