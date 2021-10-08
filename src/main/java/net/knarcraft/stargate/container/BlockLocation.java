package net.knarcraft.stargate.container;

import net.knarcraft.stargate.utility.DirectionHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.util.Vector;

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
    public BlockLocation makeRelativeBlockLocation(int x, int y, int z) {
        return (BlockLocation) this.clone().add(x, y, z);
    }

    /**
     * Makes a location relative to the block location
     *
     * @param x   <p>The x position relative to this block's position</p>
     * @param y   <p>The y position relative to this block's position</p>
     * @param z   <p>The z position relative to this block's position</p>
     * @param yaw <p>The yaw of the location</p>
     * @return <p>A new location</p>
     */
    public Location makeRelativeLocation(double x, double y, double z, float yaw) {
        Location newLocation = this.clone();
        newLocation.setYaw(yaw);
        newLocation.setPitch(0);
        return newLocation.add(x, y, z);
    }

    /**
     * Gets a location relative to this block location
     *
     * @param relativeVector <p>The relative block vector describing the relative location</p>
     * @param yaw            <p>The yaw pointing in the distance direction</p>
     * @return <p>A location relative to this location</p>
     */
    public BlockLocation getRelativeLocation(RelativeBlockVector relativeVector, double yaw) {
        Vector realVector = DirectionHelper.getCoordinateVectorFromRelativeVector(relativeVector.getRight(),
                relativeVector.getDepth(), relativeVector.getDistance(), yaw);
        return makeRelativeBlockLocation(realVector.getBlockX(), realVector.getBlockY(), realVector.getBlockZ());
    }

    /**
     * Makes a location relative to the current location according to given parameters
     *
     * @param right     <p>The amount of blocks to go right when looking the opposite direction from the yaw</p>
     * @param depth     <p>The amount of blocks to go downwards when looking the opposite direction from the yaw</p>
     * @param distance  <p>The amount of blocks to go outwards when looking the opposite direction from the yaw</p>
     * @param portalYaw <p>The yaw when looking out from the portal</p>
     * @return A new location relative to this block location
     */
    public Location getRelativeLocation(double right, double depth, double distance, float portalYaw) {
        Vector realVector = DirectionHelper.getCoordinateVectorFromRelativeVector(right, depth, distance, portalYaw);
        return makeRelativeLocation(0.5 + realVector.getBlockX(), realVector.getBlockY(),
                0.5 + realVector.getBlockZ(), portalYaw);
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
        parent = this.makeRelativeBlockLocation(offsetX, offsetY, offsetZ);
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