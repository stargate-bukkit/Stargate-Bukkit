package org.sgrewritten.stargate.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.thread.task.StargateAsyncTask;
import org.sgrewritten.stargate.thread.task.StargateQueuedAsyncTask;
import org.sgrewritten.stargate.thread.task.StargateRegionTask;
import org.sgrewritten.stargate.thread.task.StargateTask;

import java.io.File;
import java.util.Objects;

public class StargateTestHelper {

    private static final String SERVER_NAME = "test_server";
    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");

    public static ServerMock setup() {
        ServerMock server = MockBukkit.mock();
        GateFormatRegistry.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR)));
        Stargate.setServerName(SERVER_NAME);
        System.setProperty("bstats.relocatecheck", "false");
        return server;
    }

    public static void tearDown() {
        runAllTasks();
        MockBukkit.unmock();
    }

    public static void runAllTasks() {
        StargateQueuedAsyncTask.disableAsyncQueue();
        MockBukkit.getMock().getScheduler().waitAsyncTasksFinished();
        try {
            StargateTask.forceRunAllTasks();
        } catch (Exception e) {
            Stargate.log(e);
        }
        StargateRegionTask.clearPopulator();
    }

}
