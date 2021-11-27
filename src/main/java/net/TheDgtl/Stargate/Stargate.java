/*
 * Stargate - A portal plugin for Bukkit
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.TheDgtl.Stargate;

import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.listeners.BlockEventListener;
import net.TheDgtl.Stargate.listeners.MoveEventListener;
import net.TheDgtl.Stargate.listeners.PlayerEventListener;
import net.TheDgtl.Stargate.listeners.PluginEventListener;
import net.TheDgtl.Stargate.listeners.StargateBungeePluginMessageListener;
import net.TheDgtl.Stargate.listeners.WorldEventListener;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.StargateFactory;
import net.TheDgtl.Stargate.network.portal.IPortal;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * A plugin that allows for instant-teleportation between large distances and across servers.
 * Highly customisable and designed with end-user portal creation in mind!
 * <p>
 * https://git.io/JuWkU | https://discord.gg/mTaHuK6BVa
 * <p>
 * Lead Developers:
 *
 * @author Thorin (2020-Present)
 * @author PseudoKnight (2015-2020)
 * @author Drakia (2011-2013)
 * @author Dinnerbone (2010-2011)
 */
public class Stargate extends JavaPlugin {
    private static Stargate instance;

    private Level lowestMsgLevel = Level.FINEST;//setting before config loads

    final String DATAFOLDER = this.getDataFolder().getPath().replaceAll("\\\\", "/");
    final String GATEFOLDER = "gates";
    final String LANGFOLDER = "lang";
    final String PORTALFOLDER = "portals";

    private PluginManager pm;

    public static StargateFactory factory;
    public static LanguageManager languageManager;
    private final int CURRENT_CONFIG_VERSION = 5;
    /**
     * Goes through every action in the queue every 1 tick. Should be used in tasks that need to be finished within a short time frame
     */
    public static final SyncronousPopulator syncTickPopulator = new SyncronousPopulator();
    /**
     * Goes through every action it the queue every 1 second (20 ticks). Should be used in delayed actions
     */
    public static final SyncronousPopulator syncSecPopulator = new SyncronousPopulator();
    /**
     * The string length of a name consisting of only 'i'. This will fill a sign (including <>)
     * Note that this is a terribly relaxed restriction, mainly done to prevent any from arising in a SQL database.
     */
    public static final int MAX_TEXT_LENGTH = 40;

    public static EconomyManager economyManager;
    /*
     * Used in bungee / waterfall
     */
    private HashMap<String, IPortal> bungeeQueue = new HashMap<>();
    public static String serverName;
    public static boolean knowsServerName = false;

    @Override
    public void onEnable() {
        instance = this;
        // Registers bstats metrics
        int pluginId = 10451;
        new Metrics(this, pluginId);

        loadConfig();


        economyManager = new EconomyManager();
        lowestMsgLevel = Level.parse(Setting.getString(Setting.DEBUG_LEVEL));
        languageManager = new LanguageManager(this, DATAFOLDER + "/" + LANGFOLDER, Setting.getString(Setting.LANGUAGE));
        saveDefaultGates();

        GateFormat.controlMaterialFormatsMap = GateFormat.loadGateFormats(DATAFOLDER + "/" + GATEFOLDER);
        try {
            factory = new StargateFactory(this);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pm = getServer().getPluginManager();
        registerListeners();
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, syncTickPopulator, 0L, 1L);
        scheduler.scheduleSyncRepeatingTask(this, syncSecPopulator, 0L, 20L);
    }

    private void registerListeners() {
        pm.registerEvents(new BlockEventListener(), this);
        pm.registerEvents(new MoveEventListener(), this);
        pm.registerEvents(new PlayerEventListener(), this);
        pm.registerEvents(new PluginEventListener(), this);
        pm.registerEvents(new WorldEventListener(), this);
        if (Setting.getBoolean(Setting.USING_BUNGEE)) {
            Messenger msgr = Bukkit.getMessenger();

            msgr.registerOutgoingPluginChannel(this, Channel.BUNGEE.getChannel());
            msgr.registerIncomingPluginChannel(this, Channel.BUNGEE.getChannel(), new StargateBungeePluginMessageListener(this));
        }
    }

    private void saveDefaultGates() {
        //TODO is there a way to check all files in a resourcefolder? Possible solution seems unnecessarily complex
        String[] gateList = {"nether.gate", "water.gate", "wool.gate"};
        boolean replace = false;
        for (String gateName : gateList) {
            if (!(new File(DATAFOLDER + "/" + GATEFOLDER + "/" + gateName).exists()))
                this.saveResource(GATEFOLDER + "/" + gateName, replace);
        }
    }

    private void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        if (Setting.getInteger(Setting.CONFIG_VERSION) != CURRENT_CONFIG_VERSION) {
            // TODO refactoring
        }
    }

    @Override
    public void onLoad() {
        // TODO Economy (issue #88)
        //economyHandler = new EconomyHandler(this);
    }

    @Override
    public void onDisable() {
        /*
         * Replacement for legacy, which used:
         * methodPortal.closeAllGates(this); Portal.clearGates(); managedWorlds.clear();
         */
        syncTickPopulator.forceDoAllTasks();
        syncSecPopulator.forceDoAllTasks();
        if (Setting.getBoolean(Setting.USING_BUNGEE)) {
            Messenger msgr = Bukkit.getMessenger();
            msgr.unregisterOutgoingPluginChannel(this);
            msgr.unregisterIncomingPluginChannel(this);
        }
        getServer().getScheduler().cancelTasks(this);

        if (!Setting.getBoolean(Setting.USING_BUNGEE))
            return;

        try {
            factory.endInterserverConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void log(Level priorityLevel, String msg) {
        if (instance.lowestMsgLevel.intValue() <= priorityLevel.intValue()
                && priorityLevel.intValue() < Level.INFO.intValue()) {
            instance.getLogger().log(Level.INFO, msg);
            return;
        }
        instance.getLogger().log(priorityLevel, msg);
    }

    public static FileConfiguration getConfigStatic() {
        return instance.getConfig();
    }

    public static void addToQueue(String playerName, String portalName, String netName, boolean isInterserver) {
        Network net = factory.getNetwork(netName, isInterserver);
        if (net == null) {
            //TODO Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = network.
        }
        IPortal portal = net.getPortal(portalName);
        if (portal == null) {
            //TODO Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = gate.
        }

        instance.bungeeQueue.put(playerName, portal);
    }

    public static IPortal pullFromQueue(String playerName) {
        return instance.bungeeQueue.remove(playerName);
    }
}
