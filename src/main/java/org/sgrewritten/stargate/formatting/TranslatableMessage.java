package org.sgrewritten.stargate.formatting;

import java.util.HashMap;

/**
 * Keeps track of all translatable messages
 *
 * @author Thorin
 * @author Pheotis
 */
public enum TranslatableMessage {

    /**
     * The [Stargate] prefix used at the start of all messages
     */
    PREFIX("prefix"),

    /**
     * The message to display when a player is being teleported
     */
    TELEPORT("tpSuccess"),

    /**
     * The message to display when a stargate has been destroyed
     */
    DESTROY("gateDestroy"),

    /**
     * The message to display when a networked portal does not have any destinations
     */
    DESTINATION_EMPTY("tpEmptyNet"),

    /**
     * The message to display if a stargate doesn't have a valid destination
     *
     * <p>This is triggered when a stargate's button is clicked and it's missing a valid destination. It's also
     * triggered when trying to teleport if the destination is missing.</p>
     */
    INVALID("tpDestInvalid"),

    /**
     * The message to be displayed when a player is missing the required permissions to teleport to a destination
     */
    DENY("conflictPerms"),

    /**
     * The message to be displayed when gate is used by another player
     */
    TELEPORTATION_OCCUPIED("tpOccupied"),

    /**
     * The message to be displayed if a player is missing the sufficient funds to perform an action
     */
    LACKING_FUNDS("ecoInsolvent"),

    /**
     * The message to be displayed when a player successfully is charged money
     */
    ECO_DEDUCT("ecoDeduct"),

    /**
     * The message to be displayed when a player receives money from an owned portal
     */
    ECO_OBTAIN("ecoObtain"),

    /**
     * The message to be displayed when a stargate has been successfully created
     */
    CREATE("gateCreateSpecified"),

    /**
     * The message to be display when a portal has successfully been created on a personal network
     */
    CREATE_PERSONAL("gateCreatePersonal"),
    /**
     * The message to display when a player is lacking the permission to access a network
     */
    NET_DENY("faultNetwork"),

    /**
     * The message to display when a network is in conflict with a network of another type
     */
    NET_CONFLICT("faultNetworkConflict"),

    /**
     * The message to display when a player is lacking permission to create a gateDesign
     */
    GATE_DENY("faultLayout"),

    /**
     * The message to display when a player does not have access to a world
     */
    WORLD_DENY("faultWorldDeny"),

    /**
     * The message to display when a portal or network name exceeds the max length
     */
    INVALID_NAME_LENGTH("faultLength"),

    /**
     * The message to display when the NetworkType is not compatible with given name
     */
    INVALID_NAME("faultName"),

    /**
     * A generic message to display when an addon prevents the user from doing something
     */
    ADDON_INTERFERE("faultAddon"),

    /**
     * The message to display when a new stargate's name is already in use
     */
    GATE_ALREADY_EXIST("faultExists"),

    /**
     * The message to display when a network is already full
     */
    NET_FULL("faultFull"),

    /**
     * The message to display if a new stargate conflicts with an existing one
     */
    GATE_CONFLICT("faultConflict"),

    /**
     * The first line of the right-click prompt to display on signs
     */
    RIGHT_CLICK("signNonFixedL2"),

    /**
     * The second line of the right-click prompt to display on signs
     */
    TO_USE("signNonFixedL3"),

    /**
     * The sign text to use for marking a stargate as random
     */
    RANDOM("signRandom"),

    /**
     * The sign text to use for marking a stargate as disconnected
     */
    DISCONNECTED("signDisconnected"),

    /**
     * The message to display when a player is lacking permissions to create a bungee or inter-server network
     */
    BUNGEE_DENY("bungeePerms"),

    /**
     * The message to display if a BungeeCord exit network is invalid
     *
     * <p>The message to display if a player is teleporting through BungeeCord and the player cannot be teleported to
     * the destination portal because the network given in the teleportation message is invalid.</p>
     */
    BUNGEE_INVALID_NETWORK("bungeeInvalidExitNetwork"),

