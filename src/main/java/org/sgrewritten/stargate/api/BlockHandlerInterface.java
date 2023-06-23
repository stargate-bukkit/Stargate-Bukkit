package org.sgrewritten.stargate.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.Portal;

interface BlockInterfaceHandler {

    // The type of interface handled
    @NotNull PositionType getInterfaceType();

    // The material relevant to this handler
    @NotNull Material getHandledMaterial();

    // The priority of this handler relative to other handlers
    @NotNull Priority getPriority();

    // The flag related to this type of block interface
    @Nullable Character getFlag();

    // Called if a Stargate is created or a block is placed, and the result of getHandledMaterials matches with the detected location
    boolean registerPlacedBlock(Location blockLocation, @Nullable Player player, Portal portal);

    // Called if a Stargate is removed, and the add-on has registered this location
    void unRegisterPlacedBlock(Location blockLocation, @Nullable Player player, Portal portal);

}
