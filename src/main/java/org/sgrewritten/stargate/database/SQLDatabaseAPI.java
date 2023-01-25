package org.sgrewritten.stargate.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * An interface representing an SQL database
 */
public interface SQLDatabaseAPI {

    /**
     * Gets a database connection
     *
     * @return <p>A database connection</p>
     * @throws SQLException <p>If unable to establish a database connection</p>
     */
    Connection getConnection() throws SQLException;
    
    DatabaseDriver getDriver();

}
