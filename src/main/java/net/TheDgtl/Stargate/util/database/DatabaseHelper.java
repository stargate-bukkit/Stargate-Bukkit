package net.TheDgtl.Stargate.util.database;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.config.TableNameConfiguration;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.DatabaseDriver;
import net.TheDgtl.Stargate.database.MySqlDatabase;
import net.TheDgtl.Stargate.database.SQLQueryGenerator;
import net.TheDgtl.Stargate.database.SQLiteDatabase;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.StargateInitializationException;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalData;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.PositionType;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;

public class DatabaseHelper {
    /**
     * Executes and closes the given statement
     *
     * @param statement <p>The statement to execute</p>
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public static void runStatement(PreparedStatement statement) throws SQLException {
        statement.execute();
        statement.close();
    }
    
    public static void createTables(Database database, SQLQueryGenerator sqlQueryGenerator, boolean useInterServerNetworks) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement localPortalsStatement = sqlQueryGenerator.generateCreatePortalTableStatement(connection, PortalType.LOCAL);
        runStatement(localPortalsStatement);
        PreparedStatement flagStatement = sqlQueryGenerator.generateCreateFlagTableStatement(connection);
        runStatement(flagStatement);
        addMissingFlags(connection, sqlQueryGenerator);

        PreparedStatement portalPositionTypesStatement = sqlQueryGenerator.generateCreatePortalPositionTypeTableStatement(connection);
        runStatement(portalPositionTypesStatement);
        addMissingPositionTypes(connection, sqlQueryGenerator);

        PreparedStatement portalPositionsStatement = sqlQueryGenerator.generateCreatePortalPositionTableStatement(connection, PortalType.LOCAL);
        runStatement(portalPositionsStatement);
        PreparedStatement portalPositionIndex = sqlQueryGenerator.generateCreatePortalPositionIndex(connection, PortalType.LOCAL);
        if (portalPositionIndex != null) {
            runStatement(portalPositionIndex);
        }

        PreparedStatement lastKnownNameStatement = sqlQueryGenerator.generateCreateLastKnownNameTableStatement(connection);
        runStatement(lastKnownNameStatement);
        PreparedStatement portalRelationStatement = sqlQueryGenerator.generateCreateFlagRelationTableStatement(connection, PortalType.LOCAL);
        runStatement(portalRelationStatement);
        PreparedStatement portalViewStatement = sqlQueryGenerator.generateCreatePortalViewStatement(connection, PortalType.LOCAL);
        runStatement(portalViewStatement);
        DatabaseHelper.tableRefactor_1_0_0_13(connection, sqlQueryGenerator, useInterServerNetworks);

        if (!useInterServerNetworks) {
            connection.close();
            return;
        }

        PreparedStatement serverInfoStatement = sqlQueryGenerator.generateCreateServerInfoTableStatement(connection);
        runStatement(serverInfoStatement);
        PreparedStatement interServerPortalsStatement = sqlQueryGenerator.generateCreatePortalTableStatement(connection, PortalType.INTER_SERVER);
        runStatement(interServerPortalsStatement);
        PreparedStatement interServerRelationStatement = sqlQueryGenerator.generateCreateFlagRelationTableStatement(connection, PortalType.INTER_SERVER);
        runStatement(interServerRelationStatement);
        PreparedStatement interPortalViewStatement = sqlQueryGenerator.generateCreatePortalViewStatement(connection, PortalType.INTER_SERVER);
        runStatement(interPortalViewStatement);
        PreparedStatement interPortalPositionsStatement = sqlQueryGenerator.generateCreatePortalPositionTableStatement(connection, PortalType.INTER_SERVER);
        runStatement(interPortalPositionsStatement);
        PreparedStatement interPortalPositionIndex = sqlQueryGenerator.generateCreatePortalPositionIndex(connection, PortalType.INTER_SERVER);
        if (interPortalPositionIndex != null) {
            runStatement(interPortalPositionIndex);
        }

        connection.close();
    }


    /**
     * Adds any flags not already in the database
     *
     * @param connection        <p>The database connection to use</p>
     * @param sqlQueryGenerator <p>The SQL Query Generator to use for generating queries</p>
     * @throws SQLException <p>If unable to get from, or update the database</p>
     */
    private static void addMissingFlags(Connection connection, SQLQueryGenerator sqlQueryGenerator) throws SQLException {
        PreparedStatement statement = sqlQueryGenerator.generateGetAllFlagsStatement(connection);
        PreparedStatement addStatement = sqlQueryGenerator.generateAddFlagStatement(connection);

        ResultSet resultSet = statement.executeQuery();
        List<String> knownFlags = new ArrayList<>();
        while (resultSet.next()) {
            knownFlags.add(resultSet.getString("character"));
        }
        for (PortalFlag flag : PortalFlag.values()) {
            if (!knownFlags.contains(String.valueOf(flag.getCharacterRepresentation()))) {
                addStatement.setString(1, String.valueOf(flag.getCharacterRepresentation()));
                addStatement.execute();
            }
        }
        statement.close();
        addStatement.close();
    }

