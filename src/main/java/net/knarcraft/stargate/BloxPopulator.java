package net.knarcraft.stargate;

import org.bukkit.Axis;
import org.bukkit.Material;

public class BloxPopulator {

    private BlockLocation blockLocation;
    private Material nextMat;
    private Axis nextAxis;

    public BloxPopulator(BlockLocation b, Material m) {
        blockLocation = b;
        nextMat = m;
        nextAxis = null;
    }

    public BloxPopulator(BlockLocation b, Material m, Axis a) {
        blockLocation = b;
        nextMat = m;
        nextAxis = a;
    }

    public void setBlockLocation(BlockLocation b) {
        blockLocation = b;
    }

    public void setMat(Material m) {
        nextMat = m;
    }

    public void setAxis(Axis a) {
        nextAxis = a;
    }

    public BlockLocation getBlockLocation() {
        return blockLocation;
    }

    public Material getMat() {
        return nextMat;
    }

    public Axis getAxis() {
        return nextAxis;
    }

}
