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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.TheDgtl.Stargate.listeners.BlockEventsListener;
import net.TheDgtl.Stargate.listeners.EntityEventsListener;
import net.TheDgtl.Stargate.listeners.MovementEventsListener;
import net.TheDgtl.Stargate.listeners.PlayerEventsListener;
import net.TheDgtl.Stargate.listeners.PluginStatusChangeListener;
import net.TheDgtl.Stargate.listeners.WorldEventsListener;
import net.TheDgtl.Stargate.threads.BlockPopulatorThread;
import net.TheDgtl.Stargate.threads.SGThread;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public class Stargate extends JavaPlugin {
    static private Logger log;
    private EconomyHandler economyHandler;
    private LangLoader lang;
    private String portalFolder;
    private String gateFolder;
    private String defNetwork;
    private boolean destroyExplosion = false;
    private int maxGates = 0;
    private String langName;
    private final int activeTime = 10;
    private final int openTime = 10;
    private boolean destMemory = false;
    private boolean handleVehicles = true;
    private boolean sortLists = false;
    private boolean protectEntrance = false;
    private boolean enableBungee = true;
    private boolean verifyPortals = true;
    private ChatColor signColor;
    // Temp workaround for snowmen, don't check gate entrance
    private boolean ignoreEntrance = false;

    // Used for debug
    static private boolean debug = false;
    static private boolean permDebug = false;

    private final ConcurrentLinkedQueue<Portal> openList = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Portal> activeList = new ConcurrentLinkedQueue<>();

    // Used for populating gate open/closed material.
    private final Queue<BloxPopulator> blockPopulatorQueue = new LinkedList<>();

    // HashMap of player names for Bungee support
    private final Map<String, String> bungeeQueue = new HashMap<>();

    // World names that contain stargates
    private final HashSet<String> managedWorlds = new HashSet<>();

    private FileConfiguration newConfig;
    private PluginManager pm;

    @Override
    public void onDisable() {
        Portal.closeAllGates(this);
        Portal.clearGates();
        managedWorlds.clear();
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public void onEnable() {
    	enableBStats();
    	
        PluginDescriptionFile pdfFile = this.getDescription();

        pm = getServer().getPluginManager();
        newConfig = this.getConfig();
        log = getLogger();
        
        PermissionManager.server = getServer();
        
        String dataFolder = getDataFolder().getPath().replaceAll("\\\\", "/");

        String langFolder = dataFolder + "/lang/";
        portalFolder = dataFolder + "/portals/";
        gateFolder = dataFolder + "/gates/";

        log.info(pdfFile.getName() + " v." + pdfFile.getVersion() + " is enabled.");

        // Register events before loading gates to stop weird things happening.
        pm.registerEvents(new PlayerEventsListener(this), this);
        pm.registerEvents(new BlockEventsListener(this), this);

        pm.registerEvents(new MovementEventsListener(this), this);
        pm.registerEvents(new EntityEventsListener(this), this);
        pm.registerEvents(new WorldEventsListener(this), this);
        pm.registerEvents(new PluginStatusChangeListener(this), this);

        this.loadConfig();

        // Enable the required channels for Bungee support
        if (enableBungee) {
            Messenger msgr = Bukkit.getMessenger();

            msgr.registerOutgoingPluginChannel(this, "BungeeCord");
            msgr.registerIncomingPluginChannel(this, "BungeeCord", new StargateBungeePluginMessageListener(this));
        }

        // It is important to load languages here, as they are used during reloadGates()
        lang = new LangLoader(this, langFolder, langName);

        this.migrate();
        this.loadGates();
        this.loadAllPortals();
        
        // Check to see if Economy is loaded yet.
        if (economyHandler.setupEconomy(pm) && economyHandler.getEconomy() != null) {
            log.info("[Stargate] Vault v" + economyHandler.getVault().getDescription().getVersion() + " found");
        }

        BukkitScheduler scheduler = getServer().getScheduler();

        scheduler.scheduleSyncRepeatingTask(this, new SGThread(this), 0L, 100L);
        scheduler.scheduleSyncRepeatingTask(this, new BlockPopulatorThread(this), 0L, 1L);
    }

    public void loadConfig() {
        reloadConfig();

        newConfig = this.getConfig();
        newConfig.options().copyDefaults(true);

        // TODO ; this is dumb
        // Load values into variables
        portalFolder = newConfig.getString("portal-folder");
        gateFolder = newConfig.getString("gate-folder");
        defNetwork = newConfig.getString("default-gate-network").trim();
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
        String sc = newConfig.getString("signColor");

        try {
            assert sc != null;
            signColor = ChatColor.valueOf(sc.toUpperCase());
        } catch (Exception ignore) {
            signColor = ChatColor.BLACK;
            log.warning("[Stargate] You have specified an invalid color in your config.yml. Defaulting to BLACK");
        }

        // Debug
        debug = newConfig.getBoolean("debug");
        permDebug = newConfig.getBoolean("permdebug");
        
        
        economyHandler = new EconomyHandler(getServer(),newConfig);

        this.saveConfig();
    }

    private void enableBStats() {
    	//registers bstats metrics
        int pluginId = 10451;
        Metrics metrics = new Metrics(this, pluginId);
        
        metrics.addCustomChart(new SimplePie("language", new Callable<String>() {
        	@Override
        	public String call() {
        		return getConfig().getString("lang");
        	}
        }));
        
        metrics.addCustomChart(new SimplePie("gateformats",new Callable<String>() {
        	@Override
        	public String call() {
        		return String.valueOf(Gate.getGateCount());
        	}
        }));
        
        metrics.addCustomChart(new SimplePie("gates",new Callable<String>() {
        	@Override
        	public String call() {
        		return String.valueOf(Portal.gateCount);
        	}
        }));
        
        metrics.addCustomChart(new SimplePie("flags",new Callable<String>() {
        	@Override
        	public String call() {
        		return Portal.UsedFlags.returnString();
        	}
        }));
    }
    private void closeAllPortals() {
        // Close all gates prior to reloading
        for (Portal p : openList) {
            p.close(true);
        }
    }

    private void loadGates() {
        Gate.loadGates(this, gateFolder);
        log.info("[Stargate] Loaded " + Gate.getGateCount() + " gate layouts");
    }

    private void loadAllPortals() {
        for (World world : getServer().getWorlds()) {
            if (!managedWorlds.contains(world.getName())) {
                Portal.loadAllGates(this, world);
                managedWorlds.add(world.getName());
            }
        }
    }

    private void migrate() {
        // Only migrate if new file doesn't exist.
        File newPortalDir = new File(portalFolder);
        if (!newPortalDir.exists()) {
            newPortalDir.mkdirs();
        }
        File newFile = new File(portalFolder, getServer().getWorlds().get(0).getName() + ".db");
        if (!newFile.exists()) {
            newFile.getParentFile().mkdirs();
        }
    }
    /**
     * Debug for permissions
     * @param rout
     * @param msg
     */
    static public void permDebug(String rout, String msg) {
    	if(permDebug)
    		debug(rout,msg);
    }
    /**
     * Stargate's debug function :)
     * @param rout 
     * @param msg
     */
    static public void debug(String rout, String msg) {
        if (debug) {
            log.info("[" + rout + "] " + msg);
        } else {
            log.log(Level.FINEST, "[" + rout + "] " + msg);
        }
    }

    public void sendMessage(CommandSender player, String message) {
        sendMessage(player, message, true);
    }

    public void sendMessage(CommandSender player, String message, boolean error) {
        if (message.isEmpty()) return;

        message = message.replaceAll("(&([a-f0-9]))", "\u00A7$2");

        if (error)
            player.sendMessage(ChatColor.RED + getString("prefix") + ChatColor.WHITE + message);
        else
            player.sendMessage(ChatColor.GREEN + getString("prefix") + ChatColor.WHITE + message);
    }
    
    /*
     * TODO Stupid, just move signColor over to portal class instead and have the method over there
     */
    public void setLine(Sign sign, int index, String text) {
        sign.setLine(index, signColor + text);
    }
    /**
     * TODO Contributes to bloated code
     * @return
     */
    public String getSaveLocation() {
        return portalFolder;
    }

    public String getDefaultNetwork() {
        return defNetwork;
    }
    /**
     * TODO Contributes to bloated code
     * @return
     */
    public String getString(String name) {
        return lang.getString(name);
    }

    /**
     * Return true if the portal is free for the player
     */
    public boolean isFree(Player player, Portal src, Portal dest) {
        // This gate is free
        if (src.isFree()) return true;
        // Player gets free use
        if (PermissionManager.hasPerm(player, "stargate.free") || PermissionManager.hasPerm(player, "stargate.free.use")) return true;
        // Don't charge for free destination gates
        return dest != null && !economyHandler.isChargeFreeDestination() && dest.isFree();
    }

    /**
     * Check whether the player can see this gate (Hidden property check)
     */
    public boolean canSee(Player player, Portal portal) {
        // The gate is not hidden
        if (!portal.isHidden()) return true;
        // The player is an admin with the ability to see hidden gates
        if (PermissionManager.hasPerm(player, "stargate.admin") || PermissionManager.hasPerm(player, "stargate.admin.hidden")) return true;
        // The player is the owner of the gate
        return portal.isOwner(player);
    }


    /**
     * Check if the player has access to {option}
     */
    public boolean canOption(Player player, String option) {
        // Check if the player can use all options
        if (PermissionManager.hasPerm(player, "stargate.option")) return true;
        // Check if they can use this specific option
        return PermissionManager.hasPerm(player, "stargate.option." + option);
    }

    /**
     * Check if the player can create gates on {network}
     */
    public boolean canCreate(Player player, String network) {
        // Check for general create
        if (PermissionManager.hasPerm(player, "stargate.create")) return true;
        // Check for all network create permission
        if (PermissionManager.hasPerm(player, "stargate.create.network")) {
            // Do a deep check to see if the player lacks this specific network node
            return PermissionManager.hasPermDeep(player, "stargate.create.network." + network);
        }
        // Check for this specific network
        return PermissionManager.hasPerm(player, "stargate.create.network." + network);

    }

    /**
     * Check if the player can create a personal gate
     */
    public boolean canCreatePersonal(Player player) {
        // Check for general create
        if (PermissionManager.hasPerm(player, "stargate.create")) return true;
        // Check for personal
        return PermissionManager.hasPerm(player, "stargate.create.personal");
    }

    /**
     * Check if the player can create this gate layout
     */
    public boolean canCreateGate(Player player, String gate) {
        // Check for general create
        if (PermissionManager.hasPerm(player, "stargate.create")) return true;
        // Check for all gate create permissions
        if (PermissionManager.hasPerm(player, "stargate.create.gate")) {
            // Do a deep check to see if the player lacks this specific gate node
            return PermissionManager.hasPermDeep(player, "stargate.create.gate." + gate);
        }
        // Check for this specific gate
        return PermissionManager.hasPerm(player, "stargate.create.gate." + gate);
    }

    /**
     * Check if the player can destroy this gate
     */
    public boolean canDestroy(Player player, Portal portal) {
        String network = portal.getNetwork();
        // Check for general destroy
        if (PermissionManager.hasPerm(player, "stargate.destroy")) return true;
        // Check for all network destroy permission
        if (PermissionManager.hasPerm(player, "stargate.destroy.network")) {
            // Do a deep check to see if the player lacks permission for this network node
            return PermissionManager.hasPermDeep(player, "stargate.destroy.network." + network);
        }
        // Check for this specific network
        if (PermissionManager.hasPerm(player, "stargate.destroy.network." + network)) return true;
        // Check for personal gate
        return portal.isOwner(player) && PermissionManager.hasPerm(player, "stargate.destroy.personal");
    }

    /**
     * Charge player for {action} if required, true on success, false if can't afford
     */
    @Deprecated
    public boolean chargePlayer(Player player, String target, int cost) {
        // If cost is 0
        if (cost == 0) return true;
        // Economy is disabled
        if (!economyHandler.useEconomy()) return true;
        // Charge player
        return economyHandler.chargePlayer(player, target, cost);
    }

    /**
     * Charge player for {action} if required, true on success, false if can't afford
     */
    public boolean chargePlayer(Player player, UUID target, int cost) {
        // If cost is 0
        if (cost == 0) return true;
        // Economy is disabled
        if (!economyHandler.useEconomy()) return true;
        // Charge player
        return economyHandler.chargePlayer(player, target, cost);
    }

    /**
     * Charge player for {action} if required, true on success, false if can't afford
     */
    public boolean chargePlayer(Player player, int cost) {
        // If cost is 0
        if (cost == 0) return true;
        // Economy is disabled
        if (!economyHandler.useEconomy()) return true;
        // Charge player
        return economyHandler.chargePlayer(player, cost);
    }
    
    /**
     * Determine the cost of a gate
     */
    public int getUseCost(Player player, Portal src, Portal dest) {
        // Not using Economy
        if (!economyHandler.useEconomy()) return 0;
        // Portal is free
        if (src.isFree()) return 0;
        // Not charging for free destinations
        if (dest != null && !economyHandler.isChargeFreeDestination() && dest.isFree()) return 0;
        // Cost is 0 if the player owns this gate and funds go to the owner
        if (src.getGate().getToOwner() && src.isOwner(player)) return 0;
        // Player gets free gate use
        if (PermissionManager.hasPerm(player, "stargate.free") || PermissionManager.hasPerm(player, "stargate.free.use")) return 0;

        return src.getGate().getUseCost();
    }

    /**
     * Determine the cost to create the gate
     */
    public int getCreateCost(Player player, Gate gate) {
        // Not using Economy
        if (!economyHandler.useEconomy()) return 0;
        // Player gets free gate destruction
        if (PermissionManager.hasPerm(player, "stargate.free") || PermissionManager.hasPerm(player, "stargate.free.create")) return 0;

        return gate.getCreateCost();
    }

    /**
     * Determine the cost to destroy the gate
     */
    public int getDestroyCost(Player player, Gate gate) {
        // Not using Economy
        if (!economyHandler.useEconomy()) return 0;
        // Player gets free gate destruction
        if (PermissionManager.hasPerm(player, "stargate.free") || PermissionManager.hasPerm(player, "stargate.free.destroy")) return 0;

        return gate.getDestroyCost();
    }

    /**
     * Check if a plugin is loaded/enabled already. Returns the plugin if so, null otherwise
     */
    private Plugin checkPlugin(String p) {
        Plugin plugin = pm.getPlugin(p);
        return checkPlugin(plugin);
    }

    private Plugin checkPlugin(Plugin plugin) {
        if (plugin != null && plugin.isEnabled()) {
            log.info("[Stargate] Found " + plugin.getDescription().getName() + " (v" + plugin.getDescription().getVersion() + ")");
            return plugin;
        }
        return null;
    }

    /**
     * Parse a given text string and replace the variables
     */
    public String replaceVars(String format, String[] search, String[] replace) {
        if (search.length != replace.length) return "";
        for (int i = 0; i < search.length; i++) {
            format = format.replace(search[i], replace[i]);
        }
        return format;
    }

    // TODO ;
    // put some of these in their own classes & give them names that make fucking sense
    // also clean them
    // Lots of duplicated & messy code

    // Todo; command handler class, clean this up

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmd = command.getName();

        if (cmd.equalsIgnoreCase("sg")) {
            if (args.length != 1) return false;

            if (args[0].equalsIgnoreCase("about")) {
                sender.sendMessage("Stargate Plugin created by Drakia");
                if (!lang.getString("author").isEmpty())
                    sender.sendMessage("Language created by " + lang.getString("author"));
                return true;
            }

            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (!PermissionManager.hasPerm(p, "stargate.admin") && !PermissionManager.hasPerm(p, "stargate.admin.reload")) {
                    sendMessage(sender, "Permission Denied");
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("reload")) {
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
                Portal.clearGates();
                Gate.clearGates();

                // Store the old Bungee enabled value
                boolean oldEnableBungee = enableBungee;

                // Reload data
                loadConfig();
                loadGates();
                loadAllPortals();

                lang.setLang(langName);
                lang.reload();

                // Load Economy support if enabled/clear if disabled
                if (economyHandler.isEconomyEnabled() && economyHandler.getEconomy() == null) {
                    if (economyHandler.setupEconomy(pm)) {
                        if (economyHandler.getEconomy() != null)
                            log.info("[Stargate] Vault v" + economyHandler.getVault().getDescription().getVersion() + " found");
                    }
                }

                // Enable the required channels for Bungee support
                if (oldEnableBungee != enableBungee) {
                    if (enableBungee) {
                        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new StargateBungeePluginMessageListener(this));
                        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
                    } else {
                        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
                        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
                    }
                }

                sendMessage(sender, "Stargate reloaded");
                return true;
            }

            return false;
        }

        return false;
    }

    @NotNull
    public Logger getStargateLogger() {
        return log;
    }

    @NotNull
    public LangLoader getLang() {
        return lang;
    }

    @NotNull
    public String getPortalFolder() {
        return portalFolder;
    }

    @NotNull
    public String getDefNetwork() {
        return defNetwork;
    }

    public boolean isDestroyExplosion() {
        return destroyExplosion;
    }

    public int getMaxGates() {
        return maxGates;
    }

    @NotNull
    public String getLangName() {
        return langName;
    }

    public int getActiveTime() {
        return activeTime;
    }

    public int getOpenTime() {
        return openTime;
    }

    public boolean isDestMemory() {
        return destMemory;
    }

    public boolean isHandleVehicles() {
        return handleVehicles;
    }

    public boolean isSortLists() {
        return sortLists;
    }

    public boolean isProtectEntrance() {
        return protectEntrance;
    }

    public boolean isEnableBungee() {
        return enableBungee;
    }

    public boolean isVerifyPortals() {
        return verifyPortals;
    }

    public boolean isIgnoreEntrance() {
        return ignoreEntrance;
    }

    static public boolean isDebug() {
        return debug;
    }

    static public boolean isPermDebug() {
        return permDebug;
    }

    @NotNull
    public ConcurrentLinkedQueue<Portal> getOpenList() {
        return openList;
    }

    @NotNull
    public ConcurrentLinkedQueue<Portal> getActiveList() {
        return activeList;
    }

    @NotNull
    public Queue<BloxPopulator> getBlockPopulatorQueue() {
        return blockPopulatorQueue;
    }

    @NotNull
    public Map<String, String> getBungeeQueue() {
        return bungeeQueue;
    }

    @NotNull
    public HashSet<String> getManagedWorlds() {
        return managedWorlds;
    }

    @NotNull
    public FileConfiguration getNewConfig() {
        return newConfig;
    }

    @NotNull
    public EconomyHandler getEconomyHandler() {
        return economyHandler;
    }

    @NotNull
    public PluginManager getPm() {
        return pm;
    }
}
