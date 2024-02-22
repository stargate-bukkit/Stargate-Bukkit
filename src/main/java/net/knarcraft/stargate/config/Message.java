package net.knarcraft.stargate.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Translated messages displayed to players
 */
public enum Message {

    /**
     * The prefix displayed in front of all messages shown in the chat
     */
    PREFIX("prefix"),

    /**
     * The message displayed when a player is teleported
     */
    TELEPORTED("teleportMsg"),

    /**
     * The message displayed when a player destroys a Stargate
     */
    DESTROYED("destroyMsg"),

    /**
     * The message displayed when the currently selected Stargate destination is invalid
     */
    INVALID_DESTINATION("invalidMsg"),

    /**
     * The message displayed when the destination portal is busy with another player
     */
    DESTINATION_BLOCKED("blockMsg"),

    /**
     * The message displayed when the Stargate has no destinations available to the player
     */
    NO_DESTINATION("destEmpty"),

    /**
     * The message displayed when a player is denied access to any action
     */
    ACCESS_DENIED("denyMsg"),

    /**
     * The message displayed when the plugin is reloaded
     */
    RELOADED("reloaded"),

    /**
     * The message displayed when a player has some currency deducted from their account
     */
    ECONOMY_DEDUCTED("ecoDeduct"),

    /**
     * The message displayed when a player has some currency refunded to their account
     */
    ECONOMY_REFUNDED("ecoRefund"),

    /**
     * The message displayed when a player obtains some currency to their account (from portal usage)
     */
    ECONOMY_OBTAINED("ecoObtain"),

    /**
     * The message displayed when the player has an insufficient amount of currency to perform an action
     */
    ECONOMY_INSUFFICIENT("ecoInFunds"),

    /**
     * The message displayed when economy fails to load
     */
    ECONOMY_LOAD_ERROR("ecoLoadError"),

    /**
     * The message displayed when Vault fails to load
     */
    VAULT_LOAD_ERROR("vaultLoadError"),

    /**
     * The message displayed when Vault successfully loads
     */
    VAULT_LOADED("vaultLoaded"),

    /**
     * The message displayed when a Stargate is successfully created
     */
    CREATED("createMsg"),

    /**
     * The message displayed when a player is denied from creating a Stargate on the selected network
     */
    CREATION_NETWORK_DENIED("createNetDeny"),

    /**
     * The message displayed when a player is denied from creating a Stargate of the given gate type
     */
    CREATION_GATE_DENIED("createGateDeny"),

    /**
     * The message displayed when a Stargate is created on the player's personal network
     */
    CREATION_PERSONAL("createPersonal"),

    /**
     * The message displayed when the name of a Stargate is too short or too long
     */
    CREATION_NAME_LENGTH("createNameLength"),

    /**
     * The message displayed when another Stargate on the network has the same name as the new Stargate
     */
    CREATION_NAME_COLLISION("createExists"),

    /**
     * The message displayed when the specified network is full
     */
    CREATION_NETWORK_FULL("createFull"),

    /**
     * The message displayed when a player is denied from creating a Stargate in the current world
     */
    CREATION_WORLD_DENIED("createWorldDeny"),

    /**
     * The message displayed when a gate is physically conflicting with another
     */
    CREATION_CONFLICT("createConflict"),

    /**
     * The right-click prompt displayed on Stargate signs
     */
    SIGN_RIGHT_CLICK("signRightClick"),

    /**
     * The to use prompt displayed on Stargate signs
     */
    SIGN_TO_USE("signToUse"),

    /**
     * The random string displayed on Stargate signs
     */
    SIGN_RANDOM("signRandom"),

    /**
     * The disconnected string displayed on Stargate signs
     */
    SIGN_DISCONNECTED("signDisconnected"),

    /**
     * The invalid gate string displayed on Stargate signs
     */
    SIGN_INVALID("signInvalidGate"),

    /**
     * The message displayed if trying to create a bungee gate when bungee is disabled
     */
    BUNGEE_DISABLED("bungeeDisabled"),

    /**
     * The message displayed when a player is denied from creating a bungee Stargate
     */
    BUNGEE_CREATION_DENIED("bungeeDeny"),

    /**
     * The message displayed if a Stargate is missing the destination, the network or both
     */
    BUNGEE_MISSING_INFO("bungeeEmpty"),

    /**
     * The teleportation prompt shown on bungee signs
     */
    BUNGEE_SIGN("bungeeSign"),

    /**
     * The format of the title of the portal info shown in chat
     */
    PORTAL_INFO_TITLE("portalInfoTitle"),

    /**
     * The format of the name of the portal info shown in chat
     */
    PORTAL_INFO_NAME("portalInfoName"),

    /**
     * The format of the destination of the portal info shown in chat
     */
    PORTAL_INFO_DESTINATION("portalInfoDestination"),

    /**
     * The format of the network of the portal info shown in chat
     */
    PORTAL_INFO_NETWORK("portalInfoNetwork"),

    /**
     * The format of the server of the portal info shown in chat
     */
    PORTAL_INFO_SERVER("portalInfoServer"),

    /**
     * The author that created the loaded translation
     */
    AUTHOR("author"),
    ;

    private final String key;

    /**
     * Instantiates a new message
     *
     * @param key <p>The key of the message in the language files</p>
     */
    Message(@NotNull String key) {
        this.key = key;
    }

    /**
     * Gets the language file key for this message
     *
     * @return <p>This message's key</p>
     */
    @NotNull
    public String getKey() {
        return this.key;
    }

    /**
     * Gets the message corresponding to the given key
     *
     * @param key <p>The key to get a message from</p>
     * @return <p>The message, or null if not found</p>
     */
    @Nullable
    public static Message getFromKey(@NotNull String key) {
        for (Message message : Message.values()) {
            if (message.getKey().equalsIgnoreCase(key)) {
                return message;
            }
        }

        return null;
    }

    @Override
    @NotNull
    public String toString() {
        return this.getKey();
    }

}
