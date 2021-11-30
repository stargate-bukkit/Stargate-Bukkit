package net.TheDgtl.Stargate.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.SQLQueryGenerator;
import net.TheDgtl.Stargate.network.portal.IPortal;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;

public class SQLiteDatabaseTest {

    private static Database database;
    private static Connection connection;
    private static WorldMock world;
    private static SQLQueryGenerator generator;

    @BeforeAll
    public static void setUp() throws SQLException {
        System.out.println("Setting up test data");
        ServerMock server = MockBukkit.mock();
        world = new WorldMock(Material.DIRT, 5);
        server.addWorld(world);

        database = new SQLiteDatabase(new File("test.db"));
        connection = database.getConnection();
        generator = new SQLQueryGenerator("Portals", new FakeStargate());
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DROP TABLE IF EXISTS SG_Hub_Portals;");
        finishStatement(statement);
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
    void addPortalTest() throws NameError, SQLException {
        Network network = new Network("test", database, generator);
        IPortal portal = new FakePortal(world.getBlockAt(0, 0, 0).getLocation(), "portal", network,
                UUID.randomUUID());
        finishStatement(generator.generateAddPortalStatement(connection, portal, PortalType.LOCAL));
    }

    @Test
    @Order(3)
    void getPortalTest() throws SQLException {
        PreparedStatement statement = generator.generateGetAllPortalsStatement(connection, PortalType.LOCAL);

        ResultSet set = statement.executeQuery();
        ResultSetMetaData metaData = set.getMetaData();
        
        int rows = 0;
        while (set.next()) {
            rows++;
            for (int i = 1; i < metaData.getColumnCount() - 1; i++) {
                System.out.println(set.getObject(i));
            }
        }
        Assertions.assertTrue(rows > 0);
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
