package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.property.BlockEventType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Cancellable;

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
     */
    public static void onAnyBlockChangeEvent(Cancellable event, BlockEventType type, Location location) {
        Portal portal = Stargate.getRegistryStatic().getPortal(location);
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
    public static void onAnyMultiBlockChangeEvent(Cancellable event, BlockEventType type, List<Block> blocks) {
        onAnyMultiBlockChangeEvent(event, type.canDestroyPortal(), blocks);
    }

    /**
     * Does event handling for any event that changes multiple block
     *
     * @param event  <p> The event to possibly cancel </p>
     * @param type   <p> If the event could destroy a portal </p>
     * @param blocks <p> The blocks affected </p>
     */
    public static void onAnyMultiBlockChangeEvent(Cancellable event, boolean canDestroy, List<Block> blocks) {
        Set<Portal> affectedPortals = new HashSet<>();

        for (Block block : blocks) {
            Portal portal = Stargate.getRegistryStatic().getPortal(block.getLocation());
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
