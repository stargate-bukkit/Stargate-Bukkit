package org.sgrewritten.stargate.database;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateExtension;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.exception.*;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.network.StorageType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(StargateExtension.class)
public class MySQLDatabaseTest {

    private DatabaseTester tester;
    private TableNameConfiguration nameConfig;
    private SQLDatabaseAPI database;

    @BeforeEach
    void setUp() throws SQLException, InvalidStructureException, StargateInitializationException,
            TranslatableException, GateConflictException, NoFormatFoundException {
        Stargate.log(Level.FINE, "Setting up test data");
        DatabaseDriver driver = DatabaseDriver.MYSQL;
        TestCredentialsManager credentialsManager = new TestCredentialsManager("mysql_credentials.secret");
        String address = credentialsManager.getCredentialString(TestCredential.MYSQL_DB_ADDRESS, "localhost");
        int port = credentialsManager.getCredentialInt(TestCredential.MYSQL_DB_PORT, 3306);
        String databaseName = credentialsManager.getCredentialString(TestCredential.MYSQL_DB_NAME, "Stargate");
        String username = credentialsManager.getCredentialString(TestCredential.MYSQL_DB_USER);
        String password = credentialsManager.getCredentialString(TestCredential.MYSQL_DB_PASSWORD);

        if (username == null || password == null) {
            throw new IllegalArgumentException("You need to set MySQL credentials to run this test!");
        }

        SQLDatabaseAPI database = new MySqlDatabase(driver, address, port, databaseName, username, password, false);
        this.nameConfig = new TableNameConfiguration("SG_Test_", "Server_");
        SQLQueryGenerator generator = new SQLQueryGenerator(nameConfig, DatabaseDriver.MYSQL);
        tester = new DatabaseTester(database, nameConfig, generator, true);
        this.database = database;
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection connection = database.getConnection()) {
            connection.prepareStatement("DROP DATABASE Stargate;").execute();
            connection.prepareStatement("CREATE DATABASE Stargate;").execute();
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
