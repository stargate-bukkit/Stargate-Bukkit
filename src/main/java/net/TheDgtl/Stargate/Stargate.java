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

import net.TheDgtl.Stargate.command.CommandStargate;
import net.TheDgtl.Stargate.command.StargateTabCompleter;
import net.TheDgtl.Stargate.config.StargateConfiguration;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.SQLiteDatabase;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.listeners.BlockEventListener;
import net.TheDgtl.Stargate.listeners.MoveEventListener;
import net.TheDgtl.Stargate.listeners.PlayerEventListener;
import net.TheDgtl.Stargate.listeners.PluginEventListener;
import net.TheDgtl.Stargate.listeners.StargateBungeePluginMessageListener;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.StargateFactory;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.refactoring.Refactorer;
import net.TheDgtl.Stargate.util.BStatsHelper;
import net.TheDgtl.Stargate.util.FileHelper;
import net.md_5.bungee.api.ChatColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
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
public class Stargate extends JavaPlugin implements StargateLogger {
    private static Stargate instance;

    private Level lowestMsgLevel = Level.FINEST;//setting before config loads

    final String DATA_FOLDER = this.getDataFolder().getAbsolutePath();
    final String GATE_FOLDER = "gates";
    final String LANGUAGE_FOLDER = "lang";
    final String PORTAL_FOLDER = "portals";
    final String INTERNAL_FOLDER = ".internal";

    private PluginManager pm;

    public static StargateFactory factory;
    public static LanguageManager languageManager;
    public final static int CURRENT_CONFIG_VERSION = 6;
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
    private final HashMap<String, Portal> bungeeQueue = new HashMap<>();
    public static String serverName;
    public static boolean knowsServerName = false;

    public static UUID serverUUID;

    public static ChatColor defaultLightSignColor = ChatColor.BLACK;
    public static ChatColor defaultDarkColor = ChatColor.WHITE;

    private FileConfiguration config;
    private static FileConfiguration staticConfig = new StargateConfiguration();

    @Override
    public void onEnable() {
        instance = this;
        if (!new File(this.getDataFolder(), "config.yml").exists())
            super.saveDefaultConfig();

        if (Settings.getInteger(Setting.CONFIG_VERSION) != CURRENT_CONFIG_VERSION) {
            try {
                this.refactor();
            } catch (IOException | InvalidConfigurationException | SQLException e) {
                e.printStackTrace();
            }
        }


        saveDefaultGates();

        languageManager = new LanguageManager(this, new File(DATA_FOLDER, LANGUAGE_FOLDER));
        load();

        pm = getServer().getPluginManager();
        registerListeners();
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, syncTickPopulator, 0L, 1L);
        scheduler.scheduleSyncRepeatingTask(this, syncSecPopulator, 0L, 20L);
        registerCommands();

