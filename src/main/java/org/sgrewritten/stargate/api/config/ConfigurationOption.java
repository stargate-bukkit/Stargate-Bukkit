package org.sgrewritten.stargate.api.config;

import org.sgrewritten.stargate.Stargate;

/**
 * An enum containing all available settings/configuration options
 *
 * @author Thorin
 */
public enum ConfigurationOption {

    /**
     * The default network if no network is specified
     */
    DEFAULT_NETWORK("defaultGateNetwork", "The default network if no network is specified",
            "central", OptionDataType.STRING, false),

    /**
     * The network used for all legacy BungeeCord Stargates
     */
    LEGACY_BUNGEE_NETWORK("legacyBungeeNetwork", "The network used for all legacy BungeeCord stargates",
            "LegacyBungee", OptionDataType.STRING, true),

    /**
     * The language used for all translatable messages
     */
    LANGUAGE("language", "The language used for all translatable messages", "en",
            OptionDataType.LANGUAGE, false),

    /**
     * Whether BungeeCord functionality is enabled
     */
    USING_BUNGEE("bungee.usingBungee", "Whether BungeeCord functionality is enabled", false,
            OptionDataType.BOOLEAN, false),

    /**
     * The maximum number of stargates on a single network
     */
    GATE_LIMIT("networkLimit", "The maximum number of stargates on a single network", -1,
            OptionDataType.INTEGER, false),

    /**
     * Whether to enable economy functionality for stargate interaction
     */
    USE_ECONOMY("useEconomy", "Whether to enable economy functionality for stargate interaction",
            false, OptionDataType.BOOLEAN, false),

    /**
     * The UUID to pay any taxes to
     */
    TAX_DESTINATION("taxAccount", "The UUID to pay any taxes to", "", OptionDataType.STRING, false),

    /**
     * The cost of creating a new stargate
     */
    CREATION_COST("creationCost", "The cost of creating a new stargate", 0, OptionDataType.INTEGER, false),

    /**
     * The cost of destroying a stargate
     */
    DESTROY_COST("destructionCost", "The cost of destroying a stargate", 0, OptionDataType.INTEGER, false),

    /**
     * The cost of using (teleporting through) a stargate
     */
    USE_COST("usageCost", "The cost of using (teleporting through) a stargate", 0, OptionDataType.INTEGER, false),

    /**
     * Whether to send any transaction fees to the gate owner's balance
     */
    GATE_OWNER_REVENUE("gateOwnerRevenue", "Whether to send any transaction fees to the gate owner's balance",
            true, OptionDataType.BOOLEAN, false),

    /**
     * Whether to charge for a teleportation from a non-free stargate even if the destination is free
     */
    CHARGE_FREE_DESTINATION("chargeFreeDestination",
            "Whether to charge for a teleportation from a non-free stargate even if the destination is free",
            true, OptionDataType.BOOLEAN, false),

    /**
     * The default color to use for signs
     */
    DEFAULT_SIGN_COLOR("signFormatting.color", "The default color to use for signs",
            "BLACK", OptionDataType.COLOR, false),

    /**
     * How should SG style its pointer symbol?
     */
    POINTER_BEHAVIOR("signFormatting.pointerBehaviour", "How should SG style its pointer symbol?", 2, OptionDataType.INTEGER, false),

    /**
     * The multiplier to use for the exit speed when leaving a Stargate
     *
     * <p>If the multiplier is 0, the behaviour is the same as in legacy. If the multiplier is 1, it's the same as the
     * entry speed. 2 = double, 0.5 = half.</p>
     */
    GATE_EXIT_SPEED_MULTIPLIER("gateExitSpeedMultiplier",
            "The multiplier to use for the exit speed when leaving a Stargate", 1.0, OptionDataType.DOUBLE, false),

    /**
     * Whether to strip any color tags from stargate names and inter-server networks
     */
    DISABLE_CUSTOM_COLORED_NAMES("disableCustomColoredNames",
            "Whether to strip any color tags from stargate names and inter-server networks", false,
            OptionDataType.BOOLEAN, false),

    /**
     * The level of debugging/warning messages to display
     */
    DEBUG_LEVEL("loggingLevel", "The level of debugging/warning messages to display", "INFO",
            OptionDataType.LOGGING_LEVEL, false),

    /**
     * The version of the configuration. Use for updating the config file
     */
    CONFIG_VERSION("configVersion", "The version of the configuration. Use for updating the config file",
            Stargate.getCurrentConfigVersion(), OptionDataType.INTEGER, false),

    /**
     * The name of the .db file if using SQLite
     */
    DATABASE_NAME("portalFile", "The name of the .db file if using SQLite", "stargate",
            OptionDataType.STRING, false),

    /**
     * Whether to use a "real" database instead of just a .db file
     */
    USING_REMOTE_DATABASE("bungee.useRemoteDatabase",
            "Whether to use a \"real\" database instead of just a .db file", false,
            OptionDataType.BOOLEAN, false),

    /**
     * Whether to use database settings from a Hikari config file instead of the settings in the config file
     */
    SHOW_HIKARI_CONFIG("bungee.remoteDatabaseSettings.advancedDatabaseConfiguration",
            "Whether to use database settings from a Hikari config file instead of the settings in the config file",
            false, OptionDataType.BOOLEAN, false),

