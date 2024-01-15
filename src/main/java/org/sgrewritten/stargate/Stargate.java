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
package org.sgrewritten.stargate;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.action.SimpleAction;
import org.sgrewritten.stargate.api.BlockHandlerResolver;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.config.ConfigurationAPI;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.api.manager.BungeeManager;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.colors.ColorConverter;
import org.sgrewritten.stargate.colors.ColorNameInterpreter;
import org.sgrewritten.stargate.command.CommandStargate;
import org.sgrewritten.stargate.command.StargateTabCompleter;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.StargateYamlConfiguration;
import org.sgrewritten.stargate.database.SQLDatabase;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.property.PropertiesDatabase;
import org.sgrewritten.stargate.database.property.StoredPropertiesAPI;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.economy.VaultEconomyManager;
import org.sgrewritten.stargate.exception.StargateInitializationException;
import org.sgrewritten.stargate.formatting.StargateLanguageManager;
import org.sgrewritten.stargate.gate.GateFormat;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.listener.BlockEventListener;
import org.sgrewritten.stargate.listener.EntityInsideBlockEventListener;
import org.sgrewritten.stargate.listener.MoveEventListener;
import org.sgrewritten.stargate.listener.PlayerAdvancementListener;
import org.sgrewritten.stargate.listener.PlayerEventListener;
import org.sgrewritten.stargate.listener.PluginEventListener;
import org.sgrewritten.stargate.listener.StargateBungeePluginMessageListener;
import org.sgrewritten.stargate.manager.BlockLoggingManager;
import org.sgrewritten.stargate.manager.CoreProtectManager;
import org.sgrewritten.stargate.manager.StargateBungeeManager;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.migration.DataMigrator;
import org.sgrewritten.stargate.network.StargateNetworkManager;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.property.NonLegacyMethod;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.thread.SynchronousPopulator;
import org.sgrewritten.stargate.thread.ThreadHelper;
import org.sgrewritten.stargate.util.BStatsHelper;
import org.sgrewritten.stargate.util.BungeeHelper;
import org.sgrewritten.stargate.util.FileHelper;
import org.sgrewritten.stargate.util.database.DatabaseHelper;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A plugin that allows for instant-teleportation between large distances and across servers.
 * Highly customisable and designed with end-user portal creation in mind!
 * <p><a href="https://git.io/JuWkU">https://git.io/JuWkU</a> |
 * <a href="https://discord.gg/mTaHuK6BVa">https://discord.gg/mTaHuK6BVa</a></p>
 * <p>Lead Developers:
 *
 * @author Thorin (2020-Present)
 * @author EpicKnarvik97 (2021-Present)
 * @author PseudoKnight (2015-2020)
 * @author Drakia (2011-2013)
 * @author Dinnerbone (2010-2011)
 */
public class Stargate extends JavaPlugin implements StargateAPI, ConfigurationAPI {

    private static Stargate instance;


    private final String DATA_FOLDER = this.getDataFolder().getAbsolutePath();
    private static final String INTERNAL_GATE_FOLDER = "gates";
    private static final String INTERNAL_FOLDER = ".internal";
    private static final String INTERNAL_PROPERTIES_FILE = "stargate.properties";
    private static final String CONFIG_FILE = "config.yml";
    private static final int CURRENT_CONFIG_VERSION = 7;

    private static Level logLevel = Level.INFO;//setting before config loads
    private String gateFolder;
    private PluginManager pluginManager;
    private StorageAPI storageAPI;
    private LanguageManager languageManager;
    private BungeeManager bungeeManager;
    private final SynchronousPopulator synchronousTickPopulator = new SynchronousPopulator();
    private final SynchronousPopulator syncSecPopulator = new SynchronousPopulator();
    private static final int MAX_TEXT_LENGTH = 13;

    private StargateEconomyAPI economyManager;
    private ServicesManager servicesManager;
    private static String serverName;
    private static boolean knowsServerName = false;

    private static UUID serverUUID;

