package org.sgrewritten.stargate.gate;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.api.gate.structure.GateFormatStructureType;
import org.sgrewritten.stargate.util.StargateTestHelper;

import static org.junit.jupiter.api.Assertions.*;

class GateFormatTest {

    private GateFormat gateFormat;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        StargateTestHelper.setup();
        this.gateFormat = GateFormatRegistry.getFormat("nether.gate");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void getHeight() {
        Assertions.assertEquals(5, gateFormat.getHeight());
    }

    @Test
    void getWidth() {
        Assertions.assertEquals(4, gateFormat.getWidth());
    }

    @Test
    void getBoundingBox() {
        BoundingBox boundingBox = gateFormat.getBoundingBox();
        Assertions.assertEquals(1,boundingBox.getMaxX());
        Assertions.assertEquals(0,boundingBox.getMinX());
        Assertions.assertEquals(0,boundingBox.getMaxY());
        Assertions.assertEquals(-4,boundingBox.getMinY());
        Assertions.assertEquals(0, boundingBox.getMaxZ());
        Assertions.assertEquals(-3,boundingBox.getMinZ());
    }

    @Test
    void isIronDoorBlockable() {
        Assertions.assertFalse(gateFormat.isIronDoorBlockable());
    }

    @Test
    void getControlBlocks() {
        Assertions.assertFalse(gateFormat.getControlBlocks().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getIrisMaterial(boolean open) {
        Assertions.assertNotNull(gateFormat.getIrisMaterial(open));
    }

    @Test
    void getExit() {
        Assertions.assertNotNull(gateFormat.getExit());
    }

    @Test
    void getFileName() {
        Assertions.assertNotNull(gateFormat.getFileName());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getControlMaterials(boolean open) {
        Assertions.assertNotNull(gateFormat.getIrisMaterial(open));
    }

    @ParameterizedTest
    @EnumSource
    void getStructure(GateFormatStructureType structureType) {
        Assertions.assertNotNull(gateFormat.getStructure(structureType));
    }
}