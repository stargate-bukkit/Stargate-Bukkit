package org.sgrewritten.stargate.api.network.portal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;

import java.util.Set;

class StargateFlagTest {

    @Test
    void parseFlags() {
        String testString = "AG{B}";
        Set<StargateFlag> portalFlagSet = StargateFlag.parseFlags(testString);
        Assertions.assertTrue(portalFlagSet.contains(StargateFlag.ALWAYS_ON));
        Assertions.assertEquals(1, portalFlagSet.size());
    }

    @Test
    void parseFlags_lowerCase() {
        String testString = "a";
        Set<StargateFlag> portalFlagSet = StargateFlag.parseFlags(testString);
        Assertions.assertTrue(portalFlagSet.contains(StargateFlag.ALWAYS_ON));
    }


    @Test
    void getUnrecognisedFlags() {
        String testString = "GA{B}";
        Set<Character> portalFlagSet = StargateFlag.getUnrecognisedFlags(testString);
        Assertions.assertFalse(portalFlagSet.contains(StargateFlag.ALWAYS_ON.getCharacterRepresentation()));
        Assertions.assertTrue(portalFlagSet.contains('G'));
        Assertions.assertEquals(1, portalFlagSet.size());
    }

    @Test
    void getUnrecognisedFlags_lowerCase() {
        String testString = "g";
        Set<Character> portalFlagSet = StargateFlag.getUnrecognisedFlags(testString);
        Assertions.assertTrue(portalFlagSet.contains('G'));
    }
}