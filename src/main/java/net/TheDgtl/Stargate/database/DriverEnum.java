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
public enum DriverEnum {

    /**
     * Represents a MySQL driver
     */
    MYSQL("mysql"),

    /**
     * Represents a MariaDB driver
     */
    MARIADB("mysql"),

    /**
     * Represents an SQLite driver
     */
    SQLITE(""),

    /**
     * Represents a PostgreSQL driver
     * TODO: This is never used
     */
    POSTGRESQL("");

    private final String driver;

    /**
     * Instantiates a new driver enum
     *
     * @param driver <p>The string representation of the database driver</p>
     */
    DriverEnum(String driver) {
        this.driver = driver;
    }

    /**
     * Gets the string representation of this database driver
     *
     * @return <p>The string representation of this database driver</p>
     */
    public String getDriver() {
        return driver;
    }
}
