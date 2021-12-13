package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.network.portal.AbstractPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class BStatsHelper {

    public static Metrics getMetrics(int pluginId, JavaPlugin plugin) {
        Metrics metrics = new Metrics(plugin, pluginId);

        metrics.addCustomChart(new SimplePie("language", () -> Settings.getString(Setting.LANGUAGE)));

        metrics.addCustomChart(new SimplePie("gateformats", () -> String.valueOf(GateFormat.formatAmount)));

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
