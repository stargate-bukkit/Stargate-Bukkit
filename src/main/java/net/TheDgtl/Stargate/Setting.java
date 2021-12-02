package net.TheDgtl.Stargate;

/**
 * An enum containing all available settings/configuration options
 *
 * @author Thorin
 */
public enum Setting {

    /**
     * The default network if no network is specified
     */
    DEFAULT_NET("defaultGateNetwork"),

    /**
     * The language used for all translatable messages
     */
    LANGUAGE("lang"),

    /**
     * Whether BungeeCord functionality is enabled
     */
    USING_BUNGEE("bungee.usingBungee"),

    /**
     * The maximum number of stargates on a single network
     */
    GATE_LIMIT("networkLimit"),

    /**
     * Whether to enable economy functionality for stargate interaction
     */
    USE_ECONOMY("useEconomy"),

    /**
     * The UUID to pay any taxes to
     */
    TAX_DESTINATION("taxAccount"),

    /**
     * The cost of creating a new stargate
     */
    CREATION_COST("creationCost"),

    /**
     * The cost of destroying a stargate
     */
    DESTROY_COST("destructionCost"),

    /**
     * The cost of using (teleporting through) a stargate
     */
    USE_COST("usageCost"),

    /**
     * Whether to send any transaction fees to the gate owner's balance
     */
    GATE_OWNER_REVENUE("gateOwnerRevenue"),

    /**
     * Whether to charge for a teleportation from a non-free stargate even if the destination is free
     */
    CHARGE_FREE_DESTINATION("chargeFreeDestination"),

    /**
     * The default color to use for "light" signs
     */
    DEFAULT_LIGHT_SIGN_COLOR("signStyle.defaultForeground"),

    /**
     * The default color to use for "dark" signs
     */
    DEFAULT_DARK_SIGN_COLOR("signStyle.defaultBackground"),

    /**
     * The color style to use for the destination names displayed on a sign
     */
    NAME_STYLE("signStyle.listing"),

    /**
     * The color style to use for the pointers displayed on a sign
     */
    POINTER_STYLE("signStyle.pointer"),

    /**
     * The multiplier to use for the exit speed when leaving a Stargate
     *
     * <p>If the multiplier is 0, the behavior is the same as in legacy. If the multiplier is 1, it's the same as the
     * entry speed. 2 = double, 0.5 = half.</p>
     */
    GATE_EXIT_SPEED_MULTIPLIER("gateExitSpeedMultiplier"),

    /**
     * Whether to strip any color tags from stargate names and inter-server networks
     */
    DISABLE_CUSTOM_COLORED_NAMES("disableCustomColoredNames"),

    /**
     * The level of debugging/warning messages to display
     */
    DEBUG_LEVEL("loggingLevel"),

    /**
     * The version of the configuration. Use for updating the config file
     */
    CONFIG_VERSION("configVersion"),

    /**
     * The name of the .db file if using SQLite
     */
    DATABASE_NAME("portalFile"),

    /**
     * Whether to use a "real" database instead of just a .db file
     */
    USING_REMOTE_DATABASE("bungee.useRemoteDatabase"),

    /**
     * Whether to use database settings from a Hikari config file instead of the settings in the config file
     */
    SHOW_HIKARI_CONFIG("bungee.remoteDatabaseSettings.advancedDatabaseConfiguration"),

    /**
     * The database driver to use, if using a remote database
     */
    BUNGEE_DRIVER("bungee.remoteDatabaseSettings.driver"),

    /**
     * The name of the used database, if using a remote database
     */
    BUNGEE_DATABASE("bungee.remoteDatabaseSettings.database"),

    /**
     * The port of the used database, if using a remote database
     */
    BUNGEE_PORT("bungee.remoteDatabaseSettings.port"),

    /**
     * The address of the used database, if using a remote database
     */
    BUNGEE_ADDRESS("bungee.remoteDatabaseSettings.address"),

    /**
     * The username for the used database, if using a remote database
     */
    BUNGEE_USERNAME("bungee.remoteDatabaseSettings.username"),

    /**
     * The password for the used database, if using a remote database
     */
    BUNGEE_PASSWORD("bungee.remoteDatabaseSettings.password"),

    /**
     * Whether to use an SSL connection, if using a remote database
     */
    BUNGEE_USE_SSL("bungee.remoteDatabaseSettings.useSSL"),

    /**
     * TODO: These are never used
     * TODO This needs to be updated per Discussion Two.
     */
    HANDLE_VEHICLES("handleVehicles"),
    CHECK_PORTAL_VALIDITY("checkPortalValidity"),
    BUNGEE_INSTANCE_NAME("bungee.instanceName"),
    REMEMBER_LAST_DESTINATION("rememberLastDestination"),
    ALPHABETIC_NETWORKS("alphabeticNetworks"),
    CHECK_TRAVERSABLES("checkTraversables"),
    PROTECT_ENTRANCE("protectEntrance"),
    DESTROY_ON_EXPLOSION("destroyOnExplosion"),
    UPKEEP_COST("economy.upkeepCost");

    private final String configNode;

    /**
     * Instantiates a new setting
     *
     * @param configNode <p>The config file string node corresponding to this setting</p>
     */
    Setting(String configNode) {
        this.configNode = configNode;
    }

    /**
     * Gets the config file string node corresponding to this setting
     *
     * @return <p>The config file string node corresponding to this setting</p>
     */
    String getConfigNode() {
        return configNode;
    }

}
