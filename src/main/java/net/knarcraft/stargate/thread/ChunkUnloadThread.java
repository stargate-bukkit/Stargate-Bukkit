package net.knarcraft.stargate.thread;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.ChunkUnloadRequest;
import org.bukkit.Chunk;

import java.util.Queue;

/**
 * Unloads chunks which should no longer be forced to stay loaded
 */
public class ChunkUnloadThread implements Runnable {

    @Override
    public void run() {
        long systemNanoTime = System.nanoTime();
        Queue<ChunkUnloadRequest> unloadQueue = Stargate.getChunkUnloadQueue();

        //Peek at the first element to check if the chunk should be unloaded
        ChunkUnloadRequest firstElement = unloadQueue.peek();
        if (firstElement != null) {
            Stargate.debug("ChunkUnloadThread", "Found chunk unload request: " + firstElement);
            Stargate.debug("ChunkUnloadThread", "Current time: " + systemNanoTime);
        }
        //Repeat until all un-loadable chunks have been processed
        while (firstElement != null && firstElement.getUnloadNanoTime() < systemNanoTime) {
            unloadQueue.remove();
            Chunk chunkToUnload = firstElement.getChunkToUnload();
            //Allow the chunk to be unloaded
            chunkToUnload.removePluginChunkTicket(Stargate.stargate);
            Stargate.debug("ChunkUnloadThread", "Unloaded chunk " + chunkToUnload);
            firstElement = unloadQueue.peek();
        }
    }

}
