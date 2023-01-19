package org.sgrewritten.stargate.database.property;

public enum StoredProperty {
    PARITY_UPGRADES_AVAILABLE("nagKnarvikParity"), INCOMPATIBLE_DATABASE_ALPHA_1_0_0_4("incompatibleDatabaseAlpha-1_0_0_4");
    
    private String key;

    private  StoredProperty(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
}
