package net.TheDgtl.Stargate.config;

import net.TheDgtl.Stargate.Stargate;

/**
 * An enum containing all available settings/configuration options
 *
 * @author Thorin
 */
public enum ConfigurationOption {

    /**
     * The default network if no network is specified
     */
    DEFAULT_NETWORK("defaultGateNetwork", "The default network if no network is specified", "central", OptionDataType.STRING),

    /**
     * The network used for all legacy BungeeCord Stargates
     */
    LEGACY_BUNGEE_NETWORK("legacyBungeeNetwork", "The network used for all legacy BungeeCord stargates", "LegacyBungee", OptionDataType.STRING),

    /**
     * The language used for all translatable messages
     */
    LANGUAGE("language", "The language used for all translatable messages", "en", OptionDataType.LANGUAGE),

    /**
     * Whether BungeeCord functionality is enabled
     */
    USING_BUNGEE("bungee.usingBungee", "Whether BungeeCord functionality is enabled", false, OptionDataType.BOOLEAN),

    /**
     * The maximum number of stargates on a single network
     */
    GATE_LIMIT("networkLimit", "The maximum number of stargates on a single network", -1, OptionDataType.INTEGER),

    /**
     * Whether to enable economy functionality for stargate interaction
     */
    USE_ECONOMY("useEconomy", "Whether to enable economy functionality for stargate interaction", false, OptionDataType.BOOLEAN),

    /**
     * The UUID to pay any taxes to
     */
    TAX_DESTINATION("taxAccount", "The UUID to pay any taxes to", "", OptionDataType.STRING),

    /**
     * The cost of creating a new stargate
     */
    CREATION_COST("creationCost", "The cost of creating a new stargate", 0, OptionDataType.INTEGER),

    /**
     * The cost of destroying a stargate
     */
    DESTROY_COST("destructionCost", "The cost of destroying a stargate", 0, OptionDataType.INTEGER),

    /**
     * The cost of using (teleporting through) a stargate
     */
    USE_COST("usageCost", "The cost of using (teleporting through) a stargate", 0, OptionDataType.INTEGER),

    /**
     * Whether to send any transaction fees to the gate owner's balance
     */
    GATE_OWNER_REVENUE("gateOwnerRevenue", "Whether to send any transaction fees to the gate owner's balance", true, OptionDataType.BOOLEAN),

    /**
     * Whether to charge for a teleportation from a non-free stargate even if the destination is free
     */
    CHARGE_FREE_DESTINATION("chargeFreeDestination", "Whether to charge for a teleportation from a non-free stargate even if the destination is free", true, OptionDataType.BOOLEAN),

    /**
     * The default color to use for "light" signs
     */
    DEFAULT_LIGHT_SIGN_COLOR("signStyle.defaultForeground", "The default color to use for \"light\" signs", "BLACK", OptionDataType.COLOR),

    /**
     * The default color to use for "dark" signs
     */
    DEFAULT_DARK_SIGN_COLOR("signStyle.defaultBackground", "The default color to use for \"dark\" signs", "WHITE", OptionDataType.COLOR),

    /**
     * The color style to use for the destination names displayed on a sign
     */
    NAME_STYLE("signStyle.listing", "The color style to use for the destination names displayed on a sign", 1, OptionDataType.INTEGER),

    /**
     * The color style to use for the pointers displayed on a sign
     */
    POINTER_STYLE("signStyle.pointer", "The color style to use for the pointers displayed on a sign", 2, OptionDataType.INTEGER),

    /**
     * The multiplier to use for the exit speed when leaving a Stargate
     *
     * <p>If the multiplier is 0, the behavior is the same as in legacy. If the multiplier is 1, it's the same as the
     * entry speed. 2 = double, 0.5 = half.</p>
     */
    GATE_EXIT_SPEED_MULTIPLIER("gateExitSpeedMultiplier", "The multiplier to use for the exit speed when leaving a Stargate", 1, OptionDataType.DOUBLE),

    /**
     * Whether to strip any color tags from stargate names and inter-server networks
     */
    DISABLE_CUSTOM_COLORED_NAMES("disableCustomColoredNames", "Whether to strip any color tags from stargate names and inter-server networks", false, OptionDataType.BOOLEAN),

    /**
     * The level of debugging/warning messages to display
     */
    DEBUG_LEVEL("loggingLevel", "The level of debugging/warning messages to display", "INFO", OptionDataType.LOGGING_LEVEL),

    /**
     * The version of the configuration. Use for updating the config file
     */
    CONFIG_VERSION("configVersion", "The version of the configuration. Use for updating the config file", Stargate.CURRENT_CONFIG_VERSION, OptionDataType.INTEGER),

    /**
     * The name of the .db file if using SQLite
     */
    DATABASE_NAME("portalFile", "The name of the .db file if using SQLite", "stargate", OptionDataType.STRING),

    /**
     * Whether to use a "real" database instead of just a .db file
     */
    USING_REMOTE_DATABASE("bungee.useRemoteDatabase", "Whether to use a \"real\" database instead of just a .db file", false, OptionDataType.BOOLEAN),

