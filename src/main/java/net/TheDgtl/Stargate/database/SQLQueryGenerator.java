package net.TheDgtl.Stargate.database;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.config.TableNameConfig;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * The SQL query generator is responsible for generating prepared statements for various queries
 */
public class SQLQueryGenerator {

    private final StargateLogger logger;
    private final TableNameConfig tableNameConfig;
    private final DriverEnum driverEnum;

    /**
     * Instantiates a new SQL query generator
     *
     * @param tableNameConfig <p>The config to use for table names</p>
     * @param logger          <p>The logger to use for logging error messages</p>
     * @param driverEnum      <p>The currently used database driver (for syntax variations)</p>
     */
    public SQLQueryGenerator(TableNameConfig tableNameConfig, StargateLogger logger, DriverEnum driverEnum) {
        this.tableNameConfig = tableNameConfig;
        this.logger = logger;
        this.driverEnum = driverEnum;
    }

    /**
     * Gets a prepared statement for selecting all portals in a table
     *
     * @param conn       <p>The database connection to use</p>
     * @param portalType <p>The type of the portal (used to determine which table to select from)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateGetAllPortalsStatement(Connection conn, PortalType portalType) throws SQLException {
        String statementMessage = "SELECT * FROM {PortalView};";
        statementMessage = adjustStatementForPortalType(statementMessage, portalType);
        logger.logMessage(Level.FINEST, statementMessage);
        return conn.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for getting all stored flags
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateGetAllFlagsStatement(Connection connection) throws SQLException {
        String statementMessage = "SELECT id, `character` FROM {Flag};";
        statementMessage = replaceKnownTableNames(statementMessage);
        logger.logMessage(Level.FINEST, statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for inserting/updating the last known name of a player
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateUpdateLastKnownNameStatement(Connection connection) throws SQLException {
        String statementMessage = "REPLACE INTO {LastKnownName} (uuid, lastKnownName) VALUES (?, ?);";
        statementMessage = replaceKnownTableNames(statementMessage);
        logger.logMessage(Level.FINEST, statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for creating a new portals table
     *
     * @param conn       <p>The database connection to use</p>
     * @param portalType <p>The type of the portal (used to determine which table to create)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreatePortalTableStatement(Connection conn, PortalType portalType) throws SQLException {
        String interServerExtraFields = (portalType == PortalType.INTER_SERVER) ?
                " isOnline BOOLEAN, homeServerId VARCHAR(36)," : "";
        String statementMessage = String.format("CREATE TABLE IF NOT EXISTS {Portal} (name NVARCHAR(180), network NVARCHAR(180), " +
                "destination NVARCHAR(180), world NVARCHAR(255) NOT NULL, x INTEGER, y INTEGER, z INTEGER, ownerUUID VARCHAR(36),%s " +
                "PRIMARY KEY (name, network));", interServerExtraFields);
        statementMessage = adjustStatementForPortalType(statementMessage, portalType);
        //TODO: Add CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci') equivalent for SQLite
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return conn.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for creating the flag table
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreateFlagTableStatement(Connection connection) throws SQLException {
        String autoIncrement = (driverEnum == DriverEnum.MARIADB || driverEnum == DriverEnum.MYSQL) ?
                "AUTO_INCREMENT" : "AUTOINCREMENT";
        String statementMessage = String.format("CREATE TABLE IF NOT EXISTS {Flag} (id INTEGER PRIMARY KEY %s, `character` CHAR(1) " +
                "UNIQUE NOT NULL);", autoIncrement);
        statementMessage = replaceKnownTableNames(statementMessage);
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for creating the server info table
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreateServerInfoTableStatement(Connection connection) throws SQLException {
        String statementMessage = "CREATE TABLE IF NOT EXISTS {ServerInfo} (serverId VARCHAR(36), serverName NVARCHAR(255), " +
                " PRIMARY KEY (serverId));";
        statementMessage = replaceKnownTableNames(statementMessage);
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for creating the last known name table
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreateLastKnownNameTableStatement(Connection connection) throws SQLException {
        String statementMessage = "CREATE TABLE IF NOT EXISTS {LastKnownName} (uuid VARCHAR(36), lastKnownName VARCHAR(16), " +
                "PRIMARY KEY (uuid));";
        statementMessage = replaceKnownTableNames(statementMessage);
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for adding a flag to a portal
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of portal to create the table for</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreateFlagRelationTableStatement(Connection connection, PortalType portalType) throws SQLException {
        String statementMessage = "CREATE TABLE IF NOT EXISTS {PortalFlagRelation} (name NVARCHAR(180), " +
                "network NVARCHAR(180), flag INTEGER, PRIMARY KEY (name, network, flag), FOREIGN KEY (name, network) " +
                "REFERENCES {Portal} (name, network), FOREIGN KEY (flag) REFERENCES {Flag} (id));";
        statementMessage = adjustStatementForPortalType(statementMessage, portalType);
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for generating the portal view
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The type of portal to create the view for</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreatePortalViewStatement(Connection connection, PortalType portalType) throws SQLException {
        String selectServerName = portalType == PortalType.INTER_SERVER ? ", {ServerInfo}.serverName" : "";
        String joinServerName = portalType == PortalType.INTER_SERVER ?
                " LEFT OUTER JOIN {ServerInfo} ON {ServerInfo}.serverId = {InterPortal}.homeServerId" : "";
        String statementMessage = String.format("CREATE VIEW IF NOT EXISTS {PortalView} AS SELECT {Portal}.*, " +
                "COALESCE(GROUP_CONCAT({Flag}.`character`, ''), '') AS flags, {LastKnownName}.lastKnownName%s FROM " +
                "{Portal} LEFT OUTER JOIN {PortalFlagRelation} ON {Portal}.name = {PortalFlagRelation}.name AND " +
                "{Portal}.network = {PortalFlagRelation}.network LEFT OUTER JOIN {Flag} ON {PortalFlagRelation}.flag = " +
                "{Flag}.id LEFT OUTER JOIN {LastKnownName} ON {Portal}.network = {LastKnownName}.uuid%s GROUP BY " +
                "{Portal}.name, {Portal}.network;", selectServerName, joinServerName);
        statementMessage = adjustStatementForPortalType(statementMessage, portalType);
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for inserting a flag into the flag table
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateAddFlagStatement(Connection connection) throws SQLException {
        String statementMessage = String.format("INSERT INTO %s (`character`) VALUES (?);", tableNameConfig.getFlagTableName());
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for inserting a relation between a portal and a flag
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The portal type to add the flag for</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateAddPortalFlagRelationStatement(Connection connection, PortalType portalType) throws SQLException {
        String statementMessage = "INSERT INTO {PortalFlagRelation} (name, network, flag) VALUES (?, ?, " +
                "(SELECT id FROM {Flag} WHERE `character` = ?));";
        statementMessage = adjustStatementForPortalType(statementMessage, portalType);
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for removing the relation between a portal and its flags
     *
     * @param connection <p>The database connection to use</p>
     * @param portalType <p>The portal type to remove the flags from</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateRemoveFlagStatement(Connection connection, PortalType portalType) throws SQLException {
        String statementMessage = "DELETE FROM {PortalFlagRelation} WHERE name = ? AND network = ?";
        statementMessage = adjustStatementForPortalType(statementMessage, portalType);
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for adding a portal
     *
     * @param conn       <p>The database connection to use</p>
     * @param portal     <p>The portal to add</p>
     * @param portalType <p>The type of the portal (used to determine which table to update)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateAddPortalStatement(Connection conn, IPortal portal,
                                                        PortalType portalType) throws SQLException {
        boolean isInterServer = (portalType == PortalType.INTER_SERVER);
        String extraKeys = (isInterServer ? ", homeServerId, isOnline" : "");
        String extraValues = (isInterServer ? ", ?, ?" : "");
        String statementMessage = String.format("INSERT INTO {Portal} (network, name, destination, world, x, y, z, " +
                "ownerUUID%s) VALUES (?, ?, ?, ?, ?, ?, ?, ?%s);", extraKeys, extraValues);
        statementMessage = adjustStatementForPortalType(statementMessage, portalType);

        PreparedStatement statement = conn.prepareStatement(statementMessage);

        statement.setString(1, portal.getNetwork().getName());
        statement.setString(2, portal.getName());
        String destinationString = null;
        if (portal instanceof Portal) {
            IPortal destination = ((Portal) portal).loadDestination();
            if (destination != null) {
                destinationString = destination.getName();
            }
        }
        statement.setString(3, destinationString);
        Location signLocation = portal.getSignLocation();
        World signWorld = signLocation.getWorld();
        statement.setString(4, signWorld != null ? signWorld.getName() : "");
        statement.setInt(5, signLocation.getBlockX());
        statement.setInt(6, signLocation.getBlockY());
        statement.setInt(7, signLocation.getBlockZ());
        statement.setString(8, portal.getOwnerUUID().toString());

        if (isInterServer) {
            statement.setString(9, Stargate.serverUUID.toString());
            statement.setBoolean(10, true);
        }

        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);

        return statement;
    }

    /**
     * Gets a prepared statement for removing a portal
     *
     * @param conn       <p>The database connection to use</p>
     * @param portal     <p>The portal to remove</p>
     * @param portalType <p>The type of the portal (used to determine which table to update)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateRemovePortalStatement(Connection conn, IPortal portal,
                                                           PortalType portalType) throws SQLException {
        String statementMessage = "DELETE FROM {Portal} WHERE name = ? AND network = ?";
        statementMessage = adjustStatementForPortalType(statementMessage, portalType);

        PreparedStatement statement = conn.prepareStatement(statementMessage);
        statement.setString(1, portal.getName());
        statement.setString(2, portal.getNetwork().getName());
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return statement;
    }

    /**
     * Gets a prepared statement for changing the online status of a portal
     *
     * <p>An online portal is one that can be teleported to, while an offline portal cannot be teleported to until it
     * comes online again.</p>
     *
     * @param conn     <p>The database connection to use</p>
     * @param portal   <p>The portal to update</p>
     * @param isOnline <p>Whether the given portal is currently online</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateSetPortalOnlineStatusStatement(Connection conn, IPortal portal, boolean isOnline) throws SQLException {
        String statementString = "UPDATE {InterPortal} SET isOnline = ? WHERE network = ? AND name = ?;";
        String statementMessage = replaceKnownTableNames(statementString);
        PreparedStatement statement = conn.prepareStatement(statementMessage);
        statement.setBoolean(1, isOnline);
        statement.setString(2, portal.getNetwork().getName());
        statement.setString(3, portal.getName());
        return statement;
    }


    public PreparedStatement generateUpdateServerInfoStatus(Connection conn, String serverName, UUID serverUUID) throws SQLException {
        String statementString = "REPLACE INTO {ServerInfo}(serverId, serverName)"
                + " VALUES(?,?);";
        String statementMessage = replaceKnownTableNames(statementString);
        logger.logMessage(Level.FINEST, statementMessage);
        PreparedStatement statement = conn.prepareStatement(statementMessage);
        statement.setString(1, serverUUID.toString());
        statement.setString(2, serverName);
        return statement;
    }

    /**
     * Adjusts table names for portal type and replaces known table names
     *
     * @param statementMessage <p>The statement to replace values for</p>
     * @param portalType       <p>The type of portal to adjust for</p>
     * @return <p>The statement with values replaced</p>
     */
    private String adjustStatementForPortalType(String statementMessage, PortalType portalType) {
        if (portalType == PortalType.INTER_SERVER) {
            statementMessage = statementMessage.replace("{Portal", "{InterPortal");
        }
        return replaceKnownTableNames(statementMessage);
    }

