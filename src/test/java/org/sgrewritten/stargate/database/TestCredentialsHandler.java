package org.sgrewritten.stargate.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.sgrewritten.stargate.Stargate;

public class TestCredentialsHandler {
    
    static private final Properties properties = new Properties();
    static {
        try {
            loadProperties();
        } catch(IOException e) {
            Stargate.log(e);
        }
    }
    
    public static String getCredentialString(TestCredential testCredential) {
        return properties.getProperty(testCredential.name());
    }

    public static String getCredentialString(TestCredential testCredential, String defaultValue) {
        return properties.getProperty(testCredential.name(), defaultValue);
    }
    
    public static int getCredentialInt(TestCredential testCredential) {
        return Integer.parseInt(properties.getProperty(testCredential.name()));
    }

    public static int getCredentialInt(TestCredential testCredential, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(testCredential.name()));
        } catch (NumberFormatException | NullPointerException exception) {
            return defaultValue;
        }
    }
    
    private static void loadProperties() throws IOException {
        File location = new File("src/test/resources/mysql_credentials.secret");
        try (InputStream stream = new FileInputStream(location)) {
            properties.load(stream);
        }
    }
}
