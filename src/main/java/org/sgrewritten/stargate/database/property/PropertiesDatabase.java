package org.sgrewritten.stargate.database.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;

public class PropertiesDatabase implements StoredPropertiesAPI {

    private Properties handle;
    private @NotNull File fileLocation;

    public PropertiesDatabase(@NotNull File fileLocation) throws FileNotFoundException, IOException {
        this.fileLocation = Objects.requireNonNull(fileLocation);
        if(!fileLocation.exists() && !fileLocation.createNewFile()) {
            Stargate.log(Level.WARNING,"Could not create file '" + fileLocation + "'");
        }
        handle = new Properties();
        InputStream inputStream = new FileInputStream(fileLocation);
        try {
            handle.load(inputStream);
        } finally {
            inputStream.close();
        }
    }
    
    @Override
    public String getProperty(@NotNull StoredProperty property) {
        return handle.getProperty(property.getKey());
    }

    @Override
    public void setProperty(@NotNull StoredProperty property, String value) {
        handle.setProperty(property.getKey(), value);
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(fileLocation);
            handle.store(outputStream, null);
        } catch (IOException e) {
            Stargate.log(e);
        } finally {
            try {
                if(outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                Stargate.log(e);
            }
        }
    }

    @Override
    public void setProperty(StoredProperty property, Object value) {
        setProperty(property, value.toString());
    }

}
