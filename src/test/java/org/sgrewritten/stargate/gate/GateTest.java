package org.sgrewritten.stargate.gate;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.api.PositionType;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.network.portal.portaldata.GateData;
import org.sgrewritten.stargate.network.portal.portaldata.PortalData;
import org.sgrewritten.stargate.network.portal.PortalPosition;
import org.sgrewritten.stargate.util.StorageMock;

import java.io.File;
import java.util.List;

class GateTest {

    private final File testGatesDir = new File("src/test/resources/gates");
    private @NotNull WorldMock world;
    private GateData gateData;
    private Block signBlock;

    @BeforeEach
    void setUp() throws InvalidStructureException, GateConflictException {
        ServerMock server = MockBukkit.mock();
        this.world = server.addSimpleWorld("world");
        Location topLeft = new Location(world, 0, 6, 0);
        BlockFace facing = BlockFace.SOUTH;
        String gateFileName = "nether.gate";
        this.gateData = new GateData(gateFileName, topLeft.getBlockX(), topLeft.getBlockY(), topLeft.getBlockZ(), topLeft.getWorld().getName(),false,topLeft,facing);
        this.signBlock = PortalBlockGenerator.generatePortal(gateData.topLeft().clone().subtract(new Vector(0, 4, 0)));
        List<GateFormat> gateFormats = GateFormatHandler.loadGateFormats(testGatesDir, new FakeStargateLogger());
        if (gateFormats == null) {
            throw new IllegalStateException("Cannot get gate formats required for testing");
        }
        GateFormatHandler.setFormats(gateFormats);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void isValid_LoadedGate() throws GateConflictException, InvalidStructureException {
        Assertions.assertTrue(createLoadedGate(gateData).isValid(false));
    }

    @Test
    void isValid_CreatedGate() throws GateConflictException, InvalidStructureException {
        Assertions.assertTrue(createCreatedGate(gateData).isValid(false));
    }
    
    @Test
    void addAndRemovePortalPosition_loadedGate() throws InvalidStructureException {
        Gate loadedGate = createLoadedGate(gateData);
        Location location = new Location(world,0,0,0);
        loadedGate.addPortalPosition(location, PositionType.BUTTON);
        Assertions.assertTrue(gatePositionIsAdded(location, loadedGate),"A gate position was not added");
        loadedGate.removePortalPosition(location);
        Assertions.assertFalse(gatePositionIsAdded(location, loadedGate),"A gate position was not added");
    }
    
    @Test
    void addAndRemovePortalPosition_createdGate() throws InvalidStructureException, GateConflictException {
        Gate createdGate = createCreatedGate(gateData);
        Location location = new Location(world,0,0,0);
        createdGate.addPortalPosition(location, PositionType.BUTTON);
        Assertions.assertTrue(gatePositionIsAdded(location, createdGate),"A gate position was not added");
        createdGate.removePortalPosition(location);
        Assertions.assertFalse(gatePositionIsAdded(location, createdGate),"A gate position was not added");
    }
    
    Gate createLoadedGate(GateData gateData) throws InvalidStructureException {
        return new Gate(gateData, new StargateRegistry(new StorageMock()));
    }
    
    Gate createCreatedGate(GateData gateData) throws InvalidStructureException, GateConflictException {
        GateFormat format = GateFormatHandler.getFormat(gateData.gateFileName());
        return new Gate(format, signBlock.getLocation(), gateData.facing(), false, new StargateRegistry(new StorageMock()));
    }
    
    boolean gatePositionIsAdded(Location location, Gate gate) {
        BlockVector vector = gate.getRelativeVector(location).toBlockVector();
        for(PortalPosition portalPosition : gate.getPortalPositions()){
            if(portalPosition.getPositionLocation().equals(vector)) {
                return true;
            }
        }
        return false;
    }
}
