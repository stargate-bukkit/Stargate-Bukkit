package org.sgrewritten.stargate.gate;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.gate.GatePosition;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

import java.util.Objects;

public class UnkownGatePosition extends GatePosition {


    private final String name;

    /**
     * Instantiates a new portal position. Note that you can get this vector by
     * doing {@link GateAPI#getRelativeVector(Location)}
     *
     * @param positionLocation <p>The location of this portal position in gatespace</p>
     */
    public UnkownGatePosition(@NotNull BlockVector positionLocation, @NotNull String name) {
        super(positionLocation);
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public boolean onBlockClick(PlayerInteractEvent event, RealPortal portal) {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }
}
