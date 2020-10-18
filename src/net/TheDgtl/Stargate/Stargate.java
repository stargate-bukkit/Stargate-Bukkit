package net.TheDgtl.Stargate;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.TheDgtl.Stargate.event.StargateAccessEvent;
import net.TheDgtl.Stargate.event.StargateDestroyEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.EndGateway;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Stargate - A portal plugin for Bukkit
 * Copyright (C) 2011 Shaun (sturmeh)
 * Copyright (C) 2011 Dinnerbone
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

@SuppressWarnings("unused")
public class Stargate extends JavaPlugin {
	public static Logger log;
	private FileConfiguration newConfig;
	private PluginManager pm;
	public static Server server;
	public static Stargate stargate;
	private static LangLoader lang;
	
	private static String portalFolder;
	private static String gateFolder;
	private static String langFolder;
	private static String defNetwork = "central";
	private static boolean destroyExplosion = false;
	public static int maxGates = 0;
	private static String langName = "en";
	private static final int activeTime = 10;
	private static final int openTime = 10;
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
	
	public static ConcurrentLinkedQueue<Portal> openList = new ConcurrentLinkedQueue<>();
	public static ConcurrentLinkedQueue<Portal> activeList = new ConcurrentLinkedQueue<>();
	
	// Used for populating gate open/closed material.
	public static Queue<BloxPopulator> blockPopulatorQueue = new LinkedList<>();
	
	// HashMap of player names for Bungee support
	public static Map<String, String> bungeeQueue = new HashMap<>();
	
	// World names that contain stargates
	public static HashSet<String> managedWorlds = new HashSet<>();
	
	public void onDisable() {
		Portal.closeAllGates();
		Portal.clearGates();
		managedWorlds.clear();
		getServer().getScheduler().cancelTasks(this);
	}

	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		pm = getServer().getPluginManager();
		newConfig = this.getConfig();
		log = Logger.getLogger("Minecraft");
		Stargate.server = getServer();
		Stargate.stargate = this;
		
		// Set portalFile and gateFolder to the plugin folder as defaults.
		portalFolder = getDataFolder().getPath().replaceAll("\\\\", "/") + "/portals/";
		gateFolder = getDataFolder().getPath().replaceAll("\\\\", "/") + "/gates/";
		langFolder = getDataFolder().getPath().replaceAll("\\\\", "/") + "/lang/";
		
		log.info(pdfFile.getName() + " v." + pdfFile.getVersion() + " is enabled.");
		
		// Register events before loading gates to stop weird things happening.
		pm.registerEvents(new pListener(), this);
		pm.registerEvents(new bListener(), this);
		
		pm.registerEvents(new vListener(), this);
		pm.registerEvents(new eListener(), this);
		pm.registerEvents(new wListener(), this);
		pm.registerEvents(new sListener(), this);
		
		this.loadConfig();
		
