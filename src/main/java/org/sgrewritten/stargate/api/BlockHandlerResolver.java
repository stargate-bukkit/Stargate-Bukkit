package org.sgrewritten.stargate.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.Metadata;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.flag.CustomFlag;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.exception.database.StorageWriteException;

import java.util.*;

public class BlockHandlerResolver {
    private final Map<Material, List<BlockHandlerInterface>> blockHandlerMap = new EnumMap<>(Material.class);
    private final Map<BlockLocation, BlockHandlerInterface> blockBlockHandlerMap = new HashMap<>();
    private final StorageAPI storageAPI;
    private final Set<PortalFlag> customFlags = new HashSet<>();

    public BlockHandlerResolver(@NotNull StorageAPI storageAPI) {
        this.storageAPI = Objects.requireNonNull(storageAPI);
    }

    /**
     * Add a listener for block placement next by a portal
     *
     * @param blockHandlerInterface A listener for block placement next by a portal
     * @throws IllegalStateException <p>If the flag of the BlockHandlerInterface has not been registered</p>
     */
    public void addBlockHandlerInterface(@NotNull BlockHandlerInterface blockHandlerInterface) {
        PortalFlag flag = blockHandlerInterface.getFlag();
        if (flag != null && !(customFlags.contains(flag))) {
            throw new IllegalStateException("Unregistered flag: " + flag);
        }
        List<BlockHandlerInterface> blockHandlerInterfaceList = this.blockHandlerMap.computeIfAbsent(blockHandlerInterface.getHandledMaterial(), k -> new ArrayList<>());
        blockHandlerInterfaceList.add(blockHandlerInterface);
        blockHandlerInterfaceList.sort(Comparator.comparingInt(ablockHandlerInterface -> -ablockHandlerInterface.getPriority().getPriorityValue()));
    }

    /**
     * Remove a listener for block placement next by a portal
     *
     * @param blockHandlerInterface listener for block placement next by a portal
     */
    public void removeBlockHandlerInterface(BlockHandlerInterface blockHandlerInterface) {
        for (List<BlockHandlerInterface> blockHandlerInterfaceList : this.blockHandlerMap.values()) {
            if (blockHandlerInterfaceList.remove(blockHandlerInterface)) {
                return;
            }
        }
    }

    /**
     * Remove all listeners for block placement next by a portal
     *
     * @param plugin The plugin to remove listeners from
     */
    public void removeBlockHandlerInterfaces(Plugin plugin) {
        for (List<BlockHandlerInterface> blockHandlerInterfaceList : this.blockHandlerMap.values()) {
            blockHandlerInterfaceList.removeIf(blockHandlerInterface -> blockHandlerInterface.getPlugin() == plugin);
        }
    }

    /**
     * @param location The location of the block that is being placed
     * @param portals  The portal to try registration on
     * @param material The material of the block
     * @param player   The player that placed the block
     */
    public void registerPlacement(@NotNull RegistryAPI registry, @NotNull Location location, @NotNull List<RealPortal> portals, @NotNull Material material, OfflinePlayer player) {
        if (!blockHandlerMap.containsKey(material)) {
            return;
        }
        for (RealPortal portal : portals) {
            for (BlockHandlerInterface blockHandlerInterface : blockHandlerMap.get(material)) {
                Metadata metaData = new Metadata("");
                if (portal.hasFlag(blockHandlerInterface.getFlag()) && blockHandlerInterface.registerBlock(location, player, portal, metaData)) {
                    PortalPosition portalPosition = registry.savePortalPosition(portal, location, blockHandlerInterface.getInterfaceType(), blockHandlerInterface.getPlugin());
                    portalPosition.assignPortal(portal);
                    registry.registerPortalPosition(portalPosition, location, portal);
                    blockBlockHandlerMap.put(new BlockLocation(location), blockHandlerInterface);
                    if (metaData.getMetadata() != null && !metaData.getMetadata().isEmpty()) {
                        portalPosition.setMetadata(metaData.getMetadata());
                    }
                    return;
                }
            }
        }

    }

    /**
     * @param location The location of the block that is being removed
     * @param portal   The portal to try removal on
     */
    public void registerRemoval(RegistryAPI registry, Location location, RealPortal portal) {
        BlockHandlerInterface blockHandlerInterface = this.blockBlockHandlerMap.get(new BlockLocation(location));
        if (blockHandlerInterface == null) {
            return;
        }
        blockHandlerInterface.unRegisterBlock(location, portal);
        registry.removePortalPosition(location);
    }

    /**
     * Method used for performance
     *
     * @param material The material
     * @return Whether there exists a BlockHandlerInterface that
     * has registered for the material
     */
    public boolean hasRegisteredBlockHandler(Material material) {
        return blockHandlerMap.containsKey(material);
    }

    /**
     * Register a custom flag to stargate.
     *
     * @param flagCharacter <p> The character of the custom flag</p>
     */
    public void registerCustomFlag(char flagCharacter) throws IllegalArgumentException {
        for (StargateFlag flag : StargateFlag.values()) {
            if (flagCharacter == flag.getCharacterRepresentation()) {
                throw new IllegalArgumentException("Flag conflict with core flags");
            }
        }

        for (PortalFlag flag : customFlags) {
            if (flagCharacter == flag.getCharacterRepresentation()) {
                throw new IllegalArgumentException("Custom flag conflicts with another custom flag");
            }
        }

        try {
            customFlags.add(CustomFlag.getOrCreate(flagCharacter));
            storageAPI.addFlagType(flagCharacter);
        } catch (StorageWriteException ignored) {
        }
    }

    public boolean hasRegisteredCustomFlag(PortalFlag portalFlag) {
        return customFlags.contains(portalFlag);
    }
}
