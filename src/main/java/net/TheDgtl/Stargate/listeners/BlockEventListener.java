package net.TheDgtl.Stargate.listeners;

import java.util.EnumSet;
import java.util.logging.Level;

import org.bukkit.Bukkit;
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

import net.TheDgtl.Stargate.Bypass;
import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.actions.PopulatorAction;
import net.TheDgtl.Stargate.event.StargateCreateEvent;
import net.TheDgtl.Stargate.event.StargateDestroyEvent;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.gate.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;

public class BlockEventListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		
		Location loc = event.getBlock().getLocation();
		IPortal portal = Network.getPortal(loc, GateStructureType.FRAME);
		if (portal != null) {
			int cost = Setting.getInteger(Setting.DESTROY_COST);
			StargateDestroyEvent dEvent = new StargateDestroyEvent(portal, event.getPlayer(), cost);
			Bukkit.getPluginManager().callEvent(dEvent);
			PermissionManager permMngr = new PermissionManager(event.getPlayer());
			if (permMngr.hasPerm(dEvent) && !dEvent.isCancelled()) {
				/*
				 * If setting charge free destination is false, destination portal is PortalFlag.Free and portal is of Fixed type
				 * or if player has override cost permission, do not collect money
				 */
				if (!(!Setting.getBoolean(Setting.CHARGE_FREE_DESTINATION) && portal.hasFlag(PortalFlag.FIXED)
						&& ((Portal) portal).loadDestination().hasFlag(PortalFlag.FREE))
						&& !event.getPlayer().hasPermission(Bypass.COST_DESTROY.getPerm())
						&& !Stargate.economyManager.chargePlayer(event.getPlayer(), dEvent.getCost())) {
					event.getPlayer().sendMessage(Stargate.langManager.getMessage(LangMsg.LACKING_FUNDS, true));
					event.setCancelled(true);
					return;
				}

				PopulatorAction action = new PopulatorAction() {

					@Override
					public void run(boolean forceEnd) {
						String msg = Stargate.langManager.getMessage(LangMsg.DESTROY, false);
						event.getPlayer().sendMessage(msg);
						
						portal.destroy();
						Stargate.log(Level.FINEST, "Broke the portal");
					}

					@Override
					public boolean isFinished() {
						return true;
					}
					
				};
				Stargate.syncTickPopulator.addAction(action);
				return;
			}
			event.setCancelled(true);
			return;
		}
		if (Network.getPortal(loc, new GateStructureType[]{GateStructureType.CONTROLL,GateStructureType.IRIS}) != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		Block block = event.getBlock();
		if (!(block.getBlockData() instanceof WallSign))
			return;

		String[] lines = event.getLines();
		String network = lines[2];
		int cost = Setting.getInteger(Setting.CREATION_COST);
		Player player = event.getPlayer();
		EnumSet<PortalFlag> flags = PortalFlag.parseFlags(lines[3]);
		PermissionManager permMngr = new PermissionManager(player);
		
		if (network.isBlank())
			network = Setting.getString(Setting.DEFAULT_NET);
		
		if(network.endsWith("]") && network.startsWith("[")) {
			network = network.substring(1, network.length()-1);
			flags.add(PortalFlag.FANCY_INTERSERVER);
		}
		
		if(flags.contains(PortalFlag.BUNGEE)) {
			network = "§§§§§§#BUNGEE#§§§§§§";
		}
		
		
		if(!permMngr.canCreateInNetwork(network)) {
			Stargate.log(Level.CONFIG, " Player does not have perms to create on current network. Replacing to default...");
			network = Setting.getString(Setting.DEFAULT_NET);
			if(!permMngr.canCreateInNetwork(network)) {
				Stargate.log(Level.CONFIG, " Player does not have perms to create on current network. Replacing to private...");
				network = player.getName();
			}
		}
		
		if(player.getName().equals(network) || flags.contains(PortalFlag.PRIVATE))
			flags.add(PortalFlag.PERSONAL_NETWORK);
		
		flags = permMngr.returnAllowedFlags(flags);
		
		Network selectedNet;
		try {
			selectedNet = selectNetwork(network,flags);
		} catch (NameError e1) {
			player.sendMessage(Stargate.langManager.getMessage(e1.getMsg(), true));
			return;
		}
		

		try {
			IPortal portal = Portal.createPortalFromSign(selectedNet, lines, block, flags);
			StargateCreateEvent sEvent = new StargateCreateEvent(event.getPlayer(),portal,lines,cost);
			
			
			Bukkit.getPluginManager().callEvent(sEvent);
			
			boolean hasPerm = permMngr.hasPerm(sEvent);
			Stargate.log(Level.CONFIG, " player has perm = " + hasPerm);
			if (sEvent.isCancelled() || !hasPerm) {
				Stargate.log(Level.CONFIG, " Event was cancelled due to perm or external cancelation");
				player.sendMessage(Stargate.langManager.getMessage(permMngr.getDenyMsg(), true));
				portal.destroy();
				return;
			}
			if(!(!Setting.getBoolean(Setting.CHARGE_FREE_DESTINATION) && portal.hasFlag(PortalFlag.FIXED)
					&& ((Portal) portal).loadDestination().hasFlag(PortalFlag.FREE))
					&& !event.getPlayer().hasPermission(Bypass.COST_CREATE.getPerm())
					&& !Stargate.economyManager.chargePlayer(player, sEvent.getCost())) {
				player.sendMessage(Stargate.langManager.getMessage(LangMsg.LACKING_FUNDS, true));
				portal.destroy();
				return;
			}
			selectedNet.addPortal(portal,true);
			selectedNet.updatePortals();
			Stargate.log(Level.FINE, "A Gateformat matches");
			player.sendMessage(Stargate.langManager.getMessage(LangMsg.CREATE, false));
		} catch (NoFormatFound e) {
			Stargate.log(Level.FINE, "No Gateformat matches");
		} catch (GateConflict e) {
			player.sendMessage(Stargate.langManager.getMessage(LangMsg.GATE_CONFLICT, true));
		} catch (NameError e) {
			player.sendMessage(Stargate.langManager.getMessage(e.getMsg(), true));
		}
	}
	
	private Network selectNetwork(String name, EnumSet<PortalFlag> flags) throws NameError {
		try {
			if(flags.contains(PortalFlag.PERSONAL_NETWORK))
				name = Bukkit.getPlayer(name).getUniqueId().toString();
			Stargate.factory.createNetwork(name, flags);
		} catch (NameError e1) {
			LangMsg msg= e1.getMsg();
			if(msg != null) {
				throw e1;
			}
		}
		return Stargate.factory.getNetwork(name, flags.contains(PortalFlag.FANCY_INTERSERVER));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		// check if portal is affected, if so cancel
		if(Network.isInPortal(event.getBlocks(), GateStructureType.values()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		// check if portal is affected, if so cancel
		if(Network.isInPortal(event.getBlocks(), GateStructureType.values()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		if(Network.isInPortal(event.blockList(), GateStructureType.values()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockFromTo(BlockFromToEvent event) {
		// check if water or lava is flowing into a gate entrance?
		// if so, cancel
		Block to = event.getToBlock();
		Block from = event.getBlock();
		if ((Network.getPortal(to.getLocation(), GateStructureType.IRIS) != null)
				|| (Network.getPortal(from.getLocation(), GateStructureType.IRIS) != null))
			event.setCancelled(true);
	}
}