        // Registers bstats metrics
        int pluginId = 10451;
        Metrics metrics = BStatsHelper.getMetrics(pluginId, this);
    }

    private void loadBungeeServerName() {
        Stargate.log(Level.FINEST, DATA_FOLDER);
        File path = new File(this.getDataFolder(), INTERNAL_FOLDER);
        if (!path.exists() && path.mkdir()) {
            try {
                Files.setAttribute(path.toPath(), "dos:hidden", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(path, "serverUUID.txt");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new FileNotFoundException("serverUUID.txt was not found and could not be created");
                }
                BufferedWriter writer = FileHelper.getBufferedWriter(file);
                writer.write(UUID.randomUUID().toString());
                writer.close();
            } catch (IOException e1) {
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
        try {
            Stargate.defaultLightSignColor = loadColor(Settings.getString(Setting.DEFAULT_LIGHT_SIGN_COLOR));
            Stargate.defaultDarkColor = loadColor(Settings.getString(Setting.DEFAULT_DARK_SIGN_COLOR));
        } catch (IllegalArgumentException | NullPointerException e) {
            Stargate.log(Level.WARNING, "Invalid colors for sign texts, chosing default colors...");
            Stargate.defaultLightSignColor = ChatColor.BLACK;
            Stargate.defaultDarkColor = ChatColor.WHITE;
        }
    }

    private ChatColor loadColor(String colorString) throws IllegalArgumentException {
        if (colorString.startsWith("#")) {
            return ChatColor.of(colorString);
        }
        return ChatColor.valueOf(colorString);
    }

    private void registerListeners() {
        pm.registerEvents(new BlockEventListener(), this);
        pm.registerEvents(new MoveEventListener(), this);
        pm.registerEvents(new PlayerEventListener(), this);
        pm.registerEvents(new PluginEventListener(), this);
        if (Settings.getBoolean(Setting.USING_BUNGEE)) {
            Messenger msgr = Bukkit.getMessenger();

            msgr.registerOutgoingPluginChannel(this, PluginChannel.BUNGEE.getChannel());
            msgr.registerIncomingPluginChannel(this, PluginChannel.BUNGEE.getChannel(), new StargateBungeePluginMessageListener(this));
        }
    }

    private void saveDefaultGates() {
        //TODO is there a way to check all files in a resource-folder? Possible solution seems unnecessarily complex
        String[] gateList = {"nether.gate", "water.gate", "wool.gate", "end.gate"};
        boolean replace = false;
        for (String gateName : gateList) {
            if (!(new File(DATA_FOLDER + "/" + GATE_FOLDER + "/" + gateName).exists()))
                this.saveResource(GATE_FOLDER + "/" + gateName, replace);
        }
    }

    private void refactor() throws FileNotFoundException, IOException, InvalidConfigurationException, SQLException {
        File file = new File(this.getDataFolder(), "stargate.db");
        Database database = new SQLiteDatabase(file);
        StargateFactory factory = new StargateFactory(database, false, false, this);

        Refactorer middas = new Refactorer(new File(this.getDataFolder(), "config.yml"), this, Bukkit.getServer(), factory);
        Map<String, Object> newConfig = middas.getConfigModificatinos();
        this.saveResource("config.yml", true);
        middas.insertNewValues(newConfig);
        this.reloadConfig();
        middas.run();
    }

    public void reload() {
        this.reloadConfig();
        load();
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    @Override
    public void reloadConfig() {
        config = new StargateConfiguration();
        try {
            config.load(new File(this.getDataFolder(), "config.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveConfig() {
        try {
            config.save(new File(this.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        loadColors();
        if (Settings.getBoolean(Setting.USING_REMOTE_DATABASE)) {
            loadBungeeServerName();
        }
        economyManager = new EconomyManager();
        String debugLevelStr = Settings.getString(Setting.DEBUG_LEVEL);
        if (debugLevelStr == null)
            lowestMsgLevel = Level.INFO;
        else
            lowestMsgLevel = Level.parse(debugLevelStr);
        languageManager.setLanguage(Settings.getString(Setting.LANGUAGE));

        GateFormat.setFormats(GateFormat.loadGateFormats(new File(DATA_FOLDER, GATE_FOLDER)));

        try {
            factory = new StargateFactory(this);
            factory.loadFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        /*
         * Replacement for legacy, which used:
         * methodPortal.closeAllGates(this); Portal.clearGates(); managedWorlds.clear();
         */
        syncTickPopulator.forceDoAllTasks();
        syncSecPopulator.forceDoAllTasks();
        if (Settings.getBoolean(Setting.USING_BUNGEE)) {
            Messenger msgr = Bukkit.getMessenger();
            msgr.unregisterOutgoingPluginChannel(this);
            msgr.unregisterIncomingPluginChannel(this);
        }
        getServer().getScheduler().cancelTasks(this);

        if (!Settings.getBoolean(Setting.USING_BUNGEE))
            return;

        try {
            factory.endInterServerConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void log(Level priorityLevel, String msg) {
        if (instance != null) {
            instance.logMessage(priorityLevel, msg);
            return;
        }
        System.out.println(msg);
    }

    /**
     * Gets an instance of this plugin
     *
     * @return <p>An instance of this plugin</p>
     */
    public static Stargate getInstance() {
        return instance;
    }

    @Override
    public void logMessage(Level priorityLevel, String message) {
        if (this.lowestMsgLevel.intValue() <= priorityLevel.intValue()
                && priorityLevel.intValue() < Level.INFO.intValue()) {
            this.getLogger().log(Level.INFO, message);
            return;
        }
        this.getLogger().log(priorityLevel, message);
    }

    public static FileConfiguration getConfigStatic() {
        if (instance == null)
            return staticConfig;
        return instance.getConfig();
    }

    /**
     * Registers a command for this plugin
     */
    private void registerCommands() {
        PluginCommand stargateCommand = this.getCommand("stargate");
        if (stargateCommand != null) {
            stargateCommand.setExecutor(new CommandStargate());
            stargateCommand.setTabCompleter(new StargateTabCompleter());
        }
    }

    public static void addToQueue(String playerName, String portalName, String netName, boolean isInterServer) {
        Network network = factory.getNetwork(netName, isInterServer);


        /*
         * In some cases, there might be issues with a portal being delited in a server, but still present in the interserver database.
         * Therefore we have to check for that...
         */
        if (network == null) {
            // Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = network.
            String msg = String.format("Inter-server network ''%s'' could not be found", netName);
            Stargate.log(Level.WARNING, msg);
        }
        Portal portal = network == null ? null : network.getPortal(portalName);
        if (portal == null) {
            // Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = gate.
            String msg = String.format("Inter-server portal ''%s'' in network ''%s'' could not be found", portalName, netName);
            Stargate.log(Level.WARNING, msg);
        }
        instance.bungeeQueue.put(playerName, portal);
    }


    public static Portal pullFromQueue(String playerName) {
        return instance.bungeeQueue.remove(playerName);
    }

}
