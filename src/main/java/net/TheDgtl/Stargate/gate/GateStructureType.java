package net.TheDgtl.Stargate.gate;


public enum GateStructureType {
    CONTROLL("controll"), FRAME("frame"), IRIS("iris");

    private String key;

    private GateStructureType(String key) {
        this.key = key;
    }

    public String valueOf() {
        return key;
    }

}