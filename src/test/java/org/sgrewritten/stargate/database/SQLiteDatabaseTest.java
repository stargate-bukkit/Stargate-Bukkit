package org.sgrewritten.stargate.database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.PortalLoadException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.util.StargateTestHelper;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLiteDatabaseTest {

    private static DatabaseTester tester;
    private static TableNameConfiguration nameConfig;

    @BeforeAll
    public static void setUp() throws SQLException, InvalidStructureException, TranslatableException, GateConflictException, NoFormatFoundException {
        Stargate.log(Level.FINE, "Setting up test data");
        SQLDatabaseAPI database = new SQLiteDatabase(new File("src/test/resources", "test.db"));
        nameConfig = new TableNameConfiguration("SG_Test_", "Server_");
        SQLQueryGenerator generator = new SQLQueryGenerator(nameConfig, DatabaseDriver.SQLITE);
        tester = new DatabaseTester(database, nameConfig, generator, false);
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        StargateTestHelper.tearDown();
        try {
            DatabaseTester.deleteAllTables(nameConfig);
        } finally {
            DatabaseTester.connection.close();
        }
    }

    @Test
    @Order(1)
    void addPortalTableTest() {
        try {
            tester.addPortalTableTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(1)
    void addInterPortalTableTest() {
        try {
            tester.addInterPortalTableTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(1)
    void createFlagTableTest() {
        try {
            tester.createFlagTableTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(1)
    void createServerInfoTableTest() {
        try {
            tester.createServerInfoTableTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(1)
    void createLastKnownNameTableTest() {
        try {
            tester.createLastKnownNameTableTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(2)
    void createPortalFlagRelationTableTest() {
        try {
            tester.createPortalFlagRelationTableTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(2)
    void createInterPortalFlagRelationTableTest() {
        try {
            tester.createInterPortalFlagRelationTableTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(2)
    void createPortalPositionTypeTableTest() {
        try {
            tester.createPortalPositionTypeTableTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(3)
    void createPortalPositionTableTest() {
        try {
            tester.createPortalPositionTableTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(3)
    void createInterPortalPositionTableTest() {
        try {
            tester.createInterPortalPositionTableTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(3)
    void createPortalViewTest() {
        try {
            tester.createPortalViewTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(3)
    void createInterPortalViewTest() {
        try {
            tester.createInterPortalViewTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(3)
    void createPortalPositionIndexTest() {
        try {
            tester.createPortalPositionIndexTest(StorageType.LOCAL);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(3)
    void createInterPortalPositionIndexTest() {
        try {
            tester.createPortalPositionIndexTest(StorageType.INTER_SERVER);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(4)
    void portalPositionIndexExistsTest() {
        try {
            tester.portalPositionIndexExistsTest(StorageType.LOCAL);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(4)
    void interPortalPositionIndexExistsTest() {
        try {
            tester.portalPositionIndexExistsTest(StorageType.INTER_SERVER);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(4)
    void getFlagsTest() {
        try {
            tester.getFlagsTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(4)
    void updateServerInfoTest() {
        try {
            tester.updateServerInfoTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(5)
    void addFlagsTest() {
        try {
            tester.addFlags();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(5)
    void updateLastKnownNameTest() {
        try {
            tester.updateLastKnownNameTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(6)
    void addPortalTest() {
        tester.addPortalTest();
    }

    @Test
    @Order(6)
    void addInterPortalTest() {
        tester.addInterPortalTest();
    }

    @Test
    @Order(7)
    void getPortalTest() {
        try {
            tester.getPortalTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(7)
    void getInterPortalTest() {
        try {
            tester.getInterPortalTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(8)
    void addAndRemovePortalPositionTest() {
        try {
            tester.addAndRemovePortalPosition(StorageType.LOCAL);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(8)
    void addAndRemoveInterPortalPositionTest() {
        try {
            tester.addAndRemovePortalPosition(StorageType.INTER_SERVER);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(8)
    void addAndRemovePortalFlagRelationTest() throws PortalLoadException {
        try {
            tester.addAndRemovePortalFlags(StorageType.LOCAL);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(8)
    void addAndRemoveInterPortalFlagRelationTest() throws PortalLoadException {
        try {
            tester.addAndRemovePortalFlags(StorageType.INTER_SERVER);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(8)
    void setPortalMetaTest() {
        try {
            tester.setPortalMetaDataTest(StorageType.LOCAL);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(8)
    void setInterPortalMetaTest() {
        try {
            tester.setPortalMetaDataTest(StorageType.INTER_SERVER);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(8)
    void setPortalPositionMetaTest() {
        try {
            tester.setPortalPositionMetaTest(StorageType.LOCAL);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(8)
    void setInterPortalPositionMetaTest() {
        try {
            tester.setPortalPositionMetaTest(StorageType.INTER_SERVER);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(8)
    void changeNamesTest() throws StorageWriteException, SQLException, InvalidStructureException, TranslatableException {
       Assertions.assertDoesNotThrow(() ->  tester.changeNames(StorageType.LOCAL));
    }

    @Test
    @Order(8)
    void changeInterNamesTest() throws StorageWriteException, SQLException, InvalidStructureException, TranslatableException {
        Assertions.assertDoesNotThrow(() -> tester.changeNames(StorageType.INTER_SERVER));
    }

    @Test
    @Order(10)
    void destroyPortalTest() {
        try {
            tester.destroyPortalTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    @Order(10)
    void destroyInterPortalTest() {
        try {
            tester.destroyInterPortalTest();
        } catch (SQLException e) {
            fail(e);
        }
    }

}
