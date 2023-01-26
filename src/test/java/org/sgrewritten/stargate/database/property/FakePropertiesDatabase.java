package org.sgrewritten.stargate.database.property;

import java.util.Properties;

public class FakePropertiesDatabase implements StoredPropertiesAPI {

    private final Properties properties;

    public FakePropertiesDatabase() {
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
