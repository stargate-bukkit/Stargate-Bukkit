package net.knarcraft.stargate.container;

import net.knarcraft.stargate.utility.DirectionHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Sign;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents a block location
 *
 * <p>The BlockLocation class is basically a Location with some extra functionality.
 * Warning: Because of differences in the equals methods between Location and BlockLocation, a BlockLocation which
 * equals another BlockLocation does not necessarily equal the same BlockLocation if treated as a Location.</p>
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
    public BlockLocation(@NotNull World world, int x, int y, int z) {
        super(world, x, y, z);
    }

    /**
     * Creates a block location from a block
     *
     * @param block <p>The block to get the location of</p>
     */
    public BlockLocation(@NotNull Block block) {
        super(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    /**
     * Gets a block location from a string
     *
     * @param world  <p>The world the block exists in</p>
     * @param string <p>A comma separated list of x, y and z coordinates as integers</p>
     */
    public BlockLocation(@NotNull World world, @NotNull String string) {
        super(world, Integer.parseInt(string.split(",")[0]), Integer.parseInt(string.split(",")[1]),
                Integer.parseInt(string.split(",")[2]));
    }

    /**
     * Creates a new block location in a relative position to this block location
     *
     * @param x <p>The number of blocks to move in the x-direction</p>
     * @param y <p>The number of blocks to move in the y-direction</p>
     * @param z <p>The number of blocks to move in the z-direction</p>
     * @return <p>A new block location</p>
     */
    @NotNull
    public BlockLocation makeRelativeBlockLocation(int x, int y, int z) {
        return (BlockLocation) this.clone().add(x, y, z);
    }

    /**
     * Creates a location in a relative position to this block location
     *
     * @param x   <p>The number of blocks to move in the x-direction</p>
     * @param y   <p>The number of blocks to move in the y-direction</p>
     * @param z   <p>The z position relative to this block's position</p>
     * @param yaw <p>The number of blocks to move in the z-direction</p>
     * @return <p>A new location</p>
     */
    @NotNull
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
     * @param yaw            <p>The yaw pointing outwards from a portal (in the relative vector's out direction)</p>
     * @return <p>A location relative to this location</p>
     */
    @NotNull
    public BlockLocation getRelativeLocation(@NotNull RelativeBlockVector relativeVector, double yaw) {
        Vector realVector = DirectionHelper.getCoordinateVectorFromRelativeVector(relativeVector.right(),
                relativeVector.down(), relativeVector.out(), yaw);
        return makeRelativeBlockLocation(realVector.getBlockX(), realVector.getBlockY(), realVector.getBlockZ());
    }

    /**
     * Makes a location relative to the current location according to given parameters
     *
     * <p>Out goes in the direction of the yaw. Right goes in the direction of (yaw - 90) degrees.
     * Depth goes downwards following the -y direction.</p>
     *
     * @param right     <p>The amount of blocks to go right when looking towards a portal</p>
     * @param down      <p>The amount of blocks to go downwards when looking towards a portal</p>
     * @param out       <p>The amount of blocks to go outwards when looking towards a portal</p>
     * @param portalYaw <p>The yaw when looking out from the portal</p>
     * @return A new location relative to this block location
     */
    @NotNull
    public Location getRelativeLocation(double right, double down, double out, float portalYaw) {
        Vector realVector = DirectionHelper.getCoordinateVectorFromRelativeVector(right, down, out, portalYaw);
        return makeRelativeLocation(0.5 + realVector.getBlockX(), realVector.getBlockY(),
                0.5 + realVector.getBlockZ(), portalYaw);
    }

    /**
     * Gets the type of block at this block location
     *
     * @return <p>The block's material type</p>
     */
    @NotNull
    public Material getType() {
        return this.getBlock().getType();
    }

    /**
     * Sets the type of block at this location
     *
     * @param type <p>The block's new material type</p>
     */
    public void setType(@NotNull Material type) {
        this.getBlock().setType(type);
    }

    /**
     * Gets the location representing this block location
     *
     * @return <p>The location representing this block location</p>
     */
    @NotNull
    public Location getLocation() {
        return this.clone();
    }

    /**
     * Gets this block location's parent block
     *
     * <p>The parent block is the block the item at this block location is attached to. Usually this is the block a
     * sign or wall sign is attached to.</p>
     *
     * @return <p>This block location's parent block</p>
     */
    @Nullable
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
        if (blockData instanceof Directional) {
            //Get the offset of the block "behind" this block
            BlockFace facing = ((Directional) blockData).getFacing().getOppositeFace();
            offsetX = facing.getModX();
            offsetZ = facing.getModZ();
        } else if (blockData instanceof Sign) {
            //Get offset the block beneath the sign
            offsetY = -1;
        } else {
            return;
        }
        parent = this.makeRelativeBlockLocation(offsetX, offsetY, offsetZ);
    }

    @Override
    @NotNull
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
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        BlockLocation blockLocation = (BlockLocation) object;

        World thisWorld = this.getWorld();
        World otherWorld = blockLocation.getWorld();
        //Check if the worlds of the two locations match
        boolean worldsEqual = (thisWorld == null && otherWorld == null) || ((thisWorld != null && otherWorld != null)
                && thisWorld == otherWorld);

        //As this is a block location, only the block coordinates are compared
        return blockLocation.getBlockX() == this.getBlockX() && blockLocation.getBlockY() == this.getBlockY() &&
                blockLocation.getBlockZ() == this.getBlockZ() && worldsEqual;
    }

}