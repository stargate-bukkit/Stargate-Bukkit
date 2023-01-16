package org.sgrewritten.stargate.api.network.portal;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PortalFlagTest {

    @Test
    void equals() {
        Assertions.assertEquals(PortalFlag.ALWAYS_ON,PortalFlag.ALWAYS_ON);
    }

    @Test
    void notEquals() {
        Assertions.assertNotEquals(PortalFlag.ALWAYS_ON, PortalFlag.BACKWARDS);
    }
    
    @Test
    void creation() {
        PortalFlag flag = new PortalFlag('o', "NAME", true);
        PortalFlag.registerFlag(flag);
        Assertions.assertDoesNotThrow(() -> PortalFlag.valueOf('o'));
        Assertions.assertDoesNotThrow(() -> PortalFlag.valueOf("NAME"));
    }
    
    @Test
    void creation_CharacterConflict() {
        PortalFlag flag = new PortalFlag('1',"A_NAME",false);
        Assertions.assertThrows(IllegalArgumentException.class, () -> PortalFlag.registerFlag(flag));
    }
    
    @Test
    void creation_NAmeConflict() {
        PortalFlag flag = new PortalFlag('z',"ALWAYS_ON",false);
        Assertions.assertThrows(IllegalArgumentException.class, () -> PortalFlag.registerFlag(flag));
    }
}
