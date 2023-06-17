package org.sgrewritten.stargate.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * The SQL query generator is responsible for generating prepared statements for various queries
 */
public class SQLQueryGenerator {

    private final StargateLogger logger;
    private final TableNameConfiguration tableNameConfiguration;
    private final DatabaseDriver databaseDriver;

    /**
     * Instantiates a new SQL query generator
     *
     * @param tableNameConfiguration <p>The config to use for table names</p>
     * @param logger                 <p>The logger to use for logging error messages</p>
     * @param databaseDriver         <p>The currently used database driver (for syntax variations)</p>
     */
    public SQLQueryGenerator(TableNameConfiguration tableNameConfiguration, StargateLogger logger, DatabaseDriver databaseDriver) {
        this.tableNameConfiguration = tableNameConfiguration;
        this.logger = logger;
        this.databaseDriver = databaseDriver;
    }

    /**
     * Gets a prepared statement for selecting all portals in a table
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of the portal (used to determine which table to select from)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateGetAllPortalsStatement(Connection connection, StorageType portalType) throws SQLException {
        if (portalType == StorageType.LOCAL) {
            return prepareQuery(connection, getQuery(SQLQuery.GET_ALL_PORTALS));
        } else {
            return prepareQuery(connection, getQuery(SQLQuery.GET_ALL_INTER_PORTALS));
        }
    }

    /**
     * Gets a prepared statement for getting all stored flags
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateGetAllFlagsStatement(Connection connection) throws SQLException {
        return prepareQuery(connection, getQuery(SQLQuery.GET_ALL_PORTAL_FLAGS));
    }

    /**
     * Gets a prepared statement for inserting/updating the last known name of a player
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateUpdateLastKnownNameStatement(Connection connection) throws SQLException {
        return prepareQuery(connection, getQuery(SQLQuery.REPLACE_LAST_KNOWN_NAME));
    }

    /**
     * Gets a prepared statement for creating a new portals table
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of the portal (used to determine which table to create)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreatePortalTableStatement(Connection connection, StorageType portalType) throws SQLException {
        if (portalType == StorageType.LOCAL) {
            return prepareQuery(connection, getQuery(SQLQuery.CREATE_TABLE_PORTAL));
        } else {
            return prepareQuery(connection, getQuery(SQLQuery.CREATE_TABLE_INTER_PORTAL));
        }
    }

    /**
     * Gets a prepared statement for creating the portal position type table
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreatePortalPositionTypeTableStatement(Connection connection) throws SQLException {
        return prepareQuery(connection, getQuery(SQLQuery.CREATE_TABLE_PORTAL_POSITION_TYPE));
    }

    /**
     * Gets a prepared statement for inserting a portal position type into the portal position type table
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateAddPortalPositionTypeStatement(Connection connection) throws SQLException {
        return prepareQuery(connection, getQuery(SQLQuery.INSERT_PORTAL_POSITION_TYPE));
    }

    /**
     * Gets a prepared statement for getting all stored portal position types
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateGetAllPortalPositionTypesStatement(Connection connection) throws SQLException {
        return prepareQuery(connection, getQuery(SQLQuery.GET_ALL_PORTAL_POSITION_TYPES));
    }

    /**
     * Gets a prepared statement for creating the portal position table
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of the portal (used to determine which table to select from)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreatePortalPositionTableStatement(Connection connection, StorageType portalType) throws SQLException {
        if (portalType == StorageType.LOCAL) {
            return prepareQuery(connection, getQuery(SQLQuery.CREATE_TABLE_PORTAL_POSITION));
        } else {
            return prepareQuery(connection, getQuery(SQLQuery.CREATE_TABLE_INTER_PORTAL_POSITION));
        }
    }

    /**
     * Gets a prepared statement for adding an index on portalName, networkName for
     * the portal position table
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of portal to add to the index.</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreatePortalPositionIndex(Connection connection, StorageType portalType) throws SQLException {
        //Skip for non-SQLite if the index already exists
        if (databaseDriver != DatabaseDriver.SQLITE &&
                hasRows(generateShowPortalPositionIndexesStatement(connection, portalType))) {
            return null;
        }

        if (portalType == StorageType.LOCAL) {
            return prepareQuery(connection, getQuery(SQLQuery.CREATE_INDEX_PORTAL_POSITION));
        } else {
            return prepareQuery(connection, getQuery(SQLQuery.CREATE_INDEX_INTER_PORTAL_POSITION));
        }
    }

    /**
     * Gets a prepared statement for inserting a portal position into the portal
     * position table
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of the portal (used to determine which table to select from)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateAddPortalPositionStatement(Connection connection, StorageType portalType) throws SQLException {
        if (portalType == StorageType.LOCAL) {
            return prepareQuery(connection, getQuery(SQLQuery.INSERT_PORTAL_POSITION));
        } else {
            return prepareQuery(connection, getQuery(SQLQuery.INSERT_INTER_PORTAL_POSITION));
        }
    }

    /**
     * Gets a prepared statement for removing a position
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of the portal (used to determine which table to select from)</p>
     * @param portal     <b> The relevent portal.</b>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateRemovePortalPositionsStatement(Connection connection, StorageType portalType, Portal portal) throws SQLException {
        PreparedStatement removePositionsStatement;
        if (portalType == StorageType.LOCAL) {
            removePositionsStatement = prepareQuery(connection, getQuery(SQLQuery.DELETE_PORTAL_POSITIONS));
        } else {
            removePositionsStatement = prepareQuery(connection, getQuery(SQLQuery.DELETE_INTER_PORTAL_POSITIONS));
        }
        removePositionsStatement.setString(1, portal.getName());
        removePositionsStatement.setString(2, portal.getNetwork().getId());
        return removePositionsStatement;
    }

    public PreparedStatement generateRemovePortalPositionStatement(Connection connection, StorageType portalType, Portal portal, PortalPosition portalPosition) throws SQLException {
        PreparedStatement removePositionsStatement;
        if (portalType == StorageType.LOCAL) {
            removePositionsStatement = prepareQuery(connection, getQuery(SQLQuery.DELETE_PORTAL_POSITION));
        } else {
            removePositionsStatement = prepareQuery(connection, getQuery(SQLQuery.DELETE_INTER_PORTAL_POSITION));
        }
        removePositionsStatement.setString(1, portal.getName());
        removePositionsStatement.setString(2, portal.getNetwork().getId());
        BlockVector positionLocation = portalPosition.getPositionLocation();
        removePositionsStatement.setInt(3, positionLocation.getBlockX());
        removePositionsStatement.setInt(4, positionLocation.getBlockY());
        removePositionsStatement.setInt(5, -positionLocation.getBlockZ());
        return removePositionsStatement;
    }

    /**
     * Gets a prepared statement for getting all portal positions for one portal
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of the portal (used to determine which table to select from)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateGetPortalPositionsStatement(Connection connection, StorageType portalType) throws SQLException {
        if (portalType == StorageType.LOCAL) {
            return prepareQuery(connection, getQuery(SQLQuery.GET_PORTAL_POSITIONS));
        } else {
            return prepareQuery(connection, getQuery(SQLQuery.GET_INTER_PORTAL_POSITIONS));
        }
    }

    /**
     * Gets a prepared statement for creating the flag table
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreateFlagTableStatement(Connection connection) throws SQLException {
        return prepareQuery(connection, getQuery(SQLQuery.CREATE_TABLE_PORTAL_FLAG));
    }

    /**
     * Gets a prepared statement for creating the server info table
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreateServerInfoTableStatement(Connection connection) throws SQLException {
        return prepareQuery(connection, getQuery(SQLQuery.CREATE_TABLE_SERVER_INFO));
    }

    /**
     * Gets a prepared statement for creating the last known name table
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreateLastKnownNameTableStatement(Connection connection) throws SQLException {
        return prepareQuery(connection, getQuery(SQLQuery.CREATE_TABLE_LAST_KNOWN_NAME));
    }

    /**
     * Gets a prepared statement for adding a flag to a portal
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of portal to create the table for</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreateFlagRelationTableStatement(Connection connection,
                                                                      StorageType portalType) throws SQLException {
        if (portalType == StorageType.LOCAL) {
            return prepareQuery(connection, getQuery(SQLQuery.CREATE_TABLE_PORTAL_FLAG_RELATION));
        } else {
            return prepareQuery(connection, getQuery(SQLQuery.CREATE_TABLE_INTER_PORTAL_FLAG_RELATION));
        }
    }

    /**
     * Gets a prepared statement for generating the portal view
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of portal to create the view for</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreatePortalViewStatement(Connection connection,
                                                               StorageType portalType) throws SQLException {
        if (portalType == StorageType.LOCAL) {
            return prepareQuery(connection, getQuery(SQLQuery.CREATE_VIEW_PORTAL));
        } else {
            return prepareQuery(connection, getQuery(SQLQuery.CREATE_VIEW_INTER_PORTAL));
        }
    }

    /**
     * Gets a prepared statement for inserting a flag into the flag table
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateAddFlagStatement(Connection connection) throws SQLException {
        return prepareQuery(connection, getQuery(SQLQuery.INSERT_PORTAL_FLAG));
    }

    /**
     * Gets a prepared statement for inserting a relation between a portal and a flag
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The portal type to add the flag for</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateAddPortalFlagRelationStatement(Connection connection,
                                                                    StorageType portalType) throws SQLException {
        if (portalType == StorageType.LOCAL) {
            return prepareQuery(connection, getQuery(SQLQuery.INSERT_PORTAL_FLAG_RELATION));
        } else {
            return prepareQuery(connection, getQuery(SQLQuery.INSERT_INTER_PORTAL_FLAG_RELATION));
        }
    }

    /**
     * Gets a prepared statement for removing the relation between a portal and its flags
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The portal type to remove the flags from</p>
     * @param portal     <b>The relevent portal</b>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateRemoveFlagsStatement(Connection connection,
                                                          StorageType portalType, Portal portal) throws SQLException {
        PreparedStatement removeFlagsStatement;
        if (portalType == StorageType.LOCAL) {
            removeFlagsStatement = prepareQuery(connection, getQuery(SQLQuery.DELETE_PORTAL_FLAG_RELATIONS));
        } else {
            removeFlagsStatement = prepareQuery(connection, getQuery(SQLQuery.DELETE_INTER_PORTAL_FLAG_RELATIONS));
        }
        removeFlagsStatement.setString(1, portal.getName());
        removeFlagsStatement.setString(2, portal.getNetwork().getId());
        return removeFlagsStatement;
    }

    /**
     * Gets a prepared statement for removing the relation between a portal and its flag
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The portal type to remove the flags from</p>
     * @param portal     <b>The relevent portal</b>
     * @param flagChar   <p>A character representing a portal flag</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateRemoveFlagStatement(Connection connection,
                                                         StorageType portalType, Portal portal, Character flagChar) throws SQLException {
        PreparedStatement removeFlagsStatement;
        if (portalType == StorageType.LOCAL) {
            removeFlagsStatement = prepareQuery(connection, getQuery(SQLQuery.DELETE_PORTAL_FLAG_RELATION));
        } else {
            removeFlagsStatement = prepareQuery(connection, getQuery(SQLQuery.DELETE_INTER_PORTAL_FLAG_RELATION));
        }
        removeFlagsStatement.setString(1, portal.getName());
        removeFlagsStatement.setString(2, portal.getNetwork().getName());
        removeFlagsStatement.setString(3, String.valueOf(flagChar));
        return removeFlagsStatement;
    }

    /**
     * Gets a prepared statement for adding a portal
     *
     * @param connection <p>The database connection to use</p>
     * @param portal     <p>The portal to add</p>
     * @param portalType <p>The type of the portal (used to determine which table to update)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateAddPortalStatement(Connection connection, RealPortal portal,
                                                        StorageType portalType) throws SQLException {
        boolean isInterServer = (portalType == StorageType.INTER_SERVER);
        String statementMessage;
        if (isInterServer) {
            statementMessage = getQuery(SQLQuery.INSERT_INTER_PORTAL);
        } else {
            statementMessage = getQuery(SQLQuery.INSERT_PORTAL);
        }
        statementMessage = tableNameConfiguration.replaceKnownTableNames(statementMessage);

        PreparedStatement statement = connection.prepareStatement(statementMessage);

        statement.setString(1, portal.getNetwork().getId());
        statement.setString(2, portal.getName());
        String destinationName = portal.getDestinationName();
        if (destinationName == null) {
            destinationName = "";
        }
        statement.setString(3, destinationName);
        Location topLeft = portal.getGate().getTopLeft();
        World signWorld = topLeft.getWorld();
        statement.setString(4, signWorld != null ? signWorld.getName() : "");
        statement.setInt(5, topLeft.getBlockX());
        statement.setInt(6, topLeft.getBlockY());
        statement.setInt(7, topLeft.getBlockZ());
        statement.setString(8, portal.getOwnerUUID().toString());

        GateAPI gate = portal.getGate();

        statement.setString(9, gate.getFormat().getFileName());
        statement.setInt(10, gate.getFacing().ordinal());
        statement.setBoolean(11, gate.getFlipZ());

        if (isInterServer) {
            statement.setString(12, Stargate.getServerUUID());
        }

        log(Level.FINEST, "sql query: " + statementMessage);
        return statement;
    }

    /**
     * Gets a prepared statement for removing a portal
     *
     * @param connection <p>The database connection to use</p>
     * @param portal     <p>The portal to remove</p>
     * @param portalType <p>The type of the portal (used to determine which table to update)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateRemovePortalStatement(Connection connection, Portal portal,
                                                           StorageType portalType) throws SQLException {
        String statementMessage;
        if (portalType == StorageType.LOCAL) {
            statementMessage = getQuery(SQLQuery.DELETE_PORTAL);
        } else {
            statementMessage = getQuery(SQLQuery.DELETE_INTER_PORTAL);
        }
        statementMessage = tableNameConfiguration.replaceKnownTableNames(statementMessage);

        PreparedStatement statement = connection.prepareStatement(statementMessage);
        statement.setString(1, portal.getName());
        statement.setString(2, portal.getNetwork().getId());
        log(Level.FINEST, "sql query: " + statementMessage);
        return statement;
    }

    /**
     * Gets a prepared statement for updating a server's name
     *
     * @param connection <p>The database connection to use</p>
     * @param serverUUID <p>The UUID of the server to update</p>
     * @param serverName <p>The new name of the server</p>
     * @return <p>The prepared statement for updating the server info</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateUpdateServerInfoStatus(Connection connection, String serverUUID, String serverName) throws SQLException {
        String statementString = getQuery(SQLQuery.REPLACE_SERVER_INFO);
        String statementMessage = tableNameConfiguration.replaceKnownTableNames(statementString);
        log(Level.FINEST, statementMessage);
        PreparedStatement statement = connection.prepareStatement(statementMessage);
        statement.setString(1, serverUUID);
        statement.setString(2, serverName);
        return statement;
    }

    /**
     * Gets a prepared statement for getting the portal position index
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of the portal (used to determine which index to get)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateShowPortalPositionIndexesStatement(Connection connection, StorageType portalType) throws SQLException {
        if (portalType == StorageType.LOCAL) {
            return prepareQuery(connection, getQuery(SQLQuery.SHOW_INDEX_PORTAL_POSITION));
        } else {
            return prepareQuery(connection, getQuery(SQLQuery.SHOW_INDEX_INTER_PORTAL_POSITION));
        }
    }

    /**
     * Checks whether the given query returns any rows
     *
     * @param preparedStatement <p>A prepared statement with the prepared query</p>
     * @return <p>True if at least one row was found</p>
     * @throws SQLException <p>If a problem occurs</p>
     */
    private boolean hasRows(PreparedStatement preparedStatement) throws SQLException {
        boolean hasRow = preparedStatement.executeQuery().next();
        preparedStatement.close();
        return hasRow;
    }

