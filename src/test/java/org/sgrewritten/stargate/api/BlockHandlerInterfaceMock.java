package org.sgrewritten.stargate.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.MetaData;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.container.TwoTuple;

import java.util.HashMap;
import java.util.Map;

public class BlockHandlerInterfaceMock implements BlockHandlerInterface {

    private PositionType interfaceType;
    private Material handledMaterial;
    private Plugin plugin;
    private Priority priority;
    private Character flag;
    private Map<BlockLocation, TwoTuple<Player, Portal>> registeredBlocks = new HashMap<>();
    private boolean isRegisterPlacedBlock = true;

    public BlockHandlerInterfaceMock(PositionType interfaceType, Material handledMaterial, Plugin plugin, Priority priority, Character flag) {
        this.interfaceType = interfaceType;
        this.handledMaterial = handledMaterial;
        this.plugin = plugin;
        this.priority = priority;
        this.flag = flag;
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
    public @Nullable Character getFlag() {
        return flag;
    }

    @Override
    public boolean registerBlock(Location blockLocation, @Nullable Player player, Portal portal, MetaData data) {
        if (isRegisterPlacedBlock) {
            registeredBlocks.put(new BlockLocation(blockLocation), new TwoTuple<>(player, portal));
        }
        return isRegisterPlacedBlock;
    }

    @Override
    public void unRegisterBlock(Location blockLocation, Portal portal) {
        registeredBlocks.remove(new BlockLocation(blockLocation));
    }

    public boolean blockIsRegistered(Location blockLocation, @Nullable Player player, Portal portal) {
        BlockLocation key = new BlockLocation(blockLocation);
        return registeredBlocks.containsKey(key) && registeredBlocks.get(key).getFirstValue() == player
                && registeredBlocks.get(key).getSecondValue() == portal;
    }

    public void setRegisterPlacedBlock(boolean value) {
        this.isRegisterPlacedBlock = value;
    }
}
