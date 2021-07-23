package net.TheDgtl.Stargate.listeners;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.Event;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.gate.GateStructureType;
import net.TheDgtl.Stargate.portal.Network;
import net.TheDgtl.Stargate.portal.Portal;
import net.TheDgtl.Stargate.portal.SGLocation;

public class PlayerEventListener implements Listener {
	private static boolean antiDoubleActivate = true;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (block == null)
			return;
		Action action = event.getAction();
		// TODO material optimisation?
		Portal portal = Network.getPortal(block.getLocation(), GateStructureType.CONTROLL);
		if (portal == null) {
			return;
		}

		Material blockMat = block.getType();
		if ((action == Action.RIGHT_CLICK_BLOCK)) {
			// A cheat to avoid a glitch from bukkit
			if (blockMat == Material.DEAD_TUBE_CORAL_WALL_FAN) {
				antiDoubleActivate = !antiDoubleActivate;
				if (antiDoubleActivate)
					return;
			}
			// Cancel item use
			event.setUseItemInHand(Event.Result.DENY);
			event.setUseInteractedBlock(Event.Result.DENY);
		}
		Player player = event.getPlayer();
		if (Tag.WALL_SIGNS.isTagged(blockMat)) {
			if (portal.isOpenFor(player)) {
				portal.onSignClick(action, player);
			}
			return;
		}
		if (Tag.BUTTONS.isTagged(blockMat) || (blockMat == Material.DEAD_TUBE_CORAL_WALL_FAN)) {
			portal.onButtonClick(player);
			return;
		}

		Stargate.log(Level.WARNING, "This should never be triggered, an unkown glitch is occuring");
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!(boolean) Stargate.getSetting(Setting.USING_BUNGEE))
			return;

		Player player = event.getPlayer();
		Portal destination = Stargate.pullBungeeDestination(player.getName().toLowerCase());

		if (destination == null) {
			return;
		}
		destination.doTeleport(player);
	}
    
}
