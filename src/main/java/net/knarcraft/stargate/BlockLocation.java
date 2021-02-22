package net.knarcraft.stargate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;

/**
 * This class represents a block location
 *
 * <p>The BlockLocation class is basically a Location with some extra functionality.
 * Warning: Because of differences in the equals methods between Location and BlockLocation, a BlockLocation which
 * equals another BlockLocation does not necessarily equal the name BlockLocation if treated as a Location.</p>
 */
public class BlockLocation extends Location {

    private BlockLocation parent = null;

    /**
     * Creates a new block location
     *
     * @param world <p>The world the block exists in</p>
     * @param x     <p>The x coordinate of the block</p>
     * @param y     <p>The y coordinate of the block</p>
     * @param z     <p>The z coordinate of the block</p>
     */
    public BlockLocation(World world, int x, int y, int z) {
        super(world, x, y, z);
    }

    /**
     * Copies a craftbukkit block
     *
     * @param block <p>The block to </p>
     */
    public BlockLocation(Block block) {
        super(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    /**
     * Gets a block from a string
     *
     * @param world  <p>The world the block exists in</p>
     * @param string <p>A comma separated list of z, y and z coordinates as integers</p>
     */
    public BlockLocation(World world, String string) {
        super(world, Integer.parseInt(string.split(",")[0]), Integer.parseInt(string.split(",")[1]),
                Integer.parseInt(string.split(",")[2]));
    }

    /**
     * Makes a new block in a relative position to this block
     *
     * @param x <p>The x position relative to this block's position</p>
     * @param y <p>The y position relative to this block's position</p>
     * @param z <p>The z position relative to this block's position</p>
     * @return <p>A new block location</p>
     */
    public BlockLocation makeRelative(int x, int y, int z) {
        return (BlockLocation) this.clone().add(x, y, z);
    }

    /**
     * Makes a location relative to the block location
     *
     * @param x    <p>The x position relative to this block's position</p>
     * @param y    <p>The y position relative to this block's position</p>
     * @param z    <p>The z position relative to this block's position</p>
     * @param rotX <p>The x rotation of the location</p>
     * @param rotY <p>The y rotation of the location</p>
     * @return <p>A new location</p>
     */
    public Location makeRelativeLoc(double x, double y, double z, float rotX, float rotY) {
        Location newLocation = this.clone();
        newLocation.setYaw(rotX);
        newLocation.setPitch(rotY);
        return newLocation.add(x, y, z);
    }

    /**
     * Makes a block location relative to the current origin according to given parameters
     *
     * <p>See {@link RelativeBlockVector} to understand better. modX or modZ should always be 0 while the other is 1
     * or -1.</p>
     *
     * @param right    <p>The amount of right steps from the top-left origin</p>
     * @param depth    <p>The amount of downward steps from the top-left origin</p>
     * @param distance <p>The distance outward from the top-left origin</p>
     * @param modX     <p>X modifier. If modX = -1, X will increase as right increases</p>
     * @param modY     <p>Y modifier. modY = 1 for Y decreasing as depth increases</p>
     * @param modZ     <p>Z modifier. If modZ = 1, X will increase as distance increases</p>
     * @return A new location relative to this block location
     */
    public BlockLocation modRelative(int right, int depth, int distance, int modX, int modY, int modZ) {
        return makeRelative(-right * modX + distance * modZ, -depth * modY, -right * modZ + -distance * modX);
    }

    /**
     * Makes a location relative to the current location according to given parameters
     *
     * @param right    <p></p>
     * @param depth    <p>The y position relative to the current position</p>
     * @param distance <p>The distance away from the previous location to the new location</p>
     * @param rotX     <p>The yaw of the location</p>
     * @param rotY     <p>Unused</p>
     * @param modX     <p>x modifier. Defines movement along the x-axis. 0 for no movement</p>
     * @param modY     <p>Unused</p>
     * @param modZ     <p>z modifier. Defines movement along the z-axis. 0 for no movement</p>
     * @return A new location relative to this block location
     */
    public Location modRelativeLoc(double right, double depth, double distance, float rotX, float rotY, int modX, int modY, int modZ) {
        return makeRelativeLoc(0.5 + -right * modX + distance * modZ, depth, 0.5 + -right * modZ + -distance * modX, rotX, 0);
    }

    /**
     * Gets the type for the block at this location
     *
     * @return <p>The block material type</p>
     */
    public Material getType() {
        return this.getBlock().getType();
    }

    /**
     * Sets the type for the block at this location
     *
     * @param type <p>The new block material type</p>
     */
    public void setType(Material type) {
        this.getBlock().setType(type);
    }

    /**
     * Gets the location representing this block location
     *
     * @return <p>The location representing this block location</p>
     */
    public Location getLocation() {
        return this.clone();
    }

    /**
     * Gets this block location's parent block
     *
     * @return <p>This block location's parent block</p>
     */
    public Block getParent() {
        if (parent == null) {
            findParent();
        }
        if (parent == null) {
            return null;
        }
        return parent.getBlock();
    }

    /**
     * Tries to find the parent block location
     *
     * <p>If this block location is a sign, the parent is the block location of the block the sign is connected to.</p>
     */
    private void findParent() {
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;

        BlockData blockData = getBlock().getBlockData();
        if (blockData instanceof WallSign) {
            BlockFace facing = ((WallSign) blockData).getFacing().getOppositeFace();
            offsetX = facing.getModX();
            offsetZ = facing.getModZ();
        } else if (blockData instanceof Sign) {
            offsetY = -1;
        } else {
            return;
        }
        parent = this.makeRelative(offsetX, offsetY, offsetZ);
    }

    @Override
    public String toString() {
        return String.valueOf(this.getBlockX()) + ',' + this.getBlockY() + ',' + this.getBlockZ();
    }

    @Override
    public int hashCode() {
        int result = 18;

        result = result * 27 + this.getBlockX();
        result = result * 27 + this.getBlockY();
        result = result * 27 + this.getBlockZ();
        if (this.getWorld() != null) {
            result = result * 27 + this.getWorld().getName().hashCode();
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        BlockLocation blockLocation = (BlockLocation) obj;

        World thisWorld = this.getWorld();
        World otherWorld = blockLocation.getWorld();
        boolean worldsEqual = (thisWorld == null && otherWorld == null) || ((thisWorld != null && otherWorld != null)
                && thisWorld == otherWorld);

        return blockLocation.getBlockX() == this.getBlockX() && blockLocation.getBlockY() == this.getBlockY() &&
                blockLocation.getBlockZ() == this.getBlockZ() && worldsEqual;
    }

}