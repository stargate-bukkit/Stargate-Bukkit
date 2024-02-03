package org.sgrewritten.stargate.property;

import java.util.logging.Level;

public class StargateConstant {

    private StargateConstant(){
        throw new IllegalStateException("Utility class");
    }
    public static final String STARGATE_NAME = "Stargate";
    public static final String INTERNAL_GATE_FOLDER = "gates";
    public static final String INTERNAL_FOLDER = ".internal";
    public static final String INTERNAL_PROPERTIES_FILE = "stargate.properties";
    public static final String CONFIG_FILE = "config.yml";
    public static final int CURRENT_CONFIG_VERSION = 9;
    public static final Level PRE_STARTUP_LOG_LEVEL = Level.INFO;
    public static final int MAX_TEXT_LENGTH = 13;
    public static final String DEFAULT_NETWORK_ID = "<@default@>";
}
