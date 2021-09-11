package net.knarcraft.stargate;

import net.knarcraft.stargate.command.CommandStarGate;
import net.knarcraft.stargate.command.StarGateTabCompleter;
import net.knarcraft.stargate.event.StargateAccessEvent;
import net.knarcraft.stargate.listener.BlockEventListener;
import net.knarcraft.stargate.listener.BungeeCordListener;
import net.knarcraft.stargate.listener.EntityEventListener;
import net.knarcraft.stargate.listener.PlayerEventListener;
import net.knarcraft.stargate.listener.PluginEventListener;
import net.knarcraft.stargate.listener.PortalEventListener;
import net.knarcraft.stargate.listener.VehicleEventListener;
import net.knarcraft.stargate.listener.WorldEventListener;
import net.knarcraft.stargate.portal.Gate;
import net.knarcraft.stargate.portal.GateHandler;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.PortalOption;
import net.knarcraft.stargate.thread.BlockChangeThread;
import net.knarcraft.stargate.thread.StarGateThread;
import net.knarcraft.stargate.utility.EconomyHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
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
    public static boolean destMemory = false;
    public static boolean handleVehicles = true;
    public static boolean sortLists = false;
    public static boolean protectEntrance = false;
    public static boolean enableBungee = true;
    public static boolean verifyPortals = true;
    public static ChatColor signColor;
    // Temp workaround for snowmen, don't check gate entrance
    public static boolean ignoreEntrance = false;
    // Used for debug
    public static boolean debug = false;
    public static boolean permDebug = false;
    public static final ConcurrentLinkedQueue<Portal> activeList = new ConcurrentLinkedQueue<>();
    // Used for populating gate open/closed material.
    public static final Queue<BlockChangeRequest> blockChangeRequestQueue = new LinkedList<>();
    // HashMap of player names for Bungee support
    public static final Map<String, String> bungeeQueue = new HashMap<>();
    // World names that contain stargates
    public static final HashSet<String> managedWorlds = new HashSet<>();
    private static String pluginVersion;
    private static String portalFolder;
    private static String gateFolder;
    private static String langFolder;
    private static String defaultGateNetwork = "central";
    private static boolean destroyExplosion = false;
    private static String langName = "en";
    private FileConfiguration newConfig;
    private PluginManager pm;

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
        if (Stargate.debug) {
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
        if (portal.isPrivate() && !Stargate.canPrivate(player, portal)) {
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

    /*
     * Check whether the player has the given permissions.
     */
    public static boolean hasPerm(Player player, String perm) {
        if (permDebug)
            Stargate.debug("hasPerm::SuperPerm(" + player.getName() + ")", perm + " => " + player.hasPermission(perm));
        return player.hasPermission(perm);
    }

    /**
     * Check a deep permission, this will check to see if the permissions is defined for this use
     *
     * <p>If using Permissions it will return the same as hasPerm. If using SuperPerms will return true if the node
     * isn't defined, or the value of the node if it is</p>
     *
     * @param player <p>The player to check</p>
     * @param permission <p>The permission to check</p>
     * @return <p>True if the player has the permission or it is not set</p>
     */
    public static boolean hasPermDeep(Player player, String permission) {
        if (!player.isPermissionSet(permission)) {
            if (permDebug) {
                Stargate.debug("hasPermDeep::SuperPerm", permission + " => true");
            }
            return true;
        }
        if (permDebug) {
            Stargate.debug("hasPermDeep::SuperPerms", permission + " => " + player.hasPermission(permission));
        }
        return player.hasPermission(permission);
    }

    /*
     * Check whether player can teleport to dest world
     */
    public static boolean canAccessWorld(Player player, String world) {
        // Can use all stargate player features or access all worlds
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.world")) {
            // Do a deep check to see if the player lacks this specific world node
            return hasPermDeep(player, "stargate.world." + world);
        }
        // Can access dest world
        return hasPerm(player, "stargate.world." + world);
    }

    /*
     * Check whether player can use network
     */
    public static boolean canAccessNetwork(Player player, String network) {
        // Can user all stargate player features, or access all networks
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.network")) {
            // Do a deep check to see if the player lacks this specific network node
            return hasPermDeep(player, "stargate.network." + network);
        }
        // Can access this network
        if (hasPerm(player, "stargate.network." + network)) return true;
        // Is able to create personal gates (Assumption is made they can also access them)
        String playerName = player.getName();
        if (playerName.length() > 11) playerName = playerName.substring(0, 11);
        return network.equals(playerName) && hasPerm(player, "stargate.create.personal");
    }

    /*
     * Check whether the player can access this server
     */
    public static boolean canAccessServer(Player player, String server) {
        // Can user all stargate player features, or access all servers
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.servers")) {
            // Do a deep check to see if the player lacks this specific server node
            return hasPermDeep(player, "stargate.server." + server);
        }
        // Can access this server
        return hasPerm(player, "stargate.server." + server);
    }

    /*
     * Call the StargateAccessPortal event, used for other plugins to bypass Permissions checks
     */

    /**
     * Creates a stargate access event and gives the result
     *
     * <p>The event is used for other plugins to bypass the permission checks</p>
     *
     * @param player <p>The player trying to use the portal</p>
     * @param portal <p>The portal the player is trying to use</p>
     * @param deny <p>Whether the player's access has already been denied by a check</p>
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
        if (entrancePortal.isBungee() && !Stargate.canAccessServer(player, entrancePortal.getNetwork())) {
            Stargate.debug("cannotAccessPortal", "Cannot access server");
            deny = true;
        } else if (!Stargate.canAccessNetwork(player, entrancePortal.getNetwork())) {
            Stargate.debug("cannotAccessPortal", "Cannot access network");
            deny = true;
        } else if (!entrancePortal.isBungee() && !Stargate.canAccessWorld(player, destination.getWorld().getName())) {
            Stargate.debug("cannotAccessPortal", "Cannot access world");
            deny = true;
        }
        return Stargate.cannotAccessPortal(player, entrancePortal, deny);
    }

    /*
     * Return true if the portal is free for the player
     */
    public static boolean isFree(Player player, Portal src, Portal dest) {
        // This gate is free
        if (src.isFree()) return true;
        // Player gets free use
        if (hasPerm(player, "stargate.free") || Stargate.hasPerm(player, "stargate.free.use")) return true;
        // Don't charge for free destination gates
        return dest != null && !EconomyHandler.chargeFreeDestination && dest.isFree();
    }

    /*
     * Check whether the player can see this gate (Hidden property check)
     */
    public static boolean canSee(Player player, Portal portal) {
        // The gate is not hidden
        if (!portal.isHidden()) return true;
        // The player is an admin with the ability to see hidden gates
        if (hasPerm(player, "stargate.admin") || hasPerm(player, "stargate.admin.hidden")) return true;
        // The player is the owner of the gate
        return portal.isOwner(player);
    }

    /*
     * Check if the player can use this private gate
     */
    public static boolean canPrivate(Player player, Portal portal) {
        // Check if the player is the owner of the gate
        if (portal.isOwner(player)) return true;
        // The player is an admin with the ability to use private gates
        return hasPerm(player, "stargate.admin") || hasPerm(player, "stargate.admin.private");
    }

    /*
     * Check if the player has access to {option}
     */
    public static boolean canOption(Player player, PortalOption option) {
        // Check if the player can use all options
        if (hasPerm(player, "stargate.option") || option == PortalOption.BUNGEE) {
            return true;
        }
        // Check if they can use this specific option
        return hasPerm(player, option.getPermissionString());
    }

    /*
     * Check if the player can create gates on {network}
     */
    public static boolean canCreate(Player player, String network) {
        // Check for general create
        if (hasPerm(player, "stargate.create")) return true;
        // Check for all network create permission
        if (hasPerm(player, "stargate.create.network")) {
            // Do a deep check to see if the player lacks this specific network node
            return hasPermDeep(player, "stargate.create.network." + network);
        }
        // Check for this specific network
        return hasPerm(player, "stargate.create.network." + network);

    }

    /*
     * Check if the player can create a personal gate
     */
    public static boolean canCreatePersonal(Player player) {
        // Check for general create
        if (hasPerm(player, "stargate.create")) return true;
        // Check for personal
        return hasPerm(player, "stargate.create.personal");
    }

    /*
     * Check if the player can create this gate layout
     */
    public static boolean canCreateGate(Player player, String gate) {
        // Check for general create
        if (hasPerm(player, "stargate.create")) return true;
        // Check for all gate create permissions
        if (hasPerm(player, "stargate.create.gate")) {
            // Do a deep check to see if the player lacks this specific gate node
            return hasPermDeep(player, "stargate.create.gate." + gate);
        }
        // Check for this specific gate
        return hasPerm(player, "stargate.create.gate." + gate);
    }

    /*
     * Check if the player can destroy this gate
     */
    public static boolean canDestroy(Player player, Portal portal) {
        String network = portal.getNetwork();
        // Check for general destroy
        if (hasPerm(player, "stargate.destroy")) return true;
        // Check for all network destroy permission
        if (hasPerm(player, "stargate.destroy.network")) {
            // Do a deep check to see if the player lacks permission for this network node
            return hasPermDeep(player, "stargate.destroy.network." + network);
        }
        // Check for this specific network
        if (hasPerm(player, "stargate.destroy.network." + network)) return true;
        // Check for personal gate
        return portal.isOwner(player) && hasPerm(player, "stargate.destroy.personal");
    }

    /*
     * Charge player for {action} if required, true on success, false if can't afford
     */
    public static boolean chargePlayer(Player player, UUID target, int cost) {
        // If cost is 0
        if (cost == 0) return true;
        // Economy is disabled
        if (!EconomyHandler.useEconomy()) return true;
        // Charge player
        return EconomyHandler.chargePlayer(player, target, cost);
    }

    /*
     * Charge player for {action} if required, true on success, false if can't afford
     */
    public static boolean chargePlayer(Player player, int cost) {
        // If cost is 0
        if (cost == 0) return true;
        // Economy is disabled
        if (!EconomyHandler.useEconomy()) return true;
        // Charge player
        return EconomyHandler.chargePlayer(player, cost);
    }

    /*
     * Determine the cost of a gate
     */
    public static int getUseCost(Player player, Portal src, Portal dest) {
        // Not using Economy
        if (!EconomyHandler.useEconomy()) return 0;
        // Portal is free
        if (src.isFree()) return 0;
        // Not charging for free destinations
        if (dest != null && !EconomyHandler.chargeFreeDestination && dest.isFree()) return 0;
        // Cost is 0 if the player owns this gate and funds go to the owner
        if (src.getGate().getToOwner() && src.isOwner(player)) return 0;
        // Player gets free gate use
        if (hasPerm(player, "stargate.free") || hasPerm(player, "stargate.free.use")) return 0;

        return src.getGate().getUseCost();
    }

    /*
     * Determine the cost to create the gate
     */
    public static int getCreateCost(Player player, Gate gate) {
        // Not using Economy
        if (!EconomyHandler.useEconomy()) return 0;
        // Player gets free gate destruction
        if (hasPerm(player, "stargate.free") || hasPerm(player, "stargate.free.create")) return 0;

        return gate.getCreateCost();
    }

    /*
     * Determine the cost to destroy the gate
     */
    public static int getDestroyCost(Player player, Gate gate) {
        // Not using Economy
        if (!EconomyHandler.useEconomy()) return 0;
        // Player gets free gate destruction
        if (hasPerm(player, "stargate.free") || hasPerm(player, "stargate.free.destroy")) return 0;

        return gate.getDestroyCost();
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
        pm = getServer().getPluginManager();
        newConfig = this.getConfig();
        log = Logger.getLogger("Minecraft");
        Stargate.server = getServer();
        Stargate.stargate = this;

        // Set portalFile and gateFolder to the plugin folder as defaults.
        String dataFolderPath = getDataFolder().getPath().replaceAll("\\\\", "/");
        portalFolder = dataFolderPath + "/portals/";
        gateFolder = dataFolderPath + "/gates/";
        langFolder = dataFolderPath + "/lang/";

        pluginVersion = pluginDescriptionFile.getVersion();

        log.info(pluginDescriptionFile.getName() + " v." + pluginDescriptionFile.getVersion() + " is enabled.");

        // Register events before loading gates to stop weird things happening.
        pm.registerEvents(new PlayerEventListener(), this);
        pm.registerEvents(new BlockEventListener(), this);

        pm.registerEvents(new VehicleEventListener(), this);
        pm.registerEvents(new EntityEventListener(), this);
        pm.registerEvents(new PortalEventListener(), this);
        pm.registerEvents(new WorldEventListener(), this);
        pm.registerEvents(new PluginEventListener(this), this);

        this.loadConfig();

        // Enable the required channels for Bungee support
        if (enableBungee) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordListener());
        }

        // It is important to load languages here, as they are used during reloadGates()
        languageLoader = new LanguageLoader(langFolder, Stargate.langName);

        this.migrate();
        this.loadGates();
        this.loadAllPortals();

        // Check to see if Economy is loaded yet.
        if (EconomyHandler.setupEconomy(pm)) {
            if (EconomyHandler.economy != null) {
                String vaultVersion = EconomyHandler.vault.getDescription().getVersion();
                log.info(Stargate.getString("prefix") +
                        replaceVars(Stargate.getString("vaultLoaded"), "%version%", vaultVersion));
            }
        }

        getServer().getScheduler().runTaskTimer(this, new StarGateThread(), 0L, 100L);
        getServer().getScheduler().runTaskTimer(this, new BlockChangeThread(), 0L, 1L);

        this.registerCommands();
    }

    private void registerCommands() {
        PluginCommand stargateCommand = this.getCommand("stargate");
        if (stargateCommand != null) {
            stargateCommand.setExecutor(new CommandStarGate(this));
            stargateCommand.setTabCompleter(new StarGateTabCompleter());
        }
    }

    public void loadConfig() {
        reloadConfig();
        newConfig = this.getConfig();
        // Copy default values if required
        newConfig.options().copyDefaults(true);

        // Load values into variables
        portalFolder = newConfig.getString("portal-folder");
        gateFolder = newConfig.getString("gate-folder");

        String defaultNetwork = newConfig.getString("default-gate-network");
        defaultGateNetwork = defaultNetwork != null ? defaultNetwork.trim() : null;
        destroyExplosion = newConfig.getBoolean("destroyexplosion");
        maxGates = newConfig.getInt("maxgates");
        langName = newConfig.getString("lang");
        destMemory = newConfig.getBoolean("destMemory");
        ignoreEntrance = newConfig.getBoolean("ignoreEntrance");
        handleVehicles = newConfig.getBoolean("handleVehicles");
        sortLists = newConfig.getBoolean("sortLists");
        protectEntrance = newConfig.getBoolean("protectEntrance");
        enableBungee = newConfig.getBoolean("enableBungee");
        verifyPortals = newConfig.getBoolean("verifyPortals");
        // Sign color
        loadSignColor(newConfig.getString("signColor"));
        // Debug
        debug = newConfig.getBoolean("debug");
        permDebug = newConfig.getBoolean("permdebug");
        // Economy
        EconomyHandler.economyEnabled = newConfig.getBoolean("useeconomy");
        EconomyHandler.setCreateCost(newConfig.getInt("createcost"));
        EconomyHandler.setDestroyCost(newConfig.getInt("destroycost"));
        EconomyHandler.setUseCost(newConfig.getInt("usecost"));
        EconomyHandler.toOwner = newConfig.getBoolean("toowner");
        EconomyHandler.chargeFreeDestination = newConfig.getBoolean("chargefreedestination");
        EconomyHandler.freeGatesGreen = newConfig.getBoolean("freegatesgreen");

        this.saveConfig();
    }

    /**
     * Loads the correct sign color given a sign color string
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
        log.warning(Stargate.getString("prefix") + "You have specified an invalid color in your config.yml." +
                " Defaulting to BLACK");
        Stargate.signColor = ChatColor.BLACK;
    }

    public void closeAllPortals() {
        // Close all gates prior to reloading
        for (Portal p : openList) {
            p.close(true);
        }
    }

    public void loadGates() {
        GateHandler.loadGates(gateFolder);
        log.info(Stargate.getString("prefix") + "Loaded " + GateHandler.getGateCount() + " gate layouts");
    }

    public void loadAllPortals() {
        for (World world : getServer().getWorlds()) {
            if (!managedWorlds.contains(world.getName())) {
                PortalHandler.loadAllGates(world);
                managedWorlds.add(world.getName());
            }
        }
    }

    private void migrate() {
        // Only migrate if new file doesn't exist.
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

    /*
     * Check if a plugin is loaded/enabled already. Returns the plugin if so, null otherwise
     */
    private Plugin checkPlugin(String p) {
        Plugin plugin = pm.getPlugin(p);
        return checkPlugin(plugin);
    }

    private Plugin checkPlugin(Plugin plugin) {
        if (plugin != null && plugin.isEnabled()) {
            log.info(Stargate.getString("prefix") + "Found " + plugin.getDescription().getName() + " (v" + plugin.getDescription().getVersion() + ")");
            return plugin;
        }
        return null;
    }

    /**
     * Reloads all portals and files
     *
     * @param sender <p>The sender of the reload request</p>
     */
    public void reload(CommandSender sender) {
        // Deactivate portals
        for (Portal p : activeList) {
            p.deactivate();
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
        languageLoader.setChosenLanguage(langName);
        languageLoader.reload();

        // Load Economy support if enabled/clear if disabled
        if (EconomyHandler.economyEnabled && EconomyHandler.economy == null) {
            if (EconomyHandler.setupEconomy(pm)) {
                if (EconomyHandler.economy != null) {
                    String vaultVersion = EconomyHandler.vault.getDescription().getVersion();
                    log.info(Stargate.getString("prefix") + Stargate.replaceVars(
                            Stargate.getString("vaultLoaded"), "%version%", vaultVersion));
                }
            }
        }
        if (!EconomyHandler.economyEnabled) {
            EconomyHandler.vault = null;
            EconomyHandler.economy = null;
        }

        // Enable the required channels for Bungee support
        if (oldEnableBungee != enableBungee) {
            if (enableBungee) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
                Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordListener());
            } else {
                Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
                Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
            }
        }

        sendMessage(sender, "stargate reloaded");
    }

}
