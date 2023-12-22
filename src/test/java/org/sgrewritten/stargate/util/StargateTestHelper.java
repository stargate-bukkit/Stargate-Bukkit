package org.sgrewritten.stargate.util;

import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.gate.GateFormatHandler;

import java.io.File;
import java.util.Objects;

public class StargateTestHelper {

    private static final String SERVER_NAME = "test_server";
    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");

    public static void setup() {
        GateFormatRegistry.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR)));
        Stargate.setServerName(SERVER_NAME);
    }

}
