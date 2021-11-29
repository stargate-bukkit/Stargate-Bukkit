package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
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

    private final String tableName;
    private String bungeeTableName;
    private String interServerTableName;

    /**
     * Instantiates a new SQL query generator
     *
     * @param tableName <p>The name of the table used for normal portals</p>
     */
    public SQLQueryGenerator(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Instantiates a new SQL query generator
     *
     * @param tableName            <p>The name of the table used for normal portals</p>
     * @param bungeeTableName      <p>The name of the table used for bungee portals</p>
     * @param interServerTableName <p>The name of the table used for inter-server portals</p>
     */
    public SQLQueryGenerator(String tableName, String bungeeTableName, String interServerTableName) {
        String instanceName = Setting.getString(Setting.BUNGEE_INSTANCE_NAME);
        this.tableName = tableName + instanceName;
        this.bungeeTableName = bungeeTableName + instanceName;
        this.interServerTableName = interServerTableName;
    }

    /**
     * Gets the correct table name, given a portal type
     *
     * @param portalType <p>The portal type to get the corresponding table from</p>
     * @return <p>The table corresponding to the portal type, or null if not found</p>
     */
    private String getTableName(PortalType portalType) {
        switch (portalType) {
            case LOCAL:
                return tableName;
            case BUNGEE:
                return bungeeTableName;
            case INTER_SERVER:
                return interServerTableName;
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
        return conn.prepareStatement(String.format("SELECT * FROM %s", getTableName(portalType)));
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
        String interServerExtraFields = (portalType == PortalType.INTER_SERVER) ? " server TEXT, isOnline INTEGER," : "";
        String statementMessage = String.format("CREATE TABLE IF NOT EXISTS %s (network TEXT, name TEXT, " +
                "destination TEXT, world TEXT, x INTEGER, y INTEGER, z INTEGER, flags TEXT, ownerUUID TEXT,%s " +
                "UNIQUE(network, name));", getTableName(portalType), interServerExtraFields);
        Stargate.log(Level.FINEST, "sql query: " + statementMessage);
        return conn.prepareStatement(statementMessage);
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
        String extraKeys = (isInterServer ? ", server, isOnline" : "");
        String extraValues = (isInterServer ? ", ?, ?" : "");
        PreparedStatement statement = conn.prepareStatement(
                String.format("INSERT INTO %s (network, name, destination, world, x, y, z, flags, ownerUUID%s)"
                        + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?%s);", getTableName(portalType), extraKeys, extraValues));

        statement.setString(1, portal.getNetwork().getName());
        statement.setString(2, portal.getName());
        String destinationString = null;
        if (portal instanceof Portal) {
            IPortal destination = ((Portal) portal).loadDestination();
            if (destination != null)
                destinationString = destination.getName();
        }
        statement.setString(3, destinationString);
        Location signLocation = portal.getSignPos();
        World signWorld = signLocation.getWorld();
        statement.setString(4, signWorld != null ? signWorld.getName() : "");
        statement.setInt(5, signLocation.getBlockX());
        statement.setInt(6, signLocation.getBlockY());
        statement.setInt(7, signLocation.getBlockZ());
        statement.setString(8, portal.getAllFlagsString());
        statement.setString(9, portal.getOwnerUUID().toString());

        if (isInterServer) {
            statement.setString(10, Stargate.serverName);
            statement.setBoolean(11, true);
        }

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
        PreparedStatement statement = conn.prepareStatement(
                String.format("DELETE FROM %s WHERE name = ? AND network = ?", getTableName(portalType)));
        statement.setString(1, portal.getName());
        statement.setString(2, portal.getNetwork().getName());
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
                String.format("UPDATE %s SET server = ? WHERE network = ? AND name = ?;", getTableName(portalType)));

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
                String.format("UPDATE %s SET isOnline = ? WHERE network = ? AND name = ?;", getTableName(portalType)));

        statement.setBoolean(1, isOnline);
        statement.setString(2, portal.getNetwork().getName());
        statement.setString(3, portal.getName());
        return statement;
    }

}
