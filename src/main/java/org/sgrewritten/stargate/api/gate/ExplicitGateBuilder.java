package org.sgrewritten.stargate.api.gate;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.portal.portaldata.GateData;

import java.util.Objects;

public class ExplicitGateBuilder implements GateBuilder{

    private final RegistryAPI registryAPI;
    private final Location topLeft;
    private final GateFormatAPI gateFormatAPI;
    private boolean generateButtonPositions;
    private boolean flipGate;
    private BlockFace facing = BlockFace.NORTH;

    public ExplicitGateBuilder(RegistryAPI registryAPI, Location topLeft, GateFormatAPI gateFormatAPI){
        this.registryAPI = registryAPI;
        this.topLeft = topLeft;
        this.gateFormatAPI = gateFormatAPI;
    }

    @Override
    public ExplicitGateBuilder setGenerateButtonPositions(boolean generateButtonPositions){
        this.generateButtonPositions = generateButtonPositions;
        return this;
    }

    public ExplicitGateBuilder setFlipGate(boolean flipGate){
        this.flipGate = flipGate;
        return this;
    }

    public ExplicitGateBuilder setFacing(BlockFace facing){
        this.facing = Objects.requireNonNull(facing);
        return this;
    }

    @Override
    public GateAPI build() throws InvalidStructureException {
        GateData data = new GateData(Objects.requireNonNull(gateFormatAPI), Objects.requireNonNull(flipGate),
                Objects.requireNonNull(topLeft), facing);
        return new Gate(data, registryAPI);
    }
}
