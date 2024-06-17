package org.sgrewritten.stargate.database.property;

public enum StoredProperty {
    /**
     * Boolean property that indicates whether parity updates are available
     */
    PARITY_UPGRADES_AVAILABLE("nagKnarvikParity"),

    /**
     * Long property that indicates the time of the next clearing (or -1 if there's no scheduled clearing)
     */
    SCHEDULED_GATE_CLEARING("scheduledGateClearing");

    private final String key;

    StoredProperty(String key) {
        this.key = key;
    }

    /**
     * @return <p>The key that this property resembles</p>
     */
    public String getKey() {
        return key;
    }
}
