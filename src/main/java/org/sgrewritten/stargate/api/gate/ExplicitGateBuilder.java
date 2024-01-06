package org.sgrewritten.stargate.api.gate;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.portal.portaldata.GateData;

import java.util.Objects;

/**
 * Builder for gate objects based on already known parameters. To build a gate from unknown parameters, use {@link ImplicitGateBuilder}
 */
public class ExplicitGateBuilder implements GateBuilder {

    private final RegistryAPI registryAPI;
    private final Location topLeft;
    private final GateFormatAPI gateFormatAPI;
    private boolean generateButtonPositions;
    private boolean flipGate;
    private BlockFace facing = BlockFace.NORTH;
    private boolean calculatePortalPositions = false;

    /**
     * Constructor for {@link ExplicitGateBuilder}
     *
     * @param registryAPI   <p>The stargate registry for portals and networks</p>
     * @param topLeft       <p>The to be top left position of the gate (or top right if the gate is flipped)</p>
     * @param gateFormatAPI <p>The format of the gate</p>
     */
    public ExplicitGateBuilder(RegistryAPI registryAPI, Location topLeft, GateFormatAPI gateFormatAPI) {
        this.registryAPI = Objects.requireNonNull(registryAPI);
        this.topLeft = Objects.requireNonNull(topLeft);
        this.gateFormatAPI = Objects.requireNonNull(gateFormatAPI);
    }

    /**
     * @param calculatePortalPositions <p>Whether the gate should calculate its own portal positions</p>
     * @return <p>This gate builder</p>
     */
    public ExplicitGateBuilder setCalculatePortalPositions(boolean calculatePortalPositions) {
        this.calculatePortalPositions = calculatePortalPositions;
        return this;
    }

    @Override
    public ExplicitGateBuilder setGenerateButtonPositions(boolean generateButtonPositions) {
        this.generateButtonPositions = generateButtonPositions;
        return this;
    }

    /**
     * Whether the gate should be flipped, this effectively makes the top left position top right (flip z axis in format
     * space)
     *
     * @param flipGate <p>Whether the gate should be flipped</p>
     * @return <p>This gate builder</p>
     */
    public ExplicitGateBuilder setFlipGate(boolean flipGate) {
        this.flipGate = flipGate;
        return this;
    }

    /**
     * Set the facing of this gate, this equals the facing of the sign. You can alternatively see this as setting the
     * gate to different rotations.
     *
     * @param facing <p>The facing of this gate</p>
     * @return <p>This gate builder</p>
     */
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
