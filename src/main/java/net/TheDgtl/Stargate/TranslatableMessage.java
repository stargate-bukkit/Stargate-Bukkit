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
    TELEPORT("teleportMsg"),

    /**
     * The message to display when a stargate has been destroyed
     */
    DESTROY("destroyMsg"),

    /**
     * The message to display if a stargate doesn't have a valid destination
     *
     * <p>This is triggered when a stargate's button is clicked and it's missing a valid destination. It's also
     * triggered when trying to teleport if the destination is missing.</p>
     */
    INVALID("invalidMsg"),

    /**
     * The message to be displayed when a player is missing the required permissions to teleport to a destination
     */
    DENY("denyMsg"),

    /**
     * The message to be displayed if a player is missing the sufficient funds to perform an action
     */
    LACKING_FUNDS("ecoInFunds"),

    /**
     * The message to be displayed when a stargate has been successfully created
     */
    CREATE("createMsg"),

    /**
     * The message to display when a player is lacking the permission to access a network
     */
    NET_DENY("createNetDeny"),

    /**
     * The message to display when a portal or network name exceeds the max length
     */
    NAME_LENGTH_FAULT("createNameLength"),

    /**
     * The message to display when a new stargate's name is already in use
     */
    ALREADY_EXIST("createExists"),

    /**
     * The message to display when a network is already full
     */
    NET_FULL("createFull"),

    /**
     * The message to display if a new stargate conflicts with an existing one
     */
    GATE_CONFLICT("createConflict"),

    /**
     * The first line of the right-click prompt to display on signs
     */
    RIGHT_CLICK("signRightClick"),

    /**
     * The second line of the right-click prompt to display on signs
     */
    TO_USE("signToUse"),

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
    BUNGEE_EMPTY("bungeeEmpty");

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
