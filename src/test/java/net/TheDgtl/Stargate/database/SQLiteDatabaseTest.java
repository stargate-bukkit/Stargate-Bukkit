package net.TheDgtl.Stargate.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import net.TheDgtl.Stargate.FakeStargate;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.SQLQueryGenerator;
import net.TheDgtl.Stargate.network.portal.FakePortal;
import net.TheDgtl.Stargate.network.portal.IPortal;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static net.TheDgtl.Stargate.database.DatabaseTester.finishStatement;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLiteDatabaseTest {

    private static Connection connection;
    private static DatabaseTester tester;

    @BeforeAll
    public static void setUp() throws SQLException, NameError {
        System.out.println("Setting up test data");
        ServerMock server = MockBukkit.mock();
        WorldMock world = new WorldMock(Material.DIRT, 5);
        server.addWorld(world);

        Database database = new SQLiteDatabase(new File("test.db"));
        connection = database.getConnection();
        SQLQueryGenerator generator = new SQLQueryGenerator("Portal", "InterPortal",
                new FakeStargate());

        Network testNetwork = new Network("test", database, generator);
        IPortal testPortal = new FakePortal(world.getBlockAt(0, 0, 0).getLocation(), "portal",
                testNetwork, UUID.randomUUID());
        IPortal testInterPortal = new FakePortal(world.getBlockAt(0, 0, 0).getLocation(), "iportal",
                testNetwork, UUID.randomUUID());
        tester = new DatabaseTester(database, connection, generator, testPortal, testInterPortal);
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        finishStatement(connection.prepareStatement("DROP VIEW IF EXISTS SG_Hub_InterPortalView"));
        finishStatement(connection.prepareStatement("DROP VIEW IF EXISTS SG_Hub_PortalView"));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS SG_Hub_InterPortalFlagRelation"));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS SG_Hub_PortalFlagRelation"));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS SG_Hub_Portal;"));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS SG_Hub_InterPortal;"));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS SG_Hub_Flag;"));
        connection.close();
        MockBukkit.unmock();
        System.out.println("Tearing down test data");
    }

    @Test
    @Order(1)
    void addPortalTableTest() throws SQLException {
        tester.addPortalTableTest();
    }

    @Test
    @Order(2)
    void addInterPortalTableTest() throws SQLException {
        tester.addInterPortalTableTest();
    }

    @Test
    @Order(3)
    void createFlagTableTest() throws SQLException {
        tester.createFlagTableTest();
    }

    @Test
    @Order(4)
    void createPortalFlagRelationTableTest() throws SQLException {
        tester.createPortalFlagRelationTableTest();
    }

    @Test
    @Order(5)
    void createInterPortalFlagRelationTableTest() throws SQLException {
        tester.createInterPortalFlagRelationTableTest();
    }

    @Test
    @Order(6)
    void createPortalViewTest() throws SQLException {
        tester.createPortalViewTest();
    }

    @Test
    @Order(7)
    void createInterPortalViewTest() throws SQLException {
        tester.createInterPortalViewTest();
    }

    @Test
    @Order(8)
    void addFlagsTest() throws SQLException {
        tester.addFlagsTest();
    }

    @Test
    @Order(9)
    void getFlagsTest() throws SQLException {
        tester.getFlagsTest();
    }

    @Test
    @Order(10)
    void addPortalTest() throws SQLException {
        tester.addPortalTest();
    }

    @Test
    @Order(11)
    void addInterPortalTest() throws SQLException {
        tester.addInterPortalTest();
    }

    @Test
    @Order(12)
    void getPortalTest() throws SQLException {
        tester.getPortalTest();
    }

    @Test
    @Order(13)
    void getInterPortalTest() throws SQLException {
        tester.getInterPortalTest();
    }

    @Test
    @Order(14)
    void destroyPortalTest() throws SQLException {
        tester.destroyPortalTest();
    }

    @Test
    @Order(15)
    void destroyInterPortalTest() throws SQLException {
        tester.destroyInterPortalTest();
    }

}
