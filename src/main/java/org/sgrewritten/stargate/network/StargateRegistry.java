package org.sgrewritten.stargate.network;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.BlockHandlerResolver;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkRegistry;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.StargateChunk;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.property.StargateConstant;
import org.sgrewritten.stargate.thread.task.StargateQueuedAsyncTask;
import org.sgrewritten.stargate.util.ExceptionHelper;
import org.sgrewritten.stargate.util.VectorUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Registry of all portals and networks
 *
 * @author Thorin (idea from EpicKnarvik)
 */
public class StargateRegistry implements RegistryAPI {

    private final StorageAPI storageAPI;
    private final BlockHandlerResolver blockHandlerResolver;
    private final NetworkRegistry networkRegistry = new StargateNetworkRegistry();
    private final NetworkRegistry bungeeNetworkRegistry = new StargateNetworkRegistry();
    private final Map<GateStructureType, Map<BlockLocation, RealPortal>> portalFromStructureTypeMap = new EnumMap<>(GateStructureType.class);
    private final Map<BlockLocation, PortalPosition> portalPositionMap = new HashMap<>();
    private final Map<String, Map<BlockLocation, PortalPosition>> portalPositionPluginNameMap = new HashMap<>();
    private final Map<StargateChunk, Set<RealPortal>> chunkPortalMap = new HashMap<>();

    /**
     * Instantiates a new Stargate registry
     *
     * @param storageAPI <p>The database API to use for interfacing with the database</p>
     */
    public StargateRegistry(StorageAPI storageAPI, BlockHandlerResolver blockHandlerResolver) {
        this.storageAPI = storageAPI;
        this.blockHandlerResolver = blockHandlerResolver;
    }

    @Override
    public void unregisterPortal(Portal portal) {
        if (!(portal instanceof RealPortal realPortal)) {
            return;
        }
        for (GateStructureType formatType : GateStructureType.values()) {
            for (BlockLocation loc : realPortal.getGate().getLocations(formatType)) {
                Stargate.log(Level.FINEST, "Unregistering type: " + formatType + " location, at: " + loc);
                this.unRegisterLocation(formatType, loc);
            }
        }
        GateAPI gate = realPortal.getGate();
        List<PortalPosition> portalPositions = gate.getPortalPositions();
        for (PortalPosition portalPosition : portalPositions) {
            Location location = gate.getLocation(portalPosition.getRelativePositionLocation());
            if (!portalPosition.getPluginName().equals("Stargate")) {
                Stargate.log(Level.FINEST, "Unregistering non-Stargate portal position on location " + location.toString());
                blockHandlerResolver.registerRemoval(this, location, realPortal);
            }
            Stargate.log(Level.FINEST, "Unregistering portal position on location " + location.toString());
            this.removePortalPosition(location);
        }
        Set<StargateChunk> chunks = getPortalChunks(realPortal);
        chunks.forEach(chunk -> this.unregisterPortalChunk(chunk, realPortal));
    }

    private void unregisterPortalChunk(StargateChunk chunk, RealPortal realPortal) {
        Set<RealPortal> portals = chunkPortalMap.get(chunk);
        if (portals == null) {
            return;
        }
        if (portals.isEmpty()) {
            chunkPortalMap.remove(chunk);
            return;
        }
        portals.remove(realPortal);
    }

    @Override
    public void registerPortal(@NotNull RealPortal portal) {
        GateAPI gate = portal.getGate();
        for (GateStructureType key : GateStructureType.values()) {
            List<BlockLocation> locations = gate.getLocations(key);
            if (locations == null) {
                continue;
            }
            this.registerLocations(key, generateLocationMap(locations, portal));
        }
        for (PortalPosition portalPosition : gate.getPortalPositions()) {
            Location location = gate.getLocation(portalPosition.getRelativePositionLocation());
            this.registerPortalPosition(portalPosition, location, portal);
        }
        Set<StargateChunk> chunks = getPortalChunks(portal);
        chunks.forEach(chunk -> registerPortalChunk(chunk, portal));
    }

    private void registerPortalChunk(StargateChunk chunk, RealPortal portal) {
        chunkPortalMap.putIfAbsent(chunk, new HashSet<>());
        chunkPortalMap.get(chunk).add(portal);
    }

    @Override
    public boolean networkExists(String networkName, StorageType storageType) {
        return getNetworkRegistry(storageType).networkExists(networkName);
    }

    @Override
    public NetworkRegistry getNetworkRegistry(StorageType storageType) {
        return (storageType == StorageType.LOCAL ? networkRegistry : bungeeNetworkRegistry);
    }

    @Override
    public @Nullable Network getNetwork(String id, StorageType storageType) {
        return getNetworkRegistry(storageType).getNetwork(id);
    }

