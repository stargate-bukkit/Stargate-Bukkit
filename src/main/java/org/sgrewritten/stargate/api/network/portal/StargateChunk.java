package org.sgrewritten.stargate.api.network.portal;

import com.bergerkiller.bukkit.common.bases.IntVector2;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class StargateChunk {

    private final int x;
    private final int z;
    private final String worldName;

    /**
     * @param chunk <p>The chunk to represent</p>
     */
    public StargateChunk(Chunk chunk){
        this(chunk.getX(),chunk.getZ(), chunk.getWorld());
    }

    /**
     * @param x <p>X coordinate of chunk to represent</p>
     * @param z <p>Z coordinate of chunk to represent</p>
     * @param world <p>World of the chunk to represent</p>
     */
    public StargateChunk(int x, int z, World world){
        this(x,z,world.getName());
    }

    /**
     * @param x <p>X coordinate of chunk to represent</p>
     * @param z <p>Z coordinate of chunk to represent</p>
     * @param worldName <p>The name of the world for the chunk to represent</p>
     */
    public StargateChunk(int x, int z, String worldName){
        this.x = x;
        this.z = z;
        this.worldName = worldName;
    }

    /**
     * @param coordinate <p>Coordinate of chunk to represent</p>
     * @param world <p>World of chunk to represent</p>
     */
    public StargateChunk(IntVector2 coordinate, World world){
        this(coordinate.x, coordinate.z, world);
    }

    /**
     * @return <p>The chunk this represents</p>
     */
    public Chunk getChunk(){
        return Bukkit.getWorld(worldName).getChunkAt(x,z);
    }

    @Override
    public boolean equals(Object other){
        if(other == this){
            return true;
        }
        if(!(other instanceof StargateChunk otherChunk)){
            return false;
        }
        return otherChunk.x == this.x && otherChunk.z == this.z && otherChunk.worldName.equals(this.worldName);
    }

    @Override
    public int hashCode(){
        int result = 18;
        result = result * 27 + x;
        result = result * 27 + z;
        result = result * 27 + worldName.hashCode();
        return result;
    }
}
