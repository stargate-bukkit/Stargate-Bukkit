package org.sgrewritten.stargate.manager;

import dev.thorinwasher.blockutil.api.BlockUtilAPI;
import org.bukkit.block.Block;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

public class BlockDropManager {
    private static BlockDropManager manager;
    private final BlockUtilAPI provider;

    private BlockDropManager(@Nullable RegisteredServiceProvider<BlockUtilAPI> blockUtilProvider) {
        if(blockUtilProvider == null){
            this.provider = null;
        } else {
            this.provider = blockUtilProvider.getProvider();
        }
    }

    /**
     * @param blockUtilProvider <p>An provider of the block util library plugin</p>
     */
    public static void setProvider(@Nullable RegisteredServiceProvider<BlockUtilAPI> blockUtilProvider) {
        manager = new BlockDropManager(blockUtilProvider);
    }

    /**
     * Disable the block drops of the specified blocks if there is a present block util manager
     * @param block <p>Block to disable blocks from</p>
     */
    public static void disableBlockDrops(Block block){
        if(manager != null && manager.provider != null){
            manager.provider.disableItemDrops(block);
        }
    }
}
