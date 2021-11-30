package net.TheDgtl.Stargate.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.SQLQueryGenerator;
import net.TheDgtl.Stargate.network.portal.IPortal;
import org.bukkit.Material;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;

public class SQLiteDatabaseTest {

    public static Database database;

    @BeforeAll
    public static void setUp() {
        ServerMock server = MockBukkit.mock();
        server.addWorld(new WorldMock(Material.DIRT, 5));
        MockBukkit.load(Stargate.class);
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @Order(1)
    public void connectionTest() throws SQLException {
        Database database = new SQLiteDatabase(new File("test.db"));
        Assert.assertNotNull(database.getConnection());
        SQLiteDatabaseTest.database = database;
    }

    @Test
    @Order(2)
    public void addTableTest() throws SQLException {
        Database database = new SQLiteDatabase(new File("test.db"));
        SQLQueryGenerator generator = new SQLQueryGenerator("portals", new FakeStargate());
        finishStatement(generator.generateCreateTableStatement(database.getConnection(), PortalType.LOCAL));
    }

    @Test
    @Order(3)
    public void addPortalTest() throws SQLException, NameError {
        Database database = new SQLiteDatabase(new File("test.db"));
        SQLQueryGenerator generator = new SQLQueryGenerator("portals", new FakeStargate());
        Network network = new Network("test", database, generator);
        WorldMock worldMock = new WorldMock(Material.DIRT, 5);
        IPortal portal = new FakePortal(worldMock.getBlockAt(0, 0, 0).getLocation(), "portal", network, UUID.randomUUID());
        finishStatement(generator.generateAddPortalStatement(database.getConnection(), portal, PortalType.LOCAL));
    }

    @Test
    @Order(4)
    public void getPortalTest() throws SQLException {
        Database database = new SQLiteDatabase(new File("test.db"));
        SQLQueryGenerator generator = new SQLQueryGenerator("portals", new FakeStargate());
        PreparedStatement statement = generator.generateGetAllPortalsStatement(database.getConnection(), PortalType.LOCAL);

        ResultSet set = statement.executeQuery();
        ResultSetMetaData metaData = set.getMetaData();
        while (set.next()) {
            for (int i = 1; i < metaData.getColumnCount() - 1; i++) {
                System.out.println(set.getObject(i));
            }

        }
    }

    private void finishStatement(PreparedStatement statement) throws SQLException {
        statement.execute();
        statement.close();
    }

    @AfterAll
    public static void cleanUp() throws SQLException {
        PreparedStatement statement = database.getConnection().prepareStatement("DROP TABLE portals;");
        statement.executeQuery();
        statement.close();
        database.getConnection().close();
    }

}
