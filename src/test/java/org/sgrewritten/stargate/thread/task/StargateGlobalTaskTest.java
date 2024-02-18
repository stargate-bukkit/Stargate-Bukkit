package org.sgrewritten.stargate.thread.task;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.Stargate;

class StargateGlobalTaskTest {

    private ServerMock serverMock;

    @BeforeEach
    void setUp() {
        this.serverMock = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void run_noDuplicates() {
        TestRunnable runnable = new TestRunnable();
        StargateGlobalTask task = new StargateGlobalTask(runnable);
        task.run();
        task.run();
        serverMock.getScheduler().performOneTick();
        Assertions.assertTrue(runnable.hasRunBefore);
    }

    @Test
    void run_bungee_noDuplicates() {
        TestRunnable runnable = new TestRunnable();
        StargateGlobalTask task = new StargateGlobalTask(runnable);
        task.run(true);
        serverMock.getScheduler().performTicks(1000);
        Assertions.assertFalse(runnable.hasRunBefore);
        // Necessary to add a player here to avoid recursion loop
        serverMock.addPlayer();
        MockBukkit.unmock();
        Assertions.assertTrue(runnable.hasRunBefore);
    }

    @Test
    void runDelayed_noDuplicates() {
        TestRunnable runnable = new TestRunnable();
        StargateGlobalTask task = new StargateGlobalTask(runnable);
        task.runDelayed(2);
        task.runDelayed(2);
        serverMock.getScheduler().performTicks(2);
        Assertions.assertTrue(runnable.hasRunBefore);
    }

    private class TestRunnable implements Runnable {
        boolean hasRunBefore = false;

        @Override
        public void run() {
            Assertions.assertFalse(hasRunBefore);
            hasRunBefore = true;
        }
    }
}