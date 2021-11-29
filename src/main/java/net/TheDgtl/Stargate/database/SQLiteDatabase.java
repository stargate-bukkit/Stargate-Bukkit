package net.TheDgtl.Stargate.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an SQLite database
 */
public class SQLiteDatabase implements Database {

    private String url;
    private Connection previousConnection;
    final File dbFile;

    /**
     * Instantiates a new SQL database
     *
     * @param databaseFile <p>The database file to load</p>
     * @throws SQLException <p>If unable to setup SQLite for the database file</p>
     */
    public SQLiteDatabase(File databaseFile) throws SQLException {
        this.dbFile = databaseFile;
        setupSQLITE(databaseFile);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (previousConnection == null) {
            previousConnection = DriverManager.getConnection(this.url);
        }
        return previousConnection;
    }

    /**
     * Sets up SQLite
     *
     * @param databaseFile <p>The database file to load</p>
     * @throws SQLException <p>If unable to setup SQLite for the database file</p>
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

}
