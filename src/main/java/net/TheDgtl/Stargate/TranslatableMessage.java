package net.TheDgtl.Stargate;

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
    INVALID("tpDestiInvalid"),

    /**
     * The message to be displayed when a player is missing the required permissions to teleport to a destination
     */
    DENY("conflictPerms"),

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
    CREATE("gateCreatePersonal"),
//TODO: This is if it is a personal network, otherwise it is gateCreateSpecified

    /**
     * The message to display when a player is lacking the permission to access a network
     */
    NET_DENY("faultNetwork"),

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
    INVALID_NAME("faultLength"),

    /**
     * The message to display when a new stargate's name is already in use
     */
    ALREADY_EXIST("faultExists"),

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
     * The message to display when a BungeeCord portal if missing a destination or a network
     */
    BUNGEE_EMPTY("bungeeInvalidExitNetwork"),
//TODO: for networks only; otherwise, it is bungeeInvalidExitGate

    /**
     * The message to display when a player is lacking permissions to create a bungee or interserver network
     */
    BUNGEE_DENY("bungeePerms"),

    /**
     * The message to display when a player tries to interact / create / enter a bungee or interserver gate.
     */
    BUNGEE_DISABLED("bungeeDisabled");
//TODO: bungeeLocal distinction

/**
signLegacyBungee
signTerminalL2
gateCreateSpecified
gateTerminalSold
gateTerminalSale
faultFlag
bungeeLocal
bungeeLegacySyntax
bungeeInvalidExitNetwork
bungeeInvalidExitGate
bungeeProxyOffline
bungeeTargetOffline
tpDestiBlocked
conflictSpawn
ecoRefund
adminReload
adminInfo
adminHelp
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
            if (enumeration.getMessageKey().equals(key))
                return enumeration;
        }
        return null;
    }

}
