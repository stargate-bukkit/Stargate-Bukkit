package org.sgrewritten.stargate.thread;

import org.bukkit.Bukkit;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.SupplierAction;

public class ThreadHelper {

    /**
     * Makes sure that the runnable is always called syncronously.
     *
     * <p>Depending on whether this is in primary thread or not, this will directly run the runnable</p>
     *
     * @param runnable <p>The Runnable to be run</p>
     */
    public static void callSynchronously(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Stargate.addSynchronousTickAction(new SupplierAction(() -> {
                runnable.run();
                return true;
            }));
        }
    }

    /**
     * Run the task in this thread if not in primary thread, otherwise start a new async thread
     * @param runnable <p>The runnable to run</p>
     */
    public static void callAsynchronously(Runnable runnable){
        if (!Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(Stargate.getInstance(), runnable);
        }
    }
}
