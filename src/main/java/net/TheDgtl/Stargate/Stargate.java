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

import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class Stargate extends JavaPlugin {
	private static Stargate instance;

	private Level lowestMsgLevel = Level.INFO;

	@Override
	public void onEnable() {
		// registers bstats metrics
		int pluginId = 10451;
		new Metrics(this, pluginId);

		instance = this;
	}

	private void loadConfig() {
	};

	@Override
	public void onLoad() {
		// economyHandler = new EconomyHandler(this);
	}

	@Override
	public void onDisable() {
		/*
		 * Portal.closeAllGates(this); Portal.clearGates(); managedWorlds.clear();
		 * getServer().getScheduler().cancelTasks(this);
		 */
	}

	public static void log(String msg, Level priorityLevel) {
		if (instance.lowestMsgLevel.intValue() <= priorityLevel.intValue()
				&& priorityLevel.intValue() < Level.INFO.intValue()) {
			instance.getLogger().log(Level.INFO, msg);
			return;
		}
		instance.getLogger().log(priorityLevel, msg);
	}
}
