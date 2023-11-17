package org.sgrewritten.stargate.util;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.AbstractPortal;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
     * @param pluginId <p>
     *                 The id of the Stargate plugin</p>
     * @param plugin   <p>
     *                 A Stargate plugin instance</p>
     */
    public static void registerMetrics(int pluginId, JavaPlugin plugin, RegistryAPI registry) {
        Metrics metrics = new Metrics(plugin, pluginId);

        metrics.addCustomChart(new SimplePie("gateformats", () -> String.valueOf(GateFormatRegistry.formatsStored())));

        metrics.addCustomChart(new SingleLineChart("totalPortals", () -> AbstractPortal.portalCount));

        metrics.addCustomChart(
                new SimplePie("networksNumber", () -> String.valueOf(registry.getNetworkMap().size()
                        + registry.getBungeeNetworkMap().size())));

        // Registers the line chart with the number of underwater gates present on the server.
        registerUnderwaterCount(metrics, registry);

        // Registers the pie chart with the number of gates present on the largest network on this server
        registerNetworkSize(metrics, registry);

        // Registers the all addons active on this instance.
        registerAddons(metrics);

        // Registers all user-specifiable flags in use on this instance
        registerFlagMetrics(metrics);

        // Registers the pie chart with the number of fixed and networked portals on the server
        registerNetworkedOrFixedMetrics(metrics);

        // Registers the pie chart with the number of portals on personal networks and non-personal networks
        registerPersonalNetworkMetrics(metrics);

        registerConfigMetrics(metrics);
    }

    /**
     * Registers metrics for the number of portals on personal networks vs.
     * portals on non-personal networks
     *
     * @param metrics <p>
     *                The metrics object to register metrics to</p>
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
     * Registers metrics for the number of networked and fixed portals on this
     * instance
     *
     * @param metrics <p>
     *                The metrics object to register metrics to</p>
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
     * @param metrics <p>
     *                The metrics object to register metrics to</p>
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
     * Registers metrics for the number of portals in the largest network on
     * this instance.
     *
     * @param metrics <p>
     *                The metrics object to register metrics to</p>
     */
    private static void registerNetworkSize(Metrics metrics, RegistryAPI registry) {
        metrics.addCustomChart(new SimplePie("largestNetwork", () -> {
            int largest = 0;
            int count;
            for (Network localNetwork : registry.getNetworkMap().values()) {
                count = localNetwork.size();
                if (largest <= count) {
                    largest = count;
                }
            }
            for (Network bungeeNetwork : registry.getBungeeNetworkMap().values()) {
                count = bungeeNetwork.size();
                if (largest <= count) {
                    largest = count;
                }
            }
            return String.valueOf(largest);
        }));
    }

    /**
     * Registers metrics for all active addons
     *
     * @param metrics <p>
     *                The metrics object to register metrics to</p>
     */
    private static void registerAddons(Metrics metrics) {
        String stargate = "stargate";
        metrics.addCustomChart(new AdvancedPie("addonsUsed", () -> {
            Map<String, Integer> addonsList = new HashMap<>();

            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                for (String depend : plugin.getDescription().getDepend()) {
                    if (depend.toLowerCase().equals(stargate)) {
                        addonsList.put(plugin.getName(), 1);
                    }
                }
                for (String depend : plugin.getDescription().getSoftDepend()) {
                    if (depend.toLowerCase().equals(stargate)) {
                        addonsList.put(plugin.getName(), 1);
                    }
                }
            }
            return addonsList;
        }));
    }

    /**
     * Registers metrics for the number of underwater portals on the instance
     *
     * @param metrics <p>
     *                The metrics object to register metrics to</p>
     */
    private static void registerUnderwaterCount(Metrics metrics, RegistryAPI registry) {
        metrics.addCustomChart(new SingleLineChart("underwaterCount", () -> {
            int count = 0;
            Collection<Network> totalNetworkList = registry.getNetworkMap().values();
            totalNetworkList.addAll(registry.getBungeeNetworkMap().values());
            for (Network network : totalNetworkList) {
                for (Portal portal : network.getAllPortals()) {
                    if (!(portal instanceof RealPortal realPortal)) {
                        continue;
                    }
                    if (realPortal.getGate().getExit().getBlock().getType() == Material.WATER) {
                        count++;
                    }
                }
            }
            return count;
        }));
    }

    /**
     * Registers metrics for all configuration options
     *
     * @param metrics <p>
     *                The metrics object to register metrics to</p>
     */
    private static void registerConfigMetrics(Metrics metrics) {
        metrics.addCustomChart(new SimplePie("language", () -> ConfigurationHelper.getString(ConfigurationOption.LANGUAGE)));

        metrics.addCustomChart(new SimplePie("useRemoteDatabase", () -> String.valueOf(ConfigurationHelper.getBoolean(
                ConfigurationOption.USING_REMOTE_DATABASE))));

        metrics.addCustomChart(new SimplePie("defaultGateNetwork", () -> ConfigurationHelper.getString(
                ConfigurationOption.DEFAULT_NETWORK)));

        metrics.addCustomChart(new SimplePie("defaultTerminalNetwork", () -> ConfigurationHelper.getString(
                ConfigurationOption.DEFAULT_TERMINAL_NAME)));

        metrics.addCustomChart(new SimplePie("handleVehicles", () -> String.valueOf(ConfigurationHelper.getBoolean(
                ConfigurationOption.HANDLE_VEHICLES))));

        metrics.addCustomChart(new SimplePie("handleLeashedCreatures", () -> String.valueOf(ConfigurationHelper.getBoolean(
                ConfigurationOption.HANDLE_LEASHES))));

        metrics.addCustomChart(new SimplePie("checkPortalValidity", () -> String.valueOf(ConfigurationHelper.getBoolean(
                ConfigurationOption.CHECK_PORTAL_VALIDITY))));

        metrics.addCustomChart(new SimplePie("destroyOnExplosion", () -> String.valueOf(ConfigurationHelper.getBoolean(
                ConfigurationOption.DESTROY_ON_EXPLOSION))));

        metrics.addCustomChart(new SimplePie("useEconomy", () -> String.valueOf(ConfigurationHelper.getBoolean(
                ConfigurationOption.USE_ECONOMY))));

        metrics.addCustomChart(new SimplePie("defaultColor", () -> String.valueOf(ConfigurationHelper.getString(
                ConfigurationOption.DEFAULT_SIGN_COLOR))));

        metrics.addCustomChart(new SimplePie("pointerBehavior", () -> String.valueOf(ConfigurationHelper.getInteger(
                ConfigurationOption.POINTER_BEHAVIOR))));

        metrics.addCustomChart(new SimplePie("gateExitSpeedMultiplier", () -> String.valueOf(ConfigurationHelper.getDouble(
                ConfigurationOption.GATE_EXIT_SPEED_MULTIPLIER))));

        metrics.addCustomChart(new SimplePie("loggingLevel", () -> ConfigurationHelper.getString(
                ConfigurationOption.DEBUG_LEVEL)));
    }

}
