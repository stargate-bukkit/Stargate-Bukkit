package net.TheDgtl.Stargate.database;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.portal.FakePortal;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;

import org.bukkit.Location;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test class for shared database tests to prevent duplication
 *
 * @author Kristian
 */
public class DatabaseTester {
    private static Database database;
    private static SQLQueryGenerator generator;

    private static TableNameConfig nameConfig;
    private static boolean isMySQL;
    private static String serverName;
    private static UUID serverUUID;
    private static String serverPrefix;
    private static FakePortal testPortal;
    private static final String INTER_PORTAL_NAME = "iPortal";
    private static final String LOCAL_PORTAL_NAME = "portal";
    private HashMap<String,FakePortal> interServerPortals;
    private HashMap<String,FakePortal> localPortals;

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
    public DatabaseTester(Database database, TableNameConfig nameConfig, SQLQueryGenerator generator, boolean isMySQL) throws NameError {
        DatabaseTester.database = database;
        DatabaseTester.generator = generator;
        DatabaseTester.isMySQL = isMySQL;
        DatabaseTester.nameConfig = nameConfig;
        
        ServerMock server = MockBukkit.mock();
        WorldMock world = new WorldMock(Material.DIRT, 5);
        server.addWorld(world);
        
        int interServerPortalTestLength = 4;
        int localPortalTestLength = 5;
        
        
        DatabaseTester.serverName = "aServerName";
        DatabaseTester.serverUUID = UUID.randomUUID();
        Stargate.serverUUID = serverUUID;
        DatabaseTester.serverPrefix = "aPrefix";
        
        Network testNetwork = new Network("test", database, generator);
        this.interServerPortals = generatePortals(world,testNetwork,true,interServerPortalTestLength);
        this.localPortals = generatePortals(world,testNetwork,false,localPortalTestLength);
        DatabaseTester.testPortal = generateFakePortal(world,testNetwork,"testPortal",false);
        
    }
    
    private HashMap<String,FakePortal> generatePortals(WorldMock world, Network testNetwork, boolean isInterserver, int listSize){
        HashMap<String,FakePortal> output = new HashMap<>();
        for(int i = 0; i < listSize; i++) {
            String name = (isInterserver?DatabaseTester.INTER_PORTAL_NAME:DatabaseTester.LOCAL_PORTAL_NAME)+i;
            FakePortal portal = generateFakePortal(world,testNetwork, name, isInterserver);
            output.put(portal.getName(),portal);
        }
        return output;
    }

    private FakePortal generateFakePortal(WorldMock world, Network testNetwork, String name, boolean isInterserver) {
        EnumSet<PortalFlag> flags = generateRandomFlags();
        if(isInterserver)
            flags.add(PortalFlag.FANCY_INTER_SERVER);
        return new FakePortal(world.getBlockAt(0, 0, 0).getLocation(), name,
                testNetwork, UUID.randomUUID(), flags);
    }
    
    private EnumSet<PortalFlag> generateRandomFlags(){
        PortalFlag[] possibleFlags = PortalFlag.values();
        int flagAmount = ThreadLocalRandom.current().nextInt(0,possibleFlags.length);
        EnumSet<PortalFlag> flags = EnumSet.noneOf(PortalFlag.class);
        for(int i = 0; i < flagAmount; i++) {
            int flagType = ThreadLocalRandom.current().nextInt(0,possibleFlags.length);
            flags.add(possibleFlags[flagType]);
        }
        return flags;
    }
    
    private static Connection getConnection() throws SQLException {
        return database.getConnection();
    }

    void addPortalTableTest() throws SQLException {
        Connection connection = getConnection();
        finishStatement(generator.generateCreatePortalTableStatement(connection, PortalType.LOCAL));
        connection.close();
    }

    void addInterPortalTableTest() throws SQLException {
        Connection connection = getConnection();
        finishStatement(generator.generateCreatePortalTableStatement(connection, PortalType.INTER_SERVER));
        connection.close();
    }

    void createFlagTableTest() throws SQLException {
        Connection connection = getConnection();
        finishStatement(generator.generateCreateFlagTableStatement(connection));
        connection.close();
    }

    void createLastKnownNameTableTest() throws SQLException {
        Connection connection = getConnection();
        finishStatement(generator.generateCreateLastKnownNameTableStatement(connection));
        connection.close();
    }

    void createPortalFlagRelationTableTest() throws SQLException {
        Connection connection = getConnection();
        finishStatement(generator.generateCreateFlagRelationTableStatement(connection, PortalType.LOCAL));
        connection.close();
    }

    void createInterPortalFlagRelationTableTest() throws SQLException {
        Connection connection = getConnection();
        finishStatement(generator.generateCreateFlagRelationTableStatement(connection, PortalType.INTER_SERVER));
        connection.close();
    }

    void createPortalViewTest() throws SQLException {
        Connection connection = getConnection();
        finishStatement(generator.generateCreatePortalViewStatement(connection, PortalType.LOCAL));
        connection.close();
    }

    void createInterPortalViewTest() throws SQLException {
        Connection connection = getConnection();
        finishStatement(generator.generateCreatePortalViewStatement(connection, PortalType.INTER_SERVER));
        connection.close();
    }

    void createServerInfoTableTest() throws SQLException {
        Connection connection = getConnection();
        finishStatement(generator.generateCreateServerInfoTableStatement(connection));
        connection.close();
    }
    
    void addFlagsTest() throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = generator.generateAddFlagStatement(connection);

