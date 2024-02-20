package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.ConfigOption;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.property.gate.GateHandler;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * A helper for dealing with BStats
 */
public final class BStatsHelper {

    private static boolean hasBeenInitialized = false;

    private BStatsHelper() {

    }

    /**
     * Initializes BStats
     *
     * @param plugin <p>The plugin to initialize BStats for</p>
     */
    public static void initialize(@NotNull JavaPlugin plugin) {
        if (hasBeenInitialized) {
            throw new IllegalArgumentException("BStats initialized twice");
        } else {
            hasBeenInitialized = true;
        }

        int pluginId = 10451;
        Metrics metrics = new Metrics(plugin, pluginId);

        Map<ConfigOption, Object> configValues = Stargate.getStargateConfig().getConfigOptions();
        Map<String, List<String>> portalNetworks = PortalHandler.getAllPortalNetworks();
        int totalPortals = 0;
        for (List<String> portals : portalNetworks.values()) {
            totalPortals += portals.size();
        }

        metrics.addCustomChart(new SimplePie("language", () -> (String) configValues.get(ConfigOption.LANGUAGE)));
        metrics.addCustomChart(new SimplePie("gateformats", () -> String.valueOf(GateHandler.getGateCount())));
        int finalTotalPortals = totalPortals;
        metrics.addCustomChart(new SingleLineChart("gatesv3", () -> finalTotalPortals));
    }

}
