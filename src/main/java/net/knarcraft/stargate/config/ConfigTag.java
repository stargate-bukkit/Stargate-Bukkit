package net.knarcraft.stargate.config;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A config tag groups config values by a property
 */
public enum ConfigTag {

    /**
     * Color-related configuration options
     */
    COLOR(Set.of(ConfigOption.FREE_GATES_COLOR, ConfigOption.MAIN_SIGN_COLOR, ConfigOption.HIGHLIGHT_SIGN_COLOR,
            ConfigOption.PER_SIGN_COLORS)),

    /**
     * Folder-altering configuration options
     */
    FOLDER(Set.of(ConfigOption.GATE_FOLDER, ConfigOption.PORTAL_FOLDER)),

    /**
     * Dynmap-related configuration options
     */
    DYNMAP(Set.of(ConfigOption.ENABLE_DYNMAP, ConfigOption.DYNMAP_ICONS_DEFAULT_HIDDEN));

    private final Set<ConfigOption> taggedOptions;

    /**
     * Instantiates a new config tag
     *
     * @param taggedOptions <p>The config options included in this tag</p>
     */
    ConfigTag(@NotNull Set<ConfigOption> taggedOptions) {
        this.taggedOptions = taggedOptions;
    }

    /**
     * Checks whether a config tag includes the given config option
     *
     * @param option <p>The config option to check</p>
     * @return <p>True of the config option is tagged</p>
     */
    public boolean isTagged(@NotNull ConfigOption option) {
        return taggedOptions.contains(option);
    }

    /**
     * Checks whether a given config option requires a "reload of colors" to take effect
     *
     * @param configOption <p>The config option to check</p>
     * @return <p>True if changing the config option requires a "reload of colors" to take effect</p>
     */
    public static boolean requiresColorReload(@NotNull ConfigOption configOption) {
        return (COLOR.isTagged(configOption) && configOption != ConfigOption.FREE_GATES_COLOR);
    }

    /**
     * Checks whether a given config option requires a full reload to take effect
     *
     * @param option <p>The config option to check</p>
     * @return <p>True if changing the config option requires a full reload to take effect</p>
     */
    public static boolean requiresFullReload(@NotNull ConfigOption option) {
        return FOLDER.isTagged(option);
    }

    /**
     * Checks whether a given config option requires a re-load of all Dynmap markers
     *
     * @param configOption <p>The config option to check</p>
     * @return <p>True if changing the config option requires a reload of all dynmap markers</p>
     */
    public static boolean requiresDynmapReload(@NotNull ConfigOption configOption) {
        return DYNMAP.isTagged(configOption);
    }

    /**
     * Checks whether a given config option requires a portal reload to take effect
     *
     * @param option <p>The config option to check</p>
     * @return <p>True if changing the config option requires a portal reload to take effect</p>
     */
    public static boolean requiresPortalReload(@NotNull ConfigOption option) {
        return COLOR.isTagged(option) || FOLDER.isTagged(option) || option == ConfigOption.VERIFY_PORTALS;
    }

    /**
     * Checks whether a given config option requires the language loader to be reloaded
     *
     * @param option <p>The config option to check</p>
     * @return <p>True if the language loader requires a reload</p>
     */
    public static boolean requiresLanguageReload(@NotNull ConfigOption option) {
        return option == ConfigOption.LANGUAGE;
    }

    /**
     * Checks whether a given config option requires economy to be reloaded
     *
     * @param option <p>The config option to check</p>
     * @return <p>True if economy requires a reload</p>
     */
    public static boolean requiresEconomyReload(@NotNull ConfigOption option) {
        return option == ConfigOption.USE_ECONOMY;
    }

}
