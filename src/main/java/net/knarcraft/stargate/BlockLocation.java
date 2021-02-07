package net.knarcraft.stargate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;

/*
 * stargate - A portal plugin for Bukkit
 * Copyright (C) 2011 Shaun (sturmeh)
 * Copyright (C) 2011 Dinnerbone
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 * Copyright (C) 2021 Kristian Knarvik
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class represents a block location
 */
public class BlockLocation {

    private final int x;
    private final int y;
    private final int z;
    private final World world;
    private BlockLocation parent = null;

    /**
     * Creates a new block
     * @param world <p>The world the block exists in</p>
     * @param x <p>The x coordinate of the block</p>
     * @param y <p>The y coordinate of the block</p>
     * @param z <p>The z coordinate of the block</p>
     */
    public BlockLocation(World world, int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    /**
     * Copies a craftbukkit block
     * @param block <p>The block to </p>
     */
    public BlockLocation(Block block) {
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.world = block.getWorld();
    }

    /**
     * Creates a new block from a location
     * @param location <p>The location the block exists in</p>
     */
    public BlockLocation(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld();
    }

    /**
     * Gets a block from a string
     * @param world <p>The world the block exists in</p>
     * @param string <p>A comma separated list of z, y and z coordinates as integers</p>
     */
    public BlockLocation(World world, String string) {
        String[] split = string.split(",");
        this.x = Integer.parseInt(split[0]);
        this.y = Integer.parseInt(split[1]);
        this.z = Integer.parseInt(split[2]);
        this.world = world;
    }

    /**
     * Makes a new block in a relative position to this block
     * @param x <p>The x position relative to this block's position</p>
     * @param y <p>The y position relative to this block's position</p>
     * @param z <p>The z position relative to this block's position</p>
     * @return <p>A new block </p>
     */
    public BlockLocation makeRelative(int x, int y, int z) {
        return new BlockLocation(this.world, this.x + x, this.y + y, this.z + z);
    }

    public Location makeRelativeLoc(double x, double y, double z, float rotX, float rotY) {
        return new Location(this.world, (double) this.x + x, (double) this.y + y, (double) this.z + z, rotX, rotY);
    }

    public BlockLocation modRelative(int right, int depth, int distance, int modX, int modY, int modZ) {
        return makeRelative(-right * modX + distance * modZ, -depth * modY, -right * modZ + -distance * modX);
    }

    public Location modRelativeLoc(double right, double depth, double distance, float rotX, float rotY, int modX, int modY, int modZ) {
        return makeRelativeLoc(0.5 + -right * modX + distance * modZ, depth, 0.5 + -right * modZ + -distance * modX, rotX, 0);
    }

    public void setType(Material type) {
        world.getBlockAt(x, y, z).setType(type);
    }

    public Material getType() {
        return world.getBlockAt(x, y, z).getType();
    }

    public Block getBlock() {
        return world.getBlockAt(x, y, z);
    }

    public Location getLocation() {
        return new Location(world, x, y, z);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return world;
    }

    public Block getParent() {
        if (parent == null) findParent();
        if (parent == null) return null;
        return parent.getBlock();
    }

    private void findParent() {
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;

        BlockData blk = getBlock().getBlockData();
        if (blk instanceof WallSign) {
            BlockFace facing = ((WallSign) blk).getFacing().getOppositeFace();
            offsetX = facing.getModX();
            offsetZ = facing.getModZ();
        } else if (blk instanceof Sign) {
            offsetY = -1;
        } else {
            return;
        }
        parent = new BlockLocation(world, getX() + offsetX, getY() + offsetY, getZ() + offsetZ);
    }

    @Override
    public String toString() {
        return String.valueOf(x) + ',' + y + ',' + z;
    }

    @Override
    public int hashCode() {
        int result = 18;

        result = result * 27 + x;
        result = result * 27 + y;
        result = result * 27 + z;
        result = result * 27 + world.getName().hashCode();

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        BlockLocation blockLocation = (BlockLocation) obj;
        return (x == blockLocation.x) && (y == blockLocation.y) && (z == blockLocation.z) && (world.getName().equals(blockLocation.world.getName()));
    }

}