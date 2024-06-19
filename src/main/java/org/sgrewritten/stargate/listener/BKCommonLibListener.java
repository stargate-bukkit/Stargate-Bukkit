package org.sgrewritten.stargate.listener;

import com.bergerkiller.bukkit.common.bases.IntVector2;
import com.bergerkiller.bukkit.common.events.MultiBlockChangeEvent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.StargateChunk;
import org.sgrewritten.stargate.util.portal.PortalHelper;

import java.util.HashSet;
import java.util.Set;

public class BKCommonLibListener implements Listener {

    private final RegistryAPI registry;
    private final NetworkManager networkManager;

    /**
     *
     * @param registryAPI <p>A stargate registry</p>
     * @param networkManager <p>A network manager</p>
     */
    public BKCommonLibListener(RegistryAPI registryAPI, NetworkManager networkManager) {
        this.registry = registryAPI;
        this.networkManager = networkManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onMultiBlockChangeEvent(MultiBlockChangeEvent event) {
        World world = event.getWorld();
        Set<IntVector2> coordinates = event.getChunkCoordinates();
        Set<RealPortal> portalsToValidate = new HashSet<>();
        for (IntVector2 coordinate : coordinates) {
            StargateChunk chunk = new StargateChunk(coordinate, world);
            portalsToValidate.addAll(registry.getPortalsInChunk(chunk));
        }
        portalsToValidate.forEach(portal -> PortalHelper.portalValidityCheck(portal, networkManager));
    }
}
