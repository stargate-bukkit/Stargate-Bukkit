package net.TheDgtl.Stargate.property;

import java.util.List;

import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;

public enum BlockEventType {
    BlockBurnEvent,
    
    BlockPlaceEvent,

    BlockFadeEvent, BlockFertilizeEvent, BlockFormToEvent, BlockMultiPlaceEvent,
    EntityBlockFormEvent, LeavesDecayEvent, SpongeAbsorbEvent, EntityChangeBlockEvent, EntityBreakDoorEvent,
    PortalCreateEvent, EntityPlaceEvent;
    
    public boolean isDestroyPortal() {
        List<String> events = ConfigurationHelper.getStringList(ConfigurationOption.SPECIFIC_PROTECTION_OVERRIDE);
        if(events == null) {
            return false;
        }
        return events.contains(this.toString());
    }
}