    /**
     * The message to display if a BungeeCord exit gate is invalid
     *
     * <p>The message to display if a player is teleporting through BungeeCord and the player cannot be teleported to
     * the destination portal because the gate given in the teleportation message is invalid.</p>
     */
    BUNGEE_INVALID_GATE("bungeeInvalidExitGate"),

    /**
     * The message to display when a player tries to interact / create / enter a bungee or inter-server gate.
     */
    BUNGEE_DISABLED("bungeeDisabled"),

    /**
     * The message to display when a Fancy Inter-server type portal is created, but server is on a local database
     */
    INTER_SERVER_DISABLED("bungeeLocal"),

    /**
     * The last line on a sign for legacy bungee portals
     */
    BUNGEE_SIGN_LINE_4("signLegacyBungee"),

    /**
     * The message to display
     */
    LACKING_FLAGS_PERMISSION("faultFlag"),

    /**
     * The message to display if creating a legacy BungeeCord portal and the sign is missing information
     */
    BUNGEE_LACKING_SIGN_INFORMATION("bungeeLegacySyntax"),

    /**
     * The message to display if a stargate is created within the spawn area
     */
    SPAWN_CHUNKS_CONFLICTING("conflictSpawn"),

    /**
     * The message to display during portal creation if the chosen network changed
     */
    GATE_CREATE_FALLBACK("gateCreateFallback"),

    /**
     * The message to display whenever the reload command is executed
     */
    COMMAND_RELOAD("adminReload"),

    /**
     * The message to display whenever the admin-info is executed
     */
    COMMAND_INFO("adminInfo"),

    /**
     * The message to display whenever the admin-help is executed
     */
    COMMAND_HELP("adminHelp"),

    /**
     * The second line of a portals sign when displaying portal info
     */
    GATE_OWNED_BY("signPortalOwner"),

    /**
     * The message to send if the player is teleporting past the worldborder
     */
    OUTSIDE_WORLDBORDER("tpPastBorder"),

    /**
     * The message to send when there is a interserver conflict
     */
    UNIMPLEMENTED_CONFLICT("unimplementedIConflict"),

    /**
     * The message to send whenever a interserver portal is created
     */
    UNIMPLEMENTED_INTERSERVER("unimplementedInterserver"),

    /**
     * The message to send when a flag is not implemented yet
     */
    UNIMPLEMENTED_FLAG("unimplementedFlag"),

    /**
     * The name of a portal
     */
    GATE("gate"),

    /**
     * The name of a local network
     */
    NETWORK("network"),

    /**
     * The name of a cross server network
     */
    FANCY_INTER_SERVER("interserver"),

    /**
     * The name of the default network
     */
    DEFAULT_NETWORK("default"),

    /**
     * The name of a terminal network
     */
    TERMINAL_NETWORK("terminal"),

    /**
     * The name of a personal network
     */
    PERSONAL_NETWORK("personal"),

    /**
     * The name of a custom network
     */
    CUSTOM_NETWORK("custom");

    /*
     * Available additional strings:
     * signTerminalL2
     * gateTerminalSold
     * gateTerminalSale
     * bungeeProxyOffline
     * bungeeTargetOffline
     * tpDestBlocked
     */

    private final String key;
    private static final HashMap<String, TranslatableMessage> map = new HashMap<>();

    /**
     * Instantiates a new translatable message
     *
     * @param key <p>The string key used to identify the message in the language files</p>
     */
    TranslatableMessage(String key) {
        this.key = key;
    }

    /**
     * Gets this translatable message's string key used to identify the message in language files
     *
     * @return <p>This translatable message's string key</p>
     */
    public String getMessageKey() {
        return key;
    }

    /**
     * Parses the given string key into its enum representation
     *
     * @param key <p>The string key to parse</p>
     * @return <p>The TranslatableMessage corresponding to the key, or null if not found</p>
     */
    public static TranslatableMessage parse(String key) {
        if (map.isEmpty()) {
            for (TranslatableMessage message : values()) {
                map.put(message.getMessageKey(), message);
            }
        }

        for (TranslatableMessage enumeration : values()) {
            if (enumeration.getMessageKey().equals(key)) {
                return enumeration;
            }
        }
        return null;
    }

}
