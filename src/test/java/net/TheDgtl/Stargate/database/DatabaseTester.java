package net.TheDgtl.Stargate.database;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.portal.FakePortal;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;

import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Test class for shared database tests to prevent duplication
 *
 * @author Kristian
 */
public class DatabaseTester {
    private static Database database;
    private static Connection connection;
    private static SQLQueryGenerator generator;

    private static IPortal testPortal;
    private static IPortal testInterPortal;
    private static TableNameConfig nameConfig;
    private static boolean isMySQL;
    private static String serverName;
    private static UUID serverUUID;
    private static String serverPrefix;

    /**
     * Instantiates a new database tester
     *
     * @param database        <p>The database to use for tests</p>
     * @param connection      <p>The database connection to use for tests</p>
     * @param generator       <p>The SQL Query generator to use for generating test queries</p>
     * @param testPortal      <p>A normal portal to use for testing</p>
     * @param testInterPortal <p>An inter-server portal to use for testing</p>
     * @param nameConfig      <p>The config containing all table names</p>
     * @throws NameError 
     */
    public DatabaseTester(Database database, Connection connection, TableNameConfig nameConfig, SQLQueryGenerator generator, boolean isMySQL) throws NameError {
        DatabaseTester.database = database;
        DatabaseTester.connection = connection;
        DatabaseTester.generator = generator;
        DatabaseTester.isMySQL = isMySQL;
        DatabaseTester.nameConfig = nameConfig;
        
        ServerMock server = MockBukkit.mock();
        WorldMock world = new WorldMock(Material.DIRT, 5);
        server.addWorld(world);
        
        DatabaseTester.serverName = "aServerName";
        DatabaseTester.serverUUID = UUID.randomUUID();
        Stargate.serverUUID = serverUUID;
        DatabaseTester.serverPrefix = "aPrefix";
        
        Network testNetwork = new Network("test", database, generator);
        DatabaseTester.testPortal = new FakePortal(world.getBlockAt(0, 0, 0).getLocation(), "portal",
                testNetwork, UUID.randomUUID());
        DatabaseTester.testInterPortal = new FakePortal(world.getBlockAt(0, 0, 0).getLocation(), "iPortal",
                testNetwork, UUID.randomUUID());
    }


    public static void tearDown() throws SQLException {
        connection.close();
    }

    void addPortalTableTest() throws SQLException {
        finishStatement(generator.generateCreatePortalTableStatement(connection, PortalType.LOCAL));
    }

    void addInterPortalTableTest() throws SQLException {
        finishStatement(generator.generateCreatePortalTableStatement(connection, PortalType.INTER_SERVER));
    }

    void createFlagTableTest() throws SQLException {
        finishStatement(generator.generateCreateFlagTableStatement(connection));
    }

    void createLastKnownNameTableTest() throws SQLException {
        finishStatement(generator.generateCreateLastKnownNameTableStatement(connection));
    }

    void createPortalFlagRelationTableTest() throws SQLException {
        finishStatement(generator.generateCreateFlagRelationTableStatement(connection, PortalType.LOCAL));
    }

    void createInterPortalFlagRelationTableTest() throws SQLException {
        finishStatement(generator.generateCreateFlagRelationTableStatement(connection, PortalType.INTER_SERVER));
    }

    void createPortalViewTest() throws SQLException {
        finishStatement(generator.generateCreatePortalViewStatement(connection, PortalType.LOCAL));
    }

    void createInterPortalViewTest() throws SQLException {
        finishStatement(generator.generateCreatePortalViewStatement(connection, PortalType.INTER_SERVER));
    }

    void createServerInfoTableTest() throws SQLException {
        finishStatement(generator.generateCreateServerInfoTableStatement(connection));
    }

    void addFlagsTest() throws SQLException {
        PreparedStatement statement = generator.generateAddFlagStatement(connection);

        for (PortalFlag flag : PortalFlag.values()) {
            System.out.println("Adding flag " + flag.getLabel() + " to the database");
            statement.setString(1, String.valueOf(flag.getLabel()));
            statement.execute();
        }
        statement.close();
    }

