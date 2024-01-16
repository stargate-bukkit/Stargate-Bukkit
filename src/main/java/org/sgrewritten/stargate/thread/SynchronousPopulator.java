package org.sgrewritten.stargate.thread;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.sgrewritten.stargate.Stargate;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

/**
 * Cycles through a queue of actions everytime the {@link SynchronousPopulator#run()} function is triggered.
 *
 * <p>If used with the {@link org.bukkit.scheduler.BukkitScheduler#scheduleSyncRepeatingTask(Plugin, Runnable, long, long)}
 * function, you can use this as a handy way to do synchronous tasks (tasks that happens during a specific tick).
 * Warning: Running this once, even by running forceDoAllTasks does not guarantee all tasks to finish.</p>
 *
 * @author Thorin
 */
public class SynchronousPopulator implements Runnable {
    private boolean queueOverflowHasBeenAchieved = false;
    private final Queue<Runnable> populatorQueue = new LinkedList<>();
    private final Queue<Runnable> bungeePopulatorQueue = new LinkedList<>();

    @Override
    public void run() {
        cycleQueue(populatorQueue);
        // Don't try to run any bungee related action unless the server knows its own uuid and there is a player online
        if (!Stargate.knowsServerName() || Bukkit.getServer().getOnlinePlayers().isEmpty()) {
            return;
        }
        cycleQueue(bungeePopulatorQueue);
    }

    /**
     * Adds a populator action to the queue
     *
     * @param action <p>The action to add</p>
     */
    public void addAction(Runnable action) {
        addAction(action, false);
    }

    /**
     * Adds a populator action to the queue
     *
     * <p>Actions in the Bungee queue are only performed once the server name is known. If you need this behavior, use
     * isBungee = true. If not, don't use isBungee.</p>
     *
     * @param action   <p>The action to add</p>
     * @param isBungee <p>Whether the action relies on the server name being known and should be put in the bungee queue</p>
     */
    public void addAction(Runnable action, boolean isBungee) {
        if (action != null) {
            Stargate.log(Level.FINEST, "Adding action " + action);
        }
        Queue<Runnable> queue = (isBungee ? this.bungeePopulatorQueue : this.populatorQueue);
        boolean couldAdd = queue.offer(action);
        if(!couldAdd && !queueOverflowHasBeenAchieved){
            Stargate.log(Level.SEVERE, "There are too many scheduled actions: " + queue.size() + " actions");
            queueOverflowHasBeenAchieved = true;
        }
    }

    /**
     * Force all populator tasks to be performed and clear the tasks unable to be performed
     */
    public void clear() {
        populatorQueue.clear();
        bungeePopulatorQueue.clear();
    }

    /**
     * Goes through a populator action queue and runs the action
     *
     * <p>If more than 25 milliseconds pass, the cycling will stop. This is mainly a protection against infinite
     * loops, but it will also reduce lag when there are many actions in the queue.</p>
     *
     * @param queue       <p>The queue to cycle through</p>
     */
    private void cycleQueue(Queue<Runnable> queue) {
        long initialSystemTime = System.nanoTime();

        //Go through all populator actions until 25 milliseconds have passed, or the queue is empty
        while (System.nanoTime() - initialSystemTime < 25000000) {
            try {
                Runnable action = queue.poll();
                if(action == null){
                    break;
                }
                action.run();
            } catch (Exception e) {
                Stargate.log(e);
            }
        }
    }
}
