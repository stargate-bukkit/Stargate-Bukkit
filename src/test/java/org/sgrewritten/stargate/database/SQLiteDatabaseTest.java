package org.sgrewritten.stargate.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.StorageType;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLiteDatabaseTest {

    private static DatabaseTester tester;
    private static TableNameConfiguration nameConfig;

    @BeforeAll
    public static void setUp() throws SQLException, InvalidStructureException, InvalidNameException, NameLengthException {
        Stargate.log(Level.FINE, "Setting up test data");
        SQLDatabaseAPI database = new SQLiteDatabase(new File("src/test/resources", "test.db"));
        nameConfig = new TableNameConfiguration("SG_Test_", "Server_");
        SQLQueryGenerator generator = new SQLQueryGenerator(nameConfig, new FakeStargateLogger(), DatabaseDriver.SQLITE);
        tester = new DatabaseTester(database, nameConfig, generator, false);
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        MockBukkit.unmock();
        try {
            DatabaseTester.deleteAllTables(nameConfig);
        } finally {
            DatabaseTester.connection.close();
        }
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
    void createServerInfoTableTest() throws SQLException {
        tester.createServerInfoTableTest();
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
    @Order(2)
    void createPortalPositionTypeTableTest() throws SQLException {
        tester.createPortalPositionTypeTableTest();
    }

    @Test
    @Order(3)
    void createPortalPositionTableTest() throws SQLException {
        tester.createPortalPositionTableTest();
    }

    @Test
    @Order(3)
    void createInterPortalPositionTableTest() throws SQLException {
        tester.createInterPortalPositionTableTest();
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
    void createPortalPositionIndexTest() throws SQLException {
        tester.createPortalPositionIndexTest(StorageType.LOCAL);
    }

    @Test
    @Order(3)
    void createInterPortalPositionIndexTest() throws SQLException {
        tester.createPortalPositionIndexTest(StorageType.INTER_SERVER);
    }

    @Test
    @Order(4)
    void portalPositionIndexExistsTest() throws SQLException {
        tester.portalPositionIndexExistsTest(StorageType.LOCAL);
    }

    @Test
    @Order(4)
    void interPortalPositionIndexExistsTest() throws SQLException {
        tester.portalPositionIndexExistsTest(StorageType.INTER_SERVER);
    }

    @Test
    @Order(4)
    void getFlagsTest() throws SQLException {
        tester.getFlagsTest();
    }

    @Test
    @Order(4)
    void updateServerInfoTest() throws SQLException {
        tester.updateServerInfoTest();
    }

    @Test
    @Order(5)
    void addFlagsTest() throws SQLException {
        tester.addFlags();
    }

    @Test
    @Order(5)
    void updateLastKnownNameTest() throws SQLException {
        tester.updateLastKnownNameTest();
    }

    @Test
    @Order(5)
    void addPortalTest() {
        tester.addPortalTest();
    }

    @Test
    @Order(5)
    void addInterPortalTest() {
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
    void addAndRemovePortalPositionTest() throws SQLException {
        tester.addAndRemovePortalPosition(StorageType.LOCAL);
    }

    @Test
    @Order(7)
    void addAndRemoveInterPortalPositionTest() throws SQLException {
        tester.addAndRemovePortalPosition(StorageType.INTER_SERVER);
    }

    @Test
    @Order(7)
    void addAndRemovePortalFlagRelationTest() throws SQLException {
        tester.addAndRemovePortalFlags(StorageType.LOCAL);
    }

    @Test
    @Order(7)
    void addAndRemoveInterPortalFlagRelationTest() throws SQLException {
        tester.addAndRemovePortalFlags(StorageType.INTER_SERVER);
    }

    @Test
    @Order(7)
    void setPortalMetaTest() throws SQLException {
        tester.setPortalMetaDataTest(StorageType.LOCAL);
    }

    @Test
    @Order(7)
    void setInterPortalMetaTest() throws SQLException {
        tester.setPortalMetaDataTest(StorageType.INTER_SERVER);
    }

    @Test
    @Order(7)
    void setPortalPositionMetaTest() throws SQLException {
        tester.setPortalPositionMetaTest(StorageType.LOCAL);
    }

    @Test
    @Order(7)
    void setInterPortalPositionMetaTest() throws SQLException {
        tester.setPortalPositionMetaTest(StorageType.INTER_SERVER);
    }

    @Test
    @Order(7)
    void changeNamesTest() throws StorageWriteException, SQLException, InvalidStructureException, InvalidNameException, NameLengthException, UnimplementedFlagException {
        tester.changeNames(StorageType.LOCAL);
    }

    @Test
    @Order(7)
    void changeInterNamesTest() throws StorageWriteException, SQLException, InvalidStructureException, InvalidNameException, NameLengthException, UnimplementedFlagException {
        tester.changeNames(StorageType.INTER_SERVER);
    }

    @Test
    @Order(10)
    void destroyPortalTest() throws SQLException {
        tester.destroyPortalTest();
    }

    @Test
    @Order(10)
    void destroyInterPortalTest() throws SQLException {
        tester.destroyInterPortalTest();
    }

}