    /**
     * Whether to use database settings from a Hikari config file instead of the settings in the config file
     */
    SHOW_HIKARI_CONFIG("bungee.remoteDatabaseSettings.advancedDatabaseConfiguration", "Whether to use database settings from a Hikari config file instead of the settings in the config file", false, OptionDataType.BOOLEAN),

    /**
     * The database driver to use, if using a remote database
     */
    BUNGEE_DRIVER("bungee.remoteDatabaseSettings.driver", "The database driver to use, if using a remote database", "MySQL", OptionDataType.REMOTE_DATABASE_DRIVER),

    /**
     * The name of the used database, if using a remote database
     */
    BUNGEE_DATABASE("bungee.remoteDatabaseSettings.database", "The name of the used database, if using a remote database", "stargate", OptionDataType.STRING),

    /**
     * The port of the used database, if using a remote database
     */
    BUNGEE_PORT("bungee.remoteDatabaseSettings.port", "The port of the used database, if using a remote database", 3306, OptionDataType.INTEGER),

    /**
     * The address of the used database, if using a remote database
     */
    BUNGEE_ADDRESS("bungee.remoteDatabaseSettings.address", "The address of the used database, if using a remote database", "localhost", OptionDataType.STRING),

    /**
     * The username for the used database, if using a remote database
     */
    BUNGEE_USERNAME("bungee.remoteDatabaseSettings.username", "The username for the used database, if using a remote database", "root", OptionDataType.STRING),

    /**
     * The password for the used database, if using a remote database
     */
    BUNGEE_PASSWORD("bungee.remoteDatabaseSettings.password", "The password for the used database, if using a remote database", "", OptionDataType.STRING),

    /**
     * Whether to use an SSL connection, if using a remote database
     */
    BUNGEE_USE_SSL("bungee.remoteDatabaseSettings.useSSL", "Whether to use an SSL connection, if using a remote database", true, OptionDataType.BOOLEAN),


    /**
     * When using remote database, what should the name of the proxy be?
     */
    BUNGEE_INSTANCE_NAME("customRemoteDatabasePrefix", " When using remote database, what should the name of the proxy be?", "SG_", OptionDataType.STRING),

    /**
     * Allow explosions to destroy portals
     */
    DESTROY_ON_EXPLOSION("destroyOnExplosion", "Allow explosions to destroy portals", false, OptionDataType.BOOLEAN),

    /**
     * Allow vehicles to teleport without any player inside
     */
    HANDLE_VEHICLES("handleVehicles", "Allow vehicles to teleport without any player inside", true, OptionDataType.BOOLEAN),

    /**
     * Protect the entrance of a portal from entity based events
     */
    PROTECT_ENTRANCE("protectEntrance", "Protect the entrance of a portal from entity based events", true, OptionDataType.BOOLEAN),

    /**
     * Remember the last destination a networked portal was connected to
     */
    REMEMBER_LAST_DESTINATION("rememberLastDestination", "Remember the last destination a networked portal was connected to", false, OptionDataType.BOOLEAN),

    /**
     * TODO: Unimplemented
     */
    UPKEEP_COST("economy.upkeepCost", null, null, null),

    /**
     * Check if the portal is valid on startup (prevent zombie portals)
     */
    CHECK_PORTAL_VALIDITY("checkPortalValidity", "Check if the portal is valid on startup (prevent zombie portals)", true, OptionDataType.BOOLEAN),

    /**
     * Handle leashed entities during teleportation
     */
    HANDLE_LEASHES("handleLeashedCreatures", "Handle leashed entities during teleportation", true, OptionDataType.BOOLEAN),

    /**
     * TODO: Unimplemented
     */
    DEFAULT_TERMINAL_NAME("defaultTerminalNetwork", null, null, null);

    private final String configNode;
    private final String description;
    private final Object defaultValue;
    private final OptionDataType dataType;

    /**
     * Instantiates a new setting
     *
     * @param configNode <p>The config file string node corresponding to this setting</p>
     */
    ConfigurationOption(String configNode, String description, Object defaultValue, OptionDataType dataType) {
        this.configNode = configNode;
        this.description = description;
        this.defaultValue = defaultValue;
        this.dataType = dataType;
    }

    /**
     * Gets the data type used for storing this config option
     *
     * @return <p>The data type used</p>
     */
    @SuppressWarnings("unused")
    public OptionDataType getDataType() {
        return this.dataType;
    }

    /**
     * Gets the config file string node corresponding to this setting
     *
     * @return <p>The config file string node corresponding to this setting</p>
     */
    public String getConfigNode() {
        return configNode;
    }

    /**
     * Gets the description of what this config option does
     *
     * @return <p>The description of this config option</p>
     */
    @SuppressWarnings("unused")
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets this config option's default value
     *
     * @return <p>This config option's default value</p>
     */
    @SuppressWarnings("unused")
    public Object getDefaultValue() {
        return this.defaultValue;
    }

}
