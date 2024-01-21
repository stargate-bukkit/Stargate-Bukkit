package org.sgrewritten.stargate.database.property;

public enum StoredProperty {
    PARITY_UPGRADES_AVAILABLE("nagKnarvikParity"),
    SCHEDULED_GATE_CLEARING("scheduledGateClearing");

    private final String key;

    StoredProperty(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
