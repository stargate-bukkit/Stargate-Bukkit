package org.sgrewritten.stargate.util.portal;

import java.io.File;
import java.util.Objects;

import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.gate.GateFormatHandler;

public class GateTestHelper {
    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");
    
    public static void setUpGates() {

        GateFormatHandler.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR, new FakeStargateLogger())));
    }
}
