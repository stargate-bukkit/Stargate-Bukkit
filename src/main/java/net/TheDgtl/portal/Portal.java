
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
package net.TheDgtl.portal;

public class Portal {
	/**
	 * Behaviours:
	 * - Cycle through PortalStates, make current state listener for movements
	 * - (Listener) blockevents; listen for control input
	 * - (Listener) listen for portalOpen events within the network
	 * - (Static) check if layout is valid, return a layout with relevant info 
	 * about validity, rotation, location and portaltype
	 * - (Constructor) Write sign and do various logic that has not been done with 
	 * layout already
	 * 
	 * Added behaviours
	 * - (Listener) Listen for stargate clock (maybe 1 tick per minute or something)
	 * maybe follow an external script that gives when the states should change
	 */
}
