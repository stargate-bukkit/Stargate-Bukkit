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

import dev.thorinwasher.blockutil.api.BlockUtilAPI;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import org.sgrewritten.stargate.colors.ColorRegistry;
import org.sgrewritten.stargate.command.CommandStargate;
import org.sgrewritten.stargate.command.StargateTabCompleter;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.StargateYamlConfiguration;
import org.sgrewritten.stargate.database.SQLDatabase;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.property.PropertiesDatabase;
import org.sgrewritten.stargate.database.property.StoredPropertiesAPI;
import org.sgrewritten.stargate.database.property.StoredProperty;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.economy.VaultEconomyManager;
import org.sgrewritten.stargate.exception.StargateInitializationException;
import org.sgrewritten.stargate.formatting.StargateLanguageManager;
import org.sgrewritten.stargate.listener.BKCommonLibListener;
import org.sgrewritten.stargate.listener.BlockEventListener;
import org.sgrewritten.stargate.listener.EntityInsideBlockEventListener;
import org.sgrewritten.stargate.listener.MoveEventListener;
import org.sgrewritten.stargate.listener.PlayerAdvancementListener;
import org.sgrewritten.stargate.listener.PlayerEventListener;
import org.sgrewritten.stargate.listener.PluginEventListener;
import org.sgrewritten.stargate.listener.StargateBungeePluginMessageListener;
import org.sgrewritten.stargate.manager.BlockLoggingManager;
import org.sgrewritten.stargate.manager.CoreProtectManager;
import org.sgrewritten.stargate.manager.BlockDropManager;
import org.sgrewritten.stargate.manager.StargateBungeeManager;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.migration.DataMigrator;
import org.sgrewritten.stargate.network.StargateNetworkManager;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.property.NonLegacyClass;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.property.StargateConstant;
import org.sgrewritten.stargate.thread.task.StargateQueuedAsyncTask;
import org.sgrewritten.stargate.thread.task.StargateRegionTask;
import org.sgrewritten.stargate.thread.task.StargateTask;
import org.sgrewritten.stargate.util.BStatsHelper;
import org.sgrewritten.stargate.util.BungeeHelper;
import org.sgrewritten.stargate.util.FileHelper;
import org.sgrewritten.stargate.util.database.DatabaseHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final String dataFolder = this.getDataFolder().getAbsolutePath();
    private static Level logLevel = StargateConstant.PRE_STARTUP_LOG_LEVEL; //setting before config loads
    private PluginManager pluginManager;
    private StorageAPI storageAPI;
    private LanguageManager languageManager;
    private BungeeManager bungeeManager;
    private StargateEconomyAPI economyManager;
    private ServicesManager servicesManager;
    private static String serverName;
    private static boolean knowsServerName = false;

    private static UUID serverUUID;

    private FileConfiguration config;

    private StargateRegistry registry;

    private BlockLoggingManager blockLogger;

    private PropertiesDatabase storedProperties;

    private static final FileConfiguration EMPTY_CONFIG = new StargateYamlConfiguration();
    private BlockHandlerResolver blockHandlerResolver;
    private NetworkManager networkManager;
    private SQLDatabaseAPI database;


    @Override
    public void onEnable() {
        try {
            Stargate.setInstance(this);
            if (!new File(this.getDataFolder(), StargateConstant.CONFIG_FILE).exists()) {
                super.saveDefaultConfig();
            }
            fetchServerId();
            storedProperties = new PropertiesDatabase(FileHelper.createHiddenFileIfNotExists(dataFolder, StargateConstant.INTERNAL_FOLDER, StargateConstant.INTERNAL_PROPERTIES_FILE));
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
            StargateRegionTask.startPopulator(this);
            StargateQueuedAsyncTask.enableAsyncQueue();
            registerCommands();
            sendWarningMessages();

            //Register bStats metrics
            int pluginId = 13629;
            BStatsHelper.registerMetrics(pluginId, this, getRegistry());
            servicesManager = this.getServer().getServicesManager();
            servicesManager.register(StargateAPI.class, this, this, ServicePriority.High);
            RegisteredServiceProvider<BlockUtilAPI> blockUtilProvider = servicesManager.getRegistration(BlockUtilAPI.class);
            BlockDropManager.setProvider(blockUtilProvider);

        } catch (StargateInitializationException | IOException | SQLException | URISyntaxException e) {
            Stargate.log(e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private static void setInstance(@Nullable Stargate stargate) {
        Stargate.instance = stargate;
    }

    private void sendWarningMessages() {
        if ("true".equals(storedProperties.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE))) {
            try (InputStream inputStream = Stargate.class.getResourceAsStream("/messages/parityMessage.txt")) {
                Stargate.log(Level.WARNING, "\n" + FileHelper.readStreamToString(inputStream));
            } catch (IOException e) {
                Stargate.log(e);
            }
        }
        String scheduledGateClearingString = storedProperties.getProperty(StoredProperty.SCHEDULED_GATE_CLEARING);
        if (scheduledGateClearingString != null && Long.parseLong(scheduledGateClearingString) > System.currentTimeMillis()) {
            try (InputStream inputStream = Stargate.class.getResourceAsStream("/messages/gateClearingMessage.txt")) {
                Date date = new Date(Long.parseLong(scheduledGateClearingString));
                String dateString = date.toString();
                String unformattedMessage = FileHelper.readStreamToString(inputStream);
                String message = unformattedMessage.replace("%time%", dateString);
                message = message.replace("%gateFormats%", this.storageAPI.getScheduledGatesClearing().toString());
                Stargate.log(Level.WARNING, message);
            } catch (IOException e) {
                Stargate.log(e);
            }
        }
    }

    private void setupManagers() throws StargateInitializationException, SQLException, IOException, URISyntaxException {
        String languageFolder = "lang";
        languageManager = new StargateLanguageManager(new File(dataFolder, languageFolder));
        economyManager = new VaultEconomyManager(languageManager);
        database = DatabaseHelper.loadDatabase(this);
        storageAPI = new SQLDatabase(database, storedProperties);
        blockHandlerResolver = new BlockHandlerResolver(storageAPI);
        registry = new StargateRegistry(storageAPI, blockHandlerResolver);
        networkManager = new StargateNetworkManager(registry, storageAPI);
        bungeeManager = new StargateBungeeManager(this.getRegistry(), this.getLanguageManager(), this.getNetworkManager());
        blockLogger = new CoreProtectManager();
        GateFormatRegistry.loadGateFormats(this.getDataFolder());
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
     * Gets the absolute path to Stargate's data folder
     *
     * @return <p>The absolute path to the data folder</p>
     */
    public String getAbsoluteDataFolder() {
        return dataFolder;
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
     * Registers all necessary listeners for this plugin
     */
    private void registerListeners() {
        pluginManager.registerEvents(new BlockEventListener(this), this);
        pluginManager.registerEvents(new MoveEventListener(getRegistry()), this);
        pluginManager.registerEvents(new PlayerEventListener(this.getLanguageManager(), getRegistry(), this.getBungeeManager(), this.getBlockLoggerManager(), this.getStorageAPI()), this);
        pluginManager.registerEvents(new PluginEventListener(getEconomyManager(), getBlockLoggerManager()), this);
        if (NonLegacyClass.PLAYER_ADVANCEMENT_CRITERION_EVENT.isImplemented()) {
            pluginManager.registerEvents(new PlayerAdvancementListener(getRegistry()), this);
        }
        if (NonLegacyClass.ENTITY_INSIDE_BLOCK_EVENT.isImplemented()) {
            pluginManager.registerEvents(new EntityInsideBlockEventListener(getRegistry()), this);
        }
        if (NonLegacyClass.MULTI_BLOCK_CHANGE_EVENT.isImplemented()) {
            pluginManager.registerEvents(new BKCommonLibListener(getRegistry(), getNetworkManager()), this);
        }
    }

    /**
     * Migrates data files and configuration files if necessary
     *
     * @throws IOException                   <p>If unable to load or save a configuration file</p>
     * @throws InvalidConfigurationException <p>If unable to save the new configuration</p>
     * @throws SQLException                  <p>If unable to initialize the Portal Database API</p>
     */
    private boolean migrateConfigurationAndData() throws IOException, InvalidConfigurationException, SQLException, StargateInitializationException, URISyntaxException {
        DataMigrator dataMigrator = new DataMigrator(new File(this.getDataFolder(), StargateConstant.CONFIG_FILE), this.getDataFolder(), this.getStoredPropertiesAPI());

        if (dataMigrator.isMigrationNecessary()) {
            File debugDirectory = new File(this.getDataFolder(), "debug");
            if (!debugDirectory.isDirectory() && !debugDirectory.mkdir()) {
                throw new IOException("Could not create directory: " + debugDirectory);
            }
            FileUtils.copyFile(new File(this.getDataFolder(), StargateConstant.CONFIG_FILE), new File(debugDirectory, StargateConstant.CONFIG_FILE + ".old"));
            Map<String, Object> updatedConfig = dataMigrator.getUpdatedConfig();
            this.saveResource(StargateConstant.CONFIG_FILE, true);
            this.reloadConfig();
            dataMigrator.updateFileConfiguration(getConfig(), updatedConfig);
            this.reloadConfig();
            GateFormatRegistry.loadGateFormats(this.getDataFolder());
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
            config.load(new File(this.getDataFolder(), StargateConstant.CONFIG_FILE));
        } catch (IOException | InvalidConfigurationException e) {
            Stargate.log(e);
        }
    }

    @Override
    public void saveConfig() {
        try {
            config.save(new File(this.getDataFolder(), StargateConstant.CONFIG_FILE));
        } catch (IOException e) {
            Stargate.log(e);
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
            GateFormatRegistry.loadGateFormats(this.getDataFolder());
            if (storageAPI instanceof SQLDatabase sqlDatabase) {
                sqlDatabase.load(DatabaseHelper.loadDatabase(this));
            }
            registry.clear();
            networkManager.loadPortals(this);
            economyManager.setupEconomy();
        } catch (StargateInitializationException | IOException | SQLException | URISyntaxException e) {
            Stargate.log(e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void load() throws StargateInitializationException {
        ColorRegistry.loadDefaultColorsFromConfig();
        fetchServerId();
        blockLogger.setUpLogging();
        String defaultNetwork = ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK);
        if (defaultNetwork.length() >= StargateConstant.MAX_TEXT_LENGTH) {
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
            BungeeHelper.getServerId(dataFolder, StargateConstant.INTERNAL_FOLDER);
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
        StargateQueuedAsyncTask.disableAsyncQueue();
        StargateTask.forceRunAllTasks();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            Messenger messenger = Bukkit.getMessenger();
            messenger.unregisterOutgoingPluginChannel(this);
            messenger.unregisterIncomingPluginChannel(this);
        }

        if (NonLegacyClass.REGIONIZED_SERVER.isImplemented()) {
            getServer().getGlobalRegionScheduler().cancelTasks(this);
        } else {
            getServer().getScheduler().cancelTasks(this);
        }
        setInstance(null);

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
        StringBuilder exceptionBuilder = new StringBuilder(throwable.getClass().getName() + (throwable.getMessage() == null ? "" : ": " + throwable.getMessage()) + "\n");
        exceptionBuilder.append(Stargate.getStackTrace(throwable));
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
            exceptionBuilder.append("Caused by: ").append(throwable.getClass().getName());
            exceptionBuilder.append(throwable.getMessage() == null ? "" : ": " + throwable.getMessage()).append("\n");
            exceptionBuilder.append(getStackTrace(throwable));
        }
        Stargate.log(logLevel, exceptionBuilder.toString());
    }

    private static String getStackTrace(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            builder.append("\t at ").append(element.toString()).append("\n");
        }
        return builder.toString();
    }

    public static void log(Level priorityLevel, String message) {
        if (priorityLevel.intValue() < Stargate.logLevel.intValue()) {
            return;
        }
        if (instance != null) {
            instance.logMessage(priorityLevel, message);
        } else {
            Logger.getLogger(Stargate.class.getName()).log(priorityLevel, message);
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
            return EMPTY_CONFIG;
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
