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
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.config.StargateYamlConfiguration;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.PortalDatabaseAPI;
import net.TheDgtl.Stargate.database.SQLiteDatabase;
import net.TheDgtl.Stargate.database.StorageAPI;
import net.TheDgtl.Stargate.formatting.LanguageManager;
import net.TheDgtl.Stargate.formatting.StargateLanguageManager;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.gate.GateFormatHandler;
import net.TheDgtl.Stargate.listeners.BlockEventListener;
import net.TheDgtl.Stargate.listeners.MoveEventListener;
import net.TheDgtl.Stargate.listeners.PlayerEventListener;
import net.TheDgtl.Stargate.listeners.PluginEventListener;
import net.TheDgtl.Stargate.listeners.StargateBungeePluginMessageListener;
import net.TheDgtl.Stargate.manager.EconomyManager;
import net.TheDgtl.Stargate.migration.DataMigrator;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.RegistryAPI;
import net.TheDgtl.Stargate.network.StargateRegistry;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.property.PluginChannel;
import net.TheDgtl.Stargate.thread.SynchronousPopulator;
import net.TheDgtl.Stargate.util.BStatsHelper;
import net.TheDgtl.Stargate.util.BungeeHelper;
import net.TheDgtl.Stargate.util.FileHelper;
import net.TheDgtl.Stargate.util.PortalHelper;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * @author EpicKnarvik (2021-Present)
 * @author PseudoKnight (2015-2020)
 * @author Drakia (2011-2013)
 * @author Dinnerbone (2010-2011)
 */
public class Stargate extends JavaPlugin implements StargateLogger {
    private static Stargate instance;

    private Level lowestMessageLevel = Level.INFO;//setting before config loads

    final String DATA_FOLDER = this.getDataFolder().getAbsolutePath();
    final String GATE_FOLDER = "gates";
    final String LANGUAGE_FOLDER = "lang";
    final String INTERNAL_FOLDER = ".internal";

    private PluginManager pluginManager;

    private static StorageAPI storageAPI;
    public static LanguageManager languageManager;
    public static final int CURRENT_CONFIG_VERSION = 7;
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
    public static String serverName;
    public static boolean knowsServerName = false;

    public static UUID serverUUID;

    public static ChatColor defaultLightSignColor = ChatColor.BLACK;
    public static ChatColor defaultDarkColor = ChatColor.WHITE;

    private FileConfiguration config;

    private static StargateRegistry registry;
    private static final FileConfiguration staticConfig = new StargateYamlConfiguration();

    @Override
    public void onEnable() {
        instance = this;
        if (!new File(this.getDataFolder(), "config.yml").exists()) {
            super.saveDefaultConfig();
        }

        loadGateFormats();
        languageManager = new StargateLanguageManager(this, new File(DATA_FOLDER, LANGUAGE_FOLDER));
        if (ConfigurationHelper.getInteger(ConfigurationOption.CONFIG_VERSION) != CURRENT_CONFIG_VERSION) {
            try {
                this.migrateConfigurationAndData();
            } catch (IOException | InvalidConfigurationException | SQLException e) {
                e.printStackTrace();
            }
        }

        load();

        pluginManager = getServer().getPluginManager();
        registerListeners();
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, syncTickPopulator, 0L, 1L);
        scheduler.scheduleSyncRepeatingTask(this, syncSecPopulator, 0L, 20L);
        registerCommands();

