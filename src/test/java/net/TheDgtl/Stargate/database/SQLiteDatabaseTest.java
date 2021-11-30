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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLiteDatabaseTest {

    private static Connection connection;
    private static DatabaseTester tester;
    private static TableNameConfig nameConfig;

    @BeforeAll
    public static void setUp() throws SQLException, NameError {
        System.out.println("Setting up test data");
        ServerMock server = MockBukkit.mock();
        WorldMock world = new WorldMock(Material.DIRT, 5);
        server.addWorld(world);

        Database database = new SQLiteDatabase(new File("test.db"));
        connection = database.getConnection();
        nameConfig = new TableNameConfig("SG_Test_", "Server_");
        SQLQueryGenerator generator = new SQLQueryGenerator(nameConfig, new FakeStargate());

        Network testNetwork = new Network("test", database, generator);
        IPortal testPortal = new FakePortal(world.getBlockAt(0, 0, 0).getLocation(), "portal",
                testNetwork, UUID.randomUUID());
        IPortal testInterPortal = new FakePortal(world.getBlockAt(0, 0, 0).getLocation(), "iportal",
                testNetwork, UUID.randomUUID());
        tester = new DatabaseTester(database, connection, generator, testPortal, testInterPortal, nameConfig);
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        DatabaseTester.deleteAllTables(nameConfig);
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
    @Order(1)
    void addInterPortalTableTest() throws SQLException {
        tester.addInterPortalTableTest();
    }

    @Test
    @Order(1)
    void createFlagTableTest() throws SQLException {
        tester.createFlagTableTest();
    }

    @Test
    @Order(1)
    void createLastKnownNameTableTest() throws SQLException {
        tester.createLastKnownNameTableTest();
    }

    @Test
    @Order(2)
    void createPortalFlagRelationTableTest() throws SQLException {
        tester.createPortalFlagRelationTableTest();
    }

    @Test
    @Order(2)
    void createInterPortalFlagRelationTableTest() throws SQLException {
        tester.createInterPortalFlagRelationTableTest();
    }

    @Test
    @Order(3)
    void createPortalViewTest() throws SQLException {
        tester.createPortalViewTest();
    }

    @Test
    @Order(3)
    void createInterPortalViewTest() throws SQLException {
        tester.createInterPortalViewTest();
    }

    @Test
    @Order(3)
    void addFlagsTest() throws SQLException {
        tester.addFlagsTest();
    }

    @Test
    @Order(4)
    void getFlagsTest() throws SQLException {
        tester.getFlagsTest();
    }

    @Test
    @Order(5)
    void addPortalTest() throws SQLException {
        tester.addPortalTest();
    }

    @Test
    @Order(5)
    void addInterPortalTest() throws SQLException {
        tester.addInterPortalTest();
    }

    @Test
    @Order(6)
    void getPortalTest() throws SQLException {
        tester.getPortalTest();
    }

    @Test
    @Order(6)
    void getInterPortalTest() throws SQLException {
        tester.getInterPortalTest();
    }

    @Test
    @Order(7)
    void destroyPortalTest() throws SQLException {
        tester.destroyPortalTest();
    }

    @Test
    @Order(7)
    void destroyInterPortalTest() throws SQLException {
        tester.destroyInterPortalTest();
    }

}