    /**
     * Adds any position types not already in the database
     *
     * @param connection        <p>The database connection to use</p>
     * @param sqlQueryGenerator <p>The SQL Query Generator to use for generating queries</p>
     * @throws SQLException <p>If unable to get from, or update the database</p>
     */
    private static void addMissingPositionTypes(Connection connection, SQLQueryGenerator sqlQueryGenerator) throws SQLException {
        PreparedStatement statement = sqlQueryGenerator.generateGetAllPortalPositionTypesStatement(connection);
        PreparedStatement addStatement = sqlQueryGenerator.generateAddPortalPositionTypeStatement(connection);

        ResultSet resultSet = statement.executeQuery();
        List<String> knownPositionTypes = new ArrayList<>();
        while (resultSet.next()) {
            knownPositionTypes.add(resultSet.getString("positionName"));
        }
        for (PositionType type : PositionType.values()) {
            if (!knownPositionTypes.contains(type.toString())) {
                addStatement.setString(1, type.toString());
                addStatement.execute();
            }
        }
        statement.close();
        addStatement.close();
    }

    /**
     * Loads the database
     *
     * @param stargate <p>The Stargate instance to use for initialization</p>
     * @return <p>The loaded database</p>
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public static Database loadDatabase(Stargate stargate) throws SQLException, StargateInitializationException {
        if (ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE)) {
            if (ConfigurationHelper.getBoolean(ConfigurationOption.SHOW_HIKARI_CONFIG)) {
                return new MySqlDatabase(stargate);
            }

            DatabaseDriver driver = DatabaseDriver.valueOf(ConfigurationHelper.getString(ConfigurationOption.BUNGEE_DRIVER).toUpperCase());
            String bungeeDatabaseName = ConfigurationHelper.getString(ConfigurationOption.BUNGEE_DATABASE);
            int port = ConfigurationHelper.getInteger(ConfigurationOption.BUNGEE_PORT);
            String address = ConfigurationHelper.getString(ConfigurationOption.BUNGEE_ADDRESS);
            String username = ConfigurationHelper.getString(ConfigurationOption.BUNGEE_USERNAME);
            String password = ConfigurationHelper.getString(ConfigurationOption.BUNGEE_PASSWORD);
            boolean useSSL = ConfigurationHelper.getBoolean(ConfigurationOption.BUNGEE_USE_SSL);

            switch (driver) {
                case MARIADB:
                case MYSQL:
                    return new MySqlDatabase(driver, address, port, bungeeDatabaseName, username, password, useSSL);
                default:
                    throw new SQLException("Unsupported driver: Stargate currently supports MariaDb and MySql for remote databases");
            }
        } else {
            String databaseName = ConfigurationHelper.getString(ConfigurationOption.DATABASE_NAME);
            File file = new File(stargate.getAbsoluteDataFolder(), databaseName + ".db");
            return new SQLiteDatabase(file);
        }
    }
    
    public static SQLQueryGenerator getSQLGenerator(Stargate stargate, boolean usingRemoteDatabase) {
        TableNameConfiguration config = DatabaseHelper.getTableNameConfiguration(usingRemoteDatabase);
        DatabaseDriver databaseEnum = usingRemoteDatabase ? DatabaseDriver.MYSQL : DatabaseDriver.SQLITE;
        return new SQLQueryGenerator(config, stargate, databaseEnum);
    }
    
    public static TableNameConfiguration getTableNameConfiguration(boolean usingRemoteDatabase) {
        String PREFIX = usingRemoteDatabase ? ConfigurationHelper.getString(ConfigurationOption.BUNGEE_INSTANCE_NAME)
                : "";
        String serverPrefix = usingRemoteDatabase ? Stargate.getServerUUID() : "";
        return new TableNameConfiguration(PREFIX, serverPrefix.replace("-", ""));
    }
    
    public static void tableRefactor_1_0_0_13(Connection connection, SQLQueryGenerator sqlQueryGenerator, boolean useInterServerNetworks) throws SQLException {
        DatabaseHelper.runStatement(sqlQueryGenerator.generateAddMetaToPortalTableStatement(connection, PortalType.LOCAL));
        if(useInterServerNetworks) {
            DatabaseHelper.runStatement(sqlQueryGenerator.generateAddMetaToPortalTableStatement(connection, PortalType.INTER_SERVER));
        }
    }
}
