package org.sgrewritten.stargate.api.gate;


import org.sgrewritten.stargate.api.gate.structure.GateFormatStructureType;

public enum GateStructureType {

    /**
     * The traversable part of the portal, which when entered and portal is opened teleports the entity
     */
    IRIS(GateFormatStructureType.IRIS),
    /**
     * The frame of the portal
     */
    FRAME(GateFormatStructureType.FRAME);

    private final GateFormatStructureType gateFormatEquivalent;

    GateStructureType(GateFormatStructureType gateFormatStructureType) {
        this.gateFormatEquivalent = gateFormatStructureType;
    }

    /**
     * @return <p>The equivalent enum for {@link GateFormatStructureType}</p>
     */
    public GateFormatStructureType getGateFormatEquivalent() {
        return this.gateFormatEquivalent;
    }
}
