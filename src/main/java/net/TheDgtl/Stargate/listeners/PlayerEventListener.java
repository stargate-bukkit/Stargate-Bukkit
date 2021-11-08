package net.TheDgtl.Stargate.listeners;

import java.sql.SQLException;
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

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.event.Event;

import net.TheDgtl.Stargate.Channel;
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.actions.ConditionallRepeatedTask;
import net.TheDgtl.Stargate.actions.PopulatorAction;
import net.TheDgtl.Stargate.gate.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.SGLocation;

public class PlayerEventListener implements Listener {
	private static boolean antiDoubleActivate = true;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (block == null)
			return;
		Action action = event.getAction();
		// TODO material optimisation?
		IPortal portal = Network.getPortal(block.getLocation(), GateStructureType.CONTROLL);
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
		Stargate.log(Level.FINEST, "ping 5");
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

		if(!Stargate.knowsServerName) {
			Stargate.log(Level.FINEST, "First time player join");
			getBungeeServerName();
			updateInterServerPortals();
		}

		Player player = event.getPlayer();
		IPortal destination = Stargate.pullFromQueue(player.getName());

		if (destination == null) {
			return;
		}
		destination.teleportHere(player);
	}
	
	/**
	 * A stupid cheat to get serverName. A client is needed o get this data, hence
	 * this stupid solution
	 * @return
	 */
	private void getBungeeServerName() {
		/*
		 * Action for loading bungee server id
		 */
		PopulatorAction action = new PopulatorAction() {			
			@Override
			public boolean isFinished() {
				return true;
			}

			@Override
			public void run(boolean forceEnd) {
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF(Channel.GET_SERVER.getChannel());
				Bukkit.getServer().sendPluginMessage(Stargate.getPlugin(Stargate.class), Channel.BUNGEE.getChannel(),
						out.toByteArray());
			}
		};

		
		/*
		 * Repeatedly try to load bungee server id until either the id is known, or no player is able to send bungee messages.
		 */
		Stargate.syncSecPopulator.addAction(new ConditionallRepeatedTask(action) {

			@Override
			public boolean isCondition() {
				return (Stargate.knowsServerName) || (1 > Bukkit.getServer().getOnlinePlayers().size());
			}
		});
	}
    
	private void updateInterServerPortals() {
		PopulatorAction action = new PopulatorAction() {

			@Override
			public void run(boolean forceEnd) {
				try {
					Stargate.factory.startInterServerConnection();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			@Override
			public boolean isFinished() {
				return true;
			}
			
		};
		Stargate.syncSecPopulator.addAction(action,true);
		
	}
	
}
