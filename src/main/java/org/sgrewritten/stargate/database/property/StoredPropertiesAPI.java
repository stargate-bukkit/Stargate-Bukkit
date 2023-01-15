package org.sgrewritten.stargate.database.property;

public interface StoredPropertiesAPI {
    /**
     * Get a property from a stored properties file
     * @param property <p> The property to get the value of </p>
     * @return <p> The value of the property, or otherwise null </p>
     */
    String getProperty(StoredProperty property);
    
    /**
     * Set a property from a stored properties file
     * @param property <p> The property to set the value on </p>
     * @param value <p> The value to set the property to </p>
     */
    void setProperty(StoredProperty property, String value);
}
