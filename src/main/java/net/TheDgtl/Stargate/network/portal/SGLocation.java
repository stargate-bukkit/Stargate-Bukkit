package net.TheDgtl.Stargate.network.portal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SGLocation {
    /**
     * A class used to avoid memory leak from world unloading
     */
    final int x;
    final int y;
    final int z;
    final String world;

    public SGLocation(Location location) {
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
        World worldObject = location.getWorld();
        world = worldObject == null ? "" : worldObject.getName();
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }


    /*
     * USED IN HASHING RELATED STUFF
     */
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
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (getClass() != other.getClass())
            return false;

        SGLocation otherLoc = (SGLocation) other;
        return (x == otherLoc.x) && (y == otherLoc.y) && (z == otherLoc.z) && (world.equals(otherLoc.world));
    }

    @Override
    public String toString() {
        return world + "(" + x + "," + y + "," + z + ")";
    }
}
