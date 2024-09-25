package org.sgrewritten.stargate.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.thread.task.StargateQueuedAsyncTask;
import org.sgrewritten.stargate.thread.task.StargateRegionTask;
import org.sgrewritten.stargate.thread.task.StargateTask;

public class StargateTestHelper {
    public static void runAllTasks() {
        StargateQueuedAsyncTask.waitForEmptyQueue();
        try {
            StargateTask.forceRunAllTasks();
        } catch (Exception e) {
            Stargate.log(e);
        }
        StargateRegionTask.clearPopulator();
    }
}
