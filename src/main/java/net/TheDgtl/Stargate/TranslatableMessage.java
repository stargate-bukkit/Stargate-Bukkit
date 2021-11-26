package net.TheDgtl.Stargate;

import java.util.HashMap;
import java.util.logging.Level;

/**
 * Manages stargate's localisation.
 * @author Thorin
 * @author Pheotis
 */
public enum TranslatableMessage {
    PREFIX("prefix"), TELEPORT("teleportMsg"), DESTROY("destroyMsg"), INVALID("invalidMsg"), BLOCKED("blockMsg"),
    DEST_EMPTY("destEmpty"), DENY("denyMsg"), SPAWN_BLOCK("spawnBlockMsg"), DEDUCT("ecoDeduct"), REFUND("ecoRefund"),
    ECO_OBTAIN("ecoObtain"), LACKING_FUNDS("ecoInFunds"), CREATE("createMsg"), NET_DENY("createNetDeny"),
    GATE_DENY("createGateDeny"), CREATE_PERSONAL("createPersonal"), NAME_LENGTH_FAULT("createNameLength"),
    ALREADY_EXIST("createExists"), NET_FULL("createFull"), WORLD_DENY("createWorldDeny"),
    GATE_CONFLICT("createConflict"), RIGHT_CLICK("signRightClick"), TO_USE("signToUse"), RANDOM("signRandom"),
    DISCONNECTED("signDisconnected"), BUNGEE_DISABLED("bungeeDisabled"), BUNGEE_DENY("bungeeDeny"),
    BUNGEE_EMPTY("bungeeEmpty"), BUNGEE_SIGN("bungeeSign");

    private final String key;
    private static final HashMap<String, TranslatableMessage> map = new HashMap<>();

    static {
        for (TranslatableMessage value : values()) {
            map.put(value.getString(), value);
        }
    }

    TranslatableMessage(String key) {
        this.key = key;
    }

    public String getString() {
        return key;
    }

    public static TranslatableMessage parse(String value) {
        for (TranslatableMessage enumeration : values()) {
            if (enumeration.getString().equals(value))
                return enumeration;
        }
        return null;
    }
}