    /**
     * The database driver to use, if using a remote database
     */
    BUNGEE_DRIVER("bungee.remoteDatabaseSettings.driver",
            "The database driver to use, if using a remote database", "MySQL",
            OptionDataType.REMOTE_DATABASE_DRIVER, false),

    /**
     * The name of the used database, if using a remote database
     */
    BUNGEE_DATABASE("bungee.remoteDatabaseSettings.database",
            "The name of the used database, if using a remote database", "stargate",
            OptionDataType.STRING, false),

    /**
     * The port of the used database, if using a remote database
     */
    BUNGEE_PORT("bungee.remoteDatabaseSettings.port",
            "The port of the used database, if using a remote database", 3306, OptionDataType.INTEGER, false),

    /**
     * The address of the used database, if using a remote database
     */
    BUNGEE_ADDRESS("bungee.remoteDatabaseSettings.address",
            "The address of the used database, if using a remote database", "localhost",
            OptionDataType.STRING, false),

    /**
     * The username for the used database, if using a remote database
     */
    BUNGEE_USERNAME("bungee.remoteDatabaseSettings.username",
            "The username for the used database, if using a remote database", "root",
            OptionDataType.STRING, false),

    /**
     * The password for the used database, if using a remote database
     */
    BUNGEE_PASSWORD("bungee.remoteDatabaseSettings.password",
            "The password for the used database, if using a remote database", "",
            OptionDataType.STRING, false),

    /**
     * Whether to use an SSL connection, if using a remote database
     */
    BUNGEE_USE_SSL("bungee.remoteDatabaseSettings.useSSL",
            "Whether to use an SSL connection, if using a remote database", true,
            OptionDataType.BOOLEAN, false),

    /**
     * When using remote database, what should the name of the proxy be?
     */
    BUNGEE_INSTANCE_NAME("customRemoteDatabasePrefix",
            "When using remote database, what should the name of the proxy be?", "SG_",
            OptionDataType.STRING, false),

    /**
     * Allow explosions to destroy portals
     */
    DESTROY_ON_EXPLOSION("destroyOnExplosion", "Allow explosions to destroy portals", false,
            OptionDataType.BOOLEAN, false),

    /**
     * Allow vehicles to teleport without any player inside
     */
    HANDLE_VEHICLES("handleVehicles", "Allow vehicles to teleport without any player inside", true,
            OptionDataType.BOOLEAN, false),

    /**
     * Remember the last destination a networked portal was connected to
     */
    REMEMBER_LAST_DESTINATION("rememberLastDestination",
            "Remember the last destination a networked portal was connected to", false,
            OptionDataType.BOOLEAN, false),

    /**
     * TODO: Unimplemented
     */
    UPKEEP_COST("economy.upkeepCost", null, null, null, true),

    /**
     * Check if the portal is valid on startup (prevent zombie portals)
     */
    CHECK_PORTAL_VALIDITY("checkPortalValidity",
            "Check if the portal is valid on startup (prevent zombie portals)", true,
            OptionDataType.BOOLEAN, false),

    /**
     * Handle leashed entities during teleportation
     */
    HANDLE_LEASHES("handleLeashedCreatures", "Handle leashed entities during teleportation", true,
            OptionDataType.BOOLEAN, false),

    /**
     * TODO: Unimplemented
     */
    DEFAULT_TERMINAL_NAME("defaultTerminalNetwork", null, null, null, true),

    /**
     * Allows users to use/break any gates they create, regardless of any permissions that may prevent them from doing so
     */
    ENABLE_OWNED_GATES("enableOwnedGates", "Allows users to use/break any gates they create, " +
            "regardless of any permissions that may prevent them from doing so",
            true, OptionDataType.BOOLEAN, true),

    /**
     * Allows for specific events to destroy portals
     */
    SPECIFIC_PROTECTION_OVERRIDE("specificProtectionOverrides", "Allows for specific events to destroy portals", null, OptionDataType.STRING_LIST, true),

    /**
     * The folder to load gate files from
     */
    GATE_FOLDER("gateFolder","The folder to load gate files from","gates", OptionDataType.STRING, false);
    
    private final String configNode;
    private final String description;
    private final Object defaultValue;
    private final OptionDataType dataType;
    private final boolean isHidden;

    /**
     * Instantiates a new setting
     *
     * @param configNode   <p>The config file string node corresponding to this setting</p>
     * @param description  <p>A description of correct usage for this configuration option</p>
     * @param defaultValue <p>The default value used in the default configuration</p>
     * @param dataType     <p>The data type this config option requires for its value</p>
     * @param isHidden     <p>Whether this is a hidden/advanced configuration option</p>
     */
    ConfigurationOption(String configNode, String description, Object defaultValue, OptionDataType dataType,
                        boolean isHidden) {
        this.configNode = configNode;
        this.description = description;
        this.defaultValue = defaultValue;
        this.dataType = dataType;
        this.isHidden = isHidden;
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

    /**
     * Gets whether this configuration option is hidden
     *
     * <p>A hidden configuration option should not be provided to users unless they are really advanced users and have
     * an obscure need to change the value.</p>
     *
     * @return <p>Whether this configuration option is hidden</p>
     */
    @SuppressWarnings("unused")
    public boolean isHidden() {
        return this.isHidden;
    }

}
