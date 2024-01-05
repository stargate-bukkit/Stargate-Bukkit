package org.sgrewritten.stargate.api.network.portal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class PortalFlagTest {

    @Test
    void parseFlags() {
        String testString = "AG{B}";
        Set<PortalFlag> portalFlagSet = PortalFlag.parseFlags(testString);
        Assertions.assertTrue(portalFlagSet.contains(PortalFlag.ALWAYS_ON));
        Assertions.assertEquals(1, portalFlagSet.size());
    }

    @Test
    void parseFlags_lowerCase() {
        String testString = "a";
        Set<PortalFlag> portalFlagSet = PortalFlag.parseFlags(testString);
        Assertions.assertTrue(portalFlagSet.contains(PortalFlag.ALWAYS_ON));
    }


    @Test
    void getUnrecognisedFlags() {
        String testString = "GA{B}";
        Set<Character> portalFlagSet = PortalFlag.getUnrecognisedFlags(testString);
        Assertions.assertFalse(portalFlagSet.contains(PortalFlag.ALWAYS_ON.getCharacterRepresentation()));
        Assertions.assertTrue(portalFlagSet.contains('G'));
        Assertions.assertEquals(1, portalFlagSet.size());
    }

    @Test
    void getUnrecognisedFlags_lowerCase() {
        String testString = "g";
        Set<Character> portalFlagSet = PortalFlag.getUnrecognisedFlags(testString);
        Assertions.assertTrue(portalFlagSet.contains('G'));
    }
}