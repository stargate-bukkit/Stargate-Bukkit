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
 * Represents a MySQL database
 *
 * @author Frostalf
 * @author Thorin
 */
public class MySqlDatabase implements Database {

    private final HikariDataSource hikariSource;
    private HikariConfig config;
    private final JavaPlugin plugin;

    /**
     * Instantiates a new MySQL database connection using the given values
     *
     * @param driver   <p>The driver to use for this database communication</p>
     * @param address  <p>The address of the MySQL server</p>
     * @param port     <p>The port of the MySQL server</p>
     * @param database <p>The database to store Stargate tables in</p>
     * @param plugin   <p>An instance of the Stargate plugin</p>
     * @throws SQLException <p>If unable to setup a database connection</p>
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

    /**
     * Instantiates a new MySQL database connection using this stargate plugin instance's config
     *
     * @param plugin <p>An instance of the Stargate plugin</p>
     * @throws SQLException <p>If unable to setup a database connection</p>
     */
    public MySqlDatabase(JavaPlugin plugin) throws SQLException {
        this.plugin = plugin;
        this.config = setupConfig();
        hikariSource = setupMySql(this.config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.hikariSource.getConnection();
    }

    /**
     * Sets up a hikari data source
     *
     * @param config <p>The Hikari config to use</p>
     * @return <p>A Hikari data source</p>
     * @throws SQLException <p>If unable to establish the Hikari data source</p>
     */
    private HikariDataSource setupMySql(HikariConfig config) throws SQLException {
        return new HikariDataSource(config);
    }

    /**
     * Creates a new Hikari config used for MySQL connections by using the given input
     *
     * @param driver   <p>The database driver to use</p>
     * @param address  <p>The address of the MySQL server</p>
     * @param port     <p>The port of the MySQL server</p>
     * @param database <p>The database to store Stargate tables in</p>
     * @param username <p>The username for the database</p>
     * @param password <p>The password for the database</p>
     * @param useSSL   <p>Whether to use SSL for connections</p>
     * @return <p>A Hikari config with the given settings</p>
     */
    private HikariConfig setupConfig(DriverEnum driver, String address, int port, String database, String username,
                                     String password, boolean useSSL) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:" + driver.getDriver() + "://" + address + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("useSSL", useSSL);
        return config;
    }

    /**
     * Loads the Hikari config used for MySQL connections from a file
     *
     * <p>If the hikari.properties file doesn't exists, it will be created.</p>
     *
     * @return <p>A Hikari config</p>
     * @throws SQLException <p>If unable to create the directory for the config file</p>
     */
    private HikariConfig setupConfig() throws SQLException {
        // Creates a properties file if it doesn't exist
        if (!new File(plugin.getDataFolder(), "hikari.properties").exists()) {
            plugin.getLogger().warning("hikari.properties file is missing.");
            plugin.getLogger().info("Providing you with a new properties file");
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                throw new SQLException("Unable to create data folders");
            }
            plugin.saveResource("hikari.properties", true);
            getPluginManager().disablePlugin(plugin);
        }

        // Creates a config based on the properties file
        return new HikariConfig(plugin.getDataFolder() + "/hikari.properties");
    }

}