        for (PortalFlag flag : PortalFlag.values()) {
            System.out.println("Adding flag " + flag.getLabel() + " to the database");
            statement.setString(1, String.valueOf(flag.getLabel()));
            statement.execute();
        }
        statement.close();
        connection.close();
    }

    void getFlagsTest() throws SQLException {
        Connection connection = getConnection();
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
        connection.close();
        Assertions.assertTrue(rows > 0);
    }

    void addPortalTest() throws SQLException {
        Connection connection = getConnection();
        for (FakePortal portal : localPortals.values()) {
            connection.setAutoCommit(false);
            try {
                finishStatement(generator.generateAddPortalStatement(connection, portal, PortalType.LOCAL));

                PreparedStatement addFlagStatement = generator.generateAddPortalFlagRelationStatement(connection,
                        PortalType.LOCAL);
                addFlags(addFlagStatement, portal);
                addFlagStatement.close();
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException exception) {
                connection.rollback();
                connection.setAutoCommit(true);
                throw exception;
            }
        }
        connection.close();
    }

    void addInterPortalTest() throws SQLException {
        Connection connection = getConnection();
        for(FakePortal portal : interServerPortals.values()) {
            connection.setAutoCommit(false);
            try {
                finishStatement(
                        generator.generateAddPortalStatement(connection, portal, PortalType.INTER_SERVER));

                PreparedStatement addFlagStatement = generator.generateAddPortalFlagRelationStatement(connection,
                        PortalType.INTER_SERVER);
                addFlags(addFlagStatement, portal);
                addFlagStatement.close();
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException exception) {
                connection.rollback();
                connection.setAutoCommit(true);
                throw exception;
            }
        }
        connection.close();
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
        getPortals(PortalType.LOCAL,localPortals);
    }

    void getInterPortalTest() throws SQLException {
        getPortals(PortalType.INTER_SERVER,interServerPortals);
    }

    /**
     * Gets the portals of the given type and asserts that at least one portal exists
     *
     * @param portalType <p>The type of portal to get</p>
     * @param serverName <p>The expected name of the server</p>
     * @param serverUUID <p>The expected serverUUID of the server </p>
     * @throws SQLException <p>If a database error occurs</p>
     */
    private void getPortals(PortalType portalType,HashMap<String,FakePortal> portals) throws SQLException {
        String tableName = portalType == PortalType.LOCAL ? nameConfig.getPortalViewName() :
                nameConfig.getInterPortalTableName();
        printTableInfo(tableName);
        Connection connection = getConnection();
        PreparedStatement statement = generator.generateGetAllPortalsStatement(connection, portalType);

        ResultSet set = statement.executeQuery();
        ResultSetMetaData metaData = set.getMetaData();

        int rows = 0;
        try {
            while (set.next()) {
                rows++;
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    System.out.print(metaData.getColumnName(i + 1) + " = " + set.getObject(i + 1) + ", ");

                    String portalName = set.getString("name");
                    FakePortal targetPortal = portals.get(portalName);
                    Assertions.assertTrue(targetPortal.getOwnerUUID().toString().equals(set.getString("ownerUUID")));
                    Assertions.assertTrue(targetPortal.getAllFlagsString().equals(set.getString("flags")));

                    //if (PortalType.INTER_SERVER == portalType
                    //        && set.getString("serverId").equals(serverUUID.toString())) {
                    //    Assertions.assertTrue(set.getString("serverName").equals(serverName));
                    //}
                }
                System.out.println();
                Assertions.assertTrue(rows > 0);
            }
        } finally {
            connection.close();
        }
    }

    void destroyPortalTest() throws SQLException {
        for (FakePortal portal : localPortals.values()) {
            destroyPortal(portal, PortalType.LOCAL);
        }
    }

    void destroyInterPortalTest() throws SQLException {
        for (FakePortal portal : interServerPortals.values()) {
            destroyPortal(portal, PortalType.INTER_SERVER);
        }
    }

    void updateLastKnownNameTest() throws SQLException {
        Connection connection = getConnection();
        try {
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
        } finally {
            connection.close();
        }
    }

    /**
     * Gets the last known name from the database
     *
     * @param uuid <p>The uuid to get the last known name from</p>
     * @return <p>The last known name of the uuid</p>
     * @throws SQLException <p>If a database error occurs</p>
     */
    private String getLastKnownName(UUID uuid) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT lastKnownName FROM " +
                nameConfig.getLastKnownNameTableName() + " WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        result.next();
        String output = result.getString(1);
        statement.close();
        connection.close();
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
        Connection connection = getConnection();
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
        connection.close();

        String flagTable = portalType == PortalType.LOCAL ? nameConfig.getFlagRelationTableName() :
                nameConfig.getInterFlagRelationTableName();
        checkIfHasNot(flagTable, portal.getName(), portal.getNetwork().getName());

        String table = portalType == PortalType.LOCAL ? nameConfig.getPortalTableName() :
                nameConfig.getInterPortalTableName();
        checkIfHasNot(table, portal.getName(), portal.getNetwork().getName());
    }
    
    public void updateServerInfoTest() throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = generator.generateUpdateServerInfoStatus(connection, serverName, serverUUID, serverPrefix);
        finishStatement(statement);
        connection.close();
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
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table +
                " WHERE name = ? AND network = ?");
        statement.setString(1, name);
        statement.setString(2, network);
        ResultSet set = statement.executeQuery();
        try {
            Assertions.assertFalse(set.next());
        } finally {
            connection.close();
        }
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

        Connection connection = getConnection();
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
        Connection connection = getConnection();
        finishStatement(connection.prepareStatement("DROP VIEW IF EXISTS " + nameConfig.getInterPortalViewName()));
        finishStatement(connection.prepareStatement("DROP VIEW IF EXISTS " + nameConfig.getPortalViewName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getLastKnownNameTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getInterFlagRelationTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getFlagRelationTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getPortalTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getInterPortalTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getLastKnownNameTableName()));
        finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + nameConfig.getFlagTableName()));
        connection.close();
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
