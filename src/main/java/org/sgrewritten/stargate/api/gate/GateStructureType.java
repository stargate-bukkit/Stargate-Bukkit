package org.sgrewritten.stargate.api.gate;


import org.sgrewritten.stargate.api.gate.structure.GateFormatStructureType;

public enum GateStructureType {

    IRIS(GateFormatStructureType.IRIS),
    FRAME(GateFormatStructureType.FRAME);

    private final GateFormatStructureType gateFormatEquivalent;

    GateStructureType(GateFormatStructureType gateFormatStructureType) {
        this.gateFormatEquivalent = gateFormatStructureType;
    }

    public GateFormatStructureType getGateFormatEquivalent() {
        return this.gateFormatEquivalent;
    }
}
