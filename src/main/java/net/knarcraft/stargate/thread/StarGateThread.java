package net.knarcraft.stargate.thread;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.Portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * This class contains the function used to close servers which should no longer be open/active
 */
public class StarGateThread implements Runnable {

    @Override
    public void run() {
        long time = System.currentTimeMillis() / 1000;
        closeOpenPortals(time);
        deactivateActivePortals(time);
    }

    /**
     * Closes portals which are open and have timed out
     *
     * @param time <p>The current time</p>
     */
    private void closeOpenPortals(long time) {
        List<Portal> closedPortals = new ArrayList<>();
        Queue<Portal> openPortalsQueue = Stargate.getStargateConfig().getOpenPortalsQueue();

        for (Portal portal : openPortalsQueue) {
            //Skip always open and non-open gates
            if (portal.getOptions().isAlwaysOn() || portal.getOptions().isRandom() || portal.getOptions().isBungee() ||
                    !portal.isOpen()) {
                continue;
            }
            if (time > portal.getTriggeredTime() + Stargate.getGateConfig().getOpenTime()) {
                portal.getPortalOpener().closePortal(false);
                closedPortals.add(portal);
            }
        }
        openPortalsQueue.removeAll(closedPortals);
    }

    /**
     * De-activates portals which are active and have timed out
     *
     * @param time <p>The current time</p>
     */
    private void deactivateActivePortals(long time) {
        List<Portal> deactivatedPortals = new ArrayList<>();
        Queue<Portal> activePortalsQueue = Stargate.getStargateConfig().getActivePortalsQueue();

        for (Portal portal : activePortalsQueue) {
            //Skip portals which aren't active
            if (!portal.getPortalActivator().isActive()) {
                continue;
            }
            if (time > portal.getTriggeredTime() + Stargate.getGateConfig().getActiveTime()) {
                portal.getPortalActivator().deactivate();
                deactivatedPortals.add(portal);
            }
        }
        activePortalsQueue.removeAll(deactivatedPortals);
    }

}