    /**
     * Gets the query string for the given SQL query
     *
     * @param sqlQuery <p>The SQL query to get</p>
     * @return <p>The query string</p>
     */
    private String getQuery(SQLQuery sqlQuery) {
        return SQLQueryHandler.getQuery(sqlQuery, databaseDriver);
    }

    /**
     * Prepares the given query for execution
     *
     * @param connection <p>The database connection to use</p>
     * @param query      <p>The query to run</p>
     * @return <p>The resulting prepared statement</p>
     * @throws SQLException <p>If unable to prepare the query for execution</p>
     */
    private PreparedStatement prepareQuery(Connection connection, String query) throws SQLException {
        query = tableNameConfiguration.replaceKnownTableNames(query);
        log(Level.FINEST, query);
        return connection.prepareStatement(query);
    }

    public PreparedStatement generateGetPortalStatement(Connection connection, Portal portal, StorageType portalType) throws SQLException {
        PreparedStatement statement;
        if (portalType == StorageType.LOCAL) {
            statement = prepareQuery(connection, getQuery(SQLQuery.GET_PORTAL));
        } else {
            statement = prepareQuery(connection, getQuery(SQLQuery.GET_INTER_PORTAL));
        }
        statement.setString(1, portal.getName());
        statement.setString(2, portal.getNetwork().getId());
        return statement;
    }

