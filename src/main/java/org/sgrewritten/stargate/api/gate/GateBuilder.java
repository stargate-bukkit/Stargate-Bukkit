package org.sgrewritten.stargate.api.gate;

import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;

public interface GateBuilder {

    /**
     * Whether to generate a button position. This might not be necessary for gates such as always on gates
     *
     * @param generateButtonPositions <p>Whether to generate a button position.</p>
     * @return <p>This gate builder (convenience)</p>
     */
    GateBuilder setGenerateButtonPositions(boolean generateButtonPositions);

    /**
     * Build a gate
     *
     * @return <p>A gate</p>
     * @throws NoFormatFoundException    <p>If no format was found see {@link ImplicitGateBuilder}</p>
     * @throws GateConflictException <p>If the location of the gate to be built conflicts with another gate</p>
     * @throws InvalidStructureException <p>When the gate to be built does not match the format</p>
     */
    GateAPI build() throws NoFormatFoundException, GateConflictException, InvalidStructureException;
}
