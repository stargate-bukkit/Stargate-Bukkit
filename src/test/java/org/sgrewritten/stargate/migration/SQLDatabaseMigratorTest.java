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
    void renamePortalPosition() throws SQLException, IOException {
        databaseMigrator.run();
        renamePortal(nameConfiguration.getPortalTableName());
        try (Connection connection = database.getConnection()) {
            SQLTestHelper.checkIfHasNot(nameConfiguration.getPortalPositionTableName(), "portal", "network",
                    connection);
            SQLTestHelper.checkIfHas(nameConfiguration.getPortalPositionTableName(), "portal1", "network1", connection);
        }
    }

    @Test
    void renameInterPortalPosition() throws SQLException, IOException {
        databaseMigrator.run();
        renamePortal(nameConfiguration.getInterPortalTableName());
        try (Connection connection = database.getConnection()) {
            SQLTestHelper.checkIfHasNot(nameConfiguration.getInterPortalPositionTableName(), "portal", "network",
                    connection);
            SQLTestHelper.checkIfHas(nameConfiguration.getInterPortalPositionTableName(), "portal1", "network1",
                    connection);
        }
    }

    @Test
    void renamePortalFlag() throws SQLException, IOException {
        databaseMigrator.run();
        renamePortal(nameConfiguration.getPortalTableName());
        try (Connection connection = database.getConnection()) {
            SQLTestHelper.checkIfHasNot(nameConfiguration.getFlagRelationTableName(), "portal", "network", connection);
            SQLTestHelper.checkIfHas(nameConfiguration.getFlagRelationTableName(), "portal1", "network1", connection);
        }
    }

    @Test
    void renameInterPortalFlag() throws SQLException, IOException {
        databaseMigrator.run();
        renamePortal(nameConfiguration.getInterPortalTableName());
        try (Connection connection = database.getConnection()) {
            SQLTestHelper.checkIfHasNot(nameConfiguration.getInterFlagRelationTableName(), "portal", "network",
                    connection);
            SQLTestHelper.checkIfHas(nameConfiguration.getInterFlagRelationTableName(), "portal1", "network1",
                    connection);
        }
    }

    @Test
    void portalPosition_checkPluginName() throws SQLException, IOException {
        databaseMigrator.run();
        try (Connection connection = database.getConnection()) {
            SQLTestHelper.checkIfColumnIs(nameConfiguration.getPortalPositionTableName(), "pluginName", "portal", "network", "Stargate", connection);
        }
    }

    void renamePortal(String table) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE " + table + " SET network = ?,name = ? " +
                "WHERE network = ? AND name = ?;");
        statement.setString(1, "network1");
        statement.setString(2, "portal1");
        statement.setString(3, "network");
        statement.setString(4, "portal");
        DatabaseHelper.runStatement(statement);
        connection.close();
    }
}
