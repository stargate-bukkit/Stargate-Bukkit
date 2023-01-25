package org.sgrewritten.stargate.migration;

import com.google.common.io.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.database.SQLiteDatabase;
import org.sgrewritten.stargate.util.SQLTestHelper;
import org.sgrewritten.stargate.util.database.DatabaseHelper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class SQLDatabaseMigratorTest {

    private SQLDatabaseMigrator databaseMigrator;
    private SQLiteDatabase database;
    private TableNameConfiguration nameConfiguration;
    private static final File sqlDatabaseFile = new File("src/test/resources", "alpha-1_0_0_11.db");
    private static final File oldSqlDatabaseFile = new File("src/test/resources", "alpha-1_0_0_11.old");

    @BeforeEach
    void setUp() throws SQLException, IOException {
        Files.copy(sqlDatabaseFile, oldSqlDatabaseFile);


        database = new SQLiteDatabase(sqlDatabaseFile);
        nameConfiguration = new TableNameConfiguration("", "");
        databaseMigrator = new SQLDatabaseMigrator(database, nameConfiguration, "/migration/database/alpha-1_0_0_14", true);
    }

    @AfterEach
    void tearDown() {
        Assertions.assertTrue(sqlDatabaseFile.delete());
        Assertions.assertTrue(oldSqlDatabaseFile.renameTo(sqlDatabaseFile));
    }

    // CHECK IF THE UPDATE ON CASCADE OPTION IS THERE, BY LOOKING AT THE BEHAVIOR
    @Test
    void rename_PortalPosition() throws SQLException, IOException {
        databaseMigrator.run();
        renamePortal(nameConfiguration.getPortalTableName(), "network", "network1", "portal", "portal1");
        try (Connection connection = database.getConnection()) {
            SQLTestHelper.checkIfHasNot(nameConfiguration.getPortalPositionTableName(), "portal", "network",
                    connection);
            SQLTestHelper.checkIfHas(nameConfiguration.getPortalPositionTableName(), "portal1", "network1", connection);
        }
    }

    @Test
    void rename_InterPortalPosition() throws SQLException, IOException {
        databaseMigrator.run();
        renamePortal(nameConfiguration.getInterPortalTableName(), "network", "network1", "portal", "portal1");
        try (Connection connection = database.getConnection()) {
            SQLTestHelper.checkIfHasNot(nameConfiguration.getInterPortalPositionTableName(), "portal", "network",
                    connection);
            SQLTestHelper.checkIfHas(nameConfiguration.getInterPortalPositionTableName(), "portal1", "network1",
                    connection);
        }
    }

    @Test
    void rename_PortalFlag() throws SQLException, IOException {
        databaseMigrator.run();
        renamePortal(nameConfiguration.getPortalTableName(), "network", "network1", "portal", "portal1");
        try (Connection connection = database.getConnection()) {
            SQLTestHelper.checkIfHasNot(nameConfiguration.getFlagRelationTableName(), "portal", "network", connection);
            SQLTestHelper.checkIfHas(nameConfiguration.getFlagRelationTableName(), "portal1", "network1", connection);
        }
    }

    @Test
    void rename_InterPortalFlag() throws SQLException, IOException {
        databaseMigrator.run();
        renamePortal(nameConfiguration.getInterPortalTableName(), "network", "network1", "portal", "portal1");
        try (Connection connection = database.getConnection()) {
            SQLTestHelper.checkIfHasNot(nameConfiguration.getInterFlagRelationTableName(), "portal", "network",
                    connection);
            SQLTestHelper.checkIfHas(nameConfiguration.getInterFlagRelationTableName(), "portal1", "network1",
                    connection);
        }
    }

    void renamePortal(String table, String oldNetwork, String newNetwork, String oldPortal, String newPortal) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE " + table + " SET network = ?,name = ? WHERE network = ? AND name = ?;");
        statement.setString(1, newNetwork);
        statement.setString(2, newPortal);
        statement.setString(3, oldNetwork);
        statement.setString(4, oldPortal);
        DatabaseHelper.runStatement(statement);
        connection.close();
    }
}
