package net.TheDgtl.Stargate.listeners;

import java.util.logging.Level;

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
import net.TheDgtl.Stargate.portal.Gate.GateConflict;
import net.TheDgtl.Stargate.portal.GateStructure;
import net.TheDgtl.Stargate.portal.Network;
import net.TheDgtl.Stargate.portal.Network.Portal;
import net.TheDgtl.Stargate.portal.SGLocation;

public class BlockEventListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		// TODO Have a list of all possible portalMaterials and skip in not any of those
		SGLocation loc = new SGLocation(event.getBlock().getLocation());
		Portal portal = Network.getPortal(loc, GateStructure.Type.FRAME);
		if (portal != null) {
			// TODO check perms. If allowed, destroy portal
			String msg = Stargate.langManager.getString("destroyMsg");
			event.getPlayer().sendMessage(msg);
			portal.destroy();
		}
		GateStructure.Type[] targetStructures = { GateStructure.Type.CONTROLL, GateStructure.Type.IRIS };
		portal = Network.getPortal(loc, targetStructures);
		if (portal != null) {
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
		network = permMngr.getAllowedNetwork(network, PermissionManager.CREATEPERMISSION);
		if (network == null) {
			player.sendMessage(Stargate.langManager.getString(permMngr.getDenyMsg()));
		}
		if (network.isBlank())
			network = Network.DEFAULTNET;
		if (!(Network.networkList.containsKey(network))) {
			Network.networkList.put(network, new Network(network));
		}
		Network selectedNet = Network.networkList.get(network);
		try {
			if (lines[1].isBlank())
				selectedNet.new NetworkedPortal(block, lines);
			else
				selectedNet.new FixedPortal(block, lines);
			Stargate.log(Level.FINE, "A Gateformat matches");
		} catch (Portal.NoFormatFound e) {
			Stargate.log(Level.FINE, "No Gateformat matches");
		} catch (GateConflict e) {
			player.sendMessage(Stargate.langManager.getString("createConflict"));
		} catch (Portal.NameError e) {
			switch (e.getMessage()) {
			case "empty":
				break;
			case "taken":
				player.sendMessage(Stargate.langManager.getString("createExists"));
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