		// Enable the required channels for Bungee support
		if (enableBungee) {
			Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new pmListener());
		}
		
		// It is important to load languages here, as they are used during reloadGates()
		lang = new LangLoader(langFolder, Stargate.langName);
		
		this.migrate();
		this.loadGates();
		this.loadAllPortals();
		
		// Check to see if Economy is loaded yet.
		if (EconomyHandler.setupEconomy(pm)) {
			if (EconomyHandler.economy != null)
				log.info("[Stargate] Vault v" + EconomyHandler.vault.getDescription().getVersion() + " found");
        }

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new SGThread(), 0L, 100L);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new BlockPopulatorThread(), 0L, 1L);
	}

	public void loadConfig() {
		reloadConfig();
		newConfig = this.getConfig();
		// Copy default values if required
		newConfig.options().copyDefaults(true);
		
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
			signColor = ChatColor.valueOf(sc.toUpperCase());
		} catch (Exception ignore) {
			log.warning("[Stargate] You have specified an invalid color in your config.yml. Defaulting to BLACK");
			signColor = ChatColor.BLACK;
		}
		// Debug
		debug = newConfig.getBoolean("debug");
		permDebug = newConfig.getBoolean("permdebug");
		// Economy
		EconomyHandler.economyEnabled = newConfig.getBoolean("useeconomy");
		EconomyHandler.createCost = newConfig.getInt("createcost");
		EconomyHandler.destroyCost = newConfig.getInt("destroycost");
		EconomyHandler.useCost = newConfig.getInt("usecost");
		EconomyHandler.toOwner = newConfig.getBoolean("toowner");
		EconomyHandler.chargeFreeDestination = newConfig.getBoolean("chargefreedestination");
		EconomyHandler.freeGatesGreen = newConfig.getBoolean("freegatesgreen");
		
		this.saveConfig();
	}
	
	public void closeAllPortals() {
		// Close all gates prior to reloading
		for (Portal p : openList) {
			p.close(true);
		}
	}
	
	public void loadGates() {
		Gate.loadGates(gateFolder);
		log.info("[Stargate] Loaded " + Gate.getGateCount() + " gate layouts");
	}
	
	public void loadAllPortals() {
		for (World world : getServer().getWorlds()) {
			if(!managedWorlds.contains(world.getName())) {
				Portal.loadAllGates(world);
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
	
	public static void debug(String rout, String msg) {
		if (Stargate.debug) {
			log.info("[Stargate::" + rout + "] " + msg);
		} else {
			log.log(Level.FINEST, "[Stargate::" + rout + "] " + msg);
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
		return defNetwork;
	}
	
	public static String getString(String name) {
		return lang.getString(name);
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
		if ((!portal.isFixed()) && portal.isActive() &&  (portal.getActivePlayer() != player)) {
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
	
	/*
	 * Check a deep permission, this will check to see if the permissions is defined for this use
	 * If using Permissions it will return the same as hasPerm
	 * If using SuperPerms will return true if the node isn't defined
	 * Or the value of the node if it is
	 */
	public static boolean hasPermDeep(Player player, String perm) {
		if (!player.isPermissionSet(perm)) {
			if (permDebug)
				Stargate.debug("hasPermDeep::SuperPerm", perm + " => true");
			return true;
		}
		if (permDebug)
			Stargate.debug("hasPermDeep::SuperPerms", perm + " => " + player.hasPermission(perm));
		return player.hasPermission(perm);
	}
	
	/*
	 * Check whether player can teleport to dest world
	 */
	public static boolean canAccessWorld(Player player, String world) {
		// Can use all Stargate player features or access all worlds
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
		// Can user all Stargate player features, or access all networks
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
		// Can user all Stargate player features, or access all servers
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
	public static boolean canAccessPortal(Player player, Portal portal, boolean deny) {
		StargateAccessEvent event = new StargateAccessEvent(player, portal, deny);
		Stargate.server.getPluginManager().callEvent(event);
		return !event.getDeny();
	}
	
	/*
	 * Return true if the portal is free for the player
	 */
	public static boolean isFree(Player player, Portal src, Portal dest) {
		// This gate is free
		if (src.isFree()) return true;
		// Player gets free use
		if (hasPerm(player, "stargate.free") || Stargate.hasPerm(player,  "stargate.free.use")) return true;
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
	public static boolean canOption(Player player, String option) {
		// Check if the player can use all options
		if (hasPerm(player, "stargate.option")) return true;
		// Check if they can use this specific option
		return hasPerm(player, "stargate.option." + option);
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
	public static boolean chargePlayer(Player player, String target, int cost) {
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
	
	/*
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
	
	/*
	 * Parse a given text string and replace the variables
	 */
	public static String replaceVars(String format, String[] search, String[] replace) {
		if (search.length != replace.length) return "";
		for (int i = 0; i < search.length; i++) {
			format = format.replace(search[i], replace[i]);
		}
		return format;
	}
	
	private class vListener implements Listener {
		@EventHandler
		public void onVehicleMove(VehicleMoveEvent event) {
			if (!handleVehicles) return;
			List<Entity> passengers = event.getVehicle().getPassengers();
			Vehicle vehicle = event.getVehicle();
			
			Portal portal = Portal.getByEntrance(event.getTo());
			if (portal == null || !portal.isOpen()) return;
			
			// We don't support vehicles in Bungee portals
			if (portal.isBungee()) return;
			
			if (!passengers.isEmpty() && passengers.get(0) instanceof Player) {
				/*
				Player player = (Player) passengers.get(0);
				if (!portal.isOpenFor(player)) {
					Stargate.sendMessage(player, Stargate.getString("denyMsg"));
					return;
				}
				
				Portal dest = portal.getDestination(player);
				if (dest == null) return;
				boolean deny = false;
				// Check if player has access to this network
				if (!canAccessNetwork(player, portal.getNetwork())) {
					deny = true;
				}
				
				// Check if player has access to destination world
				if (!canAccessWorld(player, dest.getWorld().getName())) {
					deny = true;
				}
				
				if (!canAccessPortal(player, portal, deny)) {
					Stargate.sendMessage(player, Stargate.getString("denyMsg"));
					portal.close(false);
					return;
				}
				
				int cost = Stargate.getUseCost(player, portal, dest);
				if (cost > 0) {
					boolean success;
					if(portal.getGate().getToOwner()) {
						if(portal.getOwnerUUID() == null) {
							success = Stargate.chargePlayer(player, portal.getOwnerUUID(), cost);
						} else {
							success = Stargate.chargePlayer(player, portal.getOwnerName(), cost);
						}
					} else {
						success = Stargate.chargePlayer(player, cost);
					}
					if(!success) {
						// Insufficient Funds
						Stargate.sendMessage(player, Stargate.getString("inFunds"));
						portal.close(false);
						return;
					}
					String deductMsg = Stargate.getString("ecoDeduct");
					deductMsg = Stargate.replaceVars(deductMsg, new String[] {"%cost%", "%portal%"}, new String[] {EconomyHandler.format(cost), portal.getName()});
					sendMessage(player, deductMsg, false);
					if (portal.getGate().getToOwner()) {
						Player p;
						if(portal.getOwnerUUID() != null) {
							p = server.getPlayer(portal.getOwnerUUID());
						} else {
							p = server.getPlayer(portal.getOwnerName());
						}
						if (p != null) {
							String obtainedMsg = Stargate.getString("ecoObtain");
							obtainedMsg = Stargate.replaceVars(obtainedMsg, new String[] {"%cost%", "%portal%"}, new String[] {EconomyHandler.format(cost), portal.getName()});
							Stargate.sendMessage(p, obtainedMsg, false);
						}
					}
				}
				
				Stargate.sendMessage(player, Stargate.getString("teleportMsg"), false);
				dest.teleport(vehicle);
				portal.close(false);
				 */
			} else {
				Portal dest = portal.getDestination();
				if (dest == null) return;
				dest.teleport(vehicle);
			}
		}
	}
	
	private class pListener implements Listener {
		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent event) {
			if (!enableBungee) return;
			
			Player player = event.getPlayer();
			String destination = bungeeQueue.remove(player.getName().toLowerCase());
			if (destination == null) return;
			
			Portal portal = Portal.getBungeeGate(destination);
			if (portal == null) {
				Stargate.debug("PlayerJoin", "Error fetching destination portal: " + destination);
				return;
			}
			portal.teleport(player, portal, null);
		}

		@EventHandler
		public void onPlayerTeleport(PlayerTeleportEvent event) {
			// cancel portal and endgateway teleportation if it's from a Stargate entrance
			PlayerTeleportEvent.TeleportCause cause = event.getCause();
			if(!event.isCancelled()
			&& (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
			|| cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY && World.Environment.THE_END == event.getFrom().getWorld().getEnvironment())
			&& Portal.getByAdjacentEntrance(event.getFrom()) != null) {
				event.setCancelled(true);
			}
		}
		
		@EventHandler
		public void onPlayerMove(PlayerMoveEvent event) {
			if (event.isCancelled()) return;
			
			// Check to see if the player actually moved
			if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
				return;
			}
			
			Player player = event.getPlayer();
			Portal portal = Portal.getByEntrance(event.getTo());
			// No portal or not open
			if (portal == null || !portal.isOpen()) return;

			// Not open for this player
			if (!portal.isOpenFor(player)) {
				Stargate.sendMessage(player, Stargate.getString("denyMsg"));
				portal.teleport(player, portal, event);
				return;
			}
			
			Portal destination = portal.getDestination(player);
			if (!portal.isBungee() && destination == null) return;
			
			boolean deny = false;
			// Check if player has access to this server for Bungee gates
			if (portal.isBungee()) {
				if (!canAccessServer(player, portal.getNetwork())) {
					deny = true;
				}
			} else {
				// Check if player has access to this network
				if (!canAccessNetwork(player, portal.getNetwork())) {
					deny = true;
				}
				
				// Check if player has access to destination world
				if (!canAccessWorld(player, destination.getWorld().getName())) {
					deny = true;
				}
			}
			
			if (!canAccessPortal(player, portal, deny)) {
				Stargate.sendMessage(player, Stargate.getString("denyMsg"));
				portal.teleport(player, portal, event);
				portal.close(false);
				return;
			}
			
			int cost = Stargate.getUseCost(player, portal, destination);
			if (cost > 0) {
				boolean success;
				if(portal.getGate().getToOwner()) {
					if(portal.getOwnerUUID() == null) {
						success = Stargate.chargePlayer(player, portal.getOwnerUUID(), cost);
					} else {
						success = Stargate.chargePlayer(player, portal.getOwnerName(), cost);
					}
				} else {
					success = Stargate.chargePlayer(player, cost);
				}
				if(!success) {
					// Insufficient Funds
					Stargate.sendMessage(player, Stargate.getString("inFunds"));
					portal.close(false);
					return;
				}
				String deductMsg = Stargate.getString("ecoDeduct");
				deductMsg = Stargate.replaceVars(deductMsg, new String[] {"%cost%", "%portal%"}, new String[] {EconomyHandler.format(cost), portal.getName()});
				sendMessage(player, deductMsg, false);
				if (portal.getGate().getToOwner() && portal.getOwnerUUID() != null) {
					Player p;
					if(portal.getOwnerUUID() != null) {
						p = server.getPlayer(portal.getOwnerUUID());
					} else {
						p = server.getPlayer(portal.getOwnerName());
					}
					if (p != null) {
						String obtainedMsg = Stargate.getString("ecoObtain");
						obtainedMsg = Stargate.replaceVars(obtainedMsg, new String[] {"%cost%", "%portal%"}, new String[] {EconomyHandler.format(cost), portal.getName()});
						Stargate.sendMessage(p, obtainedMsg, false);
					}
				}
			}
			
			Stargate.sendMessage(player, Stargate.getString("teleportMsg"), false);
			
			// BungeeCord Support
			if (portal.isBungee()) {
				if (!enableBungee) {
					player.sendMessage(Stargate.getString("bungeeDisabled"));
					portal.close(false);
					return;
				}
				
				// Teleport the player back to this gate, for sanity's sake
				portal.teleport(player, portal, event);
				
				// Send the SGBungee packet first, it will be queued by BC if required
				try {
					// Build the message, format is <player>#@#<destination>
					String msg = event.getPlayer().getName() + "#@#" + portal.getDestinationName();
					// Build the message data, sent over the SGBungee bungeecord channel
					ByteArrayOutputStream bao = new ByteArrayOutputStream();
					DataOutputStream msgData = new DataOutputStream(bao);
					msgData.writeUTF("Forward");
					msgData.writeUTF(portal.getNetwork());	// Server
					msgData.writeUTF("SGBungee");			// Channel
					msgData.writeShort(msg.length()); 	// Data Length
					msgData.writeBytes(msg); 			// Data
					player.sendPluginMessage(stargate, "BungeeCord", bao.toByteArray());
				} catch (IOException ex) {
					Stargate.log.severe("[Stargate] Error sending BungeeCord teleport packet");
					ex.printStackTrace();
					return;
				}
				
				// Connect player to new server
				try {
					ByteArrayOutputStream bao = new ByteArrayOutputStream();
					DataOutputStream msgData = new DataOutputStream(bao);
					msgData.writeUTF("Connect");
					msgData.writeUTF(portal.getNetwork());
					
					player.sendPluginMessage(stargate, "BungeeCord", bao.toByteArray());
					bao.reset();
				} catch(IOException ex) {
					Stargate.log.severe("[Stargate] Error sending BungeeCord connect packet");
					ex.printStackTrace();
					return;
				}
				
				// Close portal if required (Should never be)
				portal.close(false);
				return;
			}
			
			destination.teleport(player, portal, event);
			portal.close(false);
		}
		
		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Player player = event.getPlayer();
			Block block = event.getClickedBlock();

			if(block == null) return;

			// Right click
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (block.getBlockData() instanceof WallSign) {
					Portal portal = Portal.getByBlock(block);
					if (portal == null) return;
					// Cancel item use
					event.setUseItemInHand(Result.DENY);
					event.setUseInteractedBlock(Result.DENY);
					
					boolean deny = false;
					if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
						deny = true;
					}
					
					if (!Stargate.canAccessPortal(player, portal, deny)) {
						Stargate.sendMessage(player, Stargate.getString("denyMsg"));
						return;
					}
					
					if ((!portal.isOpen()) && (!portal.isFixed())) {
						portal.cycleDestination(player);
					}
					return;
				}

				// Implement right-click to toggle a stargate, gets around spawn protection problem.
				if (Tag.BUTTONS.isTagged(block.getType())) {
					Portal portal = Portal.getByBlock(block);
					if (portal == null) return;
					
					// Cancel item use
					event.setUseItemInHand(Result.DENY);
					event.setUseInteractedBlock(Result.DENY);
					
					boolean deny = false;
					if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
						deny = true;
					}
					
					if (!Stargate.canAccessPortal(player, portal, deny)) {
						Stargate.sendMessage(player, Stargate.getString("denyMsg"));
						return;
					}
					
					openPortal(player, portal);
					if (portal.isOpenFor(player)) {
						event.setUseInteractedBlock(Result.ALLOW);
					}
				}
				return;
			}
			
			// Left click
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				// Check if we're scrolling a sign
				if (block.getBlockData() instanceof WallSign) {
					Portal portal = Portal.getByBlock(block);
					if (portal == null) return;
					
					event.setUseInteractedBlock(Result.DENY);
					// Only cancel event in creative mode
					if (player.getGameMode().equals(GameMode.CREATIVE)) {
						event.setCancelled(true);
					}
					
					boolean deny = false;
					if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
						deny = true;
					}
					
					if (!Stargate.canAccessPortal(player, portal, deny)) {
						Stargate.sendMessage(player, Stargate.getString("denyMsg"));
						return;
					}
					
					if ((!portal.isOpen()) && (!portal.isFixed())) {
						portal.cycleDestination(player, -1);
					}
				}
			}
		}
	}

	private class bListener implements Listener {
		@EventHandler
		public void onSignChange(SignChangeEvent event) {
			if (event.isCancelled()) return;
			Player player = event.getPlayer();
			Block block = event.getBlock();
			if (!(block.getBlockData() instanceof WallSign)) return;
			
			final Portal portal = Portal.createPortal(event, player);
			// Not creating a gate, just placing a sign
			if (portal == null)	return;

			Stargate.sendMessage(player, Stargate.getString("createMsg"), false);
			Stargate.debug("onSignChange", "Initialized stargate: " + portal.getName());
			Stargate.server.getScheduler().scheduleSyncDelayedTask(stargate, new Runnable() {
				public void run() {
					portal.drawSign();
				}
			}, 1);
		}
		
		// Switch to HIGHEST priority so as to come after block protection plugins (Hopefully)
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onBlockBreak(BlockBreakEvent event) {
			if (event.isCancelled()) return;
			Block block = event.getBlock();
			Player player = event.getPlayer();

			Portal portal = Portal.getByBlock(block);
			if (portal == null && protectEntrance)
				portal = Portal.getByEntrance(block);
			if (portal == null) return;
			
			boolean deny = false;
			String denyMsg = "";
			
			if (!Stargate.canDestroy(player, portal)) {
				denyMsg = "Permission Denied"; // TODO: Change to Stargate.getString()
				deny = true;
				Stargate.log.info("[Stargate] " + player.getName() + " tried to destroy gate");
			}
			
			int cost = Stargate.getDestroyCost(player,  portal.getGate());
			
			StargateDestroyEvent dEvent = new StargateDestroyEvent(portal, player, deny, denyMsg, cost);
			Stargate.server.getPluginManager().callEvent(dEvent);
			if (dEvent.isCancelled()) {
				event.setCancelled(true);
				return;
			}
			if (dEvent.getDeny()) {
				Stargate.sendMessage(player, dEvent.getDenyReason());
				event.setCancelled(true);
				return;
			}
			
			cost = dEvent.getCost();
			
			if (cost != 0) {
				if (!Stargate.chargePlayer(player, cost)) {
					Stargate.debug("onBlockBreak", "Insufficient Funds");
					Stargate.sendMessage(player, Stargate.getString("inFunds"));
					event.setCancelled(true);
					return;
				}
				
				if (cost > 0) {
					String deductMsg = Stargate.getString("ecoDeduct");
					deductMsg = Stargate.replaceVars(deductMsg, new String[] {"%cost%", "%portal%"}, new String[] {EconomyHandler.format(cost), portal.getName()});
					sendMessage(player, deductMsg, false);
				} else if (cost < 0) {
					String refundMsg = Stargate.getString("ecoRefund");
					refundMsg = Stargate.replaceVars(refundMsg, new String[] {"%cost%", "%portal%"}, new String[] {EconomyHandler.format(-cost), portal.getName()});
					sendMessage(player, refundMsg, false);
				}
			}
			
			portal.unregister(true);
			Stargate.sendMessage(player, Stargate.getString("destroyMsg"), false);
		}

		@EventHandler
		public void onBlockPhysics(BlockPhysicsEvent event) {
			Block block = event.getBlock();
			Portal portal = null;
			
			// Handle keeping portal material and buttons around
			if (block.getType() == Material.NETHER_PORTAL) {
				portal = Portal.getByEntrance(block);
			} else if (Tag.BUTTONS.isTagged(block.getType())) {
				portal = Portal.getByControl(block);
			}
			if (portal != null) event.setCancelled(true);
		}
		
		@EventHandler
		public void onBlockFromTo(BlockFromToEvent event) {
			Portal portal = Portal.getByEntrance(event.getBlock());

			if (portal != null) {
				event.setCancelled((event.getBlock().getY() == event.getToBlock().getY()));
			}
		}
		
		@EventHandler
		public void onPistonExtend(BlockPistonExtendEvent event) {
			for(Block block : event.getBlocks()) {
				Portal portal = Portal.getByBlock(block);
				if (portal != null) {
					event.setCancelled(true);
					return;
				}
			}
		}
		
		@EventHandler
		public void onPistonRetract(BlockPistonRetractEvent event) {
			if (!event.isSticky()) return;
			for(Block block : event.getBlocks()) {
				Portal portal = Portal.getByBlock(block);
				if (portal != null) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	private class wListener implements Listener {
		@EventHandler
		public void onWorldLoad(WorldLoadEvent event) {
			if(!managedWorlds.contains(event.getWorld().getName())
			&& Portal.loadAllGates(event.getWorld())) {
				managedWorlds.add(event.getWorld().getName());
			}
		}
		
		// We need to reload all gates on world unload, boo
		@EventHandler
		public void onWorldUnload(WorldUnloadEvent event) {
			Stargate.debug("onWorldUnload", "Reloading all Stargates");
			World w = event.getWorld();
			if(managedWorlds.contains(w.getName())) {
				managedWorlds.remove(w.getName());
				Portal.clearGates();
				for(World world : server.getWorlds()) {
					if(managedWorlds.contains(world.getName())) {
						Portal.loadAllGates(world);
					}
				}
			}
		}
	}
	
	private class eListener implements Listener {
		@EventHandler
		public void onEntityExplode(EntityExplodeEvent event) {
			if (event.isCancelled()) return;
			for (Block b : event.blockList()) {
				Portal portal = Portal.getByBlock(b);
				if (portal == null) continue;
				if (destroyExplosion) {
					portal.unregister(true);
				} else {
					event.setCancelled(true);
					break;
				}
			}
		}
	}
	
	private class sListener implements Listener {
		@EventHandler
		public void onPluginEnable(PluginEnableEvent event) {
			if (EconomyHandler.setupEconomy(getServer().getPluginManager())) {
				log.info("[Stargate] Vault v" + EconomyHandler.vault.getDescription().getVersion() + " found");
			}
		}
		
		@EventHandler
		public void onPluginDisable(PluginDisableEvent event) {
			if (event.getPlugin().equals(EconomyHandler.vault)) {
				log.info("[Stargate] Vault plugin lost.");
			}
		}
	}
	
	private class BlockPopulatorThread implements Runnable {
		public void run() {
			long sTime = System.nanoTime();
			while (System.nanoTime() - sTime < 25000000) {
				BloxPopulator b = Stargate.blockPopulatorQueue.poll();
				if (b == null) return;
				Block blk = b.getBlox().getBlock();
				blk.setType(b.getMat(), false);
				if(b.getMat() == Material.END_GATEWAY && blk.getWorld().getEnvironment() == World.Environment.THE_END) {
					// force a location to prevent exit gateway generation
					EndGateway gateway = (EndGateway) blk.getState();
					gateway.setExitLocation(blk.getWorld().getSpawnLocation());
					gateway.setExactTeleport(true);
					gateway.update(false, false);
				} else if(b.getAxis() != null) {
					Orientable orientable = (Orientable) blk.getBlockData();
					orientable.setAxis(b.getAxis());
					blk.setBlockData(orientable);
				}
			}
		}
	}
	
	private class SGThread implements Runnable {
		public void run() {
			long time = System.currentTimeMillis() / 1000;
			// Close open portals
			for (Iterator<Portal> iter = Stargate.openList.iterator(); iter.hasNext();) {
				Portal p = iter.next();
				// Skip always open gates
				if (p.isAlwaysOn()) continue;
				if (!p.isOpen()) continue;
				if (time > p.getOpenTime() + Stargate.openTime) {
					p.close(false);
					iter.remove();
				}
			}
			// Deactivate active portals
			for (Iterator<Portal> iter = Stargate.activeList.iterator(); iter.hasNext();) {
				Portal p = iter.next();
				if (!p.isActive()) continue;
				if (time > p.getOpenTime() + Stargate.activeTime) {
					p.deactivate();
					iter.remove();
				}
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
				Player p = (Player)sender;
				if (!hasPerm(p, "stargate.admin") && !hasPerm(p, "stargate.admin.reload")) {
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
				if (EconomyHandler.economyEnabled && EconomyHandler.economy == null) {
					if (EconomyHandler.setupEconomy(pm)) {
						if (EconomyHandler.economy != null)
							log.info("[Stargate] Vault v" + EconomyHandler.vault.getDescription().getVersion() + " found");
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
						Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new pmListener());
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
}
