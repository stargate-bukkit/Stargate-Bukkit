package net.knarcraft.stargate.thread;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockChangeRequest;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.Orientable;

/**
 * This thread changes gate blocks to display a gate as open or closed
 *
 * <p>This thread fetches some entries from blockPopulateQueue each time it's called.</p>
 */
public class BlockChangeThread implements Runnable {

    @Override
    public void run() {
        long sTime = System.nanoTime();
        //Repeat for at most 0.025 seconds
        while (System.nanoTime() - sTime < 25000000) {
            pollQueue();
        }
    }

    /**
     * Polls the block change request queue for any waiting requests
     */
    public static void pollQueue() {
        //Abort if there's no work to be done
        BlockChangeRequest blockChangeRequest = Stargate.getBlockChangeRequestQueue().poll();
        if (blockChangeRequest == null) {
            return;
        }

        //Change the material of the pulled block
        Block block = blockChangeRequest.getBlockLocation().getBlock();
        block.setType(blockChangeRequest.getMaterial(), false);

        if (blockChangeRequest.getMaterial() == Material.END_GATEWAY &&
                block.getWorld().getEnvironment() == World.Environment.THE_END) {
            //Force a specific location to prevent exit gateway generation
            fixEndGatewayGate(block);
        } else if (blockChangeRequest.getAxis() != null) {
            //If orientation is relevant, adjust the block's orientation
            orientBlock(block, blockChangeRequest.getAxis());
        }
    }

    /**
     * Prevents end gateway portal from behaving strangely
     *
     * @param block <p>The block to fix</p>
     */
    private static void fixEndGatewayGate(Block block) {
        EndGateway gateway = (EndGateway) block.getState();
        gateway.setExitLocation(block.getLocation());
        gateway.setExactTeleport(true);
        gateway.update(false, false);
    }

    /**
     * Sets the orientation axis of the placed block
     *
     * @param block <p>The block to orient</p>
     * @param axis  <p>The axis to use for orienting the block</p>
     */
    private static void orientBlock(Block block, Axis axis) {
        Orientable orientable = (Orientable) block.getBlockData();
        orientable.setAxis(axis);
        block.setBlockData(orientable);
    }

}
