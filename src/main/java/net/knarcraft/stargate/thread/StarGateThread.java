package net.knarcraft.stargate.thread;

import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.Stargate;

import java.util.Iterator;

/**
 * This class contains the function used to close servers which should no longer be open/active
 */
public class StarGateThread implements Runnable {

    public void run() {
        long time = System.currentTimeMillis() / 1000;
        // Close open portals
        for (Iterator<Portal> iterator = Stargate.openList.iterator(); iterator.hasNext(); ) {
            Portal p = iterator.next();
            // Skip always open gates
            if (p.isAlwaysOn()) continue;
            if (!p.isOpen()) continue;
            if (time > p.getOpenTime() + Stargate.getOpenTime()) {
                p.close(false);
                iterator.remove();
            }
        }
        // Deactivate active portals
        for (Iterator<Portal> iterator = Stargate.activeList.iterator(); iterator.hasNext(); ) {
            Portal p = iterator.next();
            if (!p.isActive()) continue;
            if (time > p.getOpenTime() + Stargate.getActiveTime()) {
                p.deactivate();
                iterator.remove();
            }
        }
    }

}
