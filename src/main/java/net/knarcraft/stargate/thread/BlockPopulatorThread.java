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
            BloxPopulator b = Stargate.blockPopulatorQueue.poll();
            if (b == null) return;
            Block blk = b.getBlockLocation().getBlock();
            blk.setType(b.getMat(), false);
            if (b.getMat() == Material.END_GATEWAY && blk.getWorld().getEnvironment() == World.Environment.THE_END) {
                // force a location to prevent exit gateway generation
                EndGateway gateway = (EndGateway) blk.getState();
                gateway.setExitLocation(blk.getWorld().getSpawnLocation());
                gateway.setExactTeleport(true);
                gateway.update(false, false);
            } else if (b.getAxis() != null) {
                Orientable orientable = (Orientable) blk.getBlockData();
                orientable.setAxis(b.getAxis());
                blk.setBlockData(orientable);
            }
        }
    }
}