        //Register bStats metrics
        int pluginId = 13629;
        BStatsHelper.getMetrics(pluginId, this);
    }

    private void loadColors() {
        try {
            Stargate.defaultLightSignColor = getColor(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_LIGHT_SIGN_COLOR));
            Stargate.defaultDarkColor = getColor(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_DARK_SIGN_COLOR));
        } catch (IllegalArgumentException | NullPointerException e) {
            Stargate.log(Level.WARNING, "Invalid colors for sign text. Using default colors instead...");
            Stargate.defaultLightSignColor = ChatColor.BLACK;
            Stargate.defaultDarkColor = ChatColor.WHITE;
        }
    }

    /**
     * Gets the corresponding chat color from the given color string
     *
     * @param colorString <p>The color string to parse</p>
     * @return <p>The color corresponding to the string</p>
     * @throws IllegalArgumentException <p>If the given color string is invalid</p>
     */
    private ChatColor getColor(String colorString) throws IllegalArgumentException {
        return ChatColor.of(colorString);
    }

    private void registerListeners() {
        pluginManager.registerEvents(new BlockEventListener(), this);
        pluginManager.registerEvents(new MoveEventListener(), this);
        pluginManager.registerEvents(new PlayerEventListener(), this);
        pluginManager.registerEvents(new PluginEventListener(), this);
        if (ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            Messenger msgr = Bukkit.getMessenger();

            msgr.registerOutgoingPluginChannel(this, PluginChannel.BUNGEE.getChannel());
            msgr.registerIncomingPluginChannel(this, PluginChannel.BUNGEE.getChannel(), new StargateBungeePluginMessageListener(this));
        }
    }

    private void saveDefaultGates() {
        //TODO is there a way to check all files in a resource-folder? Possible solution seems unnecessarily complex
        String[] gateList = {"nether.gate", "water.gate", "wool.gate", "end.gate"};
        for (String gateName : gateList) {
            if (!(new File(DATA_FOLDER + "/" + GATE_FOLDER + "/" + gateName).exists())) {
                this.saveResource(GATE_FOLDER + "/" + gateName, false);
            }
        }
    }

    /**
     * Migrates data files and configuration files if necessary
     *
     * @throws IOException                   <p>If unable to load or save a configuration file</p>
     * @throws InvalidConfigurationException <p>If unable to save the new configuration</p>
     * @throws SQLException                  <p>If unable to initialize the Portal Database API</p>
     */
    private void migrateConfigurationAndData() throws IOException, InvalidConfigurationException, SQLException {
        File databaseFile = new File(this.getDataFolder(), "stargate.db");
        Database database = new SQLiteDatabase(databaseFile);

        StorageAPI storageAPI = new PortalDatabaseAPI(database, false, false, this);
        registry = new StargateRegistry(storageAPI);

        DataMigrator dataMigrator = new DataMigrator(new File(this.getDataFolder(), "config.yml"), this,
                Bukkit.getServer(), registry);

        if (dataMigrator.isMigrationNecessary()) {
            Map<String, Object> updatedConfig = dataMigrator.getUpdatedConfig();
            this.saveResource("config.yml", true);
            this.reloadConfig();
            dataMigrator.updateFileConfiguration(getConfig(), updatedConfig);
            this.reloadConfig();
            dataMigrator.run();
        }
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
        config = new StargateYamlConfiguration();
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

    private void loadGateFormats() {
        saveDefaultGates();
        List<GateFormat> gateFormats = GateFormatHandler.loadGateFormats(new File(DATA_FOLDER, GATE_FOLDER), this);
        if (gateFormats == null) {
            log(Level.SEVERE, "Unable to load gate formats from the gate format folder");
            GateFormatHandler.setFormats(new ArrayList<>());
        } else {
            GateFormatHandler.setFormats(gateFormats);
        }
    }

    public void reload() {
        loadGateFormats();
        load();
    }

    private void load() {
        loadColors();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE)) {
            BungeeHelper.loadBungeeServerName(DATA_FOLDER,INTERNAL_FOLDER);
        }
        economyManager = new EconomyManager();
        String debugLevelString = ConfigurationHelper.getString(ConfigurationOption.DEBUG_LEVEL);
        if (debugLevelString == null) {
            lowestMessageLevel = Level.INFO;
        } else {
            lowestMessageLevel = Level.parse(debugLevelString);
        }
        languageManager.setLanguage(ConfigurationHelper.getString(ConfigurationOption.LANGUAGE));

        try {
            storageAPI = new PortalDatabaseAPI(this);
            registry = new StargateRegistry(storageAPI);
            registry.loadPortals();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        //Close networked always-on Stargates as they have no destination on next start
        PortalHelper.closeAllPortals(registry.getBungeeNetworkMap());
        PortalHelper.closeAllPortals(registry.getNetworkMap());
        /*
         * Replacement for legacy, which used:
         * methodPortal.closeAllGates(this); Portal.clearGates(); managedWorlds.clear();
         */
        syncTickPopulator.forceDoAllTasks();
        syncSecPopulator.forceDoAllTasks();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            Messenger messenger = Bukkit.getMessenger();
            messenger.unregisterOutgoingPluginChannel(this);
            messenger.unregisterIncomingPluginChannel(this);
        }
        getServer().getScheduler().cancelTasks(this);

        if (!ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            return;
        }
        storageAPI.endInterServerConnection();
    }

    public static void log(Level priorityLevel, String message) {
        if (instance != null) {
            instance.logMessage(priorityLevel, message);
            return;
        }
        System.out.println(message);
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
        if (priorityLevel.intValue() >= this.lowestMessageLevel.intValue()) {
            if (priorityLevel.intValue() < Level.INFO.intValue()) {
                this.getLogger().log(Level.INFO, message);
            } else {
                this.getLogger().log(priorityLevel, message);
            }
        }
    }

    public static FileConfiguration getFileConfiguration() {
        if (instance != null) {
            return instance.getConfig();
        } else {
            return staticConfig;
        }
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

    public static RegistryAPI getRegistry() {
        return registry;
    }

    public static StorageAPI getStorageAPI() {
        return storageAPI;
    }

}
