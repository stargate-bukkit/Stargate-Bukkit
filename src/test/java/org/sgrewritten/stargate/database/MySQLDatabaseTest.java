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
import org.sgrewritten.stargate.exception.StargateInitializationException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.network.StorageType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySQLDatabaseTest {

    private static DatabaseTester tester;
    private static TableNameConfiguration nameConfig;
    private static SQLDatabaseAPI database;

    @BeforeAll
    public static void setUp() throws SQLException, InvalidStructureException, StargateInitializationException,
            TranslatableException {
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
        MySQLDatabaseTest.nameConfig = new TableNameConfiguration("SG_Test_", "Server_");
        SQLQueryGenerator generator = new SQLQueryGenerator(nameConfig, new FakeStargateLogger(), DatabaseDriver.MYSQL);
        tester = new DatabaseTester(database, nameConfig, generator, true);
        MySQLDatabaseTest.database = database;
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        MockBukkit.unmock();

        try (Connection connection = database.getConnection()) {
            connection.prepareStatement("DROP DATABASE stargate;").execute();
            connection.prepareStatement("CREATE DATABASE stargate;").execute();
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
            fail();
        }
    }

    @Test
    @Order(1)
    void addInterPortalTableTest() {
        try {
            tester.addInterPortalTableTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(1)
    void createFlagTableTest() {
        try {
            tester.createFlagTableTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(1)
    void createServerInfoTableTest() {
        try {
            tester.createServerInfoTableTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(1)
    void createLastKnownNameTableTest() {
        try {
            tester.createLastKnownNameTableTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(2)
    void createPortalFlagRelationTableTest() {
        try {
            tester.createPortalFlagRelationTableTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(2)
    void createInterPortalFlagRelationTableTest() {
        try {
            tester.createInterPortalFlagRelationTableTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(2)
    void createPortalPositionTypeTableTest() {
        try {
            tester.createPortalPositionTypeTableTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(3)
    void createPortalPositionTableTest() {
        try {
            tester.createPortalPositionTableTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(3)
    void createInterPortalPositionTableTest() {
        try {
            tester.createInterPortalPositionTableTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(3)
    void createPortalViewTest() {
        try {
            tester.createPortalViewTest();
        } catch (SQLException e) {
            fail();
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
            fail();
        }
    }

    @Test
    @Order(4)
    void portalPositionIndexExistsTest() {
        try {
            tester.portalPositionIndexExistsTest(StorageType.LOCAL);
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(4)
    void interPortalPositionIndexExistsTest() {
        try {
            tester.portalPositionIndexExistsTest(StorageType.INTER_SERVER);
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(4)
    void getFlagsTest() {
        try {
            tester.getFlagsTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(4)
    void updateServerInfoTest() {
        try {
            tester.updateServerInfoTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(5)
    void updateLastKnownNameTest() {
        try {
            tester.updateLastKnownNameTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(5)
    void addFlagsTest() {
        try {
            tester.addFlags();
        } catch (SQLException e) {
            fail();
        }
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
    void addAndRemovePortalPositionTest() {
        try {
            tester.addAndRemovePortalPosition(StorageType.LOCAL);
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(7)
    void addAndRemoveInterPortalPositionTest() {
        try {
            tester.addAndRemovePortalPosition(StorageType.INTER_SERVER);
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(7)
    void setPortalMetaTest() {
        try {
            tester.setPortalMetaDataTest(StorageType.LOCAL);
        } catch (SQLException e) {
            fail();
        }
    }


    @Test
    @Order(7)
    void setInterPortalMetaTest() {
        try {
            tester.setPortalMetaDataTest(StorageType.INTER_SERVER);
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(7)
    void setPortalPositionMetaTest() {
        try {
            tester.setPortalPositionMetaTest(StorageType.LOCAL);
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(7)
    void setInterPortalPositionMetaTest() {
        try {
            tester.setPortalPositionMetaTest(StorageType.INTER_SERVER);
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(7)
    void changeNamesTest() {
        try {
            tester.changeNames(StorageType.LOCAL);
        } catch (SQLException | InvalidStructureException | StorageWriteException | TranslatableException e) {
            fail();
        }
    }

    @Test
    @Order(7)
    void changeInterNamesTest() {
        try {
            tester.changeNames(StorageType.INTER_SERVER);
        } catch (SQLException | InvalidStructureException | StorageWriteException | TranslatableException e) {
            fail();
        }
    }

    @Test
    @Order(10)
    void destroyPortalTest() {
        try {
            tester.destroyPortalTest();
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    @Order(10)
    void destroyInterPortalTest() {
        try {
            tester.destroyInterPortalTest();
        } catch (SQLException e) {
            fail();
        }
    }

}
