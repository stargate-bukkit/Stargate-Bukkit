package net.TheDgtl.Stargate.listeners;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.event.StargateDestroyEvent;
import net.TheDgtl.Stargate.gate.GateStructure;
import net.TheDgtl.Stargate.gate.Gate.GateConflict;
import net.TheDgtl.Stargate.portal.FixedPortal;
import net.TheDgtl.Stargate.portal.Network;
import net.TheDgtl.Stargate.portal.NetworkedPortal;
import net.TheDgtl.Stargate.portal.Portal;

public class BlockEventListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		// TODO Have a list of all possible portalMaterials and skip if not any of those
		Location loc = event.getBlock().getLocation();
		Portal portal = Network.getPortal(loc, GateStructure.Type.FRAME);
		if (portal != null) {
			int cost = 0; // TODO economy manager
			StargateDestroyEvent dEvent = new StargateDestroyEvent(portal, event.getPlayer(), false, "", cost);
			PermissionManager permMngr = new PermissionManager(event.getPlayer());
			if (permMngr.hasPerm(dEvent)) {

				String msg = Stargate.langManager.getMessage("destroyMsg", false);
				event.getPlayer().sendMessage(msg);
				portal.destroy();
				return;
			}

			String reason = Stargate.langManager.getMessage(permMngr.getDenyMsg(), true);
			event.getPlayer().sendMessage(reason);
			event.setCancelled(true);
			return;
		}
		if (Network.getPortal(loc, GateStructure.Type.CONTROLL) != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		// Check if a portal control block is selected
		// If so, cancel event if not shift clicking
		if(event.getPlayer().isSneaking())
			return;
		
		if(Network.getPortal(event.getBlockAgainst().getLocation(), GateStructure.Type.CONTROLL) == null)
			return;
		
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		Block block = event.getBlock();
		if (!(block.getBlockData() instanceof WallSign))
			return;

		String[] lines = event.getLines();
		String network = lines[2];
		Player player = event.getPlayer();
		PermissionManager permMngr = new PermissionManager(player);

		if (network.isBlank())
			network = Network.DEFAULTNET;
		boolean hasPerm = true;
		if (!permMngr.canCreateInNetwork(network)) {
			if (!permMngr.canCreateInNetwork(player.getName())) {
				hasPerm = false;
			}
			network = player.getName();
		}

		if (!(Network.networkList.containsKey(network))) {
			Network.networkList.put(network, new Network(network));
		}
		Network selectedNet = Network.networkList.get(network);
		try {
			Portal portal;
			if (lines[1].isBlank())
				portal = new NetworkedPortal(selectedNet, block, lines);
			else
				portal = new FixedPortal(selectedNet, block, lines);

			if (!hasPerm) {
				player.sendMessage(Stargate.langManager.getMessage(permMngr.getDenyMsg(), true));
				portal.destroy();
				return;
			}
			Stargate.log(Level.FINE, "A Gateformat matches");
			player.sendMessage(Stargate.langManager.getMessage("createMsg", false));
		} catch (Portal.NoFormatFound e) {
			Stargate.log(Level.FINE, "No Gateformat matches");
		} catch (GateConflict e) {
			player.sendMessage(Stargate.langManager.getMessage("createConflict", true));
		} catch (Portal.NameError e) {
			switch (e.getMessage()) {
			case "empty":
				break;
			case "taken":
				player.sendMessage(Stargate.langManager.getMessage("createExists", true));
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		// check if portal is affected, if so cancel
		if(Network.isInPortal(event.getBlocks(), GateStructure.Type.values()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		// check if portal is affected, if so cancel
		if(Network.isInPortal(event.getBlocks(), GateStructure.Type.values()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		if(Network.isInPortal(event.blockList(), GateStructure.Type.values()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockFromTo(BlockFromToEvent event) {
		// check if water or lava is flowing into a gate entrance?
		// if so, cancel
		Block to = event.getToBlock();
		Block from = event.getBlock();
		if ((Network.getPortal(to.getLocation(), GateStructure.Type.IRIS) != null)
				|| (Network.getPortal(from.getLocation(), GateStructure.Type.IRIS) != null))
			event.setCancelled(true);
	}
}
