package net.knarcraft.stargate;

import net.knarcraft.stargate.command.CommandStarGate;
import net.knarcraft.stargate.command.StarGateTabCompleter;
import net.knarcraft.stargate.config.EconomyConfig;
import net.knarcraft.stargate.config.LanguageLoader;
import net.knarcraft.stargate.config.StargateGateConfig;
import net.knarcraft.stargate.container.BlockChangeRequest;
import net.knarcraft.stargate.container.ChunkUnloadRequest;
import net.knarcraft.stargate.listener.BlockEventListener;
import net.knarcraft.stargate.listener.BungeeCordListener;
import net.knarcraft.stargate.listener.EntityEventListener;
import net.knarcraft.stargate.listener.PlayerEventListener;
import net.knarcraft.stargate.listener.PluginEventListener;
import net.knarcraft.stargate.listener.PortalEventListener;
import net.knarcraft.stargate.listener.TeleportEventListener;
import net.knarcraft.stargate.listener.VehicleEventListener;
import net.knarcraft.stargate.listener.WorldEventListener;
import net.knarcraft.stargate.portal.GateHandler;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.PortalRegistry;
import net.knarcraft.stargate.thread.BlockChangeThread;
import net.knarcraft.stargate.thread.ChunkUnloadThread;
import net.knarcraft.stargate.thread.StarGateThread;
import net.knarcraft.stargate.utility.FileHelper;
import net.knarcraft.stargate.utility.PortalFileHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Stargate extends JavaPlugin {

    //Used for changing gate open/closed material.
    public static final Queue<BlockChangeRequest> blockChangeRequestQueue = new LinkedList<>();
    public static final ConcurrentLinkedQueue<Portal> openPortalsQueue = new ConcurrentLinkedQueue<>();
    public static final ConcurrentLinkedQueue<Portal> activePortalsQueue = new ConcurrentLinkedQueue<>();
    private static final Queue<ChunkUnloadRequest> chunkUnloadQueue = new PriorityQueue<>();

    //Amount of seconds before deactivating/closing portals
    private static final int activeTime = 10;
    private static final int openTime = 10;

    public static Logger logger;
    public static Server server;
    public static Stargate stargate;
    public static LanguageLoader languageLoader;

    private String dataFolderPath;
    private static StargateGateConfig stargateGateConfig;
    private static EconomyConfig economyConfig;

    //Used for debug
    public static boolean debuggingEnabled = false;
    public static boolean permissionDebuggingEnabled = false;

    //World names that contain stargates
    public static final HashSet<String> managedWorlds = new HashSet<>();

    private static String pluginVersion;
    private static String portalFolder;
    private static String gateFolder;

    private static String languageName = "en";

    private FileConfiguration newConfig;
    private PluginManager pluginManager;

    /**
     * Empty constructor necessary for Spigot
     */
    public Stargate() {
        super();
    }

    /**
     * Special constructor used for MockBukkit
     *
     * @param loader          <p>The plugin loader to be used.</p>
     * @param descriptionFile <p>The description file to be used.</p>
     * @param dataFolder      <p>The data folder to be used.</p>
     * @param file            <p>The file to be used</p>
     */
    protected Stargate(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
        super(loader, descriptionFile, dataFolder, file);
    }

    /**
     * Gets the object containing gate configuration values
     *
     * @return <p>The object containing gate configuration values</p>
     */
    public static StargateGateConfig getGateConfig() {
        return stargateGateConfig;
    }

    /**
     * Gets the object containing economy config values
     *
     * @return <p>The object containing economy config values</p>
     */
    public static EconomyConfig getEconomyConfig() {
        return economyConfig;
    }

    /**
     * Gets the version of this plugin
     *
     * @return <p>This plugin's version</p>
     */
    public static String getPluginVersion() {
        return pluginVersion;
    }

    /**
     * Gets whether portals should be destroyed by explosions
     *
     * @return <p>True if portals should be destroyed</p>
     */
    public static boolean destroyedByExplosion() {
        return stargateGateConfig.destroyedByExplosion();
    }

    /**
     * Gets the amount of seconds a portal should be open before automatically closing
     *
     * @return <p>The open time of a gate</p>
     */
    public static int getOpenTime() {
        return openTime;
    }

    /**
     * Gets the amount of seconds a portal should be active before automatically deactivating
     *
     * @return <p>The active time of a gate</p>
     */
    public static int getActiveTime() {
        return activeTime;
    }

    /**
     * Sends a debug message
     *
     * @param route   <p>The class name/route where something happened</p>
     * @param message <p>A message describing what happened</p>
     */
    public static void debug(String route, String message) {
        if (Stargate.debuggingEnabled) {
            logger.info("[stargate::" + route + "] " + message);
        } else {
            logger.log(Level.FINEST, "[stargate::" + route + "] " + message);
        }
    }

    /**
     * Sends an error message to a player
     *
     * @param player  <p>The player to send the message to</p>
     * @param message <p>The message to send</p>
     */
    public static void sendErrorMessage(CommandSender player, String message) {
        sendMessage(player, message, true);
    }

    /**
     * Sends a success message to a player
     *
     * @param player  <p>The player to send the message to</p>
     * @param message <p>The message to send</p>
     */
    public static void sendSuccessMessage(CommandSender player, String message) {
        sendMessage(player, message, false);
    }

    /**
     * Sends a message to a player
     *
     * @param player  <p>The player to send the message to</p>
     * @param message <p>The message to send</p>
     * @param error   <p>Whether the message sent is an error</p>
     */
    private static void sendMessage(CommandSender player, String message, boolean error) {
        if (message.isEmpty()) {
            return;
        }
        //Replace color codes with green? What's the deal with the dollar sign?
        message = message.replaceAll("(&([a-f0-9]))", "\u00A7$2");
        if (error) {
            player.sendMessage(ChatColor.RED + Stargate.getString("prefix") + ChatColor.WHITE + message);
        } else {
            player.sendMessage(ChatColor.GREEN + Stargate.getString("prefix") + ChatColor.WHITE + message);
        }
    }

    /**
     * Sets a line on a sign, adding the chosen sign color
     *
     * @param sign  <p>The sign to update</p>
     * @param index <p>The index of the sign line to change</p>
     * @param text  <p>The new text on the sign</p>
     */
    public static void setLine(Sign sign, int index, String text) {
        sign.setLine(index, stargateGateConfig.getSignColor() + text);
    }

    /**
     * Gets the folder for saving created portals
     *
     * <p>The returned String path is the full path to the folder</p>
     *
     * @return <p>The folder for storing the portal database</p>
     */
    public static String getSaveLocation() {
        return portalFolder;
    }

    /**
     * Gets the folder storing gate files
     *
     * <p>The returned String path is the full path to the folder</p>
     *
     * @return <p>The folder storing gate files</p>
     */
    public static String getGateFolder() {
        return gateFolder;
    }

    /**
     * Gets the default network for gates where a network is not specified
     *
     * @return <p>The default network</p>
     */
    public static String getDefaultNetwork() {
        return stargateGateConfig.getDefaultPortalNetwork();
    }

    /**
     * Gets a translated string given its string key
     *
     * <p>The name/key is the string before the equals sign in the language files</p>
     *
     * @param name <p>The name/key of the string to get</p>
     * @return <p>The full translated string</p>
     */
    public static String getString(String name) {
        return languageLoader.getString(name);
    }

    /**
     * Replaces a list of variables in a string in the order they are given
     *
     * @param input  <p>The input containing the variables</p>
     * @param search <p>The variables to replace</p>
     * @param values <p>The replacement values</p>
     * @return <p>The input string with the search values replaced with the given values</p>
     */
    public static String replaceVars(String input, String[] search, String[] values) {
        if (search.length != values.length) {
            throw new IllegalArgumentException("The number of search values and replace values do not match.");
        }
        for (int i = 0; i < search.length; i++) {
            input = replaceVars(input, search[i], values[i]);
        }
        return input;
    }

    /**
     * Replaces a variable in a string
     *
     * @param input  <p>The input containing the variables</p>
     * @param search <p>The variable to replace</p>
     * @param value  <p>The replacement value</p>
     * @return <p>The input string with the search replaced with value</p>
     */
    public static String replaceVars(String input, String search, String value) {
        return input.replace(search, value);
    }

    @Override
    public void onDisable() {
        PortalHandler.closeAllPortals();
        PortalRegistry.clearPortals();
        managedWorlds.clear();
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public void onEnable() {
        PluginDescriptionFile pluginDescriptionFile = this.getDescription();
        pluginManager = getServer().getPluginManager();
        newConfig = this.getConfig();
        logger = Logger.getLogger("Minecraft");
        Stargate.server = getServer();
        Stargate.stargate = this;

        // Set portalFile and gateFolder to the plugin folder as defaults.
        dataFolderPath = getDataFolder().getPath().replaceAll("\\\\", "/");
        portalFolder = dataFolderPath + "/portals/";
        gateFolder = dataFolderPath + "/gates/";
        String languageFolder = dataFolderPath + "/lang/";

        pluginVersion = pluginDescriptionFile.getVersion();

        logger.info(pluginDescriptionFile.getName() + " v." + pluginDescriptionFile.getVersion() + " is enabled.");

        //Register events before loading gates to stop weird things happening.
        registerEventListeners();

        this.loadConfig();

        //Enable the required channels for Bungee support
        if (stargateGateConfig.enableBungee()) {
            startStopBungeeListener(true);
        }

        // It is important to load languages here, as they are used during reloadGates()
        languageLoader = new LanguageLoader(languageFolder, Stargate.languageName);
        if (debuggingEnabled) {
            languageLoader.debug();
        }

        this.createMissingFolders();
        this.loadGates();
        this.loadAllPortals();

        //Check to see if Economy is loaded yet.
        setupVaultEconomy();

        //Run necessary threads
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.runTaskTimer(this, new StarGateThread(), 0L, 100L);
        scheduler.runTaskTimer(this, new BlockChangeThread(), 0L, 1L);
        scheduler.runTaskTimer(this, new ChunkUnloadThread(), 0L, 100L);

        this.registerCommands();
    }

    /**
     * Registers all event listeners
     */
    private void registerEventListeners() {
        pluginManager.registerEvents(new PlayerEventListener(), this);
        pluginManager.registerEvents(new BlockEventListener(), this);

        pluginManager.registerEvents(new VehicleEventListener(), this);
        pluginManager.registerEvents(new EntityEventListener(), this);
        pluginManager.registerEvents(new PortalEventListener(), this);
        pluginManager.registerEvents(new WorldEventListener(), this);
        pluginManager.registerEvents(new PluginEventListener(this), this);
        pluginManager.registerEvents(new TeleportEventListener(), this);
    }

    /**
     * Registers a command for this plugin
     */
    private void registerCommands() {
        PluginCommand stargateCommand = this.getCommand("stargate");
        if (stargateCommand != null) {
            stargateCommand.setExecutor(new CommandStarGate(this));
            stargateCommand.setTabCompleter(new StarGateTabCompleter());
        }
    }

    /**
     * Loads all config values
     */
    public void loadConfig() {
        this.reloadConfig();
        newConfig = this.getConfig();

        boolean isMigrating = false;
        if (newConfig.getString("lang") != null ||
                newConfig.getString("gates.integrity.ignoreEntrance") != null ||
                newConfig.getString("ignoreEntrance") != null) {
            migrateConfig(newConfig);
            isMigrating = true;
        }

        // Copy default values if required
        newConfig.options().copyDefaults(true);

        //Language
        languageName = newConfig.getString("language");

        //Folders
        portalFolder = newConfig.getString("folders.portalFolder");
        gateFolder = newConfig.getString("folders.gateFolder");

        //Debug
        debuggingEnabled = newConfig.getBoolean("debugging.debug");
        permissionDebuggingEnabled = newConfig.getBoolean("debugging.permissionDebug");

        //If users have an outdated config, assume they also need to update their default gates
        if (isMigrating) {
            GateHandler.writeDefaultGatesToFolder(gateFolder);
        }

        //Gates
        stargateGateConfig = new StargateGateConfig(newConfig);

        //Economy
        economyConfig = new EconomyConfig(newConfig);

        this.saveConfig();
    }

    /**
     * Changes all configuration values from the old name to the new name
     *
     * @param newConfig <p>The config to read from and write to</p>
     */
    private void migrateConfig(FileConfiguration newConfig) {
        try {
            newConfig.save(dataFolderPath + "/config.old");
        } catch (IOException e) {
            Stargate.debug("Stargate::migrateConfig", "Unable to save old backup and do migration");
            e.printStackTrace();
            return;
        }

        Map<String, String> migrationFields;
        try {
            migrationFields = FileHelper.readKeyValuePairs(FileHelper.getBufferedReaderFromInputStream(
                    FileHelper.getInputStreamForInternalFile("/config-migrations.txt")));
        } catch (IOException e) {
            Stargate.debug("Stargate::migrateConfig", "Unable to load config migration file");
            e.printStackTrace();
            return;
        }

        //Replace old config names with the new ones
        for (String key : migrationFields.keySet()) {
            if (newConfig.contains(key)) {
                String newPath = migrationFields.get(key);
                Object oldValue = newConfig.get(key);
                if (!newPath.trim().isEmpty()) {
                    newConfig.set(newPath, oldValue);
                }
                newConfig.set(key, null);
            }
        }
    }

    /**
     * Forces all open portals to close
     */
    public void closeAllPortals() {
        // Close all gates prior to reloading
        for (Portal openPortal : openPortalsQueue) {
            openPortal.getPortalOpener().closePortal(true);
        }
    }

    /**
     * Loads all available gates
     */
    public void loadGates() {
        GateHandler.loadGates(gateFolder);
        logger.info(Stargate.getString("prefix") + "Loaded " + GateHandler.getGateCount() + " gate layouts");
    }

    /**
     * Loads all portals in all un-managed worlds
     */
    public void loadAllPortals() {
        for (World world : getServer().getWorlds()) {
            if (!managedWorlds.contains(world.getName())) {
                PortalFileHelper.loadAllPortals(world);
                managedWorlds.add(world.getName());
            }
        }
    }

    /**
     * Creates missing folders
     */
    private void createMissingFolders() {
        File newPortalDir = new File(portalFolder);
        if (!newPortalDir.exists()) {
            if (!newPortalDir.mkdirs()) {
                logger.severe("Unable to create portal directory");
            }
        }
        File newFile = new File(portalFolder, getServer().getWorlds().get(0).getName() + ".db");
        if (!newFile.exists() && !newFile.getParentFile().exists()) {
            if (!newFile.getParentFile().mkdirs()) {
                logger.severe("Unable to create portal database folder: " + newFile.getParentFile().getPath());
            }
        }
    }

    /**
     * Reloads all portals and files
     *
     * @param sender <p>The sender of the reload request</p>
     */
    public void reload(CommandSender sender) {
        // Deactivate portals
        for (Portal activePortal : activePortalsQueue) {
            activePortal.getPortalActivator().deactivate();
        }
        // Close portals
        closeAllPortals();
        // Clear all lists
        activePortalsQueue.clear();
        openPortalsQueue.clear();
        managedWorlds.clear();
        PortalRegistry.clearPortals();
        GateHandler.clearGates();

        // Store the old Bungee enabled value
        boolean oldEnableBungee = stargateGateConfig.enableBungee();
        // Reload data
        loadConfig();
        loadGates();
        loadAllPortals();
        languageLoader.setChosenLanguage(languageName);
        languageLoader.reload();

        //Load Economy support if enabled/clear if disabled
        reloadEconomy();

        //Enable or disable the required channels for Bungee support
        if (oldEnableBungee != stargateGateConfig.enableBungee()) {
            startStopBungeeListener(stargateGateConfig.enableBungee());
        }

        sendErrorMessage(sender, "stargate reloaded");
    }

    /**
     * Reloads economy by enabling or disabling it as necessary
     */
    private void reloadEconomy() {
        EconomyConfig economyConfig = Stargate.getEconomyConfig();
        if (economyConfig.isEconomyEnabled() && economyConfig.getEconomy() == null) {
            setupVaultEconomy();
        } else if (!economyConfig.isEconomyEnabled()) {
            economyConfig.disableEconomy();
        }
    }

    /**
     * Loads economy from Vault
     */
    private void setupVaultEconomy() {
        EconomyConfig economyConfig = Stargate.getEconomyConfig();
        if (economyConfig.setupEconomy(pluginManager) && economyConfig.getEconomy() != null) {
            String vaultVersion = economyConfig.getVault().getDescription().getVersion();
            logger.info(Stargate.getString("prefix") + Stargate.replaceVars(
                    Stargate.getString("vaultLoaded"), "%version%", vaultVersion));
        }
    }

    /**
     * Starts the listener for listening to BungeeCord messages
     */
    private void startStopBungeeListener(boolean start) {
        Messenger messenger = Bukkit.getMessenger();
        String bungeeChannel = "BungeeCord";

        if (start) {
            messenger.registerOutgoingPluginChannel(this, bungeeChannel);
            messenger.registerIncomingPluginChannel(this, bungeeChannel, new BungeeCordListener());
        } else {
            messenger.unregisterIncomingPluginChannel(this, bungeeChannel);
            messenger.unregisterOutgoingPluginChannel(this, bungeeChannel);
        }
    }

    /**
     * Gets the chunk unload queue containing chunks to unload
     *
     * @return <p>The chunk unload queue</p>
     */
    public static Queue<ChunkUnloadRequest> getChunkUnloadQueue() {
        return chunkUnloadQueue;
    }

    /**
     * Adds a new chunk unload request to the chunk unload queue
     *
     * @param request <p>The new chunk unload request to add</p>
     */
    public static void addChunkUnloadRequest(ChunkUnloadRequest request) {
        chunkUnloadQueue.removeIf((item) -> item.getChunkToUnload().equals(request.getChunkToUnload()));
        chunkUnloadQueue.add(request);
    }

}
