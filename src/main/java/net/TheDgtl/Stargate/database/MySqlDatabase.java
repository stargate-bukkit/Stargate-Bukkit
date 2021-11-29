/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.TheDgtl.Stargate.database;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.TheDgtl.Stargate.Stargate;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getPluginManager;


/**
 * @author Frostalf
 * @author Thorin
 */
public class MySqlDatabase implements Database {

    private final HikariDataSource hikariSource;

    private HikariConfig config;
    private final JavaPlugin plugin;

    /**
     * Caches and checks the status of a certain database
     *
     * @param driver   Driver to use for this database communication.
     * @param address  Address of the database being checked.
     * @param port     Port on which to communicate with the database being checked.
     * @param database Name of the database being checked.
     * @param plugin
     * @throws SQLException
     */
    public MySqlDatabase(DriverEnum driver, String address, int port, String database, String userName, String password, boolean useSSL, JavaPlugin plugin) throws SQLException {
        this.plugin = plugin;

        switch (driver) {
            case MYSQL:
            case MARIADB:
                this.config = setupConfig(driver, address, port, database, userName, password, useSSL);
                break;
            default:
                Stargate.log(Level.SEVERE, "Unknown driver, '" + driver + "' Stargate currently supports MariaDB, MySql");
                getPluginManager().disablePlugin(plugin);
        }
        hikariSource = setupMySql(this.config);
    }

    public MySqlDatabase(JavaPlugin plugin) throws SQLException {
        this.plugin = plugin;
        this.config = setupConfig();
        hikariSource = setupMySql(this.config);
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

    private HikariDataSource setupMySql(HikariConfig config) throws SQLException {
        return new HikariDataSource(config);
    }

    private HikariConfig setupConfig(DriverEnum driver, String address, int port, String database, String username, String password, boolean useSSL) throws SQLException {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:" + driver.getDriver() + "://" + address + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("useSSL", useSSL);
        return config;
    }

    private HikariConfig setupConfig() throws SQLException {
        // Creates a properties file if it doesn't exist
        if (!new File(plugin.getDataFolder(), "hikari.properties").exists()) {
            plugin.getLogger().warning("hikari.properties file is missing.");
            plugin.getLogger().info("Providing you with a new properties file");
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("hikari.properties", true);
            getPluginManager().disablePlugin(plugin);
        }

        // creates a config based on the properties file
        return new HikariConfig(plugin.getDataFolder() + "/hikari.properties");
    }
}
