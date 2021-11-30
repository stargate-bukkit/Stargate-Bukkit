package net.TheDgtl.Stargate.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.SQLQueryGenerator;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLiteDatabaseTest {

    private static Database database;
    private static Connection connection;
    private static WorldMock world;
    private static SQLQueryGenerator generator;

    private static Network testNetwork;
    private static IPortal testPortal;

    @BeforeAll
    public static void setUp() throws SQLException, NameError {
        System.out.println("Setting up test data");
        ServerMock server = MockBukkit.mock();
        world = new WorldMock(Material.DIRT, 5);
        server.addWorld(world);

        database = new SQLiteDatabase(new File("test.db"));
        connection = database.getConnection();
        generator = new SQLQueryGenerator("Portal", new FakeStargate());

        testNetwork = new Network("test", database, generator);
        testPortal = new FakePortal(world.getBlockAt(0, 0, 0).getLocation(), "portal", testNetwork,
                UUID.randomUUID());
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        finishStatement(connection.prepareStatement("DROP VIEW IF EXISTS SG_Hub_PortalView"));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS SG_Hub_PortalFlagRelation"));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS SG_Hub_Portal;"));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS SG_Hub_Flag;"));
        connection.close();
        MockBukkit.unmock();
        System.out.println("Tearing down test data");
    }

    @Test
    @Order(1)
    void addTableTest() throws SQLException {
        finishStatement(generator.generateCreateTableStatement(connection, PortalType.LOCAL));
    }

    @Test
    @Order(2)
    void createFlagTableTest() throws SQLException {
        finishStatement(generator.generateCreateFlagTableStatement(connection));
    }

    @Test
    @Order(3)
    void createPortalFlagRelationTableTest() throws SQLException {
        finishStatement(generator.generateCreateFlagRelationTableStatement(connection));
    }

    @Test
    @Order(4)
    void createPortalViewTest() throws SQLException {
        finishStatement(generator.generateCreatePortalViewTableStatement(connection));
    }

    @Test
    @Order(5)
    void addFlagsTest() throws SQLException {
        PreparedStatement statement = generator.generateAddFlagStatement(connection);

        for (PortalFlag flag : PortalFlag.values()) {
            System.out.println("Adding flag " + flag.getLabel() + " to the database");
            statement.setString(1, String.valueOf(flag.getLabel()));
            statement.execute();
        }
        statement.close();
    }

    @Test
    @Order(6)
    void getFlagsTest() throws SQLException {
        printTableInfo("SG_Hub_Flag");

        PreparedStatement statement = generator.generateGetAllFlagsStatement(connection);

        ResultSet set = statement.executeQuery();
        ResultSetMetaData metaData = set.getMetaData();

        int rows = 0;
        while (set.next()) {
            System.out.print("Flag ");
            rows++;
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                System.out.print(metaData.getColumnName(i + 1) + " = " + set.getObject(i + 1) + ", ");
            }
            System.out.println();
        }
        Assertions.assertTrue(rows > 0);
    }

    @Test
    @Order(7)
    void addPortalTest() throws SQLException {
        connection.setAutoCommit(false);
        try {
            finishStatement(generator.generateAddPortalStatement(connection, testPortal, PortalType.LOCAL));
            
            PreparedStatement addFlagStatement = generator.generateAddPortalFlagRelationStatement(connection);
            for (Character character : testPortal.getAllFlagsString().toCharArray()) {
                System.out.println("Adding flag " + character + " to portal: " + testPortal.toString());
                addFlagStatement.setString(1, testPortal.getName());
                addFlagStatement.setString(2, testPortal.getNetwork().getName());
                addFlagStatement.setString(3, String.valueOf(character));
                addFlagStatement.execute();
            }
            addFlagStatement.close();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException exception) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw exception;
        }
    }

    @Test
    @Order(8)
    void getPortalTest() throws SQLException {
        printTableInfo("SG_Hub_PortalView");

        PreparedStatement statement = generator.generateGetAllPortalsStatement(connection, PortalType.LOCAL);

        ResultSet set = statement.executeQuery();
        ResultSetMetaData metaData = set.getMetaData();

        int rows = 0;
        while (set.next()) {
            rows++;
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                System.out.print(
                        metaData.getColumnName(i + 1) + " = " + set.getObject(i + 1) + ", ");
            }
            System.out.println();
        }
        Assertions.assertTrue(rows > 0);
    }

    @Test
    @Order(9)
    void destroyPortalTest() throws SQLException {
        finishStatement(generator.generateRemovePortalStatement(connection, testPortal, PortalType.LOCAL));

        PreparedStatement statement = database.getConnection().prepareStatement("SELECT * FROM SG_Hub_Portal"
                + " WHERE name = ? AND network = ?");
        statement.setString(1, testPortal.getName());
        statement.setString(2, testPortal.getNetwork().getName());
        ResultSet set = statement.executeQuery();
        Assertions.assertFalse(set.next());
    }

    /**
     * Prints info about a table for debugging
     *
     * @param tableName <p>The table to get information about</p>
     * @throws SQLException <p>If unable to get information about the table</p>
     */
    private static void printTableInfo(String tableName) throws SQLException {
        System.out.println("Getting table info for: " + tableName);
        PreparedStatement tableInfoStatement = connection.prepareStatement(String.format("pragma table_info('%s');", tableName));
        ResultSet infoResult = tableInfoStatement.executeQuery();
        ResultSetMetaData infoMetaData = infoResult.getMetaData();
        while (infoResult.next()) {
            for (int i = 1; i < infoMetaData.getColumnCount() - 1; i++) {
                System.out.print(
                        infoMetaData.getColumnName(i) + " = " + infoResult.getObject(i) + ", ");
            }
            System.out.println();
        }
    }

    /**
     * Finishes a prepared statement by executing and closing it
     *
     * @param statement <p>The prepared statement to finish</p>
     * @throws SQLException <p>If unable to finish the statement</p>
     */
    private static void finishStatement(PreparedStatement statement) throws SQLException {
        statement.execute();
        statement.close();
    }

}
