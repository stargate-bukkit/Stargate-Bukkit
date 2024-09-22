package org.sgrewritten.stargate.migration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.util.LegacyPortalStorageLoader;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class MigrationWorldLoadListener implements Listener {
    private final Set<String> invalidWorlds;
    private final File dir;
    private final String defaultNetworkName;
    private final StargateAPI stargateAPI;


    public MigrationWorldLoadListener(Set<String> invalidWorlds, File dir, String defaultNetworkName, StargateAPI stargateAPI) {
        this.invalidWorlds = invalidWorlds;
        this.dir = dir;
        this.defaultNetworkName = defaultNetworkName;
        this.stargateAPI = stargateAPI;
    }

    @EventHandler
    void onWorldLoad(WorldLoadEvent event) {
        String worldDatabaseName = event.getWorld().getName() + ".db";
        if (invalidWorlds.contains(worldDatabaseName)) {
            try {
                LegacyPortalStorageLoader.loadWorld(new File(dir, worldDatabaseName), defaultNetworkName, stargateAPI);
            } catch (IOException e) {
                Stargate.log(e);
            }
        }
    }
}