    /**
     * Gets a map between the given block locations and the given portal
     *
     * @param locations <p>The locations related to the portal</p>
     * @param portal    <p>The portal with blocks at the given locations</p>
     * @return <p>The resulting location to portal mapping</p>
     */
    private Map<BlockLocation, RealPortal> generateLocationMap(List<BlockLocation> locations, RealPortal portal) {
        Map<BlockLocation, RealPortal> output = new HashMap<>();
        for (BlockLocation location : locations) {
            output.put(location, portal);
        }
        return output;
    }

    @Override
    public void updateAllPortals() {
        bungeeNetworkRegistry.updatePortals();
        networkRegistry.updatePortals();
    }

    @Override
    public RealPortal getPortal(BlockLocation blockLocation, GateStructureType structureType) {
        if (!(portalFromStructureTypeMap.containsKey(structureType))) {
            return null;
        }
        return portalFromStructureTypeMap.get(structureType).get(blockLocation);
    }

    @Override
    public RealPortal getPortal(BlockLocation blockLocation, GateStructureType[] structureTypes) {
        for (GateStructureType key : structureTypes) {
            RealPortal portal = getPortal(blockLocation, key);
            if (portal != null) {
                return portal;
            }
        }
        return null;
    }

    @Override
    public RealPortal getPortal(Location location, GateStructureType structureType) {
        return getPortal(new BlockLocation(location), structureType);
    }

    @Override
    public RealPortal getPortal(Location location, GateStructureType[] structureTypes) {
        return getPortal(new BlockLocation(location), structureTypes);
    }

    @Override
    public RealPortal getPortal(Location location) {
        PortalPosition portalPosition = this.getPortalPosition(location);
        if (portalPosition != null) {
            return portalPosition.getPortal();
        }
        return getPortal(location, GateStructureType.values());
    }


