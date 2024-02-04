package org.sgrewritten.stargate.api.network.portal;

import com.bergerkiller.bukkit.common.bases.IntVector2;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class StargateChunk {

    private final int x;
    private final int z;
    private final String worldName;

    public StargateChunk(Chunk chunk){
        this(chunk.getX(),chunk.getZ(), chunk.getWorld());
    }

    public StargateChunk(int x, int z, World world){
        this(x,z,world.getName());
    }

    public StargateChunk(int x, int z, String worldName){
        this.x = x;
        this.z = z;
        this.worldName = worldName;
    }

    public StargateChunk(IntVector2 coordinate, World world){
        this(coordinate.x, coordinate.z, world);
    }

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
