package net.TheDgtl.Stargate.property;

import java.util.List;

import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;

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
    
    BLOCK_DISPENSE("BlockDispenseEvent");
    
    
    private String eventName;

    BlockEventType(String eventName) {
        this.eventName = eventName;
    }
    
    /**
     * 
     * @return <p> Is the event eligible to destroy a portal </p>
     */
    public boolean canDestroyPortal() {
        List<String> events = ConfigurationHelper.getStringList(ConfigurationOption.SPECIFIC_PROTECTION_OVERRIDE);
        if(events == null) {
            return false;
        }
        return events.contains(this.eventName);
    }
}
