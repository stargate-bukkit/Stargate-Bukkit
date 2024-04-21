package net.knarcraft.stargate.config;

import net.knarcraft.knarlib.util.ColorHelper;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.PortalSignDrawer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public StargateGateConfig(@NotNull Map<ConfigOption, Object> configOptions) {
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
     * Gets whether the CraftBook vehicle removal fix is enabled
     *
     * <p>If enabled, minecarts and boats should be re-created instead of teleported.</p>
     *
     * @return <p>True if the CraftBook vehicle removal fix is enabled</p>
     */
    public boolean enableCraftBookRemoveOnEjectFix() {
        return (boolean) configOptions.get(ConfigOption.ENABLE_CRAFT_BOOK_REMOVE_ON_EJECT_FIX);
    }

    /**
     * Gets the delay to use before adding a player as passenger of a teleported vehicle
     *
     * @return <p>The delay to use before adding a player as passenger of a teleported vehicle</p>
     */
    public int waitForPlayerAfterTeleportDelay() {
        if ((int) configOptions.get(ConfigOption.WAIT_FOR_PLAYER_AFTER_TELEPORT_DELAY) < 2) {
            configOptions.put(ConfigOption.WAIT_FOR_PLAYER_AFTER_TELEPORT_DELAY, 6);
        }
        return (int) configOptions.get(ConfigOption.WAIT_FOR_PLAYER_AFTER_TELEPORT_DELAY);
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
     * Gets whether to destroy portals when any blocks are broken by explosions
     *
     * @return <p>Whether to destroy portals when any blocks are broken by explosions</p>
     */
    public boolean applyStartupFixes() {
        return (boolean) configOptions.get(ConfigOption.APPLY_STARTUP_FIXES);
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
     * Gets the exit velocity of players using stargates, relative to the entry velocity
     *
     * @return <p>The relative exit velocity</p>
     */
    public double getExitVelocity() {
        return (double) configOptions.get(ConfigOption.EXIT_VELOCITY);
    }

    /**
     * Loads all config values related to gates
     */
    private void loadGateConfig() {
        //Load the sign colors
        String mainSignColor = (String) configOptions.get(ConfigOption.MAIN_SIGN_COLOR);
        String highlightSignColor = (String) configOptions.get(ConfigOption.HIGHLIGHT_SIGN_COLOR);
        loadPerSignColor(mainSignColor, highlightSignColor);
        loadPerSignColors();
    }

    /**
     * Loads the per-sign colors specified in the config file
     */
    public void loadPerSignColors() {
        List<?> perSignColors = (List<?>) configOptions.get(ConfigOption.PER_SIGN_COLORS);
        ChatColor[] defaultColors = new ChatColor[]{PortalSignDrawer.getMainColor(), PortalSignDrawer.getHighlightColor()};
        List<Map<Material, ChatColor>> colorMaps = new ArrayList<>();
        colorMaps.add(new HashMap<>());
        colorMaps.add(new HashMap<>());

        for (Object signColorSpecification : perSignColors) {
            parsePerSignColors(signColorSpecification, defaultColors, colorMaps);
        }

        PortalSignDrawer.setPerSignMainColors(colorMaps.get(0));
        PortalSignDrawer.setPerSignHighlightColors(colorMaps.get(1));
    }

    /**
     * Parses a per-sign color specification object and stores the result
     *
     * @param signColorSpecification <p>The sign color specification to parse</p>
     * @param defaultColors          <p>The specified default colors</p>
     * @param colorMaps              <p>The list of color maps to save the resulting colors to</p>
     */
    private void parsePerSignColors(@NotNull Object signColorSpecification, @NotNull ChatColor[] defaultColors,
                                    @NotNull List<Map<Material, ChatColor>> colorMaps) {
        String[] specificationData = String.valueOf(signColorSpecification).split(":");
        Material[] signMaterials = new Material[]{Material.matchMaterial(specificationData[0] + "_SIGN"),
                Material.matchMaterial(specificationData[0] + "_WALL_SIGN")};

        if (specificationData.length != 2) {
            Stargate.logWarning("You have an invalid per-sign line in your config.yml file. Please fix it!");
            return;
        }
        String[] colors = specificationData[1].split(",");
        if (colors.length != 2) {
            Stargate.logWarning("You have an invalid per-sign line in your config.yml file. Please fix it!");
            return;
        }
        for (int colorIndex = 0; colorIndex < 2; colorIndex++) {
            if (colors[colorIndex].equalsIgnoreCase("default")) {
                continue;
            }
            loadPerSignColor(colors, colorIndex, defaultColors, signMaterials, colorMaps);
        }
    }

    /**
     * Loads a per-sign color
     *
     * @param colors        <p>The colors specified in the config file</p>
     * @param colorIndex    <p>The index of the color to load</p>
     * @param defaultColors <p>The specified default colors</p>
     * @param signMaterials <p>The materials to load this color for</p>
     * @param colorMaps     <p>The list of color maps to save the resulting color to</p>
     */
    private void loadPerSignColor(@NotNull String[] colors, int colorIndex, @NotNull ChatColor[] defaultColors,
                                  @NotNull Material[] signMaterials, @NotNull List<Map<Material, ChatColor>> colorMaps) {
        ChatColor parsedColor;
        if (colors[colorIndex].equalsIgnoreCase("inverted")) {
            //Convert from ChatColor to awt.Color to Bukkit.Color then invert and convert to ChatColor
            java.awt.Color color = defaultColors[colorIndex].getColor();
            parsedColor = ColorHelper.fromColor(ColorHelper.invert(Color.fromRGB(color.getRed(), color.getGreen(),
                    color.getBlue())));
        } else {
            try {
                parsedColor = ChatColor.of(colors[colorIndex]);
            } catch (IllegalArgumentException | NullPointerException exception) {
                Stargate.logWarning("You have specified an invalid per-sign color in your config.yml. Custom color for \"" +
                        signMaterials[0] + "\" disabled");
                return;
            }
        }
        if (parsedColor != null) {
            for (Material signMaterial : signMaterials) {
                colorMaps.get(colorIndex).put(signMaterial, parsedColor);
            }
        }
    }

    /**
     * Loads the correct sign color given a sign color string
     *
     * @param mainSignColor <p>A string representing the main sign color</p>
     */
    private void loadPerSignColor(@NotNull String mainSignColor, @NotNull String highlightSignColor) {
        try {
            PortalSignDrawer.setMainColor(ChatColor.of(mainSignColor.toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException exception) {
            Stargate.logWarning("You have specified an invalid main sign color in your config.yml (" + mainSignColor +
                    "). Defaulting to BLACK");
            PortalSignDrawer.setMainColor(ChatColor.BLACK);
        }

        try {
            PortalSignDrawer.setHighlightColor(ChatColor.of(highlightSignColor.toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException exception) {
            Stargate.logWarning("You have specified an invalid highlighting sign color in your config.yml (" +
                    highlightSignColor + "). Defaulting to WHITE");
            PortalSignDrawer.setHighlightColor(ChatColor.WHITE);
        }
    }

}
