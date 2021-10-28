package net.knarcraft.stargate.portal;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.RelativeBlockVector;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GateLayoutTest {

    private static GateLayout layout;

    @BeforeAll
    public static void setUp() {
        ServerMock server = MockBukkit.mock();
        server.addWorld(new WorldMock(Material.DIRT, 5));
        MockBukkit.load(Stargate.class);
        layout = GateHandler.getGateByName("nethergate.gate").getLayout();
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void gateLayoutExitTest() {
        assertEquals(new RelativeBlockVector(1, 3, 0), layout.getExit());
    }

    @Test
    public void gateLayoutExitsTest() {
        List<RelativeBlockVector> expected = new ArrayList<>();
        expected.add(new RelativeBlockVector(1, 3, 0));
        expected.add(new RelativeBlockVector(2, 3, 0));

        List<RelativeBlockVector> exits = layout.getExits();
        exits.forEach((blockVector) -> assertTrue(expected.contains(blockVector)));
    }

    @Test
    public void gateLayoutBorderTest() {
        List<RelativeBlockVector> expected = new ArrayList<>();
        expected.add(new RelativeBlockVector(1, 0, 0));
        expected.add(new RelativeBlockVector(2, 0, 0));
        expected.add(new RelativeBlockVector(0, 1, 0));
        expected.add(new RelativeBlockVector(0, 2, 0));
        expected.add(new RelativeBlockVector(0, 3, 0));
        expected.add(new RelativeBlockVector(1, 4, 0));
        expected.add(new RelativeBlockVector(2, 4, 0));
        expected.add(new RelativeBlockVector(3, 1, 0));
        expected.add(new RelativeBlockVector(3, 2, 0));
        expected.add(new RelativeBlockVector(3, 3, 0));

        RelativeBlockVector[] borderBlocks = layout.getBorder();
        for (RelativeBlockVector blockVector : borderBlocks) {
            assertTrue(expected.contains(blockVector));
        }
    }

    @Test
    public void gateLayoutControlsTest() {
        List<RelativeBlockVector> expected = new ArrayList<>();
        expected.add(new RelativeBlockVector(0, 2, 0));
        expected.add(new RelativeBlockVector(3, 2, 0));

        RelativeBlockVector[] controlBlocks = layout.getControls();
        for (RelativeBlockVector blockVector : controlBlocks) {
            assertTrue(expected.contains(blockVector));
        }
    }

    @Test
    public void gateLayoutEntrancesTest() {
        List<RelativeBlockVector> expected = new ArrayList<>();
        expected.add(new RelativeBlockVector(1, 1, 0));
        expected.add(new RelativeBlockVector(2, 1, 0));
        expected.add(new RelativeBlockVector(1, 2, 0));
        expected.add(new RelativeBlockVector(2, 2, 0));
        expected.add(new RelativeBlockVector(1, 3, 0));
        expected.add(new RelativeBlockVector(2, 3, 0));

        RelativeBlockVector[] controlBlocks = layout.getEntrances();
        for (RelativeBlockVector blockVector : controlBlocks) {
            assertTrue(expected.contains(blockVector));
        }
    }

}
