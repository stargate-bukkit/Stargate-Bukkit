package net.knarcraft.stargate;

import net.knarcraft.stargate.command.CommandStarGate;
import net.knarcraft.stargate.command.StarGateTabCompleter;
import net.knarcraft.stargate.container.BlockChangeRequest;
import net.knarcraft.stargate.event.StargateAccessEvent;
import net.knarcraft.stargate.listener.BlockEventListener;
import net.knarcraft.stargate.listener.BungeeCordListener;
import net.knarcraft.stargate.listener.EntityEventListener;
import net.knarcraft.stargate.listener.PlayerEventListener;
import net.knarcraft.stargate.listener.PluginEventListener;
import net.knarcraft.stargate.listener.PortalEventListener;
import net.knarcraft.stargate.listener.VehicleEventListener;
import net.knarcraft.stargate.listener.WorldEventListener;
import net.knarcraft.stargate.portal.GateHandler;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.thread.BlockChangeThread;
import net.knarcraft.stargate.thread.StarGateThread;
import net.knarcraft.stargate.utility.EconomyHandler;
import net.knarcraft.stargate.utility.PermissionHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.Messenger;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Stargate extends JavaPlugin {

    public static final ConcurrentLinkedQueue<Portal> openList = new ConcurrentLinkedQueue<>();
    private static final int activeTime = 10;
    private static final int openTime = 10;
    public static Logger log;
    public static Server server;
    public static Stargate stargate;
    public static LanguageLoader languageLoader;
    public static int maxGates = 0;
    public static boolean rememberDestination = false;
    public static boolean handleVehicles = true;
    public static boolean sortNetworkDestinations = false;
    public static boolean protectEntrance = false;
    public static boolean enableBungee = true;
    public static boolean verifyPortals = true;
    public static ChatColor signColor;
    // Temp workaround for snowmen, don't check gate entrance
    public static boolean ignoreEntrance = false;
    // Used for debug
    public static boolean debuggingEnabled = false;
    public static boolean permissionDebuggingEnabled = false;
    public static final ConcurrentLinkedQueue<Portal> activeList = new ConcurrentLinkedQueue<>();
    // Used for populating gate open/closed material.
    public static final Queue<BlockChangeRequest> blockChangeRequestQueue = new LinkedList<>();
    // HashMap of player names for Bungee support
    public static final Map<String, String> bungeeQueue = new HashMap<>();
    //World names that contain stargates
    public static final HashSet<String> managedWorlds = new HashSet<>();
    private static String pluginVersion;
    private static String portalFolder;
    private static String gateFolder;
    private static String defaultGateNetwork = "central";
    private static boolean destroyExplosion = false;
    private static String languageName = "en";
    private FileConfiguration newConfig;
    private PluginManager pluginManager;

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

    public static String getPluginVersion() {
        return pluginVersion;
    }

    public static boolean destroyedByExplosion() {
        return destroyExplosion;
    }

    public static int getOpenTime() {
        return openTime;
    }

    public static int getActiveTime() {
        return activeTime;
    }

    public static void debug(String rout, String msg) {
        if (Stargate.debuggingEnabled) {
            log.info("[stargate::" + rout + "] " + msg);
        } else {
            log.log(Level.FINEST, "[stargate::" + rout + "] " + msg);
        }
    }

    public static void sendMessage(CommandSender player, String message) {
        sendMessage(player, message, true);
    }

    public static void sendMessage(CommandSender player, String message, boolean error) {
        if (message.isEmpty()) return;
        message = message.replaceAll("(&([a-f0-9]))", "\u00A7$2");
        if (error)
            player.sendMessage(ChatColor.RED + Stargate.getString("prefix") + ChatColor.WHITE + message);
        else
            player.sendMessage(ChatColor.GREEN + Stargate.getString("prefix") + ChatColor.WHITE + message);
    }

    public static void setLine(Sign sign, int index, String text) {
        sign.setLine(index, Stargate.signColor + text);
    }

    public static String getSaveLocation() {
        return portalFolder;
    }

    public static String getGateFolder() {
        return gateFolder;
    }

    public static String getDefaultNetwork() {
        return defaultGateNetwork;
    }

    public static String getString(String name) {
        return languageLoader.getString(name);
    }

    public static void openPortal(Player player, Portal portal) {
        Portal destination = portal.getDestination();

        // Always-open gate -- Do nothing
        if (portal.isAlwaysOn()) {
            return;
        }

        // Random gate -- Do nothing
        if (portal.isRandom())
            return;

        // Invalid destination
        if ((destination == null) || (destination == portal)) {
            Stargate.sendMessage(player, Stargate.getString("invalidMsg"));
            return;
        }

        // Gate is already open
        if (portal.isOpen()) {
            // Close if this player opened the gate
            if (portal.getActivePlayer() == player) {
                portal.close(false);
            }
            return;
        }

        // Gate that someone else is using -- Deny access
        if ((!portal.isFixed()) && portal.isActive() && (portal.getActivePlayer() != player)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            return;
        }

        // Check if the player can use the private gate
        if (portal.isPrivate() && !PermissionHelper.canPrivate(player, portal)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            return;
        }

        // Destination blocked
        if ((destination.isOpen()) && (!destination.isAlwaysOn())) {
            Stargate.sendMessage(player, Stargate.getString("blockMsg"));
            return;
        }

        // Open gate
        portal.open(player, false);
    }

    /**
     * Creates a StargateAccessPortal and gives the result
     *
     * <p>The event is used for other plugins to bypass the permission checks</p>
     *
     * @param player <p>The player trying to use the portal</p>
     * @param portal <p>The portal the player is trying to use</p>
     * @param deny   <p>Whether the player's access has already been denied by a check</p>
     * @return <p>False if the player should be allowed through the portal</p>
     */
    public static boolean cannotAccessPortal(Player player, Portal portal, boolean deny) {
        StargateAccessEvent event = new StargateAccessEvent(player, portal, deny);
        Stargate.server.getPluginManager().callEvent(event);
        return event.getDeny();
    }

    /**
     * Checks whether a given user cannot travel between two portals
     *
     * @param player         <p>The player to check</p>
     * @param entrancePortal <p>The portal the user wants to enter</p>
     * @param destination    <p>The portal the user wants to exit</p>
     * @return <p>False if the user is allowed to access the portal</p>
     */
    public static boolean cannotAccessPortal(Player player, Portal entrancePortal, Portal destination) {
        boolean deny = false;
        // Check if player has access to this server for Bungee gates
        if (entrancePortal.isBungee() && !PermissionHelper.canAccessServer(player, entrancePortal.getNetwork())) {
            Stargate.debug("cannotAccessPortal", "Cannot access server");
            deny = true;
        } else if (PermissionHelper.cannotAccessNetwork(player, entrancePortal.getNetwork())) {
            Stargate.debug("cannotAccessPortal", "Cannot access network");
            deny = true;
        } else if (!entrancePortal.isBungee() && PermissionHelper.cannotAccessWorld(player, destination.getWorld().getName())) {
            Stargate.debug("cannotAccessPortal", "Cannot access world");
            deny = true;
        }
        return Stargate.cannotAccessPortal(player, entrancePortal, deny);
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
        PortalHandler.closeAllGates();
        PortalHandler.clearGates();
        managedWorlds.clear();
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public void onEnable() {
        PluginDescriptionFile pluginDescriptionFile = this.getDescription();
        pluginManager = getServer().getPluginManager();
        newConfig = this.getConfig();
        log = Logger.getLogger("Minecraft");
        Stargate.server = getServer();
        Stargate.stargate = this;

        // Set portalFile and gateFolder to the plugin folder as defaults.
        String dataFolderPath = getDataFolder().getPath().replaceAll("\\\\", "/");
        portalFolder = dataFolderPath + "/portals/";
        gateFolder = dataFolderPath + "/gates/";
        String languageFolder = dataFolderPath + "/lang/";

        pluginVersion = pluginDescriptionFile.getVersion();

        log.info(pluginDescriptionFile.getName() + " v." + pluginDescriptionFile.getVersion() + " is enabled.");

        //Register events before loading gates to stop weird things happening.
        registerEventListeners();

        this.loadConfig();

        //Enable the required channels for Bungee support
        if (enableBungee) {
            startStopBungeeListener(true);
        }

        // It is important to load languages here, as they are used during reloadGates()
        languageLoader = new LanguageLoader(languageFolder, Stargate.languageName);

        this.createMissingFolders();
        this.loadGates();
        this.loadAllPortals();

        // Check to see if Economy is loaded yet.
        setupVaultEconomy();

        //Run necessary threads
        getServer().getScheduler().runTaskTimer(this, new StarGateThread(), 0L, 100L);
        getServer().getScheduler().runTaskTimer(this, new BlockChangeThread(), 0L, 1L);

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

        //Gates
        loadGateConfig();

        //Economy
        loadEconomyConfig();

        this.saveConfig();
    }

    /**
     * Loads all config values related to gates
     */
    private void loadGateConfig() {
        String defaultNetwork = newConfig.getString("gates.defaultGateNetwork");
        defaultGateNetwork = defaultNetwork != null ? defaultNetwork.trim() : null;
        maxGates = newConfig.getInt("gates.maxGatesEachNetwork");

        //Functionality
        handleVehicles = newConfig.getBoolean("gates.functionality.handleVehicles");
        enableBungee = newConfig.getBoolean("gates.functionality.enableBungee");

        //Integrity
        protectEntrance = newConfig.getBoolean("gates.integrity.protectEntrance");
        verifyPortals = newConfig.getBoolean("gates.integrity.verifyPortals");
        ignoreEntrance = newConfig.getBoolean("gates.integrity.ignoreEntrance");
        destroyExplosion = newConfig.getBoolean("gates.integrity.destroyedByExplosion");

        //Cosmetic
        sortNetworkDestinations = newConfig.getBoolean("gates.cosmetic.sortNetworkDestinations");
        rememberDestination = newConfig.getBoolean("gates.cosmetic.rememberDestination");
        loadSignColor(newConfig.getString("gates.cosmetic.signColor"));
    }

    /**
     * Loads all config values related to economy
     */
    private void loadEconomyConfig() {
        EconomyHandler.economyEnabled = newConfig.getBoolean("economy.useEconomy");
        EconomyHandler.setCreateCost(newConfig.getInt("economy.createCost"));
        EconomyHandler.setDestroyCost(newConfig.getInt("economy.destroyCost"));
        EconomyHandler.setUseCost(newConfig.getInt("economy.useCost"));
        EconomyHandler.toOwner = newConfig.getBoolean("economy.toOwner");
        EconomyHandler.chargeFreeDestination = newConfig.getBoolean("economy.chargeFreeDestination");
        EconomyHandler.freeGatesGreen = newConfig.getBoolean("economy.freeGatesGreen");
    }

    /**
     * Loads the correct sign color given a sign color string
     *
     * @param signColor <p>A string representing a sign color</p>
     */
    private void loadSignColor(String signColor) {
        if (signColor != null) {
            try {
                Stargate.signColor = ChatColor.valueOf(signColor.toUpperCase());
                return;
            } catch (IllegalArgumentException | NullPointerException ignored) {
            }
        }
        log.warning(getString("prefix") + "You have specified an invalid color in your config.yml. Defaulting to BLACK");
        Stargate.signColor = ChatColor.BLACK;
    }

    /**
     * Forces all open portals to close
     */
    public void closeAllPortals() {
        // Close all gates prior to reloading
        for (Portal openPortal : openList) {
            openPortal.close(true);
        }
    }

    /**
     * Loads all available gates
     */
    public void loadGates() {
        GateHandler.loadGates(gateFolder);
        log.info(Stargate.getString("prefix") + "Loaded " + GateHandler.getGateCount() + " gate layouts");
    }

    /**
     * Loads all portals in all un-managed worlds
     */
    public void loadAllPortals() {
        for (World world : getServer().getWorlds()) {
            if (!managedWorlds.contains(world.getName())) {
                PortalHandler.loadAllGates(world);
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
                log.severe("Unable to create portal directory");
            }
        }
        File newFile = new File(portalFolder, getServer().getWorlds().get(0).getName() + ".db");
        if (!newFile.exists() && !newFile.getParentFile().exists()) {
            if (!newFile.getParentFile().mkdirs()) {
                log.severe("Unable to create portal database folder: " + newFile.getParentFile().getPath());
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
        for (Portal activePortal : activeList) {
            activePortal.deactivate();
        }
        // Close portals
        closeAllPortals();
        // Clear all lists
        activeList.clear();
        openList.clear();
        managedWorlds.clear();
        PortalHandler.clearGates();
        GateHandler.clearGates();

        // Store the old Bungee enabled value
        boolean oldEnableBungee = enableBungee;
        // Reload data
        loadConfig();
        loadGates();
        loadAllPortals();
        languageLoader.setChosenLanguage(languageName);
        languageLoader.reload();

        //Load Economy support if enabled/clear if disabled
        reloadEconomy();

        //Enable or disable the required channels for Bungee support
        if (oldEnableBungee != enableBungee) {
            startStopBungeeListener(enableBungee);
        }

        sendMessage(sender, "stargate reloaded");
    }

    /**
     * Reloads economy by enabling or disabling it as necessary
     */
    private void reloadEconomy() {
        if (EconomyHandler.economyEnabled && EconomyHandler.economy == null) {
            setupVaultEconomy();
        } else if (!EconomyHandler.economyEnabled) {
            EconomyHandler.vault = null;
            EconomyHandler.economy = null;
        }
    }

    /**
     * Loads economy from Vault
     */
    private void setupVaultEconomy() {
        if (EconomyHandler.setupEconomy(pluginManager) && EconomyHandler.economy != null) {
            String vaultVersion = EconomyHandler.vault.getDescription().getVersion();
            log.info(Stargate.getString("prefix") + Stargate.replaceVars(
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

}
