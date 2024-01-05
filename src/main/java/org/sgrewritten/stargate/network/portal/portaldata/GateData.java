package org.sgrewritten.stargate.network.portal.portaldata;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.sgrewritten.stargate.api.gate.GateFormatAPI;

/**
 * @param gateFormat What defines this gate's layout/format/design.
 * @param flipZ      Whether this portal is flipped on the Z axis.
 * @param topLeft    The Location of the top-left block of this portal.
 * @param facing     The direction that this portal is facing.
 */
public record GateData(GateFormatAPI gateFormat, boolean flipZ, Location topLeft, BlockFace facing) {
}
