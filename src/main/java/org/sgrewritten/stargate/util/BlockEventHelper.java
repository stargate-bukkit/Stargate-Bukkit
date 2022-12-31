package org.sgrewritten.stargate.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Cancellable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.property.BlockEventType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class BlockEventHelper {
    
    /**
     * Does event handling for any event that changes one block
     *
     * @param event    <p> The event to possibly cancel </p>
     * @param type     <p> The type of event </p>
     * @param location <p> The location of the block </p>
     * @param registry <p> The registry in use </p>
     */
    public static void onAnyBlockChangeEvent(Cancellable event, BlockEventType type, Location location, RegistryAPI registry) {
        onAnyBlockChangeEvent(event,type,location,registry,()->{});
    }
    
    
    /**
     * Does event handling for any event that changes one block
     *
     * @param event    <p> The event to possibly cancel </p>
     * @param type     <p> The type of event </p>
     * @param location <p> The location of the block </p>
     * @param registry <p> The registry in use </p>
     * @param destroyAction <p> What to do if the portal was destroyed </p>
     */
    public static void onAnyBlockChangeEvent(Cancellable event, BlockEventType type, Location location, RegistryAPI registry, Runnable destroyAction) {
        Portal portal = registry.getPortal(location);
        if (portal == null) {
            return;
        }
        if (type.canDestroyPortal()) {
            portal.destroy();
        } else {
            event.setCancelled(true);
        }
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
     * @param event  <p> The event to possibly cancel </p>
     * @param type   <p> The type of event </p>
     * @param blocks <p> The blocks affected </p>
     */
    public static void onAnyMultiBlockChangeEvent(Cancellable event, BlockEventType type, List<Block> blocks, RegistryAPI registry) {
        onAnyMultiBlockChangeEvent(event, type.canDestroyPortal(), blocks, registry);
    }

    /**
     * Does event handling for any event that changes multiple block
     *
     * @param event      <p>The event to possibly cancel</p>
     * @param canDestroy <p>If the event could destroy a portal</p>
     * @param blocks     <p>The blocks affected</p>
     */
    public static void onAnyMultiBlockChangeEvent(Cancellable event, boolean canDestroy, List<Block> blocks, RegistryAPI registry) {
        Set<Portal> affectedPortals = new HashSet<>();

        for (Block block : blocks) {
            Portal portal = registry.getPortal(block.getLocation());
            if (portal != null) {
                if (!canDestroy) {
                    event.setCancelled(true);
                    return;
                }
                affectedPortals.add(portal);
            }
        }

        for (Portal portal : affectedPortals) {
            portal.destroy();
            Stargate.log(Level.FINER, String.format("Broke portal %s in network %s from a multiple block change event",
                    portal.getName(), portal.getNetwork().getName()));
        }
    }
}
