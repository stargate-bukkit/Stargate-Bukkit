package org.sgrewritten.stargate.database;

import org.bukkit.Bukkit;
import org.sgrewritten.stargate.Stargate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

/**
 * A manager for dealing with a set of credentials
 */
public class TestCredentialsManager {

    private final Properties properties = new Properties();

    /**
     * Instantiates a new test credentials manager
     *
     * @param propertyFile <p>The property file to load credentials from</p>
     */
    public TestCredentialsManager(String propertyFile) {
        loadProperties(propertyFile);
    }

    /**
     * Gets a credential value as a string
     *
     * @param testCredential <p>The credential to get</p>
     * @return <p>The value of the credential, or the default</p>
     */
    public String getCredentialString(TestCredential testCredential) {
        return properties.getProperty(testCredential.name());
    }

    /**
     * Gets a credential value as a string
     *
     * @param testCredential <p>The credential to get</p>
     * @param defaultValue   <p>The default value, if credential is not set</p>
     * @return <p>The value of the credential, or the default</p>
     */
    public String getCredentialString(TestCredential testCredential, String defaultValue) {
        return properties.getProperty(testCredential.name(), defaultValue);
    }

    /**
     * Gets a credential value as an integer
     *
     * @param testCredential <p>The credential to get</p>
     * @param defaultValue   <p>The default value, if credential is not set</p>
     * @return <p>The value of the credential, or the default if not set, or not an int</p>
     */
    public int getCredentialInt(TestCredential testCredential, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(testCredential.name()));
        } catch (NumberFormatException | NullPointerException exception) {
            return defaultValue;
        }
    }

    /**
     * Loads the properties in the given property file
     *
     * @param propertyFile <p>The property file to load properties from</p>
     */
    private void loadProperties(String propertyFile) {
        File location = new File("src" + File.separator + "test" + File.separator + "resources" +
                File.separator + propertyFile);
        try (InputStream stream = new FileInputStream(location)) {
            properties.load(stream);
        } catch (IOException ignored) {
            Stargate.log(Level.WARNING, "Unable to load test credentials from file '" + propertyFile + "'. Defaults will be used!");
        }
    }

}
