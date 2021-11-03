package net.knarcraft.stargate.config;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.PortalSignDrawer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * The Stargate gate config keeps track of all global config values related to gates
 */
public final class StargateGateConfig {

    private int maxGatesEachNetwork = 0;
    private boolean rememberDestination = false;
    private boolean handleVehicles = true;
    private boolean handleEmptyVehicles = true;
    private boolean handleCreatureTransportation = true;
    private boolean handleNonPlayerVehicles = true;
    private boolean handleLeashedCreatures = true;
    private boolean sortNetworkDestinations = false;
    private boolean protectEntrance = false;
    private boolean enableBungee = true;
    private boolean verifyPortals = true;
    private boolean destroyExplosion = false;
    private String defaultGateNetwork = "central";
    private static final int activeTime = 10;
    private static final int openTime = 10;

    /**
     * Instantiates a new stargate config
     *
     * @param newConfig <p>The file configuration to read values from</p>
     */
    public StargateGateConfig(FileConfiguration newConfig) {
        loadGateConfig(newConfig);
    }

    /**
     * Gets the amount of seconds a portal should be open before automatically closing
     *
     * @return <p>The open time of a gate</p>
     */
    public int getOpenTime() {
        return openTime;
    }

    /**
     * Gets the amount of seconds a portal should be active before automatically deactivating
     *
     * @return <p>The active time of a gate</p>
     */
    public int getActiveTime() {
        return activeTime;
    }

    /**
     * Gets the maximum number of gates allowed on each network
     *
     * @return <p>Maximum number of gates for each network</p>
     */
    public int maxGatesEachNetwork() {
        return maxGatesEachNetwork;
    }

    /**
     * Gets whether a portal's lastly used destination should be remembered
     *
     * @return <p>Whether a portal's lastly used destination should be remembered</p>
     */
    public boolean rememberDestination() {
        return rememberDestination;
    }

    /**
     * Gets whether vehicle teleportation should be handled in addition to player teleportation
     *
     * @return <p>Whether vehicle teleportation should be handled</p>
     */
    public boolean handleVehicles() {
        return handleVehicles;
    }

    /**
     * Gets whether vehicles with no passengers should be handled
     *
     * <p>The handle vehicles option overrides this option if disabled. This option allows empty passenger
     * minecarts/boats, but also chest/tnt/hopper/furnace minecarts to teleport through stargates.</p>
     *
     * @return <p>Whether vehicles without passengers should be handled</p>
     */
    public boolean handleEmptyVehicles() {
        return handleEmptyVehicles;
    }

    /**
     * Gets whether vehicles containing creatures should be handled
     *
     * <p>The handle vehicles option overrides this option if disabled. This option allows creatures (pigs, pandas,
     * zombies, etc.) to teleport through stargates if in a vehicle.</p>
     *
     * @return <p>Whether vehicles with creatures should be handled</p>
     */
    public boolean handleCreatureTransportation() {
        return handleCreatureTransportation;
    }

    /**
     * Gets whether vehicles containing a creature, but not a player should be handled
     *
     * <p>The handle vehicles option, and the handle creature transportation option, override this option if disabled.
     * This option allows creatures (pigs, pandas, zombies, etc.) to teleport through stargates if in a vehicle, even
     * if no player is in the vehicle.
     * As it is not possible to check if a creature is allowed through a stargate, they will be able to go through
     * regardless of whether the initiating player is allowed to enter the stargate. Enabling this is necessary to
     * teleport creatures using minecarts, but only handleCreatureTransportation is required to teleport creatures
     * using a boat manned by a player.</p>
     *
     * @return <p>Whether non-empty vehicles without a player should be handled</p>
     */
    public boolean handleNonPlayerVehicles() {
        return handleNonPlayerVehicles;
    }

