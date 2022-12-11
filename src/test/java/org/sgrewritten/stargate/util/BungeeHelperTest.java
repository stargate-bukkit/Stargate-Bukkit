package org.sgrewritten.stargate.util;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.Stargate;

class BungeeHelperTest {

    @Test
    public void getServerIdTest() {
        String dataFolder = "src/test/resources";
        String internalFolder = "internal";
        BungeeHelper.getServerId(dataFolder, internalFolder);
        String stargateUUID = Stargate.getServerUUID();
        Assertions.assertNotNull(stargateUUID);
        BungeeHelper.getServerId(dataFolder, internalFolder);
        Assertions.assertEquals(stargateUUID, Stargate.getServerUUID());
        new File(dataFolder + "/" + internalFolder, "serverUUID.txt").delete();
    }

    
}
