package org.sgrewritten.stargate.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.MetaData;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PositionType;

public interface BlockHandlerInterface {

    /**
     * @return The type of interface handled
     */
    @NotNull PositionType getInterfaceType();

    /**
     * @return The material relevant to this handler
     */
    @NotNull Material getHandledMaterial();

    /**
     *
     * @return The plugin linked to this blockHandlerInterface
     */
    @NotNull Plugin getPlugin();

    /**
     * @return The priority of this handler relative to other handlers
     */
    @NotNull Priority getPriority();

    /**
     * @return The flag related to this type of block interface
     */
    @Nullable Character getFlag();

    /**
     * <p>Called if a Stargate is created or a block is placed, and the result of
     * getHandledMaterial matches with the detected location</p>
     *
     * @param blockLocation The location of the block that is placed
     * @param player The player that placed the block
     * @param portal The affected portal
     * @return Whether to claim the block
     */
    boolean registerBlock(Location blockLocation, @Nullable Player player, Portal portal, MetaData metaData);

    /**
     * Called if a Stargate is removed, and the add-on has registered this location
     * @param blockLocation The location of the block that is removed
     * @param portal The affected portal
     */
    void unRegisterBlock(Location blockLocation, Portal portal);

}