    /**
     * Gets whether leashed creatures should be teleported with a teleporting player
     *
     * @return <p>Whether leashed creatures should be handled</p>
     */
    public boolean handleLeashedCreatures() {
        return handleLeashedCreatures;
    }

    /**
     * Gets whether the list of destinations within a network should be sorted
     *
     * @return <p>Whether network destinations should be sorted</p>
     */
    public boolean sortNetworkDestinations() {
        return sortNetworkDestinations;
    }

    /**
     * Gets whether portal entrances should be protected from block breaking
     *
     * @return <p>Whether portal entrances should be protected</p>
     */
    public boolean protectEntrance() {
        return protectEntrance;
    }

    /**
     * Gets whether BungeeCord support is enabled
     *
     * @return <p>Whether bungee support is enabled</p>
     */
    public boolean enableBungee() {
        return enableBungee;
    }

    /**
     * Gets whether all portals' integrity has to be verified on startup and reload
     *
     * @return <p>Whether portals need to be verified</p>
     */
    public boolean verifyPortals() {
        return verifyPortals;
    }

    /**
     * Gets whether portals should be destroyed by nearby explosions
     *
     * @return <p>Whether portals should be destroyed by explosions</p>
     */
    public boolean destroyedByExplosion() {
        return destroyExplosion;
    }

    /**
     * Gets the default portal network to use if no other network is given
     *
     * @return <p>The default portal network</p>
     */
    public String getDefaultPortalNetwork() {
        return defaultGateNetwork;
    }

    /**
     * Loads all config values related to gates
     *
     * @param newConfig <p>The configuration containing the values to read</p>
     */
    private void loadGateConfig(FileConfiguration newConfig) {
        String defaultNetwork = newConfig.getString("gates.defaultGateNetwork");
        defaultGateNetwork = defaultNetwork != null ? defaultNetwork.trim() : null;
        maxGatesEachNetwork = newConfig.getInt("gates.maxGatesEachNetwork");

        //Functionality
        handleVehicles = newConfig.getBoolean("gates.functionality.handleVehicles");
        handleEmptyVehicles = newConfig.getBoolean("gates.functionality.handleEmptyVehicles");
        handleCreatureTransportation = newConfig.getBoolean("gates.functionality.handleCreatureTransportation");
        handleNonPlayerVehicles = newConfig.getBoolean("gates.functionality.handleNonPlayerVehicles");
        handleLeashedCreatures = newConfig.getBoolean("gates.functionality.handleLeashedCreatures");
        enableBungee = newConfig.getBoolean("gates.functionality.enableBungee");

        //Integrity
        protectEntrance = newConfig.getBoolean("gates.integrity.protectEntrance");
        verifyPortals = newConfig.getBoolean("gates.integrity.verifyPortals");
        destroyExplosion = newConfig.getBoolean("gates.integrity.destroyedByExplosion");

        //Cosmetic
        sortNetworkDestinations = newConfig.getBoolean("gates.cosmetic.sortNetworkDestinations");
        rememberDestination = newConfig.getBoolean("gates.cosmetic.rememberDestination");
        loadSignColor(newConfig.getString("gates.cosmetic.mainSignColor"),
                newConfig.getString("gates.cosmetic.highlightSignColor"));
    }

    /**
     * Loads the correct sign color given a sign color string
     *
     * @param mainSignColor <p>A string representing the main sign color</p>
     */
    private void loadSignColor(String mainSignColor, String highlightSignColor) {
        if (mainSignColor != null) {
            try {
                PortalSignDrawer.setColors(ChatColor.valueOf(mainSignColor.toUpperCase()),
                        ChatColor.valueOf(highlightSignColor.toUpperCase()));
                return;
            } catch (IllegalArgumentException | NullPointerException ignored) {
            }
        }
        Stargate.logWarning("You have specified an invalid color in your config.yml. Defaulting to BLACK and WHITE");
        PortalSignDrawer.setColors(ChatColor.BLACK, ChatColor.WHITE);
    }
}
