package org.sgrewritten.stargate.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.exception.StargateInitializationException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.network.PortalType;

import java.sql.Connection;
import java.sql.SQLException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySQLDatabaseTest {

    private static DatabaseTester tester;
    private static TableNameConfiguration nameConfig;
    private static SQLDatabaseAPI database;

    @BeforeAll
    public static void setUp() throws SQLException, InvalidStructureException, NameErrorException, StargateInitializationException {
        System.out.println("Setting up test data");
        DatabaseDriver driver = DatabaseDriver.MYSQL;
        String address = "LOCALHOST";
        int port = 3306;
        String databaseName = "stargate";
        String username = "root";
        String password = "root";

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
        tester.createPortalPositionIndexTest(PortalType.LOCAL);
    }

    @Test
    @Order(3)
    void createInterPortalPositionIndexTest() throws SQLException {
        tester.createPortalPositionIndexTest(PortalType.INTER_SERVER);
    }

    @Test
    @Order(4)
    void portalPositionIndexExistsTest() throws SQLException {
        tester.portalPositionIndexExistsTest(PortalType.LOCAL);
    }

    @Test
    @Order(4)
    void interPortalPositionIndexExistsTest() throws SQLException {
        tester.portalPositionIndexExistsTest(PortalType.INTER_SERVER);
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
    void updateLastKnownNameTest() throws SQLException {
        tester.updateLastKnownNameTest();
    }

    @Test
    @Order(5)
    void addFlagsTest() throws SQLException {
        tester.addFlags();
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
        tester.addAndRemovePortalPosition(PortalType.LOCAL);
    }

    @Test
    @Order(7)
    void addAndRemoveInterPortalPositionTest() throws SQLException {
        tester.addAndRemovePortalPosition(PortalType.INTER_SERVER);
    }

    @Test
    @Order(7)
    void setPortalMetaTest() throws SQLException {
        tester.setPortalMetaDataTest(PortalType.LOCAL);
    }


    @Test
    @Order(7)
    void setInterPortalMetaTest() throws SQLException {
        tester.setPortalMetaDataTest(PortalType.INTER_SERVER);
    }

    @Test
    @Order(7)
    void setPortalPositionMetaTest() throws SQLException {
        tester.setPortalPositionMetaTest(PortalType.LOCAL);
    }

    @Test
    @Order(7)
    void setInterPortalPositionMetaTest() throws SQLException {
        tester.setPortalPositionMetaTest(PortalType.INTER_SERVER);
    }

    @Test
    @Order(7)
    void changeNamesTest() throws StorageWriteException, SQLException, InvalidStructureException, NameErrorException {
        tester.changeNames(PortalType.LOCAL);
    }
    
    @Test
    @Order(7)
    void changeInterNamesTest() throws StorageWriteException, SQLException, InvalidStructureException, NameErrorException {
        tester.changeNames(PortalType.INTER_SERVER);
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
