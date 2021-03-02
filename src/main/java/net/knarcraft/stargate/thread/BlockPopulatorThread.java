package net.knarcraft.stargate.thread;

import net.knarcraft.stargate.BloxPopulator;
import net.knarcraft.stargate.Stargate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.Orientable;

/**
 * This thread changes gate blocks to display a gate as open or closed
 *
 * <p>This thread fetches some entries from blockPopulatorQueue each time it's called.</p>
 */
public class BlockPopulatorThread implements Runnable {

    @Override
    public void run() {
        long sTime = System.nanoTime();
        //Repeat for at most 0.025 seconds
        while (System.nanoTime() - sTime < 25000000) {
            //Abort if there's no work to be done
            BloxPopulator bloxPopulator = Stargate.blockPopulatorQueue.poll();
            if (bloxPopulator == null) {
                return;
            }

            //Change the material of the pulled block
            Block block = bloxPopulator.getBlockLocation().getBlock();
            block.setType(bloxPopulator.getMaterial(), false);

            if (bloxPopulator.getMaterial() == Material.END_GATEWAY &&
                    block.getWorld().getEnvironment() == World.Environment.THE_END) {
                //Force a specific location to prevent exit gateway generation
                EndGateway gateway = (EndGateway) block.getState();
                gateway.setExitLocation(block.getLocation());
                gateway.setExactTeleport(true);
                gateway.update(false, false);
            } else if (bloxPopulator.getAxis() != null) {
                //If orientation is relevant, adjust the block's orientation
                Orientable orientable = (Orientable) block.getBlockData();
                orientable.setAxis(bloxPopulator.getAxis());
                block.setBlockData(orientable);
            }
        }
    }

}
