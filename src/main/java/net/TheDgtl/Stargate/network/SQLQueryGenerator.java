package net.TheDgtl.Stargate.network;

import com.mysql.jdbc.MySQLConnection;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * The SQL query generator is responsible for generating prepared statements for various queries
 */
public class SQLQueryGenerator {

    private final String portalTableName;
    private String interPortalTableName;
    private final StargateLogger logger;
    private final String flagTable = "SG_Hub_Flag";
    private final String flagRelationTable = "SG_Hub_PortalFlagRelation";
    private final String portalViewName = "SG_Hub_PortalView";
    private final String interPortalViewName = "SG_Hub_InterPortalView";
    private final String interFlagRelationTable = "SG_Hub_InterPortalFlagRelation";

    /**
     * Instantiates a new SQL query generator
     *
     * @param portalTableName <p>The name of the table used for normal portals</p>
     */
    public SQLQueryGenerator(String portalTableName, StargateLogger logger) {
        String mainPrefix = "SG_";
        String instancePrefix = "Hub_";
        this.portalTableName = mainPrefix + instancePrefix + portalTableName;
        this.logger = logger;
    }

    /**
     * Instantiates a new SQL query generator
     *
     * @param portalTableName      <p>The name of the table used for normal portals</p>
     * @param interPortalTableName <p>The name of the table used for inter-server portals</p>
     */
    public SQLQueryGenerator(String portalTableName, String interPortalTableName, StargateLogger logger) {
        String mainPrefix = "SG_";
        String instancePrefix = "Hub_";
        this.portalTableName = mainPrefix + instancePrefix + portalTableName;
        this.interPortalTableName = mainPrefix + instancePrefix + interPortalTableName;
        this.logger = logger;
    }

    /**
     * Gets the correct table name, given a portal type
     *
     * @param portalType <p>The portal type to get the corresponding table from</p>
     * @param getting    <p>Whether to return the view to get from or the table to insert to</p>
     * @return <p>The table corresponding to the portal type, or null if not found</p>
     */
    private String getTableName(PortalType portalType, boolean getting) {
        switch (portalType) {
            case LOCAL:
                if (getting) {
                    return portalViewName;
                } else {
                    return portalTableName;
                }
            case INTER_SERVER:
                if (getting) {
                    return interPortalViewName;
                } else {
                    return interPortalTableName;
                }
            default:
                return null;
        }
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
        String statementMsg = String.format("SELECT * FROM %s;", getTableName(portalType, true));
        logger.logMessage(Level.FINEST, statementMsg);
        return conn.prepareStatement(statementMsg);
    }

