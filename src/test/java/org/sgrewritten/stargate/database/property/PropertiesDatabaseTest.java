package org.sgrewritten.stargate.database.property;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sgrewritten.stargate.StargateExtension;

import java.io.File;
import java.io.IOException;

@ExtendWith(StargateExtension.class)
class PropertiesDatabaseTest {

    private PropertiesDatabase propertiesDatabase;
    private File fileLocation;

    @BeforeEach
    void setUp() throws IOException {
        MockPlugin plugin = MockBukkit.createMockPlugin();
        fileLocation = new File(plugin.getDataFolder(), "test.properties");
        propertiesDatabase = new PropertiesDatabase(fileLocation);
    }

    @Test
    void setProperty() {
        propertiesDatabase.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, "true");
        Assertions.assertEquals("true", propertiesDatabase.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE));
    }

    @Test
    void setProperty_Reload() throws IOException {
        propertiesDatabase.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, "true");
        Assertions.assertEquals("true", new PropertiesDatabase(fileLocation).getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE));
    }

    @Test
    void setProperty_Boolean() {
        propertiesDatabase.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, true);
        Assertions.assertEquals("true", propertiesDatabase.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE));
    }
}
