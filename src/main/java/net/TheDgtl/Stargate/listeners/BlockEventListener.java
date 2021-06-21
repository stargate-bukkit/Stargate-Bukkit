package net.TheDgtl.Stargate.listeners;

import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
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

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.portal.Network;
import net.TheDgtl.Stargate.portal.Network.Portal.NoFormatFound;

public class BlockEventListener implements Listener{
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		 //Check if it's a portalblock
		 //check perms. If allowed, destroy portal 
    }
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		 //Check if a portal control block is selected
		 //If so, cancel event if not shift clicking
		/*
		if(event.getBlockAgainst() != controllBlock)
			return;
		if(event.getPlayer().isSneaking())
			return;
		event.setCancelled(true);
		*/
    }
	
	Network central = new Network();
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		Block block = event.getBlock();
		if(!(block instanceof WallSign))
			return;
		
		try {
			Network.Portal portal = central.new Portal(block, new String[1]);
			Stargate.log(Level.INFO, "A Gateformat matches");
		} catch (NoFormatFound e) {
			Stargate.log(Level.INFO, "No Gateformat matches");
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        // check if portal is affected, if so cancel
		
    }
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
		// check if portal is affected, if so cancel
		
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		// check if portal is affected, if so cancel
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
		// check if water or lava is flowing into a gate entrance?
		// if so, cancel
		
	}
}
