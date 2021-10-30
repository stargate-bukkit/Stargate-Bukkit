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
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.listeners.BlockEventListener;
import net.TheDgtl.Stargate.listeners.MoveEventListener;
import net.TheDgtl.Stargate.listeners.PlayerEventListener;
import net.TheDgtl.Stargate.listeners.PluginEventListener;
import net.TheDgtl.Stargate.listeners.StargateBungeePluginMessageListener;
import net.TheDgtl.Stargate.listeners.WorldEventListener;
import net.TheDgtl.Stargate.network.InterserverNetwork;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.StargateFactory;
import net.TheDgtl.Stargate.network.portal.IPortal;

public class Stargate extends JavaPlugin {
	private static Stargate instance;

	private Level lowestMsgLevel = Level.FINEST;//setting before config loads
	
	final String DATAFOLDER = this.getDataFolder().getPath().replaceAll("\\\\", "/");
	final String GATEFOLDER = "gates";
	final String LANGFOLDER =  "lang";
	final String PORTALFOLDER = "portals";
	
	private PluginManager pm;
	
	public static StargateFactory factory;
	public static LangManager langManager;
	private final int CURRENT_CONFIG_VERSION = 5;
	/**
	 * Goes through every action in the queue every 1 tick. Should be used in tasks that need to be finished within a short time frame
	 */
	public static final SyncronousPopulator syncTickPopulator = new SyncronousPopulator();
	/**
	 * Goes through every action it the queue every 1 second (20 ticks). Should be used in delayed actions
	 */
	public static final SyncronousPopulator syncSecPopulator = new SyncronousPopulator();
	/**
	 *  The string length of a name consisting of only 'i'. This will fill a sign (including <>)
	 *  Note that this is a terribly relaxed restriction, mainly done to avoid any weird issues for
	 *  in a SQL database
	 */
	public static final int MAX_TEXT_LENGTH = 40; 
	
	
	/*
	 * used in bungee / waterfall
	 */
	private HashMap<String,IPortal> bungeeQueue = new HashMap<>();
	public static String serverName; 
	public static boolean knowsServerName = false;
	@Override
	public void onEnable() {
		// registers bstats metrics
		int pluginId = 10451;
		new Metrics(this, pluginId);

		instance = this;
		loadConfig();
		
		
		
		lowestMsgLevel = Level.parse((String) getSetting(Setting.DEBUG_LEVEL));
		langManager = new LangManager(this, DATAFOLDER + "/" + LANGFOLDER, (String) getSetting(Setting.LANGUAGE));
		saveDefaultGates();

		GateFormat.controlMaterialFormatsMap = GateFormat.loadGateFormats(DATAFOLDER + "/" + GATEFOLDER);
		try {
			factory = new StargateFactory(this);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pm = getServer().getPluginManager();
		registerListeners();
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, syncTickPopulator, 0L, 1L);
		scheduler.scheduleSyncRepeatingTask(this, syncSecPopulator, 0L, 20L);
	}

	private void registerListeners() {
		pm.registerEvents(new BlockEventListener(),this);
		pm.registerEvents(new MoveEventListener(),this);
		pm.registerEvents(new PlayerEventListener(),this);
		pm.registerEvents(new PluginEventListener(),this);
		pm.registerEvents(new WorldEventListener(),this);
		if ((boolean) getSetting(Setting.USING_BUNGEE)) {
			Messenger msgr = Bukkit.getMessenger();

			msgr.registerOutgoingPluginChannel(this, Channel.BUNGEE.getChannel());
			msgr.registerIncomingPluginChannel(this, Channel.BUNGEE.getChannel(), new StargateBungeePluginMessageListener());
		}
	}

	private void saveDefaultGates() {
		//TODO is there a way to check all files in a resourcefolder? Possible solution seems unnecessarily complex
		String[] gateList = {"nether.gate", "water.gate", "wool.gate"};
		boolean replace = false;
		for(String gateName : gateList) {
			if (!(new File(DATAFOLDER+"/"+GATEFOLDER+"/"+gateName).exists()))
				this.saveResource(GATEFOLDER+"/" + gateName, replace);
		}
	}

	private void loadConfig() {
		saveDefaultConfig();
		reloadConfig();
		if ((int) getSetting(Setting.CONFIG_VERSION) != CURRENT_CONFIG_VERSION) {
			// TODO refactoring
		}
	}
	
	@Override
	public void onLoad() {
		// economyHandler = new EconomyHandler(this);
	}

	@Override
	public void onDisable() {
		/*
		 * Portal.closeAllGates(this); Portal.clearGates(); managedWorlds.clear();
		 * 
		 */
		syncTickPopulator.forceDoAllTasks();
		syncSecPopulator.forceDoAllTasks();
		if((boolean)getSetting(Setting.USING_BUNGEE)) {
			Messenger msgr = Bukkit.getMessenger();
			msgr.unregisterOutgoingPluginChannel(this);
			msgr.unregisterIncomingPluginChannel(this);
		}
		getServer().getScheduler().cancelTasks(this);
	}

	public static void log(Level priorityLevel, String msg) {
		if (instance.lowestMsgLevel.intValue() <= priorityLevel.intValue()
				&& priorityLevel.intValue() < Level.INFO.intValue()) {
			instance.getLogger().log(Level.INFO, msg);
			return;
		}
		instance.getLogger().log(priorityLevel, msg);
	}
	
	public static Object getSetting(Setting setting) {
		return instance.getConfig().get(setting.getKey());
	}
	
	public static void addToQueue(String playerName, String portalName, String netName) {
    	Network net = factory.getNetwork(netName, true);
    	if(net == null) {
    		//do some error thing
    	}
    	IPortal portal = net.getPortal(portalName);
    	if(portal == null) {
    		//same here
    	}
    	
    	instance.bungeeQueue.put(playerName, portal);
    }
    
    public static IPortal pullFromQueue(String playerName) {
    	return instance.bungeeQueue.remove(playerName);
    }
}
