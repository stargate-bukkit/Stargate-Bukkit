package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.gate.GateFormatHandler;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.AbstractPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.TheDgtl.Stargate.config.ConfigurationHelper.getBoolean;
import static net.TheDgtl.Stargate.config.ConfigurationHelper.getDouble;
import static net.TheDgtl.Stargate.config.ConfigurationHelper.getInteger;
import static net.TheDgtl.Stargate.config.ConfigurationHelper.getString;

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

        metrics.addCustomChart(new SimplePie("gateformats", () -> String.valueOf(GateFormatHandler.formatsStored())));

        metrics.addCustomChart(new SingleLineChart("gatesv3", () -> AbstractPortal.portalCount));

        // Registers all user-specifiable flags in use on this instance
        registerFlagMetrics(metrics);

        // Registers the pie chart with the number of fixed and networked portals on the server
        registerNetworkedOrFixedMetrics(metrics);

        // Registers the pie chart with the number of portals on personal networks and non-personal networks
        registerPersonalNetworkMetrics(metrics);

        registerConfigMetrics(metrics);
    }

    /**
     * Registers metrics for the number of portals on personal networks vs. portals on non-personal networks
     *
     * @param metrics <p>The metrics object to register metrics to</p>
     */
    private static void registerPersonalNetworkMetrics(Metrics metrics) {
        metrics.addCustomChart(new AdvancedPie("networkType", () -> {
            int personal = 0;
            int nonPersonal = 0;
            int defaultNetwork = 0;
            int terminal = 0;
            for (Network network : Stargate.getInstance().getRegistry().getNetworkMap().values()) {
                for (Portal portal : network.getAllPortals()) {
                    if (portal.hasFlag(PortalFlag.PERSONAL_NETWORK)) {
                        personal++;
                    } else if (portal.hasFlag(PortalFlag.DEFAULT_NETWORK)) {
                        defaultNetwork++;
                    } else if (portal.hasFlag(PortalFlag.TERMINAL_NETWORK)) {
                        terminal++;
                    } else {
                        nonPersonal++;
                    }
                }
            }
            Map<String, Integer> statistics = new HashMap<>();
            statistics.put("Player", personal);
            statistics.put("Default", defaultNetwork);
            statistics.put("Other", nonPersonal);
            statistics.put("Terminal", terminal);
            return statistics;
        }));
    }

    /**
     * Registers metrics for the number of networked and fixed portals on this instance
     *
     * @param metrics <p>The metrics object to register metrics to</p>
     */
    private static void registerNetworkedOrFixedMetrics(Metrics metrics) {
        metrics.addCustomChart(new AdvancedPie("networkedOrFixed", () -> {
            int networked = 0;
            int fixed = 0;
            for (Network network : Stargate.getInstance().getRegistry().getNetworkMap().values()) {
                for (Portal portal : network.getAllPortals()) {
                    if (portal.hasFlag(PortalFlag.FIXED)) {
                        fixed++;
                    } else if (portal.hasFlag(PortalFlag.NETWORKED)) {
                        networked++;
                    }
                }
            }
            Map<String, Integer> statistics = new HashMap<>();
            statistics.put("Fixed", fixed);
            statistics.put("Networked", networked);
            return statistics;
        }));
    }

    /**
     * Registers metrics for all flags in use on this instance
     *
     * @param metrics <p>The metrics object to register metrics to</p>
     */
    private static void registerFlagMetrics(Metrics metrics) {
        metrics.addCustomChart(new AdvancedPie("flags", () -> {
            Map<String, Integer> flagStatus = new HashMap<>();
            Set<PortalFlag> allUsedFlags = AbstractPortal.allUsedFlags;
            for (PortalFlag portalFlag : PortalFlag.values()) {
                //Skip internal flag, as those are not specifiable
                if (!portalFlag.isInternalFlag()) {
                    flagStatus.put(portalFlag.getCharacterRepresentation().toString(),
                            allUsedFlags.contains(portalFlag) ? 1 : 0);
                }
            }
            return flagStatus;
        }));
    }

    /**
     * Registers metrics for all configuration options
     *
     * @param metrics <p>The metrics object to register metrics to</p>
     */
    private static void registerConfigMetrics(Metrics metrics) {
        metrics.addCustomChart(new SimplePie("language", () -> getString(ConfigurationOption.LANGUAGE)));

        metrics.addCustomChart(new SimplePie("useRemoteDatabase", () -> String.valueOf(getBoolean(
                ConfigurationOption.USING_REMOTE_DATABASE))));

        metrics.addCustomChart(new SimplePie("defaultGateNetwork", () -> getString(
                ConfigurationOption.DEFAULT_NETWORK)));

        metrics.addCustomChart(new SimplePie("defaultTerminalNetwork", () -> getString(
                ConfigurationOption.DEFAULT_TERMINAL_NAME)));

        metrics.addCustomChart(new SimplePie("handleVehicles", () -> String.valueOf(getBoolean(
                ConfigurationOption.HANDLE_VEHICLES))));

        metrics.addCustomChart(new SimplePie("handleLeashedCreatures", () -> String.valueOf(getBoolean(
                ConfigurationOption.HANDLE_LEASHES))));

        metrics.addCustomChart(new SimplePie("checkPortalValidity", () -> String.valueOf(getBoolean(
                ConfigurationOption.CHECK_PORTAL_VALIDITY))));

        metrics.addCustomChart(new SimplePie("destroyOnExplosion", () -> String.valueOf(getBoolean(
                ConfigurationOption.DESTROY_ON_EXPLOSION))));

        metrics.addCustomChart(new SimplePie("useEconomy", () -> String.valueOf(getBoolean(
                ConfigurationOption.USE_ECONOMY))));

        metrics.addCustomChart(new SimplePie("defaultColor", () -> String.valueOf(getString(
                ConfigurationOption.DEFAULT_SIGN_COLOR))));
        
        metrics.addCustomChart(new SimplePie("pointerBehavior", () -> String.valueOf(getInteger(
                ConfigurationOption.POINTER_BEHAVIOR))));
        
        metrics.addCustomChart(new SimplePie("gateExitSpeedMultiplier", () -> String.valueOf(getDouble(
                ConfigurationOption.GATE_EXIT_SPEED_MULTIPLIER))));

        metrics.addCustomChart(new SimplePie("loggingLevel", () -> getString(
                ConfigurationOption.DEBUG_LEVEL)));
    }

}