    public PreparedStatement generateSetPortalMetaStatement(Connection connection, Portal portal, String meta, StorageType portalType) throws SQLException {
        PreparedStatement statement;
        if (portalType == StorageType.LOCAL) {
            statement = prepareQuery(connection, getQuery(SQLQuery.SET_PORTAL_META));
        } else {
            statement = prepareQuery(connection, getQuery(SQLQuery.SET_INTER_PORTAL_META));
        }
        statement.setString(1, meta);
        statement.setString(2, portal.getName());
        statement.setString(3, portal.getNetwork().getId());
        return statement;
    }

    public PreparedStatement generateSetPortalPositionMeta(Connection connection, RealPortal portal, PortalPosition portalPosition,
                                                           String meta, StorageType portalType) throws SQLException {
        PreparedStatement statement;
        if (portalType == StorageType.LOCAL) {
            statement = prepareQuery(connection, getQuery(SQLQuery.SET_PORTAL_POSITION_META));
        } else {
            statement = prepareQuery(connection, getQuery(SQLQuery.SET_INTER_PORTAL_POSITION_META));
        }
        statement.setString(1, meta);
        statement.setString(2, portal.getName());
        statement.setString(3, portal.getNetwork().getId());
        BlockVector vector = portalPosition.getPositionLocation();
        statement.setInt(4, vector.getBlockX());
        statement.setInt(5, vector.getBlockY());
        statement.setInt(6, -vector.getBlockZ());
        return statement;
    }

    public PreparedStatement generateGetPortalPositionStatement(Connection connection, Portal portal,
                                                                PortalPosition portalPosition, StorageType portalType) throws SQLException {
        PreparedStatement statement;
        if (portalType == StorageType.LOCAL) {
            statement = prepareQuery(connection, getQuery(SQLQuery.GET_PORTAL_POSITION_META));
        } else {
            statement = prepareQuery(connection, getQuery(SQLQuery.GET_INTER_PORTAL_POSITION_META));
        }
        statement.setString(1, portal.getName());
        statement.setString(2, portal.getNetwork().getId());
        BlockVector vector = portalPosition.getPositionLocation();
        statement.setInt(3, vector.getBlockX());
        statement.setInt(4, vector.getBlockY());
        statement.setInt(5, -vector.getBlockZ());
        return statement;
    }

    public PreparedStatement generateUpdateNetworkNameStatement(Connection connection, String newName, String networkName,
                                                                StorageType portalType) throws SQLException {
        PreparedStatement statement;
        if (portalType == StorageType.LOCAL) {
            statement = prepareQuery(connection, getQuery(SQLQuery.UPDATE_NETWORK_NAME));
        } else {
            statement = prepareQuery(connection, getQuery(SQLQuery.UPDATE_INTER_NETWORK_NAME));
        }
        statement.setString(1, newName);
        statement.setString(2, networkName);
        return statement;
    }

