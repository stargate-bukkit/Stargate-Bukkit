package org.sgrewritten.stargate.database.property;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;

class PropertiesDatabaseTest {

    private PropertiesDatabase propertiesDatabase;
    private File fileLocation;

    @BeforeEach
    void setUp() throws FileNotFoundException, IOException {
        MockBukkit.mock();
        MockPlugin plugin = MockBukkit.createMockPlugin();
        fileLocation = new File(plugin.getDataFolder(),"test.properties");
        propertiesDatabase = new PropertiesDatabase(fileLocation);
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void setProperty() {
        propertiesDatabase.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, "true");
        Assertions.assertEquals("true", propertiesDatabase.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE));
    }

    @Test
    void setProperty_Reload() throws FileNotFoundException, IOException {
        propertiesDatabase.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, "true");
        Assertions.assertEquals("true", new PropertiesDatabase(fileLocation).getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE));
    }
    
    @Test
    void setProperty_Boolean() {
        propertiesDatabase.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, true);
        Assertions.assertEquals("true", propertiesDatabase.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE));
    }
}
