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
import net.TheDgtl.Stargate.util.FileHelper;
import net.md_5.bungee.api.ChatColor;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
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

    final String DATA_FOLDER = this.getDataFolder().getAbsolutePath().replaceAll("\\\\", "/");
    final String GATE_FOLDER = "gates";
    final String LANGUAGE_FOLDER = "lang";
    final String PORTAL_FOLDER = "portals";
    final String INTERNAL_FOLDER = ".internal";

    private PluginManager pm;

    public static StargateFactory factory;
    public static LanguageManager languageManager;
    private final int CURRENT_CONFIG_VERSION = 5;
    /**
     * Goes through every action in the queue every 1 tick. Should be used in tasks that need to be finished within a short time frame
     */
    public static final SynchronousPopulator syncTickPopulator = new SynchronousPopulator();
    /**
     * Goes through every action it the queue every 1 second (20 ticks). Should be used in delayed actions
     */
    public static final SynchronousPopulator syncSecPopulator = new SynchronousPopulator();
    /**
     * The string length of a name consisting of only 'i'. This will fill a sign (including <>)
     * Note that this is a terribly relaxed restriction, mainly done to prevent any from arising in an SQL database.
     */
    public static final int MAX_TEXT_LENGTH = 40;

    public static EconomyManager economyManager;
    /*
     * Used in bungee / waterfall
     */
    private final HashMap<String, IPortal> bungeeQueue = new HashMap<>();
    public static String serverName;
    public static boolean knowsServerName = false;

    public static UUID serverUUID;

    public static ChatColor defaultLightSignColor = ChatColor.BLACK;

    public static ChatColor defaultDarkColor = ChatColor.WHITE;

    @Override
    public void onEnable() {
        instance = this;
        // Registers bstats metrics
        int pluginId = 10451;
        new Metrics(this, pluginId);

        loadConfig();
        loadColors();

        if (Setting.getBoolean(Setting.USING_REMOTE_DATABASE)) {
            loadBungeeServerName();
        }
        economyManager = new EconomyManager();
        lowestMsgLevel = Level.parse(Setting.getString(Setting.DEBUG_LEVEL));
        languageManager = new LanguageManager(this, DATA_FOLDER + "/" + LANGUAGE_FOLDER, Setting.getString(Setting.LANGUAGE));
        saveDefaultGates();

        GateFormat.controlMaterialFormatsMap = GateFormat.loadGateFormats(DATA_FOLDER + "/" + GATE_FOLDER);
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

    private void loadBungeeServerName() {
        Stargate.log(Level.FINEST, DATA_FOLDER);
        File path = new File(String.format("%s/%s", this.getDataFolder().getAbsolutePath(), INTERNAL_FOLDER));
        if (!path.exists()) {
            path.mkdir();
            try {
                Files.setAttribute(path.toPath(), "dos:hidden", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(path, "serverUUID.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
                BufferedWriter writer = FileHelper.getBufferedWriter(file);
                writer.write(UUID.randomUUID().toString());
                writer.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        try {
            BufferedReader reader = FileHelper.getBufferedReader(file);
            Stargate.serverUUID = UUID.fromString(reader.readLine());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadColors() {
        Stargate.defaultLightSignColor = loadColor(Setting.getString(Setting.DEFAULT_LIGHT_SIGN_COLOR));
        Stargate.defaultDarkColor = loadColor(Setting.getString(Setting.DEFAULT_DARK_SIGN_COLOR));
    }
    
    private ChatColor loadColor(String colorString) {
        if(colorString.startsWith("#")) {
            return ChatColor.of(colorString);
        }
        return ChatColor.valueOf(colorString);
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
        //TODO is there a way to check all files in a resource-folder? Possible solution seems unnecessarily complex
        String[] gateList = {"nether.gate", "water.gate", "wool.gate"};
        boolean replace = false;
        for (String gateName : gateList) {
            if (!(new File(DATA_FOLDER + "/" + GATE_FOLDER + "/" + gateName).exists()))
                this.saveResource(GATE_FOLDER + "/" + gateName, replace);
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
            factory.endInterServerConnection();
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

    public static void addToQueue(String playerName, String portalName, String netName, boolean isInterServer) {
        Network net = factory.getNetwork(netName, isInterServer);
        
        
        /*
         * TODO Error: When would errors like this appear?: Whenever a interserver server instance was
         * deleted, but not cleared from the interserver database, errors like this would occur
         */
        if (net == null) {
            // Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = network.
            String msg = String.format("Interserver network ''%s'' could not be found",netName);
            Stargate.log(Level.WARNING, msg);
        }
        IPortal portal = net.getPortal(portalName);
        if (portal == null) {
            // Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = gate.
            String msg = String.format("Interserver portal ''%s'' in network ''%s'' could not be found",portalName,netName);
            Stargate.log(Level.WARNING, msg);
        }

        instance.bungeeQueue.put(playerName, portal);
    }

    public static IPortal pullFromQueue(String playerName) {
        return instance.bungeeQueue.remove(playerName);
    }
}
