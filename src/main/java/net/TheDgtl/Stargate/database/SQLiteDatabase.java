package net.TheDgtl.Stargate.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.TheDgtl.Stargate.Stargate;

public class SQLiteDatabase implements Database{
	private String url;
	private Connection previousConnection;
	File dbFile;
	
	public SQLiteDatabase(File dbFile, Stargate stargate) throws SQLException {
        this.dbFile = dbFile;
		setupSQLITE(dbFile);
	}
	
	@Override
	public Connection getConnection() throws SQLException{
		return (previousConnection = DriverManager.getConnection(this.url));
	}
	
	private void setupSQLITE(File dbFile) throws SQLException {
        this.url = ("jdbc:sqlite:" + dbFile.getAbsoluteFile());
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MySqlDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        DriverManager.registerDriver(new org.sqlite.JDBC());
    }
}
