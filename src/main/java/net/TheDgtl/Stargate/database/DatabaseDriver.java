/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.TheDgtl.Stargate.database;

/**
 * An enum representing the different SQL drivers available
 *
 * @author Frostalf
 */
public enum DatabaseDriver {

    /**
     * Represents a MySQL driver
     */
    MYSQL("mysql", "mysql-queries.properties"),

    /**
     * Represents a MariaDB driver
     */
    MARIADB("mysql", "mysql-queries.properties"),

    /**
     * Represents an SQLite driver
     */
    SQLITE("", "sqlite-queries.properties"),

    /**
     * Represents a PostgreSQL driver
     * TODO: This is never used
     */
    POSTGRESQL("", "");

    private final String driver;
    private final String queryFile;

    /**
     * Instantiates a new driver enum
     *
     * @param driver    <p>The string representation of the database driver</p>
     * @param queryFile <p>The name of the file containing this driver's queries</p>
     */
    DatabaseDriver(String driver, String queryFile) {
        this.driver = driver;
        this.queryFile = queryFile;
    }

    /**
     * Gets the string representation of this database driver
     *
     * @return <p>The string representation of this database driver</p>
     */
    public String getDriver() {
        return this.driver;
    }

    /**
     * Gets the name of the query file containing this driver's queries
     *
     * @return <p>The name of the query file containing this driver's queries</p>
     */
    public String getQueryFile() {
        return this.queryFile;
    }

}
