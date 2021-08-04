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
public class MySqlDatabase implements Database{

    private HikariDataSource hikariSource;

    private HikariConfig config;
    private final String database;
    private final String address;
    private final int port;
    private final DriverEnum driver;
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
    public MySqlDatabase(DriverEnum driver, String address, int port, String database, Stargate plugin) throws SQLException {
    	
        this.database = database;
        this.driver = driver;
        this.port = port;
        this.address = address;
        this.plugin = plugin;
        
        switch(this.driver) {
        case MYSQL:
		case MARIADB:
			setupMySQL(driver, address, port, database);
			break;
		default:
			Stargate.log(Level.WARNING, "Unknown driver, '"+driver+"' , using SQLite by default. Stargate currently supports SQLite, MariaDB, MySql");
        }
    }

    /**
	 * Gets the current database connection.
	 * 
	 * @return Current connection.
	 * @throws java.sql.SQLException
	 */
	public Connection getConnection() throws SQLException {
		return this.hikariSource.getConnection();
	}

	private void setupMySQL(DriverEnum driver, String address, int port, String database) throws SQLException {
		// Creates a properties file if it doesn't exist
		if (!new File(plugin.getDataFolder(), "hikari.properties").exists()) {
			plugin.getLogger().warning("hikari.properties file is missing.");
			plugin.getLogger().info("Providing you with a new properties file");
			plugin.getDataFolder().mkdirs();
    		plugin.saveResource("hikari.properties", true);
    		getPluginManager().disablePlugin(plugin);
    	}
    	
    	//creates a config based on the properties file
        this.config = new HikariConfig(plugin.getDataFolder()+"/hikari.properties");
        
        
        config.setJdbcUrl("jdbc:"+ driver +"://" + address + ":" + port + "/" + database);
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MySqlDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        
        this.hikariSource = new HikariDataSource(config);
    }
}
