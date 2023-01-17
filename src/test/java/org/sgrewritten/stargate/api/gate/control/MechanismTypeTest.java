package org.sgrewritten.stargate.api.gate.control;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MechanismTypeTest {

    @Test
    void hasCode_Equals() {
        Assertions.assertEquals(MechanismType.SIGN.hashCode(), MechanismType.SIGN.hashCode());
    }
    
    @Test
    void hashCode_NotEquals() {
        Assertions.assertNotEquals(MechanismType.BUTTON.hashCode(), MechanismType.SIGN.hashCode());
    }

    @Test
    void notequals() {
        Assertions.assertNotEquals(MechanismType.BUTTON, MechanismType.SIGN);
    }
    
    @Test
    void equals() {
        Assertions.assertNotEquals(MechanismType.SIGN, MechanismType.SIGN);
    }
    
    @Test
    void register() {
        MechanismType unitTest = new MechanismType("UNIT_TEST");
        MechanismType.registerMechanismType(unitTest);
        Assertions.assertEquals(MechanismType.valueOf("UNIT_TEST"), unitTest);
    }
    
    @Test
    void alreadyRegistered() {
        MechanismType unitTest = new MechanismType(MechanismType.BUTTON.toString());
        Assertions.assertThrows(IllegalArgumentException.class, () -> MechanismType.registerMechanismType(unitTest));
    }
}
