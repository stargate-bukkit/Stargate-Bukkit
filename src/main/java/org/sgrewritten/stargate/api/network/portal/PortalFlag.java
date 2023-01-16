package org.sgrewritten.stargate.api.network.portal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a portal flag which defines an enabled behavior for a stargate
 */
public class PortalFlag {
    
    private final static Map<Character, PortalFlag> charMap = new HashMap<>();
    private final static Map<String, PortalFlag> nameMap = new HashMap<>();
    private final static List<PortalFlag> list = new ArrayList<>();

    /**
     * A random stargate which teleports to a random destination in its network
     */
    public static PortalFlag RANDOM = new PortalFlag('R', "RANDOM", true);

    /**
     * An across-server stargate using the new and better protocol
     */
    public static PortalFlag FANCY_INTER_SERVER = new PortalFlag('I', "FANCY_INTER_SERVER", true);

    /**
     * A stargate which is always on and always open
     */
    public static PortalFlag ALWAYS_ON = new PortalFlag('A', "ALWAYS_ON", true);

    /**
     * A stargate where the exit is on the opposite side of the sign
     */
    public static PortalFlag BACKWARDS = new PortalFlag('B', "BACKWARDS", true);

    /**
     * A stargate which is hidden on the network for unprivileged users, except for the owner
     */
    public static PortalFlag HIDDEN = new PortalFlag('H', "HIDDEN",true);

    /**
     * A stargate which can only be used by the owner and unprivileged users
     */
    public static PortalFlag PRIVATE = new PortalFlag('P', "PRIVATE", true);

    /**
     * A stargate displaying all available destinations, regardless of whether they are available to the player
     */
    public static PortalFlag FORCE_SHOW = new PortalFlag('S', "FORCE_SHOW",true);

    /**
     * A stargate with its network name hidden
     */
    public static PortalFlag HIDE_NETWORK = new PortalFlag('N', "HIDE_NETWORK", true);

    /**
     * A free stargate, regardless of all set prices
     */
    public static PortalFlag FREE = new PortalFlag('F', "FREE", true);

    /**
     * An across-server stargate using the legacy BungeeCord protocol
     */
    public static PortalFlag BUNGEE = new PortalFlag('U', "BUNGEE", true);

    /**
     * A silent stargate which doesn't output a teleportation message
     */
    public static PortalFlag SILENT = new PortalFlag('Q', "SILENT", true);

    /**
     * A stargate without a sign
     */
    public static PortalFlag NO_SIGN = new PortalFlag('V', "NO_SIGN", true);

    /* ***************
     * Internal flags *
     ******************/

    /**
     * A normal networked stargate
     */
    public static PortalFlag NETWORKED = new PortalFlag('1', "NETWORKED", false);

    /**
     * A fixed stargate with only one destination
     */
    public static PortalFlag FIXED = new PortalFlag('2', "FIXED", false);

    /**
     * A stargate whose opening can be entirely blocked by an iron door
     */
    public static PortalFlag IRON_DOOR = new PortalFlag('3', "IRON_DOOR", false);

    /**
     * A stargate part of a personal network
     */
    public static PortalFlag PERSONAL_NETWORK = new PortalFlag('4', "PERSONAL_NETWORK", false);

    /**
     * A stargate on the default network
     */
    public static PortalFlag DEFAULT_NETWORK = new PortalFlag('5', "DEFAULT_NETWORK", false);
    
    /**
     * A stargate custom network
     */
    public static PortalFlag CUSTOM_NETWORK = new PortalFlag('6', "CUSTOM_NETWORK", false);

    /**
     * A stargate on the terminal network
     * NOT IMPLEMENTED AT THE MOMENT, temporary an internal flag (inaccessible)
     */
    public static PortalFlag TERMINAL_NETWORK = new PortalFlag('T', "TERMINAL_NETWORK", false);
    
    static {
        registerAllStargateFlags();
    }

    private final boolean isUserSpecifiable;
    private final char characterRepresentation;
    private final @NotNull String name;

    /**
     * Instantiates a new portal flag
     *
     * @param characterRepresentation <p>The character used to identify this portal flag in flag strings</p>
     * @param isUserSpecifiable       <p>Whether a user can specify whether this flag is enabled for a portal</p>
     */
    public PortalFlag(char characterRepresentation,@NotNull String name, boolean isUserSpecifiable) {
        this.characterRepresentation = characterRepresentation;
        this.isUserSpecifiable = isUserSpecifiable;
        this.name = Objects.requireNonNull(name);
    }

    private static void registerAllStargateFlags() {
        PortalFlag[] stargateFlags = {
                RANDOM,
                FANCY_INTER_SERVER,
                ALWAYS_ON,
                BACKWARDS,
                HIDDEN,
                PRIVATE,
                FORCE_SHOW,
                HIDE_NETWORK,
                FREE,
                BUNGEE,
                SILENT,
                NO_SIGN,
                NETWORKED,
                FIXED,
                IRON_DOOR,
                PERSONAL_NETWORK,
                DEFAULT_NETWORK,
                CUSTOM_NETWORK,
                TERMINAL_NETWORK
        };
        for(PortalFlag flag : stargateFlags) {
            registerFlag(flag);
        }
    }
    
    public static void registerFlag(PortalFlag flag) {
        if (charMap.containsKey(flag.getCharacterRepresentation())) {
            throw new IllegalArgumentException(
                    String.format("The flag-character '%s' is already in use, could not register %s", flag.getCharacterRepresentation(),flag.toString()));
        }
        if (nameMap.containsKey(flag.toString())) {
            throw new IllegalArgumentException(String.format("The flag-name '%s' is already in use", flag.toString()));
        }
        charMap.put(flag.getCharacterRepresentation(), flag);
        nameMap.put(flag.toString(), flag);
        list.add(flag);
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
        Set<PortalFlag> foundFlags = new HashSet<>();
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
    public static PortalFlag valueOf(char label) {
        PortalFlag flag = charMap.get(label);
        if (flag != null) {
            return flag;
        } else {
            throw new IllegalArgumentException("No flag has been assigned the char '" + label + "'");
        }
    }
    
    /**
     * 
     * @param name
     * @return
     * @throws IllegalArgumentException <p>If unable to find a matching flag</p>
     */
    public static PortalFlag valueOf(String name) {
        PortalFlag flag = nameMap.get(name);
        if (flag != null) {
            return flag;
        } else {
            throw new IllegalArgumentException("No flag has been assigned the name '" + name + "'");
        }
    }
    public static List<PortalFlag> values(){
        return list;
    }
    
    @Override
    public boolean equals(Object other) {
        if(other == null || !other.getClass().equals(PortalFlag.class)) {
            return false;
        }
        PortalFlag otherFlag = (PortalFlag) other;
        return otherFlag.getCharacterRepresentation().equals(this.getCharacterRepresentation());
    }

    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public int hashCode() {
        return this.getCharacterRepresentation();
    }
    
    
}