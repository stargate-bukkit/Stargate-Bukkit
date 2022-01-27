package net.knarcraft.stargate.config;

/**
 * A ConfigOption represents one of the available config options
 */
public enum ConfigOption {

    /**
     * The language used for player-interface text
     */
    LANGUAGE("language", "The language used for all signs and all messages to players", "en"),

    /**
     * The folder for portal files
     */
    PORTAL_FOLDER("folders.portalFolder", "The folder containing the portal databases", "plugins/Stargate/portals/"),

    /**
     * The folder for gate files
     */
    GATE_FOLDER("folders.gateFolder", "The folder containing all gate files", "plugins/Stargate/gates/"),

    /**
     * The max number of portals on a single network
     */
    MAX_GATES_EACH_NETWORK("gates.maxGatesEachNetwork", "The max number of stargates in a single network", 0),

    /**
     * The network used if not specified
     */
    DEFAULT_GATE_NETWORK("gates.defaultGateNetwork", "The network used when no network is specified", "central"),

    /**
     * Whether to remember the lastly used destination
     */
    REMEMBER_DESTINATION("gates.cosmetic.rememberDestination", "Whether to remember the last destination used", false),

    /**
     * Whether to sort the network destinations
     */
    SORT_NETWORK_DESTINATIONS("gates.cosmetic.sortNetworkDestinations", "Whether to sort destinations by name", false),

    /**
     * The main color to use for all signs
     */
    MAIN_SIGN_COLOR("gates.cosmetic.mainSignColor", "The main text color of all stargate signs", "BLACK"),

    /**
     * The color to use for highlighting sign text
     */
    HIGHLIGHT_SIGN_COLOR("gates.cosmetic.highlightSignColor", "The text color used for highlighting stargate signs", "WHITE"),

    PER_SIGN_COLORS("gates.cosmetic.perSignColors", "The per-sign color specification", new String[]{
            "'ACACIA:default,default'", "'BIRCH:default,default'", "'CRIMSON:inverted,inverted'", "'DARK_OAK:inverted,inverted'",
            "'JUNGLE:default,default'", "'OAK:default,default'", "'SPRUCE:inverted,inverted'", "'WARPED:inverted,inverted'"}),

    /**
     * Whether to destroy portals when any blocks are broken by explosions
     */
    DESTROYED_BY_EXPLOSION("gates.integrity.destroyedByExplosion", "Whether stargates should be destroyed by explosions", false),

    /**
     * Whether to verify each portal's gate layout after each load
     */
    VERIFY_PORTALS("gates.integrity.verifyPortals", "Whether to verify that portals match their gate layout on load", false),

    /**
     * Whether to protect the entrance of portals
     */
    PROTECT_ENTRANCE("gates.integrity.protectEntrance", "Whether to protect stargates' entrances", false),

    /**
     * Whether to enable BungeeCord support
     */
    ENABLE_BUNGEE("gates.functionality.enableBungee", "Whether to enable BungeeCord support", false),

    /**
     * Whether to enable vehicle teleportation
     */
    HANDLE_VEHICLES("gates.functionality.handleVehicles", "Whether to enable vehicle teleportation", true),

    /**
     * Whether to enable teleportation of empty vehicles
     */
    HANDLE_EMPTY_VEHICLES("gates.functionality.handleEmptyVehicles", "Whether to enable teleportation of empty vehicles", true),

    /**
     * Whether to enable teleportation of creatures using vehicles
     */
    HANDLE_CREATURE_TRANSPORTATION("gates.functionality.handleCreatureTransportation",
            "Whether to enable teleportation of vehicles containing non-player creatures", true),

    /**
     * Whether to allow creatures to teleport alone, bypassing any access restrictions
     */
    HANDLE_NON_PLAYER_VEHICLES("gates.functionality.handleNonPlayerVehicles",
            "Whether to enable teleportation of non-empty vehicles without a player", true),

    /**
     * Whether to enable teleportations of creatures on a leash
     */
    HANDLE_LEASHED_CREATURES("gates.functionality.handleLeashedCreatures",
            "Whether to enable players to teleport a creature on a leash", true),

    /**
     * Whether to enable economy support for taking payment from players creating/destroying/using stargates
     */
    USE_ECONOMY("economy.useEconomy", "Whether to use economy to incur fees when stargates are used, created or destroyed", false),

