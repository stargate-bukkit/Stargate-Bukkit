package org.sgrewritten.stargate.gate;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.network.portal.PortalData;
import org.sgrewritten.stargate.util.StorageMock;

import java.io.File;
import java.util.List;

class GateTest {

    private static Gate loadGate;
    private static Gate createdGate;
    private static final File testGatesDir = new File("src/test/resources/gates");

    @BeforeAll
    public static void setUp() throws InvalidStructureException, GateConflictException {
        ServerMock server = MockBukkit.mock();
        WorldMock world = server.addSimpleWorld("world");
        PortalData portalData = new PortalData();
        portalData.topLeft = new Location(world, 0, 6, 0);
        portalData.facing = BlockFace.SOUTH;
        portalData.gateFileName = "nether.gate";
        Block signBlock = PortalBlockGenerator.generatePortal(portalData.topLeft.clone().subtract(new Vector(0, 4, 0)));
        List<GateFormat> gateFormats = GateFormatHandler.loadGateFormats(testGatesDir, new FakeStargateLogger());
        if (gateFormats == null) {
            throw new IllegalStateException("Cannot get gate formats required for testing");
        }
        GateFormatHandler.setFormats(gateFormats);

        loadGate = new Gate(portalData, new StargateRegistry(new StorageMock()));
        GateFormat format = GateFormatHandler.getFormat(portalData.gateFileName);
        // Note that this is created in a different registry, to avoid any conflicts
        createdGate = new Gate(format, signBlock.getLocation(), portalData.facing, false, new StargateRegistry(new StorageMock()));
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void isValid_LoadedGate() throws GateConflictException {
        Assertions.assertTrue(loadGate.isValid(false));
    }

    @Test
    public void isValid_CreatedGate() throws GateConflictException {
        Assertions.assertTrue(createdGate.isValid(false));
    }
}