    @Override
    public boolean isPartOfPortal(List<Block> blocks) {
        for (Block block : blocks) {
            if (getPortal(block.getLocation()) != null) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isNextToPortal(Location location, GateStructureType structureType) {
        return !getPortalsFromTouchingBlock(location, structureType).isEmpty();
    }

    @Override
    public List<RealPortal> getPortalsFromTouchingBlock(Location location, GateStructureType structureType) {
        List<RealPortal> portals = new ArrayList<>();
        for (BlockVector adjacentVector : VectorUtils.getAdjacentRelativePositions()) {
            Location adjacentLocation = location.clone().add(adjacentVector);
            RealPortal portal = getPortal(adjacentLocation, structureType);
            if (portal != null) {
                portals.add(portal);
            }
        }
        return portals;
    }


    @Override
    public void registerLocations(GateStructureType structureType, Map<BlockLocation, RealPortal> locationsMap) {
        portalFromStructureTypeMap.putIfAbsent(structureType, new HashMap<>());
        portalFromStructureTypeMap.get(structureType).putAll(locationsMap);
    }

    @Override
    public void registerLocation(GateStructureType structureType, BlockLocation location, RealPortal portal) {
        portalFromStructureTypeMap.putIfAbsent(structureType, new HashMap<>());
        portalFromStructureTypeMap.get(structureType).put(location, portal);
    }

    @Override
    public void unRegisterLocation(GateStructureType structureType, BlockLocation blockLocation) {
        Map<BlockLocation, RealPortal> map = portalFromStructureTypeMap.get(structureType);
        if (map != null && map.get(blockLocation) != null) {
            Stargate.log(Level.FINER, "Unregistering portal " + map.get(blockLocation).getName() +
                    " with structType " + structureType + " at location " + blockLocation.toString());
            map.remove(blockLocation);
        }
    }

    /**
     * Clear this registry
     */
    public void clear() {
        portalFromStructureTypeMap.clear();
        portalPositionMap.clear();
        portalPositionPluginNameMap.clear();
        portalFromStructureTypeMap.clear();
        networkRegistry.clear();
        bungeeNetworkRegistry.clear();
    }

    @Override
    public String getValidNewName(Network network) throws InvalidNameException {
        //noinspection ResultOfMethodCallIgnored
        if (ExceptionHelper.doesNotThrow(IllegalArgumentException.class, () -> UUID.fromString(network.getId()))) {
            throw new InvalidNameException("Can not rename the network as it's name is an UUID.");
        }
        String newName = network.getId();
        int i = 1;
        try {
            while (networkExists(newName, network.getStorageType()) || storageAPI.netWorkExists(newName, network.getStorageType())) {
                newName = network.getId() + i;
                i++;
            }
            if (newName.length() < StargateConstant.MAX_TEXT_LENGTH) {
                return newName;
            }
            String annoyinglyOverThoughtName = network.getId();
            int n = 1;
            while (networkExists(annoyinglyOverThoughtName, network.getStorageType()) || storageAPI.netWorkExists(newName, network.getStorageType())) {
                String number = String.valueOf(n);
                annoyinglyOverThoughtName = network.getId().substring(0, network.getId().length() - number.length())
                        + number;
            }
            return annoyinglyOverThoughtName;
        } catch (StorageReadException e) {
            Stargate.log(e);
        }
        throw new InvalidNameException("Unable to find a valid name");
    }

    @Override
    public Map<BlockLocation, PortalPosition> getPortalPositions() {
        return this.portalPositionMap;
    }

    @Override
    public Map<BlockLocation, PortalPosition> getPortalPositionsOwnedByPlugin(Plugin plugin) {
        this.portalPositionPluginNameMap.putIfAbsent(plugin.getName(), new HashMap<>());
        return this.portalPositionPluginNameMap.get(plugin.getName());
    }

    @Override
    public PortalPosition savePortalPosition(RealPortal portal, Location location, PositionType type, Plugin plugin) {
        BlockVector relativeVector = portal.getGate().getRelativeVector(location).toBlockVector();
        PortalPosition portalPosition = new PortalPosition(type, relativeVector, plugin.getName());
        new StargateQueuedAsyncTask(){
            @Override
            public void run() {
                try {
                    storageAPI.addPortalPosition(portal, portal.getStorageType(), portalPosition);
                } catch (StorageWriteException e) {
                    Stargate.log(e);
                }
            }
        }.runNow();
        return portalPosition;
    }

    @Override
    public void removePortalPosition(Location location) {
        BlockLocation blockLocation = new BlockLocation(location);
        PortalPosition portalPosition = portalPositionMap.get(blockLocation);
        if (portalPosition == null) {
            return;
        }
        portalPositionMap.remove(blockLocation);
        portalPositionPluginNameMap.get(portalPosition.getPluginName()).remove(blockLocation);
        RealPortal portal = portalPosition.getPortal();
        portal.getGate().removePortalPosition(portalPosition);
        new StargateQueuedAsyncTask() {
            @Override
            public void run() {
                try {
                    storageAPI.removePortalPosition(portal, portal.getStorageType(), portalPosition);
                } catch (StorageWriteException e) {
                    Stargate.log(e);
                }
            }
        }.runNow();
    }

    @Override
    public void registerPortalPosition(PortalPosition portalPosition, Location location, RealPortal portal) {
        Stargate.log(Level.FINEST, String.format("Registering portal position at %s for portal %s", location.toString(), portal.getName()));
        BlockLocation blockLocation = new BlockLocation(location);
        portalPositionMap.put(blockLocation, portalPosition);
        portalPositionPluginNameMap.putIfAbsent(portalPosition.getPluginName(), new HashMap<>());
        portalPositionPluginNameMap.get(portalPosition.getPluginName()).put(blockLocation, portalPosition);
        portalPosition.assignPortal(portal);
        portal.getGate().addPortalPosition(portalPosition);
    }

    @Override
    public PortalPosition getPortalPosition(Location location) {
        return portalPositionMap.get(new BlockLocation(location));
    }

    @Override
    public void registerNetwork(Network network) {
        network.assignToRegistry(this);
        getNetworkRegistry(network.getStorageType()).registerNetwork(network);
    }

    @Override
    public void renameNetwork(String newId, String oldId, StorageType storageType) throws InvalidNameException, UnimplementedFlagException, NameLengthException {
        getNetworkRegistry(storageType).renameNetwork(newId, oldId);
    }

    @Override
    public @NotNull Set<RealPortal> getPortalsInChunk(StargateChunk chunk) {
        Set<RealPortal> output = chunkPortalMap.get(chunk);
        if (output == null) {
            return new HashSet<>();
        }
        return output;
    }

    private @NotNull Set<StargateChunk> getPortalChunks(RealPortal portal) {
        GateAPI gate = portal.getGate();
        BoundingBox boundingBox = gate.getFormat().getBoundingBox();
        BlockVector corner1 = new BlockVector(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        BlockVector corner2 = new BlockVector(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ());

        Location corner1Location = gate.getLocation(corner1);
        Location corner2Location = gate.getLocation(corner2);
        Chunk corner1Chunk = corner1Location.getChunk();
        Chunk corner2Chunk = corner2Location.getChunk();
        World world = corner1Location.getWorld();

        int xMod = corner1Chunk.getX() < corner2Chunk.getX() ? 1 : -1;
        int zMod = corner1Chunk.getZ() < corner2Chunk.getZ() ? 1 : -1;

        Set<StargateChunk> chunks = new HashSet<>();
        for (int x = corner1Chunk.getX(); !shouldStop(corner2Chunk.getX(), x, xMod); x += xMod) {
            for (int z = corner1Chunk.getZ(); !shouldStop(corner2Chunk.getZ(), z, zMod); z += zMod) {
                chunks.add(new StargateChunk(x, z, world));
            }
        }
        return chunks;
    }

    private boolean shouldStop(int target, int currentPosition, int modifier) {
        if (modifier > 0) {
            return currentPosition > target;
        } else {
            return currentPosition < target;
        }
    }

}
