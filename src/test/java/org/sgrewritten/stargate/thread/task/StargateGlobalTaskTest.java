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
        TestRunnable runnable = new TestRunnable(true);
        runnable.runNow();
        runnable.runNow();
        serverMock.getScheduler().performOneTick();
        Assertions.assertTrue(runnable.hasRunBefore);
    }

    @Test
    void run_bungee_noDuplicates() {
        TestRunnable runnable = new TestRunnable(true, true);
        runnable.runNow();
        serverMock.getScheduler().performTicks(30);
        Assertions.assertFalse(runnable.hasRunBefore);
        // Necessary to add a player here to avoid recursion loop
        serverMock.addPlayer();
        Stargate.setKnowsServerName(true);
        serverMock.getScheduler().performTicks(30);
        Assertions.assertTrue(runnable.hasRunBefore);
    }

    @Test
    void runDelayed_noDuplicates() {
        TestRunnable runnable = new TestRunnable(true);
        runnable.runDelayed(2);
        runnable.runDelayed(2);
        serverMock.getScheduler().performTicks(2);
        Assertions.assertTrue(runnable.hasRunBefore);
    }

    @Test
    void runTaskTimer(){
        TestRunnable runnable = new TestRunnable(false);
        runnable.runTaskTimer(1,0);
        serverMock.getScheduler().performTicks(20);
        Assertions.assertEquals(20, runnable.runCount);
    }

    @Test
    void runTaskTimer_cancelled(){
        TestRunnable runnable = new TestRunnable(false) {
            @Override
            public void run() {
                super.run();
                if(runCount > 9){
                    this.cancel();
                }
                Assertions.assertTrue(runCount < 11);
            }
        };
        runnable.runTaskTimer(1,0);
        serverMock.getScheduler().performTicks(20);
        Assertions.assertEquals(10, runnable.runCount);
    }

    private static class TestRunnable extends StargateGlobalTask {
        private final boolean onlyAllowOneRun;
        boolean hasRunBefore = false;
        int runCount = 0;

        TestRunnable(boolean onlyAllowOneRun) {
            this(false, onlyAllowOneRun);
        }

        TestRunnable(boolean bungee,boolean onlyAllowOneRun) {
            super(bungee);
            this.onlyAllowOneRun = onlyAllowOneRun;
        }

        @Override
        public void run() {
            if(onlyAllowOneRun){
                Assertions.assertFalse(hasRunBefore);
            }
            hasRunBefore = true;
            runCount++;
        }
    }
}