package org.sgrewritten.stargate.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.exception.ParsingErrorException;

import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

class GateFormatReaderTest {

    @BeforeAll
    static void setUp() {
        StargateTestHelper.setup();
        Stargate.setLogLevel(Level.FINEST);
    }

    @AfterAll
    static void tearDown() {
        StargateTestHelper.tearDown();
        Stargate.setLogLevel(Level.INFO);
    }

    @Test
    void parseMaterialTest_Material() throws ParsingErrorException {
        Set<Material> materials = GateFormatReader.parseMaterial("OAK_PLANKS", "");
        Assertions.assertTrue(materials.contains(Material.OAK_PLANKS));
        Assertions.assertEquals(1, materials.size());
    }

    @Test
    void parseMaterialTest_LegacyMaterial() throws ParsingErrorException {
        Set<Material> materials = GateFormatReader.parseMaterial("SILVER_GLAZED_TERRACOTTA ", "");
        Assertions.assertTrue(materials.contains(Material.LIGHT_GRAY_GLAZED_TERRACOTTA));
        Assertions.assertEquals(1, materials.size());
    }

    @Test
    void parseMaterialTest_LegacyNumberStupidFormat() throws ParsingErrorException {
        Set<Material> materials = GateFormatReader.parseMaterial("1 : 1", "");
        Assertions.assertTrue(materials.contains(Material.GRANITE));
        Assertions.assertEquals(1, materials.size());
    }

    @Test
    void parseMaterialTest_LegacyNumberWithData() throws ParsingErrorException {
        Set<Material> materials = GateFormatReader.parseMaterial("1:1", "");
        Assertions.assertTrue(materials.contains(Material.GRANITE));
        Assertions.assertEquals(1, materials.size());
    }

    @Test
    void parseMaterialTest_LegacyNumber() throws ParsingErrorException {
        Set<Material> materials = GateFormatReader.parseMaterial("1", "");
        Assertions.assertTrue(materials.contains(Material.STONE));
        Assertions.assertEquals(1, materials.size());
    }

    @Test
    void parseMaterialTest_invalidNumber() {
        Assertions.assertThrows(ParsingErrorException.class, () -> GateFormatReader.parseMaterial("3000", ""));
    }

    @Test
    void parseMaterialTest_InvalidMaterial() {
        Assertions.assertThrows(ParsingErrorException.class, () -> GateFormatReader.parseMaterial("invalid", ""));
    }

    @Test
    void parseMaterialTest_InvalidMaterial2() {
        Assertions.assertThrows(ParsingErrorException.class, () -> GateFormatReader.parseMaterial("ACACIA_BOAT", ""));
    }

    @Test
    void parseMaterialTest_InvalidTag() {
        Assertions.assertThrows(ParsingErrorException.class, () -> GateFormatReader.parseMaterial("#invalid", ""));
    }


    @Test
    void parseMaterialTest_Tag() throws ParsingErrorException {
        Set<Material> materials = GateFormatReader.parseMaterial("#ACACIA_LOGS", "");
        Assertions.assertEquals(materials, Tag.ACACIA_LOGS.getValues());
    }

    @ParameterizedTest
    @MethodSource("getMaterialCounterparts")
    void parseMaterialTest_NoWallDifferentiation(TwoTuple<Material, Material> test) throws ParsingErrorException {
        Set<Material> wallSomething = GateFormatReader.parseMaterial(test.getFirstValue().toString(), "");
        Assertions.assertTrue(wallSomething.contains(test.getFirstValue()), "Missing " + test.getFirstValue());
        Assertions.assertTrue(wallSomething.contains(test.getSecondValue()), "Missing " + test.getSecondValue());
        Set<Material> something = GateFormatReader.parseMaterial(test.getSecondValue().toString(), "");
        Assertions.assertTrue(something.contains(test.getFirstValue()), "Missing " + test.getFirstValue());
        Assertions.assertTrue(something.contains(test.getSecondValue()), "Missing " + test.getSecondValue());
    }


    static Stream<TwoTuple<Material, Material>> getMaterialCounterparts() {
        return Stream.of(
                new TwoTuple<>(Material.WALL_TORCH, Material.TORCH),
                new TwoTuple<>(Material.ACACIA_WALL_SIGN, Material.ACACIA_SIGN),
                new TwoTuple<>(Material.BRAIN_CORAL_WALL_FAN, Material.BRAIN_CORAL_FAN)
        );
    }


}
