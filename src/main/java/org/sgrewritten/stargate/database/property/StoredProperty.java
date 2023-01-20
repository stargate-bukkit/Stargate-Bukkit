package org.sgrewritten.stargate.database.property;

public enum StoredProperty {
    PARITY_UPGRADES_AVAILABLE("nagKnarvikParity");
    
    private String key;

    private  StoredProperty(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
}
