package org.sgrewritten.stargate.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

public class WorldEventListener implements Listener {

    private final StargateAPI stargateAPI;

    public WorldEventListener(StargateAPI stargateAPI) {
        this.stargateAPI = stargateAPI;
    }

    @EventHandler
    void onWordUnload(WorldUnloadEvent event) {
        stargateAPI.getRegistry().getAllPortals().filter(portal -> portal instanceof RealPortal)
                .map(portal -> (RealPortal) portal)
                .filter(realPortal -> realPortal.getExit().getWorld().equals(event.getWorld()))
                .forEach(stargateAPI.getRegistry()::unregisterPortal);
    }

    @EventHandler
    void onWorldLoad(WorldLoadEvent event) {
        stargateAPI.getNetworkManager().loadWorld(event.getWorld(), stargateAPI);
    }
}
