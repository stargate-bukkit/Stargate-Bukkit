package org.sgrewritten.stargate.property;

import java.util.logging.Level;

/**
 * Some stargate constants used internally
 */
public class StargateConstant {

    private StargateConstant(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * The name of Stargate
     */
    public static final String STARGATE_NAME = "Stargate";

    /**
     * Where the default gate formats are stored internally
     */
    public static final String INTERNAL_GATE_FOLDER = "gates";

    /**
     * Where should stargate store internal data (such as server uuid, and the .properties database)
     */
    public static final String INTERNAL_FOLDER = ".internal";

    /**
     * The name of the stargate properties database
     */
    public static final String INTERNAL_PROPERTIES_FILE = "stargate.properties";

    /**
     * The name of the config file
     */
    public static final String CONFIG_FILE = "config.yml";

    /**
     * The current config (and database) version; used in migrations
     */
    public static final int CURRENT_CONFIG_VERSION = 9;

    /**
     * The log level to use before loading the log level from config
     */
    public static final Level PRE_STARTUP_LOG_LEVEL = Level.INFO;

    /**
     * Maximum name length of networks and portals
     */
    public static final int MAX_TEXT_LENGTH = 14;

    /**
     * The id of the default network
     */
    public static final String DEFAULT_NETWORK_ID = "<@default@>";
}
