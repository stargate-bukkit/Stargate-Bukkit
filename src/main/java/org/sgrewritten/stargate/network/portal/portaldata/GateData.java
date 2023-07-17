package org.sgrewritten.stargate.network.portal.portaldata;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 * @param gateFileName      The name of the gate file that defines this portal's layout/format/design.
 * @param topLeftX          This portal's left-most block's position on the X axis.
 * @param topLeftY          This portal's left-most block's position on the Y axis.
 * @param topLeftZ          This portal's left-most block's position on the Y axis.
 * @param worldName         The name of the world that this portal is located in.
 * @param flipZ             Whether this portal is flipped on the Z axis.
 * @param topLeft           The Location of the top-left block of this portal.
 * @param facing            The direction that this portal is facing.
 */
public record GateData(String gateFileName, int topLeftX, int topLeftY, int topLeftZ, String worldName, boolean flipZ,
                       Location topLeft, BlockFace facing) {
}
