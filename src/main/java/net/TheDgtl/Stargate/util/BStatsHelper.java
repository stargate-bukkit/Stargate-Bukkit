package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.concurrent.Callable;

public class BStatsHelper {

    public static Metrics getMetrics(int pluginId, JavaPlugin plugin) {
        Metrics metrics = new Metrics(plugin, pluginId);

        metrics.addCustomChart(new SimplePie("language", new Callable<String>() {
            @Override
            public String call() {
                return Settings.getString(Setting.LANGUAGE);
            }
        }));

        metrics.addCustomChart(new SimplePie("gateformats", new Callable<String>() {
            @Override
            public String call() {
                return String.valueOf(GateFormat.formatAmount);
            }
        }));

        metrics.addCustomChart(new SingleLineChart("gatesv3", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return Portal.portalCount;
            }

        }));

        metrics.addCustomChart(new SimplePie("flags", new Callable<String>() {
            @Override
            public String call() {
                EnumSet<PortalFlag> allUsedFlags = Portal.allUsedFlags;
                String flagsString = "";
                for (PortalFlag flag : allUsedFlags) {
                    flagsString = flagsString + flag.getCharacterRepresentation();
                }
                return flagsString;
            }
        }));
        return metrics;
    }
}
