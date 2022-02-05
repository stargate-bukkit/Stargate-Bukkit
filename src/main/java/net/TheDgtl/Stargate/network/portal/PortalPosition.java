package net.TheDgtl.Stargate.network.portal;

import org.bukkit.util.BlockVector;

/**
 * A position of a portal's control block
 */
public class PortalPosition {

    private final PositionType positionType;
    private final BlockVector positionLocation;

    /**
     * Instantiates a new portal position
     *
     * @param positionType     <p>The type of this portal position</p>
     * @param positionLocation <p>The location of this portal position</p>
     */
    public PortalPosition(PositionType positionType, BlockVector positionLocation) {
        this.positionType = positionType;
        this.positionLocation = positionLocation;
    }

    /**
     * Gets this portal position's type
     *
     * @return <p>This portal position's type</p>
     */
    public PositionType getPositionType() {
        return this.positionType;
    }

    /**
     * Gets this portal position's location
     *
     * @return <p>This portal position's location</p>
     */
    public BlockVector getPositionLocation() {
        return this.positionLocation;
    }

}
