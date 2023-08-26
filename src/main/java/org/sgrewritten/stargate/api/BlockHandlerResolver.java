package org.sgrewritten.stargate.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.network.portal.BlockLocation;

import java.util.*;

public class BlockHandlerResolver {
    private final Map<Material,List<BlockHandlerInterface>> blockHandlerMap = new HashMap<>();
    private final Map<BlockLocation,BlockHandlerInterface> blockBlockHandlerMap = new HashMap<>();
    private final StorageAPI storageAPI;

    public BlockHandlerResolver(@NotNull StorageAPI storageAPI){
        this.storageAPI = Objects.requireNonNull(storageAPI);
    }

    /**
     * Add a listener for block placement next by a portal
     *
     * @param blockHandlerInterface A listener for block placement next by a portal
     */
    public void addBlockHandlerInterface(BlockHandlerInterface blockHandlerInterface) {
        List<BlockHandlerInterface> blockHandlerInterfaceList = this.blockHandlerMap.computeIfAbsent(blockHandlerInterface.getHandledMaterial(), k -> new ArrayList<>());
        blockHandlerInterfaceList.add(blockHandlerInterface);
        blockHandlerInterfaceList.sort(Comparator.comparingInt((ablockHandlerInterface) -> -ablockHandlerInterface.getPriority().getPriorityValue()));
    }

    /**
     * Remove a listener for block placement next by a portal
     *
     * @param blockHandlerInterface listener for block placement next by a portal
     */
    public void removeBlockHandlerInterface(BlockHandlerInterface blockHandlerInterface) {
        for(Material key : this.blockHandlerMap.keySet()){
            List<BlockHandlerInterface> blockHandlerInterfaceList = this.blockHandlerMap.get(key);
            if(blockHandlerInterfaceList.remove(blockHandlerInterface)){
                return;
            }
        }
    }

    /**
     * Remove all listeners for block placement next by a portal
     * @param plugin The plugin to remove listeners from
     */
    public void removeBlockHandlerInterfaces(Plugin plugin) {
        for(Material key : this.blockHandlerMap.keySet()){
            List<BlockHandlerInterface> blockHandlerInterfaceList = this.blockHandlerMap.get(key);
            blockHandlerInterfaceList.removeIf(blockHandlerInterface -> blockHandlerInterface.getPlugin() == plugin);
        }
    }

    /**
     *
     * @param location The location of the block that is being placed
     * @param portals The portal to try registration on
     * @param material The material of the block
     * @param player The player that placed the block
     */
    public void registerPlacement(RegistryAPI registry, Location location, List<RealPortal> portals, Material material, Player player) {
        if(!blockHandlerMap.containsKey(material)){
            return;
        }
        for(RealPortal portal : portals) {
            for(BlockHandlerInterface blockHandlerInterface : blockHandlerMap.get(material)){
                if(portal.hasFlag(blockHandlerInterface.getFlag()) && blockHandlerInterface.registerBlock(location,player,portal)){
                    registry.savePortalPosition(portal,location,blockHandlerInterface.getInterfaceType(),blockHandlerInterface.getPlugin());
                    blockBlockHandlerMap.put(new BlockLocation(location),blockHandlerInterface);
                }
            }
        }

    }

    /**
     *
     * @param location The location of the block that is being removed
     * @param portal The portal to try removal on
     */
    public void registerRemoval(RegistryAPI registry, Location location, RealPortal portal) {
        BlockHandlerInterface blockHandlerInterface = this.blockBlockHandlerMap.get(new BlockLocation(location));
        if(blockHandlerInterface == null){
            return;
        }
        blockHandlerInterface.unRegisterBlock(location,portal);
        registry.removePortalPosition(location);
    }

    /**
     * Method used for performance
     * @param material The material
     * @return Whether there exists a BlockHandlerInterface that
     * has registered for the material
     */
    public boolean hasRegisteredBlockHandler(Material material) {
        return blockHandlerMap.containsKey(material);
    }
}
