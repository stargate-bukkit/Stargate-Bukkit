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
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Tag;
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
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.action.SimpleAction;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.command.CommandStargate;
import org.sgrewritten.stargate.command.StargateTabCompleter;
import org.sgrewritten.stargate.config.ConfigurationAPI;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.config.StargateYamlConfiguration;
import org.sgrewritten.stargate.database.SQLDatabase;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.SQLiteDatabase;
import org.sgrewritten.stargate.database.StorageAPI;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.economy.VaultEconomyManager;
import org.sgrewritten.stargate.exception.StargateInitializationException;
import org.sgrewritten.stargate.formatting.LanguageManager;
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
import org.sgrewritten.stargate.manager.PermissionManager;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.migration.DataMigrator;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.property.NonLegacyMethod;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.thread.SynchronousPopulator;
import org.sgrewritten.stargate.util.BStatsHelper;
import org.sgrewritten.stargate.util.BungeeHelper;
import org.sgrewritten.stargate.util.colors.ColorConverter;
import org.sgrewritten.stargate.util.colors.ColorNameInterpreter;
import org.sgrewritten.stargate.util.colors.ColorProperty;
import org.sgrewritten.stargate.util.database.DatabaseHelper;
import org.sgrewritten.stargate.util.portal.PortalHelper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
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
public class Stargate extends JavaPlugin implements StargateLogger, StargateAPI, ConfigurationAPI {

    private static Stargate instance;

    private Level lowestMessageLevel = Level.INFO;//setting before config loads

    private final String DATA_FOLDER = this.getDataFolder().getAbsolutePath();
    private final String GATE_FOLDER = "gates";

    private PluginManager pluginManager;

    private StorageAPI storageAPI;
    private LanguageManager languageManager;
    private static final int CURRENT_CONFIG_VERSION = 6;
    private static final SynchronousPopulator synchronousTickPopulator = new SynchronousPopulator();
    private static final SynchronousPopulator syncSecPopulator = new SynchronousPopulator();
    private static final int MAX_TEXT_LENGTH = 40;

    private static StargateEconomyAPI economyManager;
    private static ServicesManager servicesManager;
    private static String serverName;
    private static boolean knowsServerName = false;

    private static UUID serverUUID;

    private static org.bukkit.ChatColor legacySignColor;

    private static short defaultSignColorHue = 0;
    private static Map<Material, DyeColor> defaultSignDyeColors;

    private FileConfiguration config;

    private StargateRegistry registry;

    private static final FileConfiguration staticConfig = new StargateYamlConfiguration();

    @Override
    public void onEnable() {
        try {
            instance = this;
            if (!new File(this.getDataFolder(), "config.yml").exists()) {
                super.saveDefaultConfig();
            }

            loadGateFormats();
            String LANGUAGE_FOLDER = "lang";
            languageManager = new StargateLanguageManager(this, new File(DATA_FOLDER, LANGUAGE_FOLDER));
            if (ConfigurationHelper.getInteger(ConfigurationOption.CONFIG_VERSION) != CURRENT_CONFIG_VERSION) {
                try {
                    this.migrateConfigurationAndData();
                } catch (IOException | InvalidConfigurationException | SQLException e) {
                    e.printStackTrace();
                }
            }

            load();
            economyManager = new VaultEconomyManager(languageManager);
            SQLDatabaseAPI database = DatabaseHelper.loadDatabase(this);
            storageAPI = new SQLDatabase(database, this);
            registry = new StargateRegistry(storageAPI);
            registry.loadPortals();

            pluginManager = getServer().getPluginManager();
            registerListeners();
            BukkitScheduler scheduler = getServer().getScheduler();
            scheduler.scheduleSyncRepeatingTask(this, synchronousTickPopulator, 0L, 1L);
            scheduler.scheduleSyncRepeatingTask(this, syncSecPopulator, 0L, 20L);
            registerCommands();

            //Register bStats metrics
            int pluginId = 13629;
            BStatsHelper.registerMetrics(pluginId, this);
            servicesManager = this.getServer().getServicesManager();
            servicesManager.register(StargateAPI.class, this, this, ServicePriority.High);
        } catch (StargateInitializationException exception) {
            getServer().getPluginManager().disablePlugin(this);
        } catch (SQLException e) {
            getServer().getPluginManager().disablePlugin(this);
            e.printStackTrace();
        }
    }

    /**
     * Gets the economy manager used for this plugin
     *
     * @return <p>The economy manager used for this plugin</p>
     */
    public static StargateEconomyAPI getEconomyManager() {
        return economyManager;
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
        return GATE_FOLDER;
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
        synchronousTickPopulator.addAction(action);
    }

    /**
     * Adds a 1-second action
     *
     * <p>The task is added to a queue which is processed every second (20 ticks). Should be used in delayed actions</p>
     *
     * @param action <p>The action to add</p>
     */
    public static void addSynchronousSecAction(SimpleAction action) {
        syncSecPopulator.addAction(action, false);
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
        syncSecPopulator.addAction(action, isBungee);
    }

    /**
     * Gets the max text length which will fit on a sign
     *
     * <p>The string length of a name consisting of only 'i'. This will fill a sign (including {@literal <>})
     * Note that this is a terribly relaxed restriction, mainly done to prevent any from arising in an SQL database.</p>
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
        return serverName;
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
        return knowsServerName;
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
        return serverUUID.toString();
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
     * Gets the default hue used for signs
     *
     * @return <p>The default color used for light signs</p>
     */
    public static short getDefaultSignHue() {
        return Stargate.defaultSignColorHue;
    }

