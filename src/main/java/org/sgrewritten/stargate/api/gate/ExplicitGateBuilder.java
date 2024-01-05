package org.sgrewritten.stargate.api.gate;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.portal.portaldata.GateData;

import java.util.Objects;

public class ExplicitGateBuilder implements GateBuilder {

    private final RegistryAPI registryAPI;
    private final Location topLeft;
    private final GateFormatAPI gateFormatAPI;
    private boolean generateButtonPositions;
    private boolean flipGate;
    private BlockFace facing = BlockFace.NORTH;
    private boolean calculatePortalPositions = false;

    public ExplicitGateBuilder(RegistryAPI registryAPI, Location topLeft, GateFormatAPI gateFormatAPI) {
        this.registryAPI = Objects.requireNonNull(registryAPI);
        this.topLeft = Objects.requireNonNull(topLeft);
        this.gateFormatAPI = Objects.requireNonNull(gateFormatAPI);
    }

    public ExplicitGateBuilder setCalculatePortalPositions(boolean calculatePortalPositions) {
        this.calculatePortalPositions = calculatePortalPositions;
        return this;
    }

    @Override
    public ExplicitGateBuilder setGenerateButtonPositions(boolean generateButtonPositions) {
        this.generateButtonPositions = generateButtonPositions;
        return this;
    }

    public ExplicitGateBuilder setFlipGate(boolean flipGate) {
        this.flipGate = flipGate;
        return this;
    }

    public ExplicitGateBuilder setFacing(BlockFace facing) {
        this.facing = Objects.requireNonNull(facing);
        return this;
    }

    @Override
    public GateAPI build() throws InvalidStructureException {
        GateData data = new GateData(gateFormatAPI, flipGate, topLeft, facing);
        Gate gate = new Gate(data, registryAPI);
        if (calculatePortalPositions) {
            gate.calculatePortalPositions(!generateButtonPositions);
        }
        return gate;
    }
}
