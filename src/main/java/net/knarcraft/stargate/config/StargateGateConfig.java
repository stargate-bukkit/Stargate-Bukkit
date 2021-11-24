package net.knarcraft.stargate.config;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.PortalSignDrawer;
import org.bukkit.ChatColor;

import java.util.Map;

/**
 * The Stargate gate config keeps track of all global config values related to gates
 */
public final class StargateGateConfig {
    private static final int activeTime = 10;
    private static final int openTime = 10;
    private final Map<ConfigOption, Object> configOptions;

    /**
     * Instantiates a new stargate config
     *
     * @param configOptions <p>The loaded config options to use</p>
     */
    public StargateGateConfig(Map<ConfigOption, Object> configOptions) {
        this.configOptions = configOptions;
        loadGateConfig();
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
        return (int) configOptions.get(ConfigOption.MAX_GATES_EACH_NETWORK);
    }

    /**
     * Gets whether a portal's lastly used destination should be remembered
     *
     * @return <p>Whether a portal's lastly used destination should be remembered</p>
     */
    public boolean rememberDestination() {
        return (boolean) configOptions.get(ConfigOption.REMEMBER_DESTINATION);
    }

    /**
     * Gets whether vehicle teleportation should be handled in addition to player teleportation
     *
     * @return <p>Whether vehicle teleportation should be handled</p>
     */
    public boolean handleVehicles() {
        return (boolean) configOptions.get(ConfigOption.HANDLE_VEHICLES);
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
        return (boolean) configOptions.get(ConfigOption.HANDLE_EMPTY_VEHICLES);
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
        return (boolean) configOptions.get(ConfigOption.HANDLE_CREATURE_TRANSPORTATION);
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
        return (boolean) configOptions.get(ConfigOption.HANDLE_NON_PLAYER_VEHICLES);
    }

    /**
     * Gets whether leashed creatures should be teleported with a teleporting player
     *
     * @return <p>Whether leashed creatures should be handled</p>
     */
    public boolean handleLeashedCreatures() {
        return (boolean) configOptions.get(ConfigOption.HANDLE_LEASHED_CREATURES);
    }

    /**
     * Gets whether the list of destinations within a network should be sorted
     *
     * @return <p>Whether network destinations should be sorted</p>
     */
    public boolean sortNetworkDestinations() {
        return (boolean) configOptions.get(ConfigOption.SORT_NETWORK_DESTINATIONS);
    }

    /**
     * Gets whether portal entrances should be protected from block breaking
     *
     * @return <p>Whether portal entrances should be protected</p>
     */
    public boolean protectEntrance() {
        return (boolean) configOptions.get(ConfigOption.PROTECT_ENTRANCE);
    }

    /**
     * Gets whether BungeeCord support is enabled
     *
     * @return <p>Whether bungee support is enabled</p>
     */
    public boolean enableBungee() {
        return (boolean) configOptions.get(ConfigOption.ENABLE_BUNGEE);
    }

    /**
     * Gets whether all portals' integrity has to be verified on startup and reload
     *
     * @return <p>Whether portals need to be verified</p>
     */
    public boolean verifyPortals() {
        return (boolean) configOptions.get(ConfigOption.VERIFY_PORTALS);
    }

    /**
     * Gets whether portals should be destroyed by nearby explosions
     *
     * @return <p>Whether portals should be destroyed by explosions</p>
     */
    public boolean destroyedByExplosion() {
        return (boolean) configOptions.get(ConfigOption.DESTROYED_BY_EXPLOSION);
    }

    /**
     * Gets the default portal network to use if no other network is given
     *
     * @return <p>The default portal network</p>
     */
    public String getDefaultPortalNetwork() {
        return (String) configOptions.get(ConfigOption.DEFAULT_GATE_NETWORK);
    }

    /**
     * Loads all config values related to gates
     */
    private void loadGateConfig() {
        //Load the sign colors
        loadSignColor((String) configOptions.get(ConfigOption.MAIN_SIGN_COLOR),
                (String) configOptions.get(ConfigOption.HIGHLIGHT_SIGN_COLOR));
    }

    /**
     * Loads the correct sign color given a sign color string
     *
     * @param mainSignColor <p>A string representing the main sign color</p>
     */
    private void loadSignColor(String mainSignColor, String highlightSignColor) {
        if (mainSignColor != null && highlightSignColor != null) {
            try {
                PortalSignDrawer.setMainColor(ChatColor.valueOf(highlightSignColor.toUpperCase()));
                PortalSignDrawer.setHighlightColor(ChatColor.valueOf(highlightSignColor.toUpperCase()));
            } catch (IllegalArgumentException | NullPointerException ignored) {
                Stargate.logWarning("You have specified an invalid color in your config.yml. Defaulting to BLACK and WHITE");
                PortalSignDrawer.setMainColor(ChatColor.BLACK);
                PortalSignDrawer.setHighlightColor(ChatColor.WHITE);
            }
        }
    }
}