    /**
     * Replaces known table keys with their proper names
     *
     * @param input <p>The input query string to replace in</p>
     * @return <p>The query string with keys replaced</p>
     */
    private String replaceKnownTableNames(String input) {
        return replaceTableNames(input,
                new String[]{"{Portal}", "{PortalView}", "{Flag}", "{PortalFlagRelation}", "{InterPortal}",
                        "{InterPortalView}", "{InterPortalFlagRelation}", "{LastKnownName}", "{ServerInfo}"},
                new String[]{tableNameConfig.getPortalTableName(), tableNameConfig.getPortalViewName(),
                        tableNameConfig.getFlagTableName(), tableNameConfig.getFlagRelationTableName(),
                        tableNameConfig.getInterPortalTableName(), tableNameConfig.getInterPortalViewName(),
                        tableNameConfig.getInterFlagRelationTableName(), tableNameConfig.getLastKnownNameTableName(),
                        tableNameConfig.getServerInfoTableName()});
    }

    /**
     * Replaces the table name keys with the table name values
     *
     * @param query  <p>The query to replace keys for</p>
     * @param keys   <p>The keys to replace</p>
     * @param values <p>The corresponding values of each key</p>
     * @return <p>The query with the values replaced</p>
     */
    private String replaceTableNames(String query, String[] keys, String[] values) {
        int min = Math.min(keys.length, values.length);
        for (int i = 0; i < min; i++) {
            query = query.replace(keys[i], values[i]);
        }
        return query;
    }

}
