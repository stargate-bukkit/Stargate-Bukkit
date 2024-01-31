package org.sgrewritten.stargate.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.thread.ThreadHelper;
import org.sgrewritten.stargate.thread.task.StargateAsyncTask;
import org.sgrewritten.stargate.thread.task.StargateRegionTask;
import org.sgrewritten.stargate.thread.task.StargateTask;

import java.io.File;
import java.util.Objects;

public class StargateTestHelper {

    private static final String SERVER_NAME = "test_server";
    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");

    public static ServerMock setup() {
        return setup(true);
    }

    public static ServerMock setup(boolean enableQueue){
        ServerMock server = MockBukkit.mock();
        GateFormatRegistry.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR)));
        Stargate.setServerName(SERVER_NAME);
        System.setProperty("bstats.relocatecheck", "false");
        if(enableQueue) {
            enableAsyncQueue();
        }
        return server;
    }

    public static void tearDown() {
        runAllTasks(false);
        MockBukkit.unmock();
    }

    public static void runAllTasks(){
        runAllTasks(true);
    }

    private static void runAllTasks(boolean reEnableAsyncQueue) {
        ThreadHelper.setAsyncQueueEnabled(false);
        MockBukkit.getMock().getScheduler().waitAsyncTasksFinished();
        if(ThreadHelper.getAsyncQueueEnabled()){
            throw new IllegalStateException("Async queue is already enabled");
        }
        try {
            StargateTask.forceRunAllTasks();
        } catch (Exception e) {
            Stargate.log(e);
        }
        StargateRegionTask.clearPopulator();
        if(reEnableAsyncQueue) {
            enableAsyncQueue();
        }
    }

    private static void enableAsyncQueue(){
        ThreadHelper.setAsyncQueueEnabled(true);
        new StargateAsyncTask(ThreadHelper::cycleThroughAsyncQueue).run();
    }

}
