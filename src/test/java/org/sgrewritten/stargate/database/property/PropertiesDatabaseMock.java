package org.sgrewritten.stargate.database.property;

import java.util.Properties;

public class PropertiesDatabaseMock implements StoredPropertiesAPI {

    private final Properties properties;

    public PropertiesDatabaseMock() {
        properties = new Properties();
    }

    @Override
    public String getProperty(StoredProperty property) {
        return properties.getProperty(property.getKey());
    }

    @Override
    public void setProperty(StoredProperty property, String value) {
        properties.setProperty(property.getKey(), value);
    }

    @Override
    public void setProperty(StoredProperty property, Object value) {
        this.setProperty(property, value.toString());
    }

}
