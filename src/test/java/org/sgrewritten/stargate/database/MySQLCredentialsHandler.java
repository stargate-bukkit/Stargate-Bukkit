package org.sgrewritten.stargate.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.sgrewritten.stargate.Stargate;

public class MySQLCredentialsHandler {
    
    
    static private Properties properties = new Properties();
    static {
        try {
            loadProperties();
        } catch(IOException e) {
            Stargate.log(e);
        }
    }
    
    
    public static String getCredentialString(Credential credential) {
        return properties.getProperty(credential.name());
    }
    
    public static int getCredentialInt(Credential credential) {
        return Integer.valueOf(properties.getProperty(credential.name()));
    }
    
    private static void loadProperties() throws IOException {
        File location = new File("src/test/resources/mysql_credentials.secret");
        try(InputStream stream = new FileInputStream(location)) {
            properties.load(stream);
        }
    }
}