    /**
     * Get the dye-color that when applied to a sign gets the text converted into the default configuration
     *
     * @param signMaterial <p>A type of sign</p>
     * @return <p>A color related to that sign</p>
     */
    public static DyeColor getDefaultSignDyeColor(Material signMaterial) {
        try {
            return Stargate.defaultSignDyeColors.get(signMaterial);
        } catch (NullPointerException e) {
            return DyeColor.WHITE;
        }
    }

    /**
     * Gets the default color used for light signs with legacy coloring
     *
     * @return <p>The default legacy color used for light signs</p>
     */
    public static org.bukkit.ChatColor getLegacySignColor() {
        return legacySignColor;
    }

    private void loadColors() {
        try {
            if (!NonLegacyMethod.CHAT_COLOR.isImplemented()) {
                logMessage(Level.INFO,
                        "Default stargate coloring is not supported on your current server implementation");
                Stargate.legacySignColor = org.bukkit.ChatColor
                        .valueOf(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_SIGN_COLOR).toUpperCase());
                return;
            }
            ChatColor color = ColorNameInterpreter.getColor(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_SIGN_COLOR));
            Stargate.defaultSignColorHue = ColorConverter.getHue(color);
            Stargate.defaultSignDyeColors = new EnumMap<>(Material.class);
            for (Material signMaterial : Tag.WALL_SIGNS.getValues()) {
                defaultSignDyeColors.put(signMaterial, ColorConverter.getClosestDyeColor(ColorProperty.getColorFromHue(signMaterial, defaultSignColorHue, false)));
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            Stargate.log(Level.WARNING, "Invalid colors for sign text. Using default colors instead...");
        }
    }

    /**
     * Registers all necessary listeners for this plugin
     */
    private void registerListeners() {
        pluginManager.registerEvents(new BlockEventListener(getRegistry()), this);
        pluginManager.registerEvents(new MoveEventListener(), this);
        pluginManager.registerEvents(new PlayerEventListener(), this);
        pluginManager.registerEvents(new PluginEventListener(), this);
        if (NonLegacyMethod.PLAYER_ADVANCEMENT_CRITERION_EVENT.isImplemented()) {
            pluginManager.registerEvents(new PlayerAdvancementListener(), this);
        }
        if (NonLegacyMethod.ENTITY_INSIDE_BLOCK_EVENT.isImplemented()) {
            pluginManager.registerEvents(new EntityInsideBlockEventListener(), this);
        }
    }

    /**
     * Saves all the default gate designs to the gate folder
     */
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
        SQLDatabaseAPI database = new SQLiteDatabase(databaseFile);

        StorageAPI storageAPI = new SQLDatabase(database, false, false, this);
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
        PortalHelper.closeAllPortals(registry.getBungeeNetworkMap());
        PortalHelper.closeAllPortals(registry.getNetworkMap());
        try {
            load();
            loadGateFormats();
            if (storageAPI instanceof SQLDatabase) {
                SQLDatabaseAPI database = DatabaseHelper.loadDatabase(this);
                ((SQLDatabase) storageAPI).load(database, this);
            }
            registry.load();
            economyManager.setupEconomy();
        } catch (StargateInitializationException exception) {
            getServer().getPluginManager().disablePlugin(this);
        } catch (SQLException e) {
            getServer().getPluginManager().disablePlugin(this);
            e.printStackTrace();
        }
    }

    private void load() {
        loadColors();

        languageManager.setLanguage(ConfigurationHelper.getString(ConfigurationOption.LANGUAGE));
        fetchServerId();
        loadConfigLevel();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            Messenger messenger = Bukkit.getMessenger();

            messenger.registerOutgoingPluginChannel(this, PluginChannel.BUNGEE.getChannel());
            messenger.registerIncomingPluginChannel(this, PluginChannel.BUNGEE.getChannel(),
                    new StargateBungeePluginMessageListener(this, this));
        }
    }

    private void fetchServerId() {

        if (ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE)) {
            String INTERNAL_FOLDER = ".internal";
            BungeeHelper.getServerId(DATA_FOLDER, INTERNAL_FOLDER);
        }
    }

    private void loadConfigLevel() {

        String debugLevelString = ConfigurationHelper.getString(ConfigurationOption.DEBUG_LEVEL);
        if (debugLevelString == null) {
            lowestMessageLevel = Level.INFO;
        } else {
            lowestMessageLevel = Level.parse(debugLevelString);
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
        synchronousTickPopulator.forceDoAllTasks();
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
        servicesManager.unregisterAll(this);
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
        if (priorityLevel.intValue() < this.lowestMessageLevel.intValue()) {
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
            stargateCommand.setExecutor(new CommandStargate());
            stargateCommand.setTabCompleter(new StargateTabCompleter());
        }
    }

    public static RegistryAPI getRegistryStatic() {
        return instance.registry;
    }

    public static StorageAPI getStorageAPIStatic() {
        return instance.storageAPI;
    }

    @SuppressWarnings("unused")
    public static ConfigurationAPI getConfigAPIStatic() {
        return instance;
    }

    public static LanguageManager getLanguageManagerStatic() {
        return instance.languageManager;
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
        return new StargatePermissionManager(entity);
    }

    @Override
    public ConfigurationAPI getConfigurationAPI() {
        return this;
    }

}
