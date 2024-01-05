package org.sgrewritten.stargate.api.gate;

import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.gate.GateFormat;

import java.util.List;
import java.util.logging.Level;

public class ImplicitGateBuilder implements GateBuilder {

    private final Location signLocation;
    private final RegistryAPI registryAPI;
    private boolean generateButtonPositions = false;

    public ImplicitGateBuilder(Location signLocation, RegistryAPI registryAPI) {
        this.signLocation = signLocation;
        this.registryAPI = registryAPI;
    }

    public ImplicitGateBuilder setGenerateButtonPositions(boolean generateButtonPositions) {
        this.generateButtonPositions = generateButtonPositions;
        return this;
    }

    /**
     * Creates a new gate from the given sign
     *
     * @return <p>A new Gate</p>
     * @throws NoFormatFoundException <p>If no gate format is found that matches the physical gate</p>
     * @throws GateConflictException  <p>If a registered gate conflicts with the new gate</p>
     */
    @Override
    public GateAPI build() throws NoFormatFoundException, GateConflictException {
        Block sign = signLocation.getBlock();
        if (!(Tag.WALL_SIGNS.isTagged(sign.getType()))) {
            throw new NoFormatFoundException();
        }
        //Get the block behind the sign; the material of that block is stored in a register with available gateFormats
        Directional signDirection = (Directional) sign.getBlockData();
        Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
        List<GateFormat> gateFormats = GateFormatRegistry.getPossibleGateFormatsFromControlBlockMaterial(behind.getType());
        return findMatchingGate(gateFormats, sign.getLocation(), signDirection.getFacing(), !generateButtonPositions, registryAPI);
    }

    /**
     * Tries to find a gate at the given location matching one of the given gate formats
     *
     * @param gateFormats  <p>The gate formats to look for</p>
     * @param signLocation <p>The location of the sign of the portal to look for</p>
     * @param signFacing   <p>The direction the sign is facing</p>
     * @param alwaysOn     <p>Whether the portal is always on</p>
     * @return <p>A gate if found, otherwise throws an {@link NoFormatFoundException}</p>
     * @throws NoFormatFoundException <p>If no gate was found at the given location matching any of the given formats</p>
     * @throws GateConflictException  <p>If the found gate conflicts with another gate</p>
     */
    private Gate findMatchingGate(List<GateFormat> gateFormats, Location signLocation, BlockFace signFacing,
                                  boolean alwaysOn, RegistryAPI registry)
            throws NoFormatFoundException, GateConflictException {
        Stargate.log(Level.FINE, "Amount of GateFormats: " + gateFormats.size());
        for (GateFormat gateFormat : gateFormats) {
            Stargate.log(Level.FINE, "--------- " + gateFormat.getFileName() + " ---------");
            try {
                return new Gate(gateFormat, signLocation, signFacing, alwaysOn, registry);
            } catch (InvalidStructureException ignored) {
            }
        }
        throw new NoFormatFoundException();
    }
}
