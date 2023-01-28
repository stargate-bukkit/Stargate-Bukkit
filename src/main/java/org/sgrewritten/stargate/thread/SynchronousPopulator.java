package org.sgrewritten.stargate.thread;

import org.bukkit.plugin.Plugin;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.ForcibleAction;
import org.sgrewritten.stargate.action.SimpleAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    private static final int MAX_FORCE_COUNTER = 20;
    private final List<SimpleAction> populatorQueue = new ArrayList<>();
    private final List<SimpleAction> bungeePopulatorQueue = new ArrayList<>();
    private final List<SimpleAction> addList = new ArrayList<>();
    private final List<SimpleAction> bungeeAddList = new ArrayList<>();

    @Override
    public void run() {
        populatorQueue.addAll(addList);
        addList.clear();

        if (Stargate.knowsServerName()) {
            bungeePopulatorQueue.addAll(bungeeAddList);
            bungeeAddList.clear();
        }

        cycleQueues(false);
    }

    /**
     * Adds a populator action to the queue
     *
     * @param action <p>The action to add</p>
     */
    public void addAction(SimpleAction action) {
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
    public void addAction(SimpleAction action, boolean isBungee) {
        if (action != null) {
            Stargate.log(Level.FINEST, "Adding action " + action);
        }
        (isBungee ? this.bungeeAddList : this.addList).add(action);
    }

    /**
     * Force all populator tasks to be performed and clear the tasks unable to be performed
     */
    public void clear() {
        populatorQueue.addAll(addList);
        addList.clear();
        bungeePopulatorQueue.addAll(bungeeAddList);
        bungeeAddList.clear();
        int counter = 0;
        while (hasNotCompletedAllTasks() && counter < MAX_FORCE_COUNTER) {
            cycleQueues(true);
            counter++;
        }
        populatorQueue.clear();
        bungeePopulatorQueue.clear();
    }

    /**
     * Cycle through all queues and perform necessary actions
     *
     * @param forceAction <p>Whether to force the actions to run, regardless of anything that might prevent them</p>
     */
    private void cycleQueues(boolean forceAction) {
        cycleQueue(populatorQueue, forceAction);
        if (!Stargate.knowsServerName()) {
            return;
        }
        cycleQueue(bungeePopulatorQueue, forceAction);
    }

    /**
     * Goes through a populator action queue and runs the action
     *
     * <p>If more than 25 milliseconds pass, the cycling will stop. This is mainly a protection against infinite
     * loops, but it will also reduce lag when there are many actions in the queue.</p>
     *
     * @param queue       <p>The queue to cycle through</p>
     * @param forceAction <p>Whether to force the actions to run, regardless of anything that might prevent them</p>
     */
    private void cycleQueue(List<SimpleAction> queue, boolean forceAction) {
        Iterator<SimpleAction> iterator = queue.iterator();
        long initialSystemTime = System.nanoTime();

        //Go through all populator actions until 25 milliseconds have passed, or the queue is empty
        while (iterator.hasNext() && (System.nanoTime() - initialSystemTime < 25000000)) {
            try {
                SimpleAction action = iterator.next();
                if (action instanceof ForcibleAction) {
                    ((ForcibleAction) action).run(forceAction);
                } else {
                    action.run();
                }

                if (action.isFinished()) {
                    iterator.remove();
                }
            } catch (Exception e) {
                Stargate.log(e);
                iterator.remove();
            }
        }
    }

    public boolean hasNotCompletedAllTasks() {
        return !populatorQueue.isEmpty() || !bungeePopulatorQueue.isEmpty() || !addList.isEmpty() || !bungeeAddList.isEmpty();
    }

}