    void getFlagsTest() throws SQLException {
        printTableInfo("SG_Test_Flag");

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

    void addPortalTest() throws SQLException {
        connection.setAutoCommit(false);
        try {
            finishStatement(generator.generateAddPortalStatement(connection, testPortal, PortalType.LOCAL));

            PreparedStatement addFlagStatement = generator.generateAddPortalFlagRelationStatement(connection,
                    PortalType.LOCAL);
            addFlags(addFlagStatement, testPortal);
            addFlagStatement.close();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException exception) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw exception;
        }
    }

    void addInterPortalTest() throws SQLException {
        connection.setAutoCommit(false);
        try {
            finishStatement(generator.generateAddPortalStatement(connection, testInterPortal, PortalType.INTER_SERVER));

            PreparedStatement addFlagStatement = generator.generateAddPortalFlagRelationStatement(connection,
                    PortalType.INTER_SERVER);
            addFlags(addFlagStatement, testInterPortal);
            addFlagStatement.close();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException exception) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw exception;
        }
    }

    /**
     * Adds flags for the given portal to the database
     *
     * @param addFlagStatement <p>The statement used to add flags</p>
     * @param portal           <p>The portal to add the flags of</p>
     * @throws SQLException <p>If unable to set the flags</p>
     */
    private void addFlags(PreparedStatement addFlagStatement, IPortal portal) throws SQLException {
        for (Character character : portal.getAllFlagsString().toCharArray()) {
            System.out.println("Adding flag " + character + " to portal: " + portal);
            addFlagStatement.setString(1, portal.getName());
            addFlagStatement.setString(2, portal.getNetwork().getName());
            addFlagStatement.setString(3, String.valueOf(character));
            addFlagStatement.execute();
        }
    }

    void getPortalTest() throws SQLException {
        getPortals(PortalType.LOCAL,null,null);
    }

    void getInterPortalTest() throws SQLException {
        getPortals(PortalType.INTER_SERVER,serverName,serverUUID);
    }

    /**
     * Gets the portals of the given type and asserts that at least one portal exists
     *
     * @param portalType <p>The type of portal to get</p>
     * @param serverName <p>The expected name of the server</p>
     * @param serverUUID <p>The expected serverUUID of the server </p>
     * @throws SQLException <p>If a database error occurs</p>
     */
    private void getPortals(PortalType portalType,String serverName, UUID serverUUID) throws SQLException {
        String tableName = portalType == PortalType.LOCAL ? nameConfig.getPortalViewName() :
                nameConfig.getInterPortalTableName();
        printTableInfo(tableName);

        PreparedStatement statement = generator.generateGetAllPortalsStatement(connection, portalType);

        ResultSet set = statement.executeQuery();
        ResultSetMetaData metaData = set.getMetaData();

        int rows = 0;
        while (set.next()) {
            rows++;
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                System.out.print(
                        metaData.getColumnName(i + 1) + " = " + set.getObject(i + 1) + ", ");
                
                //if(PortalType.INTER_SERVER == portalType && set.getString("serverId").equals(serverUUID.toString())) {
                //    Assertions.assertTrue(set.getString("serverName").equals(serverName));
                //}
            }
            System.out.println();
        }
        Assertions.assertTrue(rows > 0);
    }

    void destroyPortalTest() throws SQLException {
        destroyPortal(testPortal, PortalType.LOCAL);
    }

    void destroyInterPortalTest() throws SQLException {
        destroyPortal(testInterPortal, PortalType.INTER_SERVER);
    }

    void updateLastKnownNameTest() throws SQLException {
        UUID uuid = testPortal.getOwnerUUID();
        String lastKnownName = "AUserName";
        String lastKnownName2 = "AUserName2";
        PreparedStatement statement = generator.generateUpdateLastKnownNameStatement(connection);
        statement.setString(1, uuid.toString());
        statement.setString(2, lastKnownName);
        statement.execute();
        Assertions.assertEquals(lastKnownName, getLastKnownName(uuid));
        statement.setString(2, lastKnownName2);
        statement.execute();
        statement.close();
        Assertions.assertEquals(lastKnownName2, getLastKnownName(uuid));
    }

    /**
     * Gets the last known name from the database
     *
     * @param uuid <p>The uuid to get the last known name from</p>
     * @return <p>The last known name of the uuid</p>
     * @throws SQLException <p>If a database error occurs</p>
     */
    private String getLastKnownName(UUID uuid) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT lastKnownName FROM " +
                nameConfig.getLastKnownNameTableName() + " WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        result.next();
        String output = result.getString(1);
        statement.close();
        return output;
    }

    /**
     * Destroys a portal, and fails the assertion if the portal isn't properly destroyed
     *
     * @param portal     <p>The portal to destroy</p>
     * @param portalType <p>The type of the portal to destroy</p>
     * @throws SQLException <p>If a database error occurs</p>
     */
    private void destroyPortal(IPortal portal, PortalType portalType) throws SQLException {
        connection.setAutoCommit(false);

        try {
            PreparedStatement removeFlagsStatement = generator.generateRemoveFlagStatement(connection, portalType);
            removeFlagsStatement.setString(1, portal.getName());
            removeFlagsStatement.setString(2, portal.getNetwork().getName());
            finishStatement(removeFlagsStatement);
            finishStatement(generator.generateRemovePortalStatement(connection, portal, portalType));
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException exception) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw exception;
        }
        connection.setAutoCommit(true);

        String flagTable = portalType == PortalType.LOCAL ? nameConfig.getFlagRelationTableName() :
                nameConfig.getInterFlagRelationTableName();
        checkIfHasNot(flagTable, portal.getName(), portal.getNetwork().getName());

        String table = portalType == PortalType.LOCAL ? nameConfig.getPortalTableName() :
                nameConfig.getInterPortalTableName();
        checkIfHasNot(table, portal.getName(), portal.getNetwork().getName());
    }
    
    public void updateServerInfoTest() throws SQLException {
        PreparedStatement statement = generator.generateUpdateServerInfoStatus(connection, serverName, serverUUID, serverPrefix);
        finishStatement(statement);
    }
    
    /**
     * Checks if a table, where each element is identified by a name and a network does not contain an element
     *
     * @param table   <p>The name of the table to check</p>
     * @param name    <p>The name of the element to check for</p>
     * @param network <p>The network of the element to check for</p>
     * @throws SQLException <p>If unable to get data from the database</p>
     */
    private void checkIfHasNot(String table, String name, String network) throws SQLException {
        PreparedStatement statement = database.getConnection().prepareStatement("SELECT * FROM " + table +
                " WHERE name = ? AND network = ?");
        statement.setString(1, name);
        statement.setString(2, network);
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
        String statementMsg = String.format(!isMySQL ? "pragma table_info('%s');" : "DESCRIBE %s", tableName);

        PreparedStatement tableInfoStatement = connection.prepareStatement(statementMsg);
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
     * Deletes all table names used for testing
     *
     * @param nameConfig <p>The name config to get table names from</p>
     * @throws SQLException <p>If unable to delete one of the tables</p>
     */
    static void deleteAllTables(TableNameConfig nameConfig) throws SQLException {
        finishStatement(connection.prepareStatement("DROP VIEW IF EXISTS " + nameConfig.getInterPortalViewName()));
        finishStatement(connection.prepareStatement("DROP VIEW IF EXISTS " + nameConfig.getPortalViewName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getLastKnownNameTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getInterFlagRelationTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getFlagRelationTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getPortalTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getInterPortalTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getLastKnownNameTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getFlagTableName()));
    }

    /**
     * Finishes a prepared statement by executing and closing it
     *
     * @param statement <p>The prepared statement to finish</p>
     * @throws SQLException <p>If unable to finish the statement</p>
     */
    static void finishStatement(PreparedStatement statement) throws SQLException {
        statement.execute();
        statement.close();
    }


    

}
