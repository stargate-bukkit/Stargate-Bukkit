package org.sgrewritten.stargate.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.Metadata;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.flag.CustomFlag;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.container.TwoTuple;

import java.util.HashMap;
import java.util.Map;

public class BlockHandlerInterfaceMock implements BlockHandlerInterface {

    private final PositionType interfaceType;
    private final Material handledMaterial;
    private final Plugin plugin;
    private final Priority priority;
    private final PortalFlag flag;
    private final Map<BlockLocation, TwoTuple<OfflinePlayer, Portal>> registeredBlocks = new HashMap<>();
    private boolean isRegisterPlacedBlock = true;

    public BlockHandlerInterfaceMock(PositionType interfaceType, Material handledMaterial, Plugin plugin, Priority priority, Character flag) {
        this.interfaceType = interfaceType;
        this.handledMaterial = handledMaterial;
        this.plugin = plugin;
        this.priority = priority;
        this.flag = CustomFlag.getOrCreate(flag);
    }


    @Override
    public @NotNull PositionType getInterfaceType() {
        return interfaceType;
    }

    @Override
    public @NotNull Material getHandledMaterial() {
        return handledMaterial;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    @Override
    public @NotNull Priority getPriority() {
        return priority;
    }

    @Override
    public @Nullable PortalFlag getFlag() {
        return flag;
    }

    @Override
    public boolean registerBlock(Location blockLocation, @Nullable OfflinePlayer player, Portal portal, Metadata data) {
        if (isRegisterPlacedBlock) {
            registeredBlocks.put(new BlockLocation(blockLocation), new TwoTuple<>(player, portal));
        }
        return isRegisterPlacedBlock;
    }

    @Override
    public void unRegisterBlock(Location blockLocation, Portal portal) {
        registeredBlocks.remove(new BlockLocation(blockLocation));
    }

    public boolean blockIsRegistered(Location blockLocation, @Nullable OfflinePlayer player, Portal portal) {
        BlockLocation key = new BlockLocation(blockLocation);
        return registeredBlocks.containsKey(key) && registeredBlocks.get(key).getFirstValue() == player
                && registeredBlocks.get(key).getSecondValue() == portal;
    }

    public void setRegisterPlacedBlock(boolean value) {
        this.isRegisterPlacedBlock = value;
    }
}
