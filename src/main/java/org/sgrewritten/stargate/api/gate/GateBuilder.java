package org.sgrewritten.stargate.api.gate;

import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;

public interface GateBuilder {

    GateBuilder setGenerateButtonPositions(boolean generateButtonPositions);

    GateAPI build() throws NoFormatFoundException, GateConflictException, InvalidStructureException;
}
