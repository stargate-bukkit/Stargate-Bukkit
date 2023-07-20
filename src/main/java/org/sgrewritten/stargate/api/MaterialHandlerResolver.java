package org.sgrewritten.stargate.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.BlockHandlerInterface;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.structure.GateStructureType;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.network.portal.BlockLocation;
import org.sgrewritten.stargate.network.portal.PortalPosition;

import java.util.*;

public class MaterialHandlerResolver {
    private final Map<Material,List<BlockHandlerInterface>> blockHandlerMap = new HashMap<>();
    private final Map<BlockLocation,BlockHandlerInterface> blockBlockHandlerMap = new HashMap<>();
    private final RegistryAPI registry;
    private final StorageAPI storageAPI;

    public MaterialHandlerResolver(@NotNull RegistryAPI registry, @NotNull StorageAPI storageAPI){
        this.registry = Objects.requireNonNull(registry);
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
    public void registerPlacement(Location location, List<RealPortal> portals, Material material, Player player) {
        if(!blockHandlerMap.containsKey(material)){
            return;
        }
        for(RealPortal portal : portals) {
            for(BlockHandlerInterface blockHandlerInterface : blockHandlerMap.get(material)){
                if(portal.hasFlag(blockHandlerInterface.getFlag()) && blockHandlerInterface.registerPlacedBlock(location,player,portal)){
                    BlockVector relativeVector = portal.getGate().getRelativeVector(location).toBlockVector();
                    PortalPosition portalPosition = new PortalPosition(blockHandlerInterface.getInterfaceType(),relativeVector);
                    portal.getGate().addPortalPosition(portalPosition);
                    blockBlockHandlerMap.put(new BlockLocation(location),blockHandlerInterface);
                    registry.registerLocation(GateStructureType.CONTROL_BLOCK,new BlockLocation(location),portal);
                    try {
                        storageAPI.addPortalPosition(portal,portal.getStorageType(),portalPosition);
                    } catch (StorageWriteException e) {
                        Stargate.log(e);
                        return;
                    }
                    return;
                }
            }
        }

    }

    /**
     *
     * @param location The location of the block that is being removed
     * @param portal The portal to try removal on
     * @param material The material of the block that is being removed
     * @param player The player that removed the block
     */
    public void registerRemoval(Location location, RealPortal portal,  Material material, Player player) {
        if(!blockHandlerMap.containsKey(material)){
            return;
        }
        BlockHandlerInterface blockHandlerInterface = this.blockBlockHandlerMap.get(new BlockLocation(location));
        if(blockHandlerInterface == null){
            return;
        }
        blockHandlerInterface.unRegisterPlacedBlock(location,player,portal);
        portal.getGate().removePortalPosition(location);
        registry.unRegisterLocation(GateStructureType.CONTROL_BLOCK,new BlockLocation(location));
    }

    /**
     * Method used for performance
     * @param material The material
     * @return Whether there exists a BlockHandlerInterface that
     * has registed for the material
     */
    public boolean hasRegisteredBlockHandler(Material material) {
        return blockHandlerMap.containsKey(material);
    }
}
