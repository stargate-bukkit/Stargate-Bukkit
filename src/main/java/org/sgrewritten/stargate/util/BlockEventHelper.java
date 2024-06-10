package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Cancellable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.portal.StargateDestroyPortalEvent;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.property.BlockEventType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class BlockEventHelper {

    private BlockEventHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Does event handling for any event that changes one block
     *
     * @param event       <p> The event to possibly cancel </p>
     * @param type        <p> The type of event </p>
     * @param location    <p> The location of the block </p>
     * @param stargateAPI <p> The stargate api </p>
     */
    public static boolean onAnyBlockChangeEvent(Cancellable event, BlockEventType type, Location location, StargateAPI stargateAPI) {
        RealPortal portal = stargateAPI.getRegistry().getPortal(location);
        if (portal == null) {
            return false;
        }
        if (type.canDestroyPortal()) {
            StargateDestroyPortalEvent stargateDestroyPortalEvent = new StargateDestroyPortalEvent(portal, type);
            Bukkit.getPluginManager().callEvent(stargateDestroyPortalEvent);
            if (!stargateDestroyPortalEvent.isCancelled()) {
                stargateAPI.getNetworkManager().destroyPortal(portal);
                return true;
            }
        }
        event.setCancelled(true);
        return false;
    }

    /**
     * Convert a list of block states into a list of blocks
     *
     * @param blockStates <p> A list of blockStates </p>
     * @return <p> A list of blocks </p>
     */
    public static List<Block> getBlockList(List<BlockState> blockStates) {
        List<Block> blocks = new ArrayList<>();
        for (BlockState state : blockStates) {
            blocks.add(state.getBlock());
        }
        return blocks;
    }

    /**
     * Does event handling for any event that changes multiple block
     *
     * @param event  <p>The event to possibly cancel</p>
     * @param type   <p>The type of event</p>
     * @param blocks <p>The blocks affected</p>
     */
    public static void onAnyMultiBlockChangeEvent(Cancellable event, BlockEventType type, List<Block> blocks, StargateAPI stargateAPI) {
        Set<RealPortal> affectedPortals = new HashSet<>();
        boolean canDestroy = type.canDestroyPortal();
        if (type == BlockEventType.BLOCK_EXPLODE || type == BlockEventType.ENTITY_EXPLODE) {
            canDestroy = ConfigurationHelper.getBoolean(ConfigurationOption.DESTROY_ON_EXPLOSION);
        }
        for (Block block : blocks) {
            RealPortal portal = stargateAPI.getRegistry().getPortal(block.getLocation());
            if (portal != null) {
                if (!canDestroy) {
                    event.setCancelled(true);
                    return;
                }
                affectedPortals.add(portal);
            }
        }

        for (RealPortal portal : affectedPortals) {
            stargateAPI.getNetworkManager().destroyPortal(portal);
            Stargate.log(Level.FINER, String.format("Broke portal %s in network %s from a multiple block change event",
                    portal.getName(), portal.getNetwork().getName()));
        }
    }
}