    private org.bukkit.ChatColor legacySignColor;

    private FileConfiguration config;

    private StargateRegistry registry;

    private BlockLoggingManager blockLogger;

    private PropertiesDatabase storedProperties;

    private static final FileConfiguration staticConfig = new StargateYamlConfiguration();
    private BlockHandlerResolver blockHandlerResolver;
    private NetworkManager networkManager;
    private SQLDatabaseAPI database;
    private ChatColor defaultTextColor;
    private ChatColor defaultPointerColor;
    private DyeColor defaultDyeColor;
    private BukkitTask asyncCycleThroughTask;


    @Override
    public void onEnable() {
        try {
            instance = this;
            if (!new File(this.getDataFolder(), CONFIG_FILE).exists()) {
                super.saveDefaultConfig();
            }
            fetchServerId();
            storedProperties = new PropertiesDatabase(FileHelper.createHiddenFileIfNotExists(DATA_FOLDER, INTERNAL_FOLDER, INTERNAL_PROPERTIES_FILE));
            this.setupManagers();
            boolean hasMigrated = false;
            try {
                hasMigrated = this.migrateConfigurationAndData();
            } catch (IOException | InvalidConfigurationException | SQLException e) {
                Stargate.log(e);
            }

            load();
            if (!hasMigrated) {
                networkManager.loadPortals(this);
            }

            pluginManager = getServer().getPluginManager();
            
            registerListeners();
            if (NonLegacyMethod.FOLIA.isImplemented()) {
                GlobalRegionScheduler globalScheduler = getServer().getGlobalRegionScheduler();
                globalScheduler.runAtFixedRate(this, task -> synchronousTickPopulator.run(), 1L, 1L);
                globalScheduler.runAtFixedRate(this, task -> syncSecPopulator.run(), 1L, 20L);
                        
            } else {
                BukkitScheduler scheduler;
                scheduler = getServer().getScheduler();
                scheduler.runTaskTimer(this, synchronousTickPopulator, 0L, 1L);
                scheduler.runTaskTimer(this, syncSecPopulator, 0L, 20L);
                ThreadHelper.setAsyncQueueEnabled(true);
                this.asyncCycleThroughTask = scheduler.runTaskAsynchronously(this, ThreadHelper::cycleThroughAsyncQueue);
            }
            
            registerCommands();

            //Register bStats metrics
            int pluginId = 13629;
            BStatsHelper.registerMetrics(pluginId, this, getRegistry());
            servicesManager = this.getServer().getServicesManager();
            servicesManager.register(StargateAPI.class, this, this, ServicePriority.High);
        } catch (StargateInitializationException | IOException | SQLException e) {
            Stargate.log(e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void setupManagers() throws StargateInitializationException, SQLException, IOException {
        String LANGUAGE_FOLDER = "lang";
        languageManager = new StargateLanguageManager(new File(DATA_FOLDER, LANGUAGE_FOLDER));
        economyManager = new VaultEconomyManager(languageManager);
        database = DatabaseHelper.loadDatabase(this);
        storageAPI = new SQLDatabase(database);
        blockHandlerResolver = new BlockHandlerResolver(storageAPI);
        registry = new StargateRegistry(storageAPI, blockHandlerResolver);
        networkManager = new StargateNetworkManager(registry, storageAPI);
        bungeeManager = new StargateBungeeManager(this.getRegistry(), this.getLanguageManager(), this.getNetworkManager());
        blockLogger = new CoreProtectManager();
        loadGateFormats();
    }

    /**
     * Gets the economy manager used for this plugin
     *
     * @return <p>The economy manager used for this plugin</p>
     */
    public StargateEconomyAPI getEconomyManager() {
        return economyManager;
    }

    @Override
    public BlockHandlerResolver getMaterialHandlerResolver() {
        return this.blockHandlerResolver;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return this.networkManager;
    }

    /**
     * Gets the current version of the Stargate configuration
     *
     * @return <p>The current version of the Stargate configuration</p>
     */
    public static int getCurrentConfigVersion() {
        return CURRENT_CONFIG_VERSION;
    }

    /**
     * Gets the absolute path to Stargate's data folder
     *
     * @return <p>The absolute path to the data folder</p>
     */
    public String getAbsoluteDataFolder() {
        return DATA_FOLDER;
    }

    /**
     * Gets the gate folder where all .gate files are stored
     *
     * @return <p>The gate folder</p>
     */
    public String getGateFolder() {
        return gateFolder;
    }

    /**
     * Adds a 1-tick action
     *
     * <p>The task is added to a queue which is processed every tick. Should be used in tasks that need to be finished
     * within a short time frame</p>
     *
     * @param action <p>The action to add</p>
     */
    public static void addSynchronousTickAction(SimpleAction action) {
        if (getInstance() == null) {
            return;
        }
        getInstance().synchronousTickPopulator.addAction(action);
    }

    /**
     * Adds a 1-second action
     *
     * <p>The task is added to a queue which is processed every second (20 ticks). Should be used in delayed actions</p>
     *
     * @param action <p>The action to add</p>
     */
    public static void addSynchronousSecAction(SimpleAction action) {
        if (getInstance() == null) {
            return;
        }
        getInstance().syncSecPopulator.addAction(action, false);
    }

    /**
     * Adds a 1-second action
     *
     * <p>The task is added to a queue which is processed every second (20 ticks). Should be used in delayed actions</p>
     *
     * @param action   <p>The action to add</p>
     * @param isBungee <p>Whether the action relies on the server name being known and should be put in the bungee queue</p>
     */
    public static void addSynchronousSecAction(SimpleAction action, boolean isBungee) {
        if (getInstance() == null) {
            return;
        }
        getInstance().syncSecPopulator.addAction(action, isBungee);
    }

    /**
     * Gets the max text length which will fit on a sign
     *
     * <p>The string length of a name consisting of only 'i'. This will fill a sign (including {@literal <>})</p>
     *
     * @return <p>The max text length which will fit on a sign</p>
     */
    public static int getMaxTextLength() {
        return MAX_TEXT_LENGTH;
    }

    /**
     * Gets the name of this server as defined by BungeeCord
     *
     * @return <p>The name of this server</p>
     */
    public static String getServerName() {
        return Stargate.serverName;
    }

    /**
     * Sets this server's name as defined by BungeeCord
     *
     * @param serverName <p>The new name of this server</p>
     */
    public static void setServerName(String serverName) {
        Stargate.serverName = serverName;
    }

    /**
     * Gets whether this server knows its own name
     *
     * @return <p>True if this server knows its own name</p>
     */
    public static boolean knowsServerName() {
        return Stargate.knowsServerName;
    }

    /**
     * Sets whether this server knows its server name
     *
     * @param knowsServerName <p>Whether this server knows its server name</p>
     */
    public static void setKnowsServerName(boolean knowsServerName) {
        Stargate.knowsServerName = knowsServerName;
    }

    /**
     * Gets this server's unique id
     *
     * @return <p>This server's unique id</p>
     */
    public static String getServerUUID() {
        return Stargate.serverUUID.toString();
    }

    /**
     * Sets this server's unique id
     *
     * @param serverUUID <p>This server's new unique id</p>
     */
    public static void setServerUUID(UUID serverUUID) {
        Stargate.serverUUID = serverUUID;
    }

    /**
     * Gets the default color used for light signs with legacy coloring
     *
     * @return <p>The default legacy color used for light signs</p>
     */
    public org.bukkit.ChatColor getLegacySignColor() {
        return this.legacySignColor;
    }

    public static ChatColor getDefaultPointerColor() {
        if (instance == null) {
            return ChatColor.WHITE;
        }
        return instance.defaultPointerColor;
    }

    public static ChatColor getDefaultTextColor() {
        if (instance == null) {
            return ChatColor.BLACK;
        }
        return instance.defaultTextColor;
    }

    public static DyeColor getDefaultDyeColor() {
        if (instance == null) {
            return DyeColor.BLACK;
        }
        return instance.defaultDyeColor;
    }

    private void loadColors() {
        try {
            if (!NonLegacyMethod.CHAT_COLOR.isImplemented()) {
                logMessage(Level.INFO, "Default stargate coloring is not supported on your current server implementation");
                this.legacySignColor = org.bukkit.ChatColor.valueOf(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_SIGN_COLOR).toUpperCase());
                return;
            }
            String defaultColorString = ConfigurationHelper.getString(ConfigurationOption.DEFAULT_SIGN_COLOR);
            this.defaultTextColor = ColorNameInterpreter.getDefaultTextColor(defaultColorString);
            this.defaultPointerColor = ColorNameInterpreter.getDefaultPointerColor(defaultColorString);
            this.defaultDyeColor = ColorConverter.getClosestDyeColor(defaultTextColor);
        } catch (IllegalArgumentException | NullPointerException e) {
            Stargate.log(e);
            Stargate.log(Level.WARNING, "Invalid colors for sign text. Using default colors instead...");
        }
    }

    /**
     * Registers all necessary listeners for this plugin
     */
    private void registerListeners() {
        pluginManager.registerEvents(new BlockEventListener(this), this);
        pluginManager.registerEvents(new MoveEventListener(getRegistry()), this);
        pluginManager.registerEvents(new PlayerEventListener(this.getLanguageManager(), getRegistry(), this.getBungeeManager(), this.getBlockLoggerManager(), this.getStorageAPI()), this);
        pluginManager.registerEvents(new PluginEventListener(getEconomyManager(), getBlockLoggerManager()), this);
        if (NonLegacyMethod.PLAYER_ADVANCEMENT_CRITERION_EVENT.isImplemented()) {
            pluginManager.registerEvents(new PlayerAdvancementListener(getRegistry()), this);
        }
        if (NonLegacyMethod.ENTITY_INSIDE_BLOCK_EVENT.isImplemented()) {
            pluginManager.registerEvents(new EntityInsideBlockEventListener(getRegistry()), this);
        }
    }

    /**
     * Saves all the default gate designs to the gate folder
     *
     * @throws IOException <p>If unable to read or write the default gates</p>
     */
    private void saveDefaultGates() throws IOException {
        //TODO is there a way to check all files in a resource-folder? Possible solution seems unnecessarily complex
        String[] gateList = {"nether.gate", "water.gate", "wool.gate", "end.gate"};
        File directory = new File(this.getDataFolder(), this.getGateFolder());
        if (!directory.exists() && !directory.mkdirs()) {
            Stargate.log(Level.SEVERE, "Could not make gates directory");
        }
        for (String gateName : gateList) {
            File fileToWrite = new File(directory, gateName);
            if (!fileToWrite.exists()) {
                InputStream stream = this.getResource(INTERNAL_GATE_FOLDER + "/" + gateName);
                if (stream == null) {
                    Stargate.log(Level.WARNING, "Unable to read internal gate file " + gateName);
                    continue;
                }
                stream.transferTo(new FileOutputStream(fileToWrite));
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
    private boolean migrateConfigurationAndData() throws IOException, InvalidConfigurationException, SQLException, StargateInitializationException {
        DataMigrator dataMigrator = new DataMigrator(new File(this.getDataFolder(), CONFIG_FILE), this.getServer(), this.getStoredPropertiesAPI());

        if (dataMigrator.isMigrationNecessary()) {
            File debugDirectory = new File(this.getDataFolder(), "debug");
            if (!debugDirectory.isDirectory() && !debugDirectory.mkdir()) {
                throw new IOException("Could not create directory: " + debugDirectory);
            }
            FileUtils.copyFile(new File(this.getDataFolder(), CONFIG_FILE), new File(debugDirectory, CONFIG_FILE + ".old"));
            Map<String, Object> updatedConfig = dataMigrator.getUpdatedConfig();
            this.saveResource(CONFIG_FILE, true);
            this.reloadConfig();
            dataMigrator.updateFileConfiguration(getConfig(), updatedConfig);
            this.reloadConfig();
            this.loadGateFormats();
            this.setupManagers();
            dataMigrator.run(database, this);
            return true;
        }
        return false;
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
            config.load(new File(this.getDataFolder(), CONFIG_FILE));
        } catch (IOException | InvalidConfigurationException e) {
            Stargate.log(e);
        }
    }

    @Override
    public void saveConfig() {
        try {
            config.save(new File(this.getDataFolder(), CONFIG_FILE));
        } catch (IOException e) {
            Stargate.log(e);
        }
    }

    private void loadGateFormats() throws IOException {
        this.gateFolder = ConfigurationHelper.getString(ConfigurationOption.GATE_FOLDER);
        saveDefaultGates();
        List<GateFormat> gateFormats = GateFormatHandler.loadGateFormats(new File(this.getDataFolder(), this.getGateFolder()));
        if (gateFormats == null) {
            log(Level.SEVERE, "Unable to load gate formats from the gate format folder");
            GateFormatRegistry.setFormats(new ArrayList<>());
        } else {
            GateFormatRegistry.setFormats(gateFormats);
        }
    }

    @Override
    public void setConfigurationOptionValue(ConfigurationOption configurationOption, Object newValue) {
        config.set(configurationOption.getConfigNode(), newValue);
    }

    @Override
    public Object getConfigurationOptionValue(ConfigurationOption configurationOption) {
        return config.get(configurationOption.getConfigNode());
    }

    @Override
    public void saveConfiguration() {
        saveConfig();
    }

    @Override
    public void reload() {
        registry.getNetworkRegistry(StorageType.LOCAL).closeAllPortals();
        registry.getNetworkRegistry(StorageType.INTER_SERVER).closeAllPortals();
        try {
            load();
            loadGateFormats();
            if (storageAPI instanceof SQLDatabase) {
                SQLDatabaseAPI database = DatabaseHelper.loadDatabase(this);
                ((SQLDatabase) storageAPI).load(database);
            }
            registry.clear(this);
            networkManager.loadPortals(this);
            economyManager.setupEconomy();
        } catch (StargateInitializationException | IOException | SQLException e) {
            Stargate.log(e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void load() throws StargateInitializationException {
        loadColors();
        fetchServerId();
        blockLogger.setUpLogging();
        String defaultNetwork = ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK);
        if (defaultNetwork.length() >= Stargate.MAX_TEXT_LENGTH) {
            throw new StargateInitializationException("Invalid configuration name for default network, '" + defaultNetwork + "' name too long");
        }
        if (defaultNetwork.isBlank()) {
            throw new StargateInitializationException("Invalid configuration name for default network, name can not be empty");
        }
        if (defaultNetwork.contains("\n")) {
            throw new StargateInitializationException("Invalid configuration name for default network, name can not contain newlines");
        }
        languageManager.setLanguage(ConfigurationHelper.getString(ConfigurationOption.LANGUAGE));
        loadConfigLevel();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            Messenger messenger = Bukkit.getMessenger();

            messenger.registerOutgoingPluginChannel(this, PluginChannel.BUNGEE.getChannel());
            messenger.registerIncomingPluginChannel(this, PluginChannel.BUNGEE.getChannel(), new StargateBungeePluginMessageListener(getBungeeManager()));
        }
    }

    private void fetchServerId() {

        if (ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE)) {
            BungeeHelper.getServerId(DATA_FOLDER, INTERNAL_FOLDER);
        }
    }

    private static void loadConfigLevel() {

        String debugLevelString = ConfigurationHelper.getString(ConfigurationOption.DEBUG_LEVEL);
        if (debugLevelString == null) {
            logLevel = Level.INFO;
        } else {
            logLevel = Level.parse(debugLevelString);
        }
    }

    @Override
    public void onDisable() {
        //Close networked always-on Stargates as they have no destination on next start
        registry.getNetworkRegistry(StorageType.LOCAL).closeAllPortals();
        registry.getNetworkRegistry(StorageType.INTER_SERVER).closeAllPortals();
        /*
         * Replacement for legacy, which used:
         * methodPortal.closeAllGates(this); Portal.clearGates(); managedWorlds.clear();
         */
        synchronousTickPopulator.clear();
        syncSecPopulator.clear();
        ThreadHelper.setAsyncQueueEnabled(false);
        if (ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            Messenger messenger = Bukkit.getMessenger();
            messenger.unregisterOutgoingPluginChannel(this);
            messenger.unregisterIncomingPluginChannel(this);
        }
        
        if (NonLegacyMethod.FOLIA.isImplemented()) {
            getServer().getGlobalRegionScheduler().cancelTasks(this);
        } else {
            getServer().getScheduler().cancelTasks(this);
        }
        instance = null;
        
        if (!ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            return;
        }
        servicesManager.unregisterAll(this);
    }

    public static void log(Throwable throwable) {
        Stargate.log(Level.WARNING, throwable);
    }

    public static void log(Level logLevel, Throwable throwable) {
        if (throwable == null) {
            return;
        }
        Stargate.log(logLevel, throwable.getClass().getName() + (throwable.getMessage() == null ? "" : " : " + throwable.getMessage()));
        Stargate.logError(logLevel, throwable);
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
            Stargate.log(logLevel, "Caused by: " + throwable.getClass().getName() + (throwable.getMessage() == null ? "" : " : " + throwable.getMessage()));
            logError(logLevel, throwable);
        }
    }

    private static void logError(Level logLevel, Throwable throwable) {
        for (StackTraceElement element : throwable.getStackTrace()) {
            Stargate.log(logLevel, "\t at " + element.toString());
        }
    }

    public static void log(Level priorityLevel, String message) {
        if (priorityLevel.intValue() < Stargate.logLevel.intValue()) {
            return;
        }
        if (instance != null) {
            instance.logMessage(priorityLevel, message);
        } else {
            System.out.println("[" + priorityLevel + "]: " + message);
        }
    }

    /**
     * Change the log level of Stargate. Does not save new level to the config
     *
     * @param priorityLevel <p> The new priority level to set </p>
     */
    public static void setLogLevel(Level priorityLevel) {
        Stargate.logLevel = priorityLevel;
    }

    /**
     * Gets an instance of this plugin
     *
     * @return <p>An instance of this plugin</p>
     */
    public static Stargate getInstance() {
        return instance;
    }

    public void logMessage(Level priorityLevel, String message) {
        if (priorityLevel.intValue() < logLevel.intValue()) {
            return;
        }
        if (priorityLevel.intValue() < Level.INFO.intValue()) {
            this.getLogger().log(Level.INFO, message);
        } else {
            this.getLogger().log(priorityLevel, message);
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
            stargateCommand.setExecutor(new CommandStargate(this));
            stargateCommand.setTabCompleter(new StargateTabCompleter(this.getStoredPropertiesAPI()));
        }
    }

    public static StorageAPI getStorageAPIStatic() {
        return instance.storageAPI;
    }

    @Override
    public RegistryAPI getRegistry() {
        return registry;
    }

    @Override
    public StorageAPI getStorageAPI() {
        return storageAPI;
    }

    @Override
    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    @Override
    public PermissionManager getPermissionManager(Entity entity) {
        return new StargatePermissionManager(entity, this.getLanguageManager());
    }

    @Override
    public ConfigurationAPI getConfigurationAPI() {
        return this;
    }

    @Override
    public BungeeManager getBungeeManager() {
        return this.bungeeManager;
    }

    /**
     * @return <p> The block-logger used for dealing with external block logging plugin compatibility </p>
     */
    public BlockLoggingManager getBlockLoggerManager() {
        return this.blockLogger;
    }

    /**
     * @return <p> The database dealing with internally stored properties </p>
     */
    public StoredPropertiesAPI getStoredPropertiesAPI() {
        return this.storedProperties;
    }

}