    public PreparedStatement generateUpdatePortalNameStatement(Connection connection, String newName, String portalName, String networkName,
                                                               StorageType portalType) throws SQLException {
        PreparedStatement statement;
        if (portalType == StorageType.LOCAL) {
            statement = prepareQuery(connection, getQuery(SQLQuery.UPDATE_PORTAL_NAME));
        } else {
            statement = prepareQuery(connection, getQuery(SQLQuery.UPDATE_INTER_PORTAL_NAME));
        }
        statement.setString(1, newName);
        statement.setString(2, portalName);
        statement.setString(3, networkName);
        return statement;
    }

    public PreparedStatement generateGetAllPortalsOfNetwork(Connection connection, String netName,
                                                            StorageType portalType) throws SQLException {
        PreparedStatement statement;
        if (portalType == StorageType.LOCAL) {
            statement = prepareQuery(connection, getQuery(SQLQuery.GET_ALL_PORTALS_OF_NETWORK));
        } else {
            statement = prepareQuery(connection, getQuery(SQLQuery.GET_ALL_INTER_PORTALS_OF_NETWORK));
        }
        statement.setString(1, netName);
        return statement;
    }

    /**
     * Logs a message at the finest log level
     *
     * @param message <p>The message to log</p>
     */
    public void log(Level level, String message) {
        if (logger != null) {
            logger.logMessage(level, message);
        } else {
            Bukkit.getLogger().log(level, message);
        }
    }

}
