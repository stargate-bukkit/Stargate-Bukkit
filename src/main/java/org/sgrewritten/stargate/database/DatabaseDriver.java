/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.sgrewritten.stargate.database;

/**
 * An enum representing the different SQL drivers available
 *
 * @author Frostalf
 */
public enum DatabaseDriver {

    /**
     * Represents a MySQL driver
     */
    MYSQL("mysql", "mysql-queries"),

    /**
     * Represents a MariaDB driver
     */
    MARIADB("mysql", "mysql-queries"),

    /**
     * Represents an SQLite driver
     */
    SQLITE("", "sqlite-queries"),

    /**
     * Represents a PostgreSQL driver
     */
    POSTGRESQL("postgresql", "postgresql-queries");

    private final String driver;
    private final String queryFolder;

    /**
     * Instantiates a new driver enum
     *
     * @param driver      <p>The string representation of the database driver</p>
     * @param queryFolder <p>The name of the folder containing this driver's queries</p>
     */
    DatabaseDriver(String driver, String queryFolder) {
        this.driver = driver;
        this.queryFolder = queryFolder;
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
    public String getQueryFolder() {
        return this.queryFolder;
    }

}
