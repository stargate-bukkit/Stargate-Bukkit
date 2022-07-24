package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.gate.GateFormatHandler;
import net.TheDgtl.Stargate.network.portal.AbstractPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

/**
 * A helper for preparing bStats metrics
 */
public final class BStatsHelper {

    private BStatsHelper() {
    }

    /**
     * Registers a metrics with all relevant portal data
     *
     * @param pluginId <p>The id of the Stargate plugin</p>
     * @param plugin   <p>A Stargate plugin instance</p>
     */
    public static void registerMetrics(int pluginId, JavaPlugin plugin) {
        Metrics metrics = new Metrics(plugin, pluginId);

        metrics.addCustomChart(new SimplePie("language", () -> ConfigurationHelper.getString(ConfigurationOption.LANGUAGE)));

        metrics.addCustomChart(new SimplePie("gateformats", () -> String.valueOf(GateFormatHandler.formatsStored())));

        metrics.addCustomChart(new SingleLineChart("gatesv3", () -> AbstractPortal.portalCount));

        metrics.addCustomChart(new SimplePie("flags", () -> {
            Set<PortalFlag> allUsedFlags = AbstractPortal.allUsedFlags;
            StringBuilder flagsString = new StringBuilder();
            for (PortalFlag flag : allUsedFlags) {
                flagsString.append(flag.getCharacterRepresentation());
            }
            return flagsString.toString();
        }));

        metrics.addCustomChart(new SimplePie("useRemoteDatabase", () -> String.valueOf(ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE))));

        metrics.addCustomChart(new SimplePie("defaultGateNetwork", () -> ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK)));

        metrics.addCustomChart(new SimplePie("defaultTerminalNetwork", () -> ConfigurationHelper.getString(ConfigurationOption.DEFAULT_TERMINAL_NAME)));

        metrics.addCustomChart(new SimplePie("handleVehicles", () -> String.valueOf(ConfigurationHelper.getBoolean(ConfigurationOption.HANDLE_VEHICLES))));

        metrics.addCustomChart(new SimplePie("handleLeashedCreatures", () -> String.valueOf(ConfigurationHelper.getBoolean(ConfigurationOption.HANDLE_LEASHES))));

        metrics.addCustomChart(new SimplePie("checkPortalValidity", () -> String.valueOf(ConfigurationHelper.getBoolean(ConfigurationOption.CHECK_PORTAL_VALIDITY))));

        metrics.addCustomChart(new SimplePie("destroyOnExplosion", () -> String.valueOf(ConfigurationHelper.getBoolean(ConfigurationOption.DESTROY_ON_EXPLOSION))));

        metrics.addCustomChart(new SimplePie("useEconomy", () -> String.valueOf(ConfigurationHelper.getBoolean(ConfigurationOption.USE_ECONOMY))));

        metrics.addCustomChart(new SimplePie("pointer", () -> String.valueOf(ConfigurationHelper.getInteger(ConfigurationOption.POINTER_STYLE))));

        metrics.addCustomChart(new SimplePie("listing", () -> String.valueOf(ConfigurationHelper.getInteger(ConfigurationOption.NAME_STYLE))));

        metrics.addCustomChart(new SimplePie("defaultForeground", () -> String.valueOf(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_LIGHT_SIGN_COLOR))));

        metrics.addCustomChart(new SimplePie("defaultBackground", () -> String.valueOf(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_DARK_SIGN_COLOR))));

        metrics.addCustomChart(new SimplePie("gateExitSpeedMultiplier", () -> String.valueOf(ConfigurationHelper.getInteger(ConfigurationOption.GATE_EXIT_SPEED_MULTIPLIER))));

        metrics.addCustomChart(new SimplePie("loggingLevel", () -> ConfigurationHelper.getString(ConfigurationOption.DEBUG_LEVEL)));
    }

}
