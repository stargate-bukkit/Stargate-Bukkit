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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.listeners.BlockEventListener;
import net.TheDgtl.Stargate.listeners.MoveEventListener;
import net.TheDgtl.Stargate.listeners.PlayerEventListener;
import net.TheDgtl.Stargate.listeners.PluginEventListener;
import net.TheDgtl.Stargate.listeners.WorldEventListener;
import net.TheDgtl.Stargate.portal.Network;
import net.TheDgtl.Stargate.portal.Portal;

public class Stargate extends JavaPlugin {
	private static Stargate instance;

	private Level lowestMsgLevel = Level.FINEST;
	
	final String DATAFOLDER = this.getDataFolder().getPath().replaceAll("\\\\", "/");
	final String GATEFOLDER = "gates";
	final String LANGFOLDER =  "lang";
	final String PORTALFOLDER = "portals";

	private PluginManager pm;
	public static LangManager langManager;
	private final int CURRENT_CONFIG_VERSION = 5;
	private EnumMap<Setting, Object> settings;
	private HashMap<String,Portal> bungeeQueue = new HashMap<>();
	public static final SyncronousPopulator syncPopulator = new SyncronousPopulator();
	
	@Override
	public void onEnable() {
		// registers bstats metrics
		int pluginId = 10451;
		new Metrics(this, pluginId);

		instance = this;
		settings = loadConfig();
		
		if ((int) settings.get(Setting.CONFIG_VERSION) != CURRENT_CONFIG_VERSION) {
			// TODO refactoring
		}

		langManager = new LangManager(this, DATAFOLDER + "/" + LANGFOLDER, (String) settings.get(Setting.LANGUAGE));
		saveDefaultGates();

		GateFormat.controlMaterialFormatsMap = GateFormat.loadGateFormats(DATAFOLDER + "/" + GATEFOLDER);

		pm = getServer().getPluginManager();
		registerListeners();
		
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, syncPopulator, 0L, 1L);
	}

	private void registerListeners() {
		pm.registerEvents(new BlockEventListener(),this);
		pm.registerEvents(new MoveEventListener(),this);
		pm.registerEvents(new PlayerEventListener(),this);
		pm.registerEvents(new PluginEventListener(),this);
		pm.registerEvents(new WorldEventListener(),this);
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

	private EnumMap<Setting, Object>  loadConfig() {
		reloadConfig();
		FileConfiguration config = getConfig();
		EnumMap<Setting, Object> settings = new EnumMap<>(Setting.class);
		for(String key : config.getKeys(true)) {
			settings.put(Setting.valueOf(key), config.get(key));
		}
		return settings;
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
		syncPopulator.forceDoAllTasks();
		
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
		return instance.settings.get(setting);
	}
	
	public static void addItemToBungeeQueue(String playerName, String portalName) {
		instance.bungeeQueue.put(playerName, Network.getBungeePortal(portalName));
	}
	
	public static Portal pullBungeeDestination(String playerName) {
		return instance.bungeeQueue.remove(playerName);
	}
}
