package org.sgrewritten.stargate.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.logging.Level;

import org.junit.jupiter.api.Assertions;
import org.sgrewritten.stargate.Stargate;

public class SQLTestHelper {
    /**
     * Prints info about a table for debugging
     *
     * @param tableName <p>The table to get information about</p>
     * @throws SQLException <p>If unable to get information about the table</p>
     */
    public static void printTableInfo(Level logLevel, String tableName, Connection connection, boolean isMySQL) throws SQLException {
        Stargate.log(logLevel,"Getting table info for: " + tableName);
        String statementMsg = String.format(!isMySQL ? "pragma table_info('%s');" : "DESCRIBE %s", tableName);

        PreparedStatement tableInfoStatement = connection.prepareStatement(statementMsg);
        ResultSet infoResult = tableInfoStatement.executeQuery();
        ResultSetMetaData infoMetaData = infoResult.getMetaData();
        while (infoResult.next()) {
            String msg = "";
            for (int i = 1; i < infoMetaData.getColumnCount() - 1; i++) {
                msg = msg +
                        infoMetaData.getColumnName(i) + " = " + infoResult.getObject(i) + ", ";
            }
            Stargate.log(logLevel, msg);
        }
    }
    
    /**
     * Checks if a table, where each element is identified by a name and a network does not contain an element
     *
     * @param table   <p>The name of the table to check</p>
     * @param name    <p>The name of the element to check for</p>
     * @param network <p>The network of the element to check for</p>
     * @throws SQLException <p>If unable to get data from the database</p>
     */
    public static void checkIfHasNot(String table, String name, String network, Connection connection) throws SQLException {
        PreparedStatement statement;
        if(table.contains("PortalPosition")) {
            statement = connection.prepareStatement("SELECT * FROM " + table +
                    " WHERE portalName = ? AND networkName = ?");
        } else {
            statement = connection.prepareStatement("SELECT * FROM " + table +
                    " WHERE name = ? AND network = ?");
        }
        statement.setString(1, name);
        statement.setString(2, network);
        ResultSet set = statement.executeQuery();
        Assertions.assertFalse(set.next());
        statement.close();
    }
    
    public static void checkIfHas(String table, String name, String network, Connection connection) throws SQLException {
        PreparedStatement statement;
        if(table.contains("PortalPosition$")) {
            statement = connection.prepareStatement("SELECT * FROM " + table +
                    " WHERE portalName = ? AND networkName = ?");
        } else {
            statement = connection.prepareStatement("SELECT * FROM " + table +
                    " WHERE name = ? AND network = ?");
        }
        statement.setString(1, name);
        statement.setString(2, network);
        ResultSet set = statement.executeQuery();
        Assertions.assertTrue(set.next());
        statement.close();
    }

}
