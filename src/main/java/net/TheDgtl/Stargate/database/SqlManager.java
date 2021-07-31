/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.TheDgtl.Stargate.database;




import static org.bukkit.Bukkit.getPluginManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.TheDgtl.Stargate.Stargate;


/**
 * @author Frostalf
 * @author Thorin
 */
public class SqlManager {

    private HikariDataSource boneCP;
    private Connection connection;

    private HikariConfig config = null;
    private File dbFile;
    private final String database;
    private final String address;
    private final int port;
    private final DriverEnum driver;
    private String url;
	private Stargate plugin;
    
    /**
     * Caches and checks the status of a certain database..
     * @param driver Driver to use for this database communication.
     * @param address Address of the database being checked.
     * @param port Port on which to communicate with the database being checked.
     * @param database Name of the database being checked.
     * @param plugin
     * @throws SQLException 
     */
    public SqlManager(String driver, String address, int port, String database, Stargate plugin) throws SQLException {
    	
        this.database = database;
        this.driver = getDriver(driver);
        this.port = port;
        this.address = address;
        this.plugin = plugin;
        
        switch(this.driver) {
        case MYSQL:
        case MARIADB:
        	setupMySQL();
        	break;
        default:
        	Stargate.log(Level.WARNING, "Unknown driver, '"+driver+"' , using SQLite by default. Stargate currently supports SQLite, MariaDB, MySql");
        case SQLITE:
        	setupSQLITE();
        	break;
        }
    }

    /**
     * Gets the current database connection.
     * @return Current connection.
     * @throws java.sql.SQLException
     */
    public Connection getConnection() throws SQLException {
    	switch(driver) {
    	case MYSQL:
    	case MARIADB:
    		return connection = this.boneCP.getConnection();
    	default:
    	case SQLITE:
    		return connection = DriverManager.getConnection(this.url);
    	}
        
    }

    public DriverEnum getDriver(String driver) {
        return DriverEnum.valueOf(driver.toUpperCase());
    }

    private void setupMySQL() throws SQLException {
    	//Creates a properties file if it doesn't exist
    	if ( !new File(plugin.getDataFolder(), "hikari.properties").exists() ) {
    		plugin.getLogger().warning("[Startup] hikari.properties file is missing.");
    		plugin.getLogger().info("Providing you with a new properties file");
    		plugin.getDataFolder().mkdirs();
    		plugin.saveResource("hikari.properties", true);
    		getPluginManager().disablePlugin(plugin);
    	}
    	
    	//creates a config based on the properties file
        this.config = new HikariConfig(plugin.getDataFolder()+"/hikari.properties");
        
        
        config.setJdbcUrl("jdbc:"+ this.driver +"://" + this.address + ":" + this.port + "/" + this.database);
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SqlManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        
        this.boneCP = new HikariDataSource(config);
    }

    private void setupSQLITE() throws SQLException {
        dbFile = new File(plugin.getDataFolder().getAbsoluteFile(), this.database + ".db");

        this.url = ("jdbc:sqlite:" + dbFile.getAbsoluteFile());
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SqlManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        DriverManager.registerDriver(new org.sqlite.JDBC());
    }
}
