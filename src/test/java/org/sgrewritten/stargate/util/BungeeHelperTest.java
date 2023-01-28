package org.sgrewritten.stargate.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.Stargate;

import java.io.File;

import static org.junit.jupiter.api.Assertions.fail;

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
        if (!new File(dataFolder + File.separator + internalFolder, "serverUUID.txt").delete()) {
            fail();
        }
    }


}