    /**
     * The cost of creating a new stargate
     */
    CREATE_COST("economy.createCost", "The cost of creating a new stargate", 0),

    /**
     * The cost of destroying a stargate
     */
    DESTROY_COST("economy.destroyCost", "The cost of destroying a stargate. Negative to refund", 0),

    /**
     * The cost of using (teleporting through) a stargate
     */
    USE_COST("economy.useCost", "The cost of using (teleporting through) a stargate", 0),

    /**
     * Whether any payments should go to the stargate's owner
     */
    TO_OWNER("economy.toOwner", "Whether any teleportation fees should go to the owner of the used stargate", false),

    /**
     * Whether to charge for using a stargate, even if its destination is free
     */
    CHARGE_FREE_DESTINATION("economy.chargeFreeDestination",
            "Whether to require payment if the destination is free, but the entrance stargate is not", true),

    /**
     * Whether to mark free gates with a different color
     */
    FREE_GATES_COLORED("economy.freeGatesColored", "Whether to use coloring to mark all free stargates", false),

    /**
     * The color to use for marking free stargates
     */
    FREE_GATES_COLOR("economy.freeGatesColor", "The color to use for marking free stargates", "DARK_GREEN"),

    /**
     * Whether to enable debug output
     */
    DEBUG("debugging.debug", "Whether to enable debugging output", false),

    /**
     * Whether to enable debug output for debugging permissions
     */
    PERMISSION_DEBUG("debugging.permissionDebug", "Whether to enable permission debugging output", false),

    /**
     * Whether to alert admins about new updates
     */
    ADMIN_UPDATE_ALERT("adminUpdateAlert", "Whether to alert admins about new plugin updates", true),

    /**
     * The velocity of players exiting a stargate, relative to the entry velocity
     */
    EXIT_VELOCITY("gates.exitVelocity", "The velocity of players exiting stargates, relative to the entry velocity", 0.1D);

    private final String configNode;
    private final String description;
    private final Object defaultValue;
    private final OptionDataType dataType;

    /**
     * Instantiates a new config option
     *
     * @param configNode   <p>The full path of this config option's config node</p>
     * @param description  <p>The description of what this config option does</p>
     * @param defaultValue <p>The default value of this config option</p>
     */
    ConfigOption(String configNode, String description, Object defaultValue) {
        this.configNode = configNode;
        this.description = description;
        this.defaultValue = defaultValue;

        if (defaultValue instanceof String[]) {
            this.dataType = OptionDataType.STRING_LIST;
        } else if (defaultValue instanceof String) {
            this.dataType = OptionDataType.STRING;
        } else if (defaultValue instanceof Boolean) {
            this.dataType = OptionDataType.BOOLEAN;
        } else if (defaultValue instanceof Integer) {
            this.dataType = OptionDataType.INTEGER;
        } else if (defaultValue instanceof Double) {
            this.dataType = OptionDataType.DOUBLE;
        } else {
            throw new IllegalArgumentException("Unknown config data type encountered: " + defaultValue);
        }
    }

    /**
     * Gets a config option given its name
     *
     * @param name <p>The name of the config option to get</p>
     * @return <p>The corresponding config option, or null if the name is invalid</p>
     */
    public static ConfigOption getByName(String name) {
        for (ConfigOption option : ConfigOption.values()) {
            if (option.getName().equalsIgnoreCase(name)) {
                return option;
            }
        }
        return null;
    }

    /**
     * Gets the name of this config option
     *
     * @return <p>The name of this config option</p>
     */
    public String getName() {
        if (!this.configNode.contains(".")) {
            return this.configNode;
        }
        String[] pathParts = this.configNode.split("\\.");
        return pathParts[pathParts.length - 1];
    }

    /**
     * Gets the data type used for storing this config option
     *
     * @return <p>The data type used</p>
     */
    public OptionDataType getDataType() {
        return this.dataType;
    }

    /**
     * Gets the config node of this config option
     *
     * @return <p>This config option's config node</p>
     */
    public String getConfigNode() {
        return this.configNode;
    }

    /**
     * Gets the description of what this config option does
     *
     * @return <p>The description of this config option</p>
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets this config option's default value
     *
     * @return <p>This config option's default value</p>
     */
    public Object getDefaultValue() {
        return this.defaultValue;
    }

}
