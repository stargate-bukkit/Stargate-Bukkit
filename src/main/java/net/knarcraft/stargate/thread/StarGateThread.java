package net.knarcraft.stargate.thread;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.Portal;

import java.util.Iterator;

/**
 * This class contains the function used to close servers which should no longer be open/active
 */
public class StarGateThread implements Runnable {

    @Override
    public void run() {
        long time = System.currentTimeMillis() / 1000;
        // Close open portals
        for (Iterator<Portal> iterator = Stargate.openList.iterator(); iterator.hasNext(); ) {
            Portal portal = iterator.next();
            // Skip always open and non-open gates
            if (portal.isAlwaysOn() || !portal.isOpen()) {
                continue;
            }
            if (time > portal.getOpenTime() + Stargate.getOpenTime()) {
                portal.close(false);
                iterator.remove();
            }
        }
        // Deactivate active portals
        for (Iterator<Portal> iterator = Stargate.activeList.iterator(); iterator.hasNext(); ) {
            Portal portal = iterator.next();
            if (!portal.isActive()) {
                continue;
            }
            if (time > portal.getOpenTime() + Stargate.getActiveTime()) {
                portal.deactivate();
                iterator.remove();
            }
        }
    }

}
