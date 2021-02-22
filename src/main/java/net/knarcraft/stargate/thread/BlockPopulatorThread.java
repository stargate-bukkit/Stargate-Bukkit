package net.knarcraft.stargate.thread;

import net.knarcraft.stargate.BloxPopulator;
import net.knarcraft.stargate.Stargate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.Orientable;

public class BlockPopulatorThread implements Runnable {
    public void run() {
        long sTime = System.nanoTime();
        while (System.nanoTime() - sTime < 25000000) {
            BloxPopulator bloxPopulator = Stargate.blockPopulatorQueue.poll();
            if (bloxPopulator == null) {
                return;
            }
            Block block = bloxPopulator.getBlockLocation().getBlock();
            block.setType(bloxPopulator.getMaterial(), false);
            if (bloxPopulator.getMaterial() == Material.END_GATEWAY && block.getWorld().getEnvironment() == World.Environment.THE_END) {
                // force a location to prevent exit gateway generation
                EndGateway gateway = (EndGateway) block.getState();
                gateway.setExitLocation(block.getWorld().getSpawnLocation());
                gateway.setExactTeleport(true);
                gateway.update(false, false);
            } else if (bloxPopulator.getAxis() != null) {
                Orientable orientable = (Orientable) block.getBlockData();
                orientable.setAxis(bloxPopulator.getAxis());
                block.setBlockData(orientable);
            }
        }
    }
}
