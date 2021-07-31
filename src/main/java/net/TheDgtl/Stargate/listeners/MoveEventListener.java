package net.TheDgtl.Stargate.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.jetbrains.annotations.NotNull;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.gate.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.IPortal;

public class MoveEventListener implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
		// cancel portal and endgateway teleportation if it's from a Stargate entrance
		PlayerTeleportEvent.TeleportCause cause = event.getCause();

		/*
		 * A refactor of the legacy version, I don't know the exact purpose of the
		 * end_gateway logic, but it's there now, This is done to avoid players from
		 * teleporting in the vanilla way:
		 * 
		 * Check if the cause is one of the critical scenarios, if not the case return.
		 */
		switch (cause) {
		case END_GATEWAY:
			if ((World.Environment.THE_END == event.getFrom().getWorld().getEnvironment())) {
				break;
			}
			return;
		case NETHER_PORTAL:
			break;
		default:
			return;
		}
		if (Network.isNextToPortal(event.getFrom(), GateStructureType.IRIS)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(@NotNull PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		// Check if player really moved
		if (to == null || from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
				&& from.getBlockZ() == to.getBlockZ()) {
			return;
		}
		IPortal portal = Network.getPortal(to, GateStructureType.IRIS);

		if (portal == null || !portal.isOpen())
			return;
		Player player = event.getPlayer();
		if (!portal.isOpenFor(player)) {
			// TODO send deny message
			portal.teleportHere(player);
			return;
		}
		// TODO check perm's
		
		portal.doTeleport(player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onVehicleMove(VehicleMoveEvent event) {
		// same thing as for player
		// low priority implementation
	}
}
