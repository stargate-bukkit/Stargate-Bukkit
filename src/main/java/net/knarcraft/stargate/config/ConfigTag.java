package net.knarcraft.stargate.config;

import java.util.Arrays;

/**
 * A config tag groups config values by a property
 */
public enum ConfigTag {

    COLOR(new ConfigOption[]{ConfigOption.FREE_GATES_COLOR, ConfigOption.MAIN_SIGN_COLOR,
            ConfigOption.HIGHLIGHT_SIGN_COLOR, ConfigOption.PER_SIGN_COLORS}),
    FOLDER(new ConfigOption[]{ConfigOption.GATE_FOLDER, ConfigOption.PORTAL_FOLDER});

    private final ConfigOption[] taggedOptions;

    /**
     * Instantiates a new config tag
     *
     * @param taggedOptions <p>The config options included in this tag</p>
     */
    ConfigTag(ConfigOption[] taggedOptions) {
        this.taggedOptions = taggedOptions;
    }

    /**
     * Checks whether a config tag includes the given config option
     *
     * @param option <p>The config option to check</p>
     * @return <p>True of the config option is tagged</p>
     */
    public boolean isTagged(ConfigOption option) {
        return Arrays.stream(taggedOptions).anyMatch((item) -> item == option);
    }

    /**
     * Checks whether a given config option requires a "reload of colors" to take effect
     *
     * @param configOption <p>The config option to check</p>
     * @return <p>True if changing the config option requires a "reload of colors" to take effect</p>
     */
    public static boolean requiresColorReload(ConfigOption configOption) {
        return (COLOR.isTagged(configOption) && configOption != ConfigOption.FREE_GATES_COLOR);
    }

    /**
     * Checks whether a given config option requires a full reload to take effect
     *
     * @param option <p>The config option to check</p>
     * @return <p>True if changing the config option requires a full reload to take effect</p>
     */
    public static boolean requiresFullReload(ConfigOption option) {
        return FOLDER.isTagged(option);
    }

    /**
     * Checks whether a given config option requires a portal reload to take effect
     *
     * @param option <p>The config option to check</p>
     * @return <p>True if changing the config option requires a portal reload to take effect</p>
     */
    public static boolean requiresPortalReload(ConfigOption option) {
        return COLOR.isTagged(option) || FOLDER.isTagged(option) || option == ConfigOption.VERIFY_PORTALS;
    }

    /**
     * Checks whether a given config option requires the language loader to be reloaded
     *
     * @param option <p>The config option to check</p>
     * @return <p>True if the language loader requires a reload</p>
     */
    public static boolean requiresLanguageReload(ConfigOption option) {
        return option == ConfigOption.LANGUAGE;
    }

    /**
     * Checks whether a given config option requires economy to be reloaded
     *
     * @param option <p>The config option to check</p>
     * @return <p>True if economy requires a reload</p>
     */
    public static boolean requiresEconomyReload(ConfigOption option) {
        return option == ConfigOption.USE_ECONOMY;
    }

}
