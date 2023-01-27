package org.sgrewritten.stargate.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import org.sgrewritten.stargate.Stargate;

public class TestCredentialsHandler {
    
    
    static private Properties properties = new Properties();
    static {
        try {
            loadProperties();
        } catch(IOException e) {
            Stargate.log(e);
        }
    }
    
    
    public static String getPassword(){
        return properties.getProperty("DB_PASSWORD");
    }
    
    public static String getUser() {
        return properties.getProperty("DB_USER");
    }
    
    public static int getPort() {
        return Integer.valueOf(properties.getProperty("DB_PORT"));
    }
    
    public static String getDatabaseName() {
        return properties.getProperty("DB_NAME");
    }
    
    public static String getAddress() {
        return properties.getProperty("DB_ADDRESS");
    }
    
    private static void loadProperties() throws IOException {
        File location = new File("src/test/resources/credentials.secret");
        try(InputStream stream = new FileInputStream(location)) {
            properties.load(stream);
        }
    }
}
