package org.sgrewritten.stargate.manager;

import dev.thorinwasher.blockutil.api.BlockUtilAPI;
import org.bukkit.block.Block;
import org.bukkit.plugin.RegisteredServiceProvider;

public class StargateBlockDropManager {
    private static StargateBlockDropManager manager;
    private final BlockUtilAPI provider;

    private StargateBlockDropManager(RegisteredServiceProvider<BlockUtilAPI> blockUtilProvider) {
        if(blockUtilProvider == null){
            this.provider = null;
        } else {
            this.provider = blockUtilProvider.getProvider();
        }
    }

    public static void setProvider(RegisteredServiceProvider<BlockUtilAPI> blockUtilProvider) {
        manager = new StargateBlockDropManager(blockUtilProvider);
    }

    public static void disableBlockDrops(Block block){
        if(manager != null && manager.provider != null){
            manager.provider.disableItemDrops(block);
        }
    }
}
