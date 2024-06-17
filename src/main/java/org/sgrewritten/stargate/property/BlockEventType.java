package org.sgrewritten.stargate.property;

import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.config.ConfigurationHelper;

import java.util.List;

/**
 * Enum to keep track of all supported block events that can destroy a portal
 */
public enum BlockEventType {

    BLOCK_BURN("BlockBurnEvent"),

    BLOCK_PLACE("BlockPlaceEvent"),

    BLOCK_FADE("BlockFadeEvent"),

    BLOCK_FERTILIZE("BlockFertilizeEvent"),

    BLOCK_PHYSICS("BlockPhysicsEvent"),

    BLOCK_FROM_TO("BlockFromToEvent"),

    BLOCK_FORM("BlockFormEvent"),

    BLOCK_MULTI_PLACE("BlockMultiPlaceEvent"),

    BLOCK_PISTON_RETRACT("BlockPistonRetractEvent"),

    BLOCK_PISTON_EXTEND("BlockPistonExtendEvent"),

    ENTITY_BLOCK_FORM("EntityBlockFormEvent"),

    LEAVES_DECAY("LeavesDecayEvent"),

    SPONGE_ABSORB("SpongeAbsorbEvent"),

    ENTITY_CHANGE_EVENT_BLOCK("EntityChangeBlockEvent"),

    ENTITY_BREAK_DOOR("EntityBreakDoorEvent"),

    PORTAL_CREATE("PortalCreateEvent"),

    ENTITY_PLACE("EntityPlaceEvent"),

    PLAYER_BUCKET_EMPTY("PlayerBucketEmptyEvent"),

    BLOCK_DISPENSE("BlockDispenseEvent"),

    BLOCK_BREAK("BlockBreakEvent"),

    TNT_PRIME("TNTPrimeEvent"),

    ENTITY_EXPLODE("EntityExplodeEvent"),
    BLOCK_EXPLODE("BlockExplodeEvent");


    private final String eventName;

    BlockEventType(String eventName) {
        this.eventName = eventName;
    }

    /**
     * @return <p> Is the event eligible to destroy a portal </p>
     */
    public boolean canDestroyPortal() {
        List<String> events = ConfigurationHelper.getStringList(ConfigurationOption.SPECIFIC_PROTECTION_OVERRIDE);
        if (events.isEmpty()) {
            return false;
        }
        return events.contains(this.eventName);
    }
}
