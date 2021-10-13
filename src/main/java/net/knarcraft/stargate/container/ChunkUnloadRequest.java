package net.knarcraft.stargate.container;

import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a requests for the unloading of a chunk which has been previously loaded by the Stargate plugin
 */
public class ChunkUnloadRequest implements Comparable<ChunkUnloadRequest> {

    private final Long unloadNanoTime;
    private final Chunk chunkToUnload;

    /**
     * Instantiates a new chunk unloading request
     *
     * @param chunkToUnload   <p>The chunk to request the unloading of</p>
     * @param timeUntilUnload <p>The time in milliseconds to wait before unloading the chunk</p>
     */
    public ChunkUnloadRequest(Chunk chunkToUnload, Long timeUntilUnload) {
        this.chunkToUnload = chunkToUnload;
        long systemNanoTime = System.nanoTime();
        this.unloadNanoTime = systemNanoTime + (timeUntilUnload * 1000000);
    }

    /**
     * Gets the chunk to unload
     *
     * @return <p>The chunk to unload</p>
     */
    public Chunk getChunkToUnload() {
        return this.chunkToUnload;
    }

    /**
     * Gets the system nano time denoting at which time the unload request should be executed
     *
     * @return <p>The system nano time denoting when the chunk is to be unloaded</p>
     */
    public Long getUnloadNanoTime() {
        return this.unloadNanoTime;
    }

    @Override
    public String toString() {
        return "{" + chunkToUnload + ", " + unloadNanoTime + "}";
    }

    @Override
    public int compareTo(@NotNull ChunkUnloadRequest otherRequest) {
        //Prioritize requests based on time until unload
        return unloadNanoTime.compareTo(otherRequest.unloadNanoTime);
    }

}
