package net.TheDgtl.Stargate.listeners;

import net.TheDgtl.Stargate.Portal;
import net.TheDgtl.Stargate.Stargate;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;

public class WorldEventsListener extends StargateListener {

    public WorldEventsListener(@NotNull Stargate stargate) {
        super(stargate);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (!stargate.getManagedWorlds().contains(event.getWorld().getName())
                && Portal.loadAllGates(stargate, event.getWorld())) {
            stargate.getManagedWorlds().add(event.getWorld().getName());
        }
    }

    // We need to reload all gates on world unload, boo
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        Stargate.debug("onWorldUnload", "Reloading all Stargates");
        World w = event.getWorld();
        if (stargate.getManagedWorlds().contains(w.getName())) {
            stargate.getManagedWorlds().remove(w.getName());
            Portal.clearGates();
            for (World world : stargate.getServer().getWorlds()) {
                if (stargate.getManagedWorlds().contains(world.getName())) {
                    Portal.loadAllGates(stargate, world);
                }
            }
        }
    }
}
