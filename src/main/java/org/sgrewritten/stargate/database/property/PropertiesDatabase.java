package org.sgrewritten.stargate.database.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;

public class PropertiesDatabase implements StoredPropertiesAPI {

    private Properties handle;
    private @NotNull File fileLocation;

    public PropertiesDatabase(@NotNull File fileLocation) throws FileNotFoundException, IOException {
        this.fileLocation = Objects.requireNonNull(fileLocation);
        if(!fileLocation.exists()) {
            fileLocation.createNewFile();
        }
        handle = new Properties();
        handle.load(new FileInputStream(fileLocation));
    }
    
    @Override
    public String getProperty(@NotNull StoredProperty property) {
        return handle.getProperty(property.getKey());
    }

    @Override
    public void setProperty(@NotNull StoredProperty property, String value) {
        handle.setProperty(property.getKey(), value);
        try {
            handle.store(new FileOutputStream(fileLocation), null);
        } catch (IOException e) {
            Stargate.log(e);
        }
    }

    @Override
    public void setProperty(StoredProperty property, Object value) {
        setProperty(property, value.toString());
    }

}
