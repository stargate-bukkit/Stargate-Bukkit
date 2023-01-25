package org.sgrewritten.stargate.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an SQLite database
 */
public class SQLiteDatabase implements SQLDatabaseAPI {

    private String url;

    /**
     * Instantiates a new SQL database
     *
     * @param databaseFile <p>The database file to load</p>
     * @throws SQLException <p>If unable to setup SQLite for the database file</p>
     */
    public SQLiteDatabase(File databaseFile) throws SQLException {
        setupSQLITE(databaseFile);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(this.url);
        try (PreparedStatement statement = connection.prepareStatement("PRAGMA foreign_keys = ON;")) {
            statement.execute();
        }
        return connection;
    }

    /**
     * Sets up SQLite
     *
     * @param databaseFile <p>
     *                     The database file to load
     *                     </p>
     * @throws SQLException <p>
     *                      If unable to setup SQLite for the database file
     *                      </p>
     */
    private void setupSQLITE(File databaseFile) throws SQLException {
        this.url = ("jdbc:sqlite:" + databaseFile.getAbsoluteFile());
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MySqlDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        DriverManager.registerDriver(new org.sqlite.JDBC());
    }

    @Override
    public DatabaseDriver getDriver() {
        return DatabaseDriver.SQLITE;
    }

}