    /**
     * Gets a prepared statement for getting all stored flags
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateGetAllFlagsStatement(Connection connection) throws SQLException {
        String statementMessage = String.format("SELECT id, character FROM %s;", flagTable);
        logger.logMessage(Level.FINEST, statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for creating a new table
     *
     * @param conn       <p>The database connection to use</p>
     * @param portalType <p>The type of the portal (used to determine which table to create)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreateTableStatement(Connection conn, PortalType portalType) throws SQLException {
        String interServerExtraFields = (portalType == PortalType.INTER_SERVER) ?
                " isOnline BOOLEAN, homeServerId VARCHAR(36)," : " isBungee BOOLEAN,";
        String statementMessage = String.format("CREATE TABLE IF NOT EXISTS %s (name NVARCHAR(180), network NVARCHAR(180), " +
                "destination NVARCHAR(180), world NVARCHAR(255) NOT NULL, x INTEGER, y INTEGER, z INTEGER, ownerUUID VARCHAR(36),%s " +
                "PRIMARY KEY (name, network));", getTableName(portalType, false), interServerExtraFields);
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
        String autoIncrement = (connection instanceof MySQLConnection) ? "AUTO_INCREMENT" : "AUTOINCREMENT";
        String statementMessage = String.format("CREATE TABLE {Flag} (id INTEGER PRIMARY KEY %s, character CHAR(1) " +
                "UNIQUE NOT NULL);", autoIncrement);
        statementMessage = replaceKnownTableNames(statementMessage);
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for adding a flag to a portal
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreateFlagRelationTableStatement(Connection connection) throws SQLException {
        String statementMessage = "CREATE TABLE {PortalFlagRelation} (name NVARCHAR(180), " +
                "network NVARCHAR(180), flag INTEGER, PRIMARY KEY (name, network, flag), FOREIGN KEY (name) REFERENCES " +
                "{Portal} (name), FOREIGN KEY (network) REFERENCES {Portal} (network), FOREIGN KEY (flag) REFERENCES {Flag} (id));";
        statementMessage = replaceKnownTableNames(statementMessage);
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for adding a flag to an inter-portal
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreateInterFlagRelationTableStatement(Connection connection) throws SQLException {
        String statementMessage = "CREATE TABLE {InterPortalFlagRelation} (name NVARCHAR(180), " +
                "network NVARCHAR(180), flag INTEGER, PRIMARY KEY (name, network, flag), FOREIGN KEY (name) REFERENCES " +
                "{InterPortal} (name), FOREIGN KEY (network) REFERENCES {InterPortal} (network), " +
                "FOREIGN KEY (flag) REFERENCES {Flag} (id));";
        statementMessage = replaceKnownTableNames(statementMessage);
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }

    /**
     * Gets a prepared statement for generating the portal view
     *
     * @param connection <p>The database connection to use</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateCreatePortalViewTableStatement(Connection connection) throws SQLException {
        String statementMessage = "CREATE VIEW {PortalView} AS SELECT {Portal}.*, " +
                "GROUP_CONCAT({Flag}.character, '') AS flags FROM {Portal} LEFT OUTER JOIN {PortalFlagRelation} ON " +
                "{Portal}.name = {PortalFlagRelation}.name AND {Portal}.network = {PortalFlagRelation}.network LEFT " +
                "OUTER JOIN {Flag} ON {PortalFlagRelation}.flag = {Flag}.id;";
        statementMessage = replaceKnownTableNames(statementMessage);
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return connection.prepareStatement(statementMessage);
    }
    
    public PreparedStatement generateCreateInterPortalViewTableStatement(Connection connection) throws SQLException {
        String statementMessage = "CREATE VIEW {InterPortalView} AS SELECT {InterPortal}.*, " +
                "GROUP_CONCAT({Flag}.character, '') AS flags FROM {InterPortal} LEFT OUTER JOIN {InterPortalFlagRelation} ON " +
                "{InterPortal}.name = {InterPortalFlagRelation}.name AND {InterPortal}.network = {InterPortalFlagRelation}.network LEFT " +
                "OUTER JOIN {Flag} ON {InterPortalFlagRelation}.flag = {Flag}.id;";
        statementMessage = replaceKnownTableNames(statementMessage);
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
        String statementMessage = String.format("INSERT INTO %s (character) VALUES (?);", flagTable);
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
                "(SELECT id FROM {Flag} WHERE character = ?));";
        if (portalType == PortalType.INTER_SERVER) {
            statementMessage = statementMessage.replace("{PortalFlagRelation}", "{InterPortalFlagRelation}");
        }
        statementMessage = replaceKnownTableNames(statementMessage);
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
        String extraKeys = (isInterServer ? ", homeServerId, isOnline" : ", isBungee");
        String extraValues = (isInterServer ? ", ?" : "");
        String statementMessage = String.format("INSERT INTO %s (network, name, destination, world, x, y, z, ownerUUID%s)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?%s);", getTableName(portalType, false), extraKeys, extraValues);

        PreparedStatement statement = conn.prepareStatement(statementMessage);

        //TODO: Add portal flags

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
        Location signLocation = portal.getSignPos();
        World signWorld = signLocation.getWorld();
        statement.setString(4, signWorld != null ? signWorld.getName() : "");
        statement.setInt(5, signLocation.getBlockX());
        statement.setInt(6, signLocation.getBlockY());
        statement.setInt(7, signLocation.getBlockZ());
        statement.setString(8, portal.getOwnerUUID().toString());

        if (isInterServer) {
            statement.setString(9, Stargate.serverName);
            statement.setBoolean(10, true);
        } else {
            statement.setBoolean(9, portal.hasFlag(PortalFlag.BUNGEE));
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
        String statementMessage = String.format("DELETE FROM %s WHERE name = ? AND network = ?", getTableName(portalType, false));
        PreparedStatement statement = conn.prepareStatement(statementMessage);
        statement.setString(1, portal.getName());
        statement.setString(2, portal.getNetwork().getName());
        logger.logMessage(Level.FINEST, "sql query: " + statementMessage);
        return statement;
    }

    /**
     * Gets a prepared statement for changing the server of a portal
     *
     * @param conn       <p>The database connection to use</p>
     * @param portal     <p>The portal to update</p>
     * @param portalType <p>The type of the portal (used to determine which table to update)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateSetServerStatement(Connection conn, IPortal portal,
                                                        PortalType portalType) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(
                String.format("UPDATE %s SET server = ? WHERE network = ? AND name = ?;", getTableName(portalType, false)));

        statement.setString(1, Stargate.serverName);
        statement.setString(2, portal.getNetwork().getName());
        statement.setString(3, portal.getName());
        return statement;
    }

    /**
     * Gets a prepared statement for changing the online status of a portal
     *
     * <p>An online portal is one that can be teleported to, while an offline portal cannot be teleported to until it
     * comes online again.</p>
     *
     * @param conn       <p>The database connection to use</p>
     * @param portal     <p>The portal to update</p>
     * @param isOnline   <p>Whether the given portal is currently online</p>
     * @param portalType <p>The type of the portal (used to determine which table to update)</p>
     * @return <p>A prepared statement</p>
     * @throws SQLException <p>If unable to prepare the statement</p>
     */
    public PreparedStatement generateSetPortalOnlineStatusStatement(Connection conn, IPortal portal, boolean isOnline,
                                                                    PortalType portalType) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(
                String.format("UPDATE %s SET isOnline = ? WHERE network = ? AND name = ?;", getTableName(portalType, false)));

        statement.setBoolean(1, isOnline);
        statement.setString(2, portal.getNetwork().getName());
        statement.setString(3, portal.getName());
        return statement;
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
                        "{InterPortalView}", "{InterPortalFlagRelation}"},
                new String[]{portalTableName, portalViewName, flagTable, flagRelationTable, interPortalTableName, 
                        interPortalViewName, interFlagRelationTable});
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
