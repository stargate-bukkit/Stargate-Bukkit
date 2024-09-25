package org.sgrewritten.stargate.database;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateExtension;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.network.StorageType;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(StargateExtension.class)
public class SQLiteDatabaseTest {

    private static DatabaseTester tester;
    private static TableNameConfiguration nameConfig;

    @BeforeEach
    public void setUp() throws SQLException, InvalidStructureException, TranslatableException, GateConflictException, NoFormatFoundException {
        Stargate.log(Level.FINE, "Setting up test data");
        SQLDatabaseAPI database = new SQLiteDatabase(new File("src/test/resources", "test.db"));
        nameConfig = new TableNameConfiguration("SG_Test_", "Server_");
        SQLQueryGenerator generator = new SQLQueryGenerator(nameConfig, DatabaseDriver.SQLITE);
        tester = new DatabaseTester(database, nameConfig, generator, false);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try {
            DatabaseTester.deleteAllTables(nameConfig);
        } finally {
            DatabaseTester.connection.close();
        }
    }

    @Test
    void integrationTest() throws SQLException, TranslatableException, StorageWriteException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        tester.addPortalTableTest();
        tester.addInterPortalTableTest();
        tester.createFlagTableTest();
        tester.createServerInfoTableTest();
        tester.createLastKnownNameTableTest();
        tester.createPortalFlagRelationTableTest();
        tester.createInterPortalFlagRelationTableTest();
        tester.createPortalPositionTypeTableTest();
        tester.createPortalPositionTableTest();
        tester.createInterPortalPositionTableTest();
        tester.createPortalViewTest();
        tester.createInterPortalViewTest();
        tester.createPortalPositionIndexTest(StorageType.LOCAL);
        tester.createPortalPositionIndexTest(StorageType.INTER_SERVER);
        tester.portalPositionIndexExistsTest(StorageType.LOCAL);
        tester.portalPositionIndexExistsTest(StorageType.INTER_SERVER);
        tester.getFlagsTest();
        tester.updateServerInfoTest();
        tester.updateLastKnownNameTest();
        tester.addFlags();
        tester.addPortalTest();
        tester.addInterPortalTest();
        tester.getPortalTest();
        tester.getInterPortalTest();
        tester.addAndRemovePortalPosition(StorageType.LOCAL);
        tester.addAndRemovePortalPosition(StorageType.INTER_SERVER);
        tester.setPortalMetaDataTest(StorageType.LOCAL);
        tester.setPortalMetaDataTest(StorageType.INTER_SERVER);
        tester.setPortalPositionMetaTest(StorageType.LOCAL);
        tester.setPortalPositionMetaTest(StorageType.INTER_SERVER);
        tester.changeNames(StorageType.LOCAL);
        tester.changeNames(StorageType.INTER_SERVER);
        tester.destroyPortalTest();
        tester.destroyInterPortalTest();
    }

}
