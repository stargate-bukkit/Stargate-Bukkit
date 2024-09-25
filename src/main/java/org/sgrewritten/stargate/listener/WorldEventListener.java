package org.sgrewritten.stargate.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

import java.util.List;

public class WorldEventListener implements Listener {

    private final StargateAPI stargateAPI;

    public WorldEventListener(StargateAPI stargateAPI) {
        this.stargateAPI = stargateAPI;
    }

    @EventHandler
    void onWordUnload(WorldUnloadEvent event) {
        List<RealPortal> unloadedPortals = stargateAPI.getRegistry().getAllPortals()
                .filter(portal -> portal instanceof RealPortal)
                .map(portal -> (RealPortal) portal)
                .filter(realPortal -> realPortal.getWorldUuid().equals(event.getWorld().getUID()))
                .toList();
        unloadedPortals.forEach(stargateAPI.getRegistry()::unregisterPortal);
        unloadedPortals.forEach(portal -> portal.getNetwork().removePortal(portal));
        stargateAPI.getRegistry().updateAllPortals();
    }

    @EventHandler
    void onWorldLoad(WorldLoadEvent event) {
        stargateAPI.getNetworkManager().loadWorld(event.getWorld(), stargateAPI);
        stargateAPI.getRegistry().updateAllPortals();
    }
}
