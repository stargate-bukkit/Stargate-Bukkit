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
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.Event;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.portal.GateStructure;
import net.TheDgtl.Stargate.portal.Network;
import net.TheDgtl.Stargate.portal.Network.Portal;
import net.TheDgtl.Stargate.portal.SGLocation;

public class PlayerInteractEventListener implements Listener {
	private static boolean antiDoubleActivate = true;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (block == null)
			return;
		Action action = event.getAction();
		// TODO material optimisation?
		Portal portal = Network.getPortal(new SGLocation(block.getLocation()), GateStructure.Type.CONTROLL);
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
				portal.scrollDesti(action, player);
			}
			return;
		}
		if (Tag.BUTTONS.isTagged(blockMat) || (blockMat == Material.DEAD_TUBE_CORAL_WALL_FAN)) {
			openPortal(portal, player);
			return;
		}

		Stargate.log(Level.WARNING, "This should never be triggered, an unkown glitch is occuring");
	}

	private void openPortal(Portal portal, Player player) {
		if (portal.getDestination() == null) {
			// TODO write message?
			return;
		}
		// TODO checkPerms
		portal.open(player);
		portal.getDestination().open(player);
	}
}
