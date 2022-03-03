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
public class BStatsHelper {

    /**
     * Creates a new metrics containing relevant portal data
     *
     * @param pluginId <p>The id of the Stargate plugin</p>
     * @param plugin   <p>A Stargate plugin instance</p>
     * @return <p>A metrics object</p>
     */
    public static Metrics getMetrics(int pluginId, JavaPlugin plugin) {
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
        return metrics;
    }
}
