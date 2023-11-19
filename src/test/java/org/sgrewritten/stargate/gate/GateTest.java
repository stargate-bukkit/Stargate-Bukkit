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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.network.RegistryMock;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.network.portal.portaldata.GateData;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;

import java.io.File;
import java.util.List;

class GateTest {

    private final File testGatesDir = new File("src/test/resources/gates");
    private @NotNull WorldMock world;
    private GateData gateData;
    private Block signBlock;
    private String gateFileName;

    @BeforeEach
    void setUp() throws InvalidStructureException, GateConflictException {
        List<GateFormat> gateFormats = GateFormatHandler.loadGateFormats(testGatesDir);
        if (gateFormats == null) {
            throw new IllegalStateException("Cannot get gate formats required for testing");
        }
        GateFormatRegistry.setFormats(gateFormats);
        ServerMock server = MockBukkit.mock();
        this.world = server.addSimpleWorld("world");
        Location topLeft = new Location(world, 0, 6, 0);
        BlockFace facing = BlockFace.SOUTH;
        this.gateFileName = "nether.gate";
        this.gateData = new GateData(GateFormatRegistry.getFormat(gateFileName),false,topLeft,facing);
        this.signBlock = PortalBlockGenerator.generatePortal(gateData.topLeft().clone().subtract(new Vector(0, 4, 0)));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void isValid_LoadedGate() throws GateConflictException, InvalidStructureException {
        Assertions.assertTrue(createLoadedGate(gateData).isValid());
    }

    @Test
    void isValid_CreatedGate() throws GateConflictException, InvalidStructureException {
        Assertions.assertTrue(createCreatedGate(gateData).isValid());
    }
    
    @Test
    void addAndRemovePortalPosition_loadedGate() throws InvalidStructureException {
        Gate loadedGate = createLoadedGate(gateData);
        Location location = new Location(world,0,0,0);
        loadedGate.addPortalPosition(location, PositionType.BUTTON, "Stargate");
        Assertions.assertTrue(gatePositionIsAdded(location, loadedGate),"A gate position was not added");
        loadedGate.removePortalPosition(location);
        Assertions.assertFalse(gatePositionIsAdded(location, loadedGate),"A gate position was not added");
    }
    
    @Test
    void addAndRemovePortalPosition_createdGate() throws InvalidStructureException, GateConflictException {
        Gate createdGate = createCreatedGate(gateData);
        Location location = new Location(world,0,0,0);
        createdGate.addPortalPosition(location, PositionType.BUTTON, "Stargate");
        Assertions.assertTrue(gatePositionIsAdded(location, createdGate),"A gate position was not added");
        createdGate.removePortalPosition(location);
        Assertions.assertFalse(gatePositionIsAdded(location, createdGate),"A gate position was not added");
    }

    @ParameterizedTest
    @EnumSource(value = BlockFace.class, names = {"EAST", "WEST", "SOUTH", "NORTH"})
    void createGateStructure(BlockFace facing) throws InvalidStructureException, GateConflictException {
        Location topLeft = new Location(world, 100,10,100);
        new GateData(GateFormatRegistry.getFormat("nether.gate"),false,topLeft,facing);
        Gate gate = createLoadedGate(gateData);
        gate.forceGenerateStructure();
        Assertions.assertTrue(gate.isValid(), "Gate was not created on a valid structure");
    }
    
    Gate createLoadedGate(GateData gateData) throws InvalidStructureException {
        return new Gate(gateData, new RegistryMock());
    }
    
    Gate createCreatedGate(GateData gateData) throws InvalidStructureException, GateConflictException {
        return new Gate(gateData.gateFormat(), signBlock.getLocation(), gateData.facing(), false, new RegistryMock());
    }
    
    boolean gatePositionIsAdded(Location location, Gate gate) {
        BlockVector vector = gate.getRelativeVector(location).toBlockVector();
        for(PortalPosition portalPosition : gate.getPortalPositions()){
            if(portalPosition.getRelativePositionLocation().equals(vector)) {
                return true;
            }
        }
        return false;
    }
}
