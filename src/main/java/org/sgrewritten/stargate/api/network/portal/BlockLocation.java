package org.sgrewritten.stargate.api.network.portal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * A class used to avoid memory leak from world unloading
 */
public class BlockLocation {

    final int x;
    final int y;
    final int z;
    final String world;

    /**
     * Instantiates a new block location
     *
     * @param location <p>The location this block location should represent</p>
     */
    public BlockLocation(Location location) {
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
        World world = location.getWorld();
        this.world = world == null ? "" : world.getName();
    }

    /**
     * Gets the location corresponding to this block location
     *
     * @return <p>The location corresponding to this block location</p>
     */
    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public int hashCode() {
        int result = 18;

        result = result * 27 + x;
        result = result * 27 + y;
        result = result * 27 + z;
        result = result * 27 + world.hashCode();

        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }

        BlockLocation otherLoc = (BlockLocation) other;
        return (x == otherLoc.x) && (y == otherLoc.y) && (z == otherLoc.z) && (world.equals(otherLoc.world));
    }

    @Override
    public String toString() {
        return world + "(" + x + "," + y + "," + z + ")";
    }

}
