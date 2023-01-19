package org.sgrewritten.stargate.database.property;

import org.sgrewritten.stargate.Stargate;

public class StoredPropertyHelper {

    
    
    public static String getStoredProperty(StoredProperty property) {
        if(Stargate.getInstance() == null) {
            return null;
        }
        return Stargate.getInstance().getStoredPropertiesAPI().getProperty(property);
    }
}
