package org.sgrewritten.stargate.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.sgrewritten.stargate.FakeStargate;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.PortalType;
import org.sgrewritten.stargate.network.portal.FakePortalGenerator;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalData;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.PortalPosition;
import org.sgrewritten.stargate.network.portal.RealPortal;
import org.sgrewritten.stargate.util.database.DatabaseHelper;
import org.sgrewritten.stargate.util.database.PortalStorageHelper;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Test class for shared database tests to prevent duplication
 *
 * @author Kristian
 */
public class DatabaseTester {

    private static Connection connection;
    private static SQLQueryGenerator generator;

    private static TableNameConfiguration nameConfig;
    private static boolean isMySQL;
    private static String serverName;
    private static UUID serverUUID;
    private static Portal testPortal;
    private static final String INTER_PORTAL_NAME = "iPortal";
    private static final String LOCAL_PORTAL_NAME = "portal";
    private static final File testGatesDir = new File("src/test/resources/gates");
    private final Map<String, RealPortal> interServerPortals;
    private final Map<String, RealPortal> localPortals;
    private final StorageAPI portalDatabaseAPI;

    /**
     * Instantiates a new database tester
     *
     * @param database   <p>The database to use for tests</p>
     * @param nameConfig <p>The config containing all table names</p>
     * @param generator  <p>The SQL Query generator to use for generating test queries</p>
     * @param isMySQL    <p>Whether this database tester is testing MySQL as opposed to SQLite</p>
     * @throws InvalidStructureException <p>If an invalid structure is encountered</p>
     * @throws NameErrorException        <p>If an invalid portal name is encountered</p>
     */
    public DatabaseTester(Database database, TableNameConfiguration nameConfig, SQLQueryGenerator generator,
                          boolean isMySQL) throws SQLException, InvalidStructureException, NameErrorException {
        DatabaseTester.connection = database.getConnection();
        DatabaseTester.generator = generator;
        DatabaseTester.isMySQL = isMySQL;
        DatabaseTester.nameConfig = nameConfig;

        ServerMock server = MockBukkit.mock();
        WorldMock world = new WorldMock(Material.DIRT, 5);
        server.addWorld(world);

        int interServerPortalTestLength = 3;
        int localPortalTestLength = 4;

        DatabaseTester.serverName = "aServerName";
        DatabaseTester.serverUUID = UUID.randomUUID();
        Stargate.setServerUUID(serverUUID);
        StargateLogger logger = new FakeStargate();
        this.portalDatabaseAPI = new DatabaseAPI(database, false, isMySQL, logger, nameConfig);

        Network testNetwork = null;
        try {
            testNetwork = new LocalNetwork("test");
        } catch (NameErrorException e) {
            e.printStackTrace();
        }
        GateFormatHandler.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(testGatesDir, logger)));
        FakePortalGenerator portalGenerator = new FakePortalGenerator(LOCAL_PORTAL_NAME, INTER_PORTAL_NAME);

        this.interServerPortals = portalGenerator.generateFakePortals(world, testNetwork, true, interServerPortalTestLength, logger);
        this.localPortals = portalGenerator.generateFakePortals(world, testNetwork, false, localPortalTestLength, logger);
        DatabaseTester.testPortal = portalGenerator.generateFakePortal(world, testNetwork, "testPortal", false, logger);
    }

    void addPortalTableTest() throws SQLException {
        finishStatement(generator.generateCreatePortalTableStatement(connection, PortalType.LOCAL));
        finishStatement(generator.generateAddMetaToPortalTableStatement(connection, PortalType.LOCAL));
    }

    void addInterPortalTableTest() throws SQLException {
        finishStatement(generator.generateCreatePortalTableStatement(connection, PortalType.INTER_SERVER));
        finishStatement(generator.generateAddMetaToPortalTableStatement(connection, PortalType.INTER_SERVER));
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

    void createPortalPositionTypeTableTest() throws SQLException {
        finishStatement(generator.generateCreatePortalPositionTypeTableStatement(connection));
    }

    void createPortalPositionTableTest() throws SQLException {
        createPortalPositionTableTest(PortalType.LOCAL);
    }

    void createInterPortalPositionTableTest() throws SQLException {
        createPortalPositionTableTest(PortalType.INTER_SERVER);
    }

    private void createPortalPositionTableTest(PortalType type) throws SQLException {
        finishStatement(generator.generateCreatePortalPositionTableStatement(connection, type));
        finishStatement(generator.addMetaToPortalPositionTableStatement(connection, type));
    }

    void createPortalPositionIndexTest(PortalType type) throws SQLException {
        PreparedStatement preparedStatement = generator.generateCreatePortalPositionIndex(connection, type);
        if (preparedStatement != null) {
            finishStatement(preparedStatement);
        }
    }

    void portalPositionIndexExistsTest(PortalType portalType) throws SQLException {
        PreparedStatement preparedStatement = generator.generateShowPortalPositionIndexesStatement(connection, portalType);
        ResultSet resultSet = preparedStatement.executeQuery();
        Assertions.assertTrue(resultSet.next());
        preparedStatement.close();
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

    void addPortalTest() {
        for (RealPortal portal : localPortals.values()) {
            Assertions.assertTrue(this.portalDatabaseAPI.savePortalToStorage(portal, PortalType.LOCAL));
        }
    }

    void addInterPortalTest() {
        System.out.println("InterServerTableName: " + nameConfig.getInterPortalTableName());
        for (RealPortal portal : interServerPortals.values()) {
            Assertions.assertTrue(this.portalDatabaseAPI.savePortalToStorage(portal, PortalType.INTER_SERVER));
        }
    }

    private List<PortalPosition> fetchPortalPositions(RealPortal portal, PortalType portalType) throws SQLException {
        PreparedStatement statement = generator.generateGetPortalPositionsStatement(connection, portalType);
        statement.setString(1, portal.getNetwork().getName());
        statement.setString(2, portal.getName());
        ResultSet portalPositionsData = statement.executeQuery();
        List<PortalPosition> output = new ArrayList<>();
        while (portalPositionsData.next()) {
            PortalPosition position = PortalStorageHelper.loadPortalPosition(portalPositionsData);
            output.add(position);
        }
        return output;
    }

    void addAndRemovePortalPosition(PortalType type) throws SQLException {
        Map<String, RealPortal> portals = (type == PortalType.LOCAL) ? localPortals : interServerPortals;

        for (RealPortal portal : portals.values()) {
            List<PortalPosition> portalPositions = fetchPortalPositions(portal, type);
            System.out.println("---- Initial portalPositions ----");
            for (PortalPosition fetchedPosition : portalPositions) {
                System.out.printf("%s%n", fetchedPosition);
            }
            for (PortalPosition position : portalPositions) {
                DatabaseHelper.runStatement(generator.generateRemovePortalPositionStatement(connection, type, portal, position));
                List<PortalPosition> updatedPortalPositionList = fetchPortalPositions(portal, type);
                System.out.println("---- fetched portalPositions ----");
                for (PortalPosition fetchedPosition : updatedPortalPositionList) {
                    System.out.printf("%s, isEqualToRemovedPosition = %b%n", fetchedPosition, fetchedPosition.equals(position));
                }
                System.out.printf("Removed position: %s%n", position);
                Assertions.assertFalse(updatedPortalPositionList.contains(position), "PortalPosition did not get properly removed");
                Assertions.assertTrue(updatedPortalPositionList.size() < portalPositions.size(), "Nothing got removed");
                PreparedStatement addPositionStatement = generator.generateAddPortalPositionStatement(connection, type);
                PortalStorageHelper.addPortalPosition(addPositionStatement, portal, position);
                addPositionStatement.close();
                updatedPortalPositionList = fetchPortalPositions(portal, type);
                Assertions.assertEquals(updatedPortalPositionList.size(), portalPositions.size(), "Nothing got added");
                Assertions.assertTrue(updatedPortalPositionList.contains(position), "PortalPosition did not get properly added");
            }
        }
    }

    private List<String> getKnownFlags() throws SQLException {
        PreparedStatement statement = generator.generateGetAllFlagsStatement(connection);
        ResultSet resultSet = statement.executeQuery();
        List<String> knownFlags = new ArrayList<>();
        while (resultSet.next()) {
            knownFlags.add(resultSet.getString("character"));
        }
        return knownFlags;
    }

    void addFlags() throws SQLException {
        getKnownFlags();
        PreparedStatement addStatement = generator.generateAddFlagStatement(connection);
        addStatement.setString(1, String.valueOf('G'));
        DatabaseHelper.runStatement(addStatement);
        Assertions.assertTrue(getKnownFlags().contains("G"), "Flag did not get added properly");
    }

    void getPortalTest() throws SQLException {
        getPortals(PortalType.LOCAL, localPortals);
    }

    void getInterPortalTest() throws SQLException {
        getPortals(PortalType.INTER_SERVER, interServerPortals);
    }

    /**
     * Gets the portals of the given type and asserts that at least one portal exists
     *
     * @param portalType <p>The type of portal to get</p>
     * @param portals    <p>The portals available for testing</p>
     * @throws SQLException <p>If a database error occurs</p>
     */
    private void getPortals(PortalType portalType, Map<String, RealPortal> portals) throws SQLException {

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
                System.out.print(metaData.getColumnName(i + 1) + " = " + set.getObject(i + 1) + ", ");

                String portalName = set.getString("name");
                Portal targetPortal = portals.get(portalName);
                Assertions.assertEquals(targetPortal.getOwnerUUID().toString(), set.getString("ownerUUID"));
                Assertions.assertEquals(PortalFlag.parseFlags(targetPortal.getAllFlagsString()),
                        PortalFlag.parseFlags(set.getString("flags")));

                if (PortalType.INTER_SERVER == portalType
                        && set.getString("homeServerId").equals(serverUUID.toString())) {
                    Assertions.assertEquals(set.getString("serverName"), serverName);
                }
            }
            System.out.println();
        }
        Assertions.assertEquals(portals.size(), rows);
    }

    private List<PortalData> getKnownPortalData(PortalType type) throws SQLException {
        PreparedStatement statement = generator.generateGetAllPortalsStatement(connection, type);

        ResultSet set = statement.executeQuery();
        List<PortalData> portals = new ArrayList<>();
        while (set.next()) {
            portals.add(PortalStorageHelper.loadPortalData(set, type));
        }
        return portals;
    }

    void addAndRemovePortalFlags(PortalType type) throws SQLException {
        System.out.println("---------- ADDING AND REMOVING FLAG RELATIONS");
        Map<String, RealPortal> portals = (type == PortalType.LOCAL) ? localPortals : interServerPortals;
        for (Portal portal : portals.values()) {
            PreparedStatement addFlagStatement = generator.generateAddPortalFlagRelationStatement(connection, type);
            addFlagStatement.setString(1, portal.getName());
            addFlagStatement.setString(2, portal.getNetwork().getName());
            addFlagStatement.setString(3, String.valueOf('G'));
            addFlagStatement.execute();
            // This for loop is just a lazy search algorithm
            for (PortalData data : getKnownPortalData(type)) {
                if (data.name.equals(portal.getName()) && data.networkName.equals(portal.getNetwork().getName())) {
                    Assertions.assertTrue(data.flagString.contains("G"), "No flag was added to the portal " + portal.getName());
                }
            }
            DatabaseHelper.runStatement(generator.generateRemoveFlagStatement(connection, type, portal, 'G'));
            for (PortalData data : getKnownPortalData(type)) {
                if (data.name.equals(portal.getName()) && data.networkName.equals(portal.getNetwork().getName())) {
                    Assertions.assertFalse(data.flagString.contains("G"), "No flag was removed from the portal " + portal.getName());
                }
            }
        }
    }

    private String getPortalMetaData(Portal portal, PortalType portalType) throws SQLException {
        PreparedStatement statement = generator.generateGetPortalStatement(connection, portal, portalType);
        ResultSet set = statement.executeQuery();
        if (!set.next()) {
            return null;
        }
        String data = set.getString("metaData");
        statement.close();
        return data;
    }

    void setPortalMetaDataTest(PortalType portalType) throws SQLException {
        String meta = "TEST";
        Map<String, RealPortal> portals = (portalType == PortalType.LOCAL) ? localPortals : interServerPortals;
        for (Portal portal : portals.values()) {
            finishStatement(generator.generateSetPortalMetaStatement(connection, portal, meta, portalType));
            Assertions.assertEquals(meta, getPortalMetaData(portal, portalType));
        }
    }

    private String getPortalPositionMeta(Portal portal, PortalPosition portalPosition, PortalType portalType) throws SQLException {
        PreparedStatement statement = generator.generateGetPortalPositionStatement(connection, portal, portalPosition, portalType);
        ResultSet set = statement.executeQuery();
        if (!set.next()) {
            return null;
        }
        String data = set.getString("metaData");
        statement.close();
        return data;
    }

    void setPortalPositionMetaTest(PortalType portalType) throws SQLException {
        Map<String, RealPortal> portals = (portalType == PortalType.LOCAL) ? localPortals : interServerPortals;
        String meta = "TEST";
        for (RealPortal portal : portals.values()) {
            for (PortalPosition portalPosition : portal.getGate().getPortalPositions()) {
                finishStatement(generator.generateSetPortalPositionMeta(connection, portal, portalPosition, meta, portalType));
                Assertions.assertEquals(getPortalPositionMeta(portal, portalPosition, portalType), meta);
            }
        }
    }

    void destroyPortalTest() throws SQLException {
        for (Portal portal : localPortals.values()) {
            destroyPortal(portal, PortalType.LOCAL);
        }
    }

    void destroyInterPortalTest() throws SQLException {
        for (Portal portal : interServerPortals.values()) {
            destroyPortal(portal, PortalType.INTER_SERVER);
        }
    }

    void updateLastKnownNameTest() throws SQLException {
        UUID uuid = testPortal.getOwnerUUID();
        String lastKnownName = "AUserName";
        String lastKnownName2 = "AUserName2";
        PreparedStatement updateLastKnownNameStatement = generator.generateUpdateLastKnownNameStatement(connection);
        updateLastKnownNameStatement.setString(1, uuid.toString());
        updateLastKnownNameStatement.setString(2, lastKnownName);
        updateLastKnownNameStatement.execute();
        Assertions.assertEquals(lastKnownName, getLastKnownName(uuid));

        updateLastKnownNameStatement.setString(1, uuid.toString());
        updateLastKnownNameStatement.setString(2, lastKnownName2);
        updateLastKnownNameStatement.execute();
        updateLastKnownNameStatement.close();
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
    private void destroyPortal(Portal portal, PortalType portalType) throws SQLException {
        this.portalDatabaseAPI.removePortalFromStorage(portal, portalType);

        String flagTable = portalType == PortalType.LOCAL ? nameConfig.getFlagRelationTableName() :
                nameConfig.getInterFlagRelationTableName();
        checkIfHasNot(flagTable, portal.getName(), portal.getNetwork().getName());

        String table = portalType == PortalType.LOCAL ? nameConfig.getPortalTableName() :
                nameConfig.getInterPortalTableName();
        checkIfHasNot(table, portal.getName(), portal.getNetwork().getName());
    }

    /**
     * Tests that information about a server can be updated
     *
     * @throws SQLException <p>If a database error occurs</p>
     */
    public void updateServerInfoTest() throws SQLException {
        PreparedStatement statement = generator.generateUpdateServerInfoStatus(connection, serverUUID.toString(), serverName);
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
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table +
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
    static void deleteAllTables(TableNameConfiguration nameConfig) throws SQLException {
        System.out.println("Running database cleanup...");
        List<String> tablesToRemove = new ArrayList<>();
        tablesToRemove.add(nameConfig.getServerInfoTableName());
        tablesToRemove.add(nameConfig.getPortalPositionTableName());
        tablesToRemove.add(nameConfig.getInterPortalPositionTableName());
        tablesToRemove.add(nameConfig.getPositionTypeTableName());
        tablesToRemove.add(nameConfig.getInterPortalViewName());
        tablesToRemove.add(nameConfig.getPortalViewName());
        tablesToRemove.add(nameConfig.getLastKnownNameTableName());
        tablesToRemove.add(nameConfig.getInterFlagRelationTableName());
        tablesToRemove.add(nameConfig.getFlagRelationTableName());
        tablesToRemove.add(nameConfig.getPortalTableName());
        tablesToRemove.add(nameConfig.getInterPortalTableName());
        tablesToRemove.add(nameConfig.getFlagTableName());
        for (String table : tablesToRemove) {
            if (table.contains("View")) {
                finishStatement(connection.prepareStatement("DROP VIEW IF EXISTS " + table));
            } else {
                finishStatement(connection.prepareStatement("DROP TABLE IF EXISTS " + table));
            }
        }
        System.out.println("Finished database cleanup");
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
