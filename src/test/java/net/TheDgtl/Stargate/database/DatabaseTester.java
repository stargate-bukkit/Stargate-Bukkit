package net.TheDgtl.Stargate.database;

import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.SQLQueryGenerator;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import org.junit.jupiter.api.Assertions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DatabaseTester {

    private static Database database;
    private static Connection connection;
    private static SQLQueryGenerator generator;

    private static IPortal testPortal;

    public DatabaseTester(Database database, Connection connection, SQLQueryGenerator generator, IPortal testPortal) {
        DatabaseTester.database = database;
        DatabaseTester.connection = connection;
        DatabaseTester.generator = generator;
        DatabaseTester.testPortal = testPortal;
    }

    public static void tearDown() throws SQLException {
        connection.close();
    }

    void addPortalTableTest() throws SQLException {
        finishStatement(generator.generateCreateTableStatement(connection, PortalType.LOCAL));
    }

    void addInterPortalTableTest() throws SQLException {
        finishStatement(generator.generateCreateTableStatement(connection, PortalType.INTER_SERVER));
    }

    void createFlagTableTest() throws SQLException {
        finishStatement(generator.generateCreateFlagTableStatement(connection));
    }

    void createPortalFlagRelationTableTest() throws SQLException {
        finishStatement(generator.generateCreateFlagRelationTableStatement(connection));
    }

    void createPortalViewTest() throws SQLException {
        finishStatement(generator.generateCreatePortalViewTableStatement(connection));
    }

    void addFlagsTest() throws SQLException {
        PreparedStatement statement = generator.generateAddFlagStatement(connection);

        for (PortalFlag flag : PortalFlag.values()) {
            System.out.println("Adding flag " + flag.getLabel() + " to the database");
            statement.setString(1, String.valueOf(flag.getLabel()));
            statement.execute();
        }
        statement.close();
    }

    void getFlagsTest() throws SQLException {
        printTableInfo("SG_Hub_Flag");

        PreparedStatement statement = generator.generateGetAllFlagsStatement(connection);

        ResultSet set = statement.executeQuery();
        ResultSetMetaData metaData = set.getMetaData();

        int rows = 0;
        while (set.next()) {
            System.out.print("Flag ");
            rows++;
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                System.out.print(metaData.getColumnName(i + 1) + " = " + set.getObject(i + 1) + ", ");
            }
            System.out.println();
        }
        Assertions.assertTrue(rows > 0);
    }

    void addPortalTest() throws SQLException {
        connection.setAutoCommit(false);
        try {
            finishStatement(generator.generateAddPortalStatement(connection, testPortal, PortalType.LOCAL));

            PreparedStatement addFlagStatement = generator.generateAddPortalFlagRelationStatement(connection);
            for (Character character : testPortal.getAllFlagsString().toCharArray()) {
                System.out.println("Adding flag " + character + " to portal: " + testPortal.toString());
                addFlagStatement.setString(1, testPortal.getName());
                addFlagStatement.setString(2, testPortal.getNetwork().getName());
                addFlagStatement.setString(3, String.valueOf(character));
                addFlagStatement.execute();
            }
            addFlagStatement.close();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException exception) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw exception;
        }
    }

    void getPortalTest() throws SQLException {
        printTableInfo("SG_Hub_PortalView");

        PreparedStatement statement = generator.generateGetAllPortalsStatement(connection, PortalType.LOCAL);

        ResultSet set = statement.executeQuery();
        ResultSetMetaData metaData = set.getMetaData();

        int rows = 0;
        while (set.next()) {
            rows++;
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                System.out.print(
                        metaData.getColumnName(i + 1) + " = " + set.getObject(i + 1) + ", ");
            }
            System.out.println();
        }
        Assertions.assertTrue(rows > 0);
    }

    void destroyPortalTest() throws SQLException {
        finishStatement(generator.generateRemovePortalStatement(connection, testPortal, PortalType.LOCAL));

        PreparedStatement statement = database.getConnection().prepareStatement("SELECT * FROM SG_Hub_Portal"
                + " WHERE name = ? AND network = ?");
        statement.setString(1, testPortal.getName());
        statement.setString(2, testPortal.getNetwork().getName());
        ResultSet set = statement.executeQuery();
        Assertions.assertFalse(set.next());
    }

    /**
     * Prints info about a table for debugging
     *
     * @param tableName <p>The table to get information about</p>
     * @throws SQLException <p>If unable to get information about the table</p>
     */
    private static void printTableInfo(String tableName) throws SQLException {
        System.out.println("Getting table info for: " + tableName);
        PreparedStatement tableInfoStatement = connection.prepareStatement(String.format("pragma table_info('%s');", tableName));
        ResultSet infoResult = tableInfoStatement.executeQuery();
        ResultSetMetaData infoMetaData = infoResult.getMetaData();
        while (infoResult.next()) {
            for (int i = 1; i < infoMetaData.getColumnCount() - 1; i++) {
                System.out.print(
                        infoMetaData.getColumnName(i) + " = " + infoResult.getObject(i) + ", ");
            }
            System.out.println();
        }
    }

    /**
     * Finishes a prepared statement by executing and closing it
     *
     * @param statement <p>The prepared statement to finish</p>
     * @throws SQLException <p>If unable to finish the statement</p>
     */
    static void finishStatement(PreparedStatement statement) throws SQLException {
        statement.execute();
        statement.close();
    }

}
