package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class SQLQueryMaker {
    private String tableName;
    private String bungeeTableName;
    private String interServerTableName;


    public SQLQueryMaker(String tableName) {
        this.tableName = tableName;
    }

    public SQLQueryMaker(String tableName, String bungeeTableName, String interServerTableName) {
        String instanceName = Setting.getString(Setting.BUNGEE_INSTANCE_NAME);
        this.tableName = tableName + instanceName;
        this.bungeeTableName = bungeeTableName + instanceName;
        this.interServerTableName = interServerTableName;
    }

    public enum Type {
        LOCAL, BUNGEE, INTER_SERVER;
    }

    private String getName(Type type) {
        switch (type) {
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

    public PreparedStatement selectAll(Connection conn, Type type) throws SQLException {
        PreparedStatement output = conn.prepareStatement(
                "SELECT * FROM " + getName(type));
        return output;
    }

    public PreparedStatement compileCreateStatement(Connection conn, Type type) throws SQLException {
        String statementMsg = "CREATE TABLE IF NOT EXISTS " + getName(type) + " ("
                + " network TEXT, name TEXT, destination TEXT, world TEXT,"
                + " x INTEGER, y INTEGER, z INTEGER, flags TEXT, ownerUUID TEXT,"
                + ((type == Type.INTER_SERVER) ? " server TEXT, isOnline INTEGER," : "")
                + " UNIQUE(network,name) );";
        Stargate.log(Level.FINEST, "sql query: " + statementMsg);
        return conn.prepareStatement(statementMsg);
    }

    public PreparedStatement compileAddStatement(Connection conn, IPortal portal, Type type) throws SQLException {
        boolean isInterServer = (type == Type.INTER_SERVER);
        PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO " + getName(type)
                        + " (network,name,destination,world,x,y,z,flags,ownerUUID" + (isInterServer ? ",server,isOnline" : "") + ")"
                        + " VALUES(?,?,?,?,?,?,?,?,?" + (isInterServer ? ",?,?" : "") + ");");

        statement.setString(1, portal.getNetwork().getName());
        statement.setString(2, portal.getName());
        String destinationString = null;
        if (portal instanceof Portal) {
            IPortal destination = ((Portal) portal).loadDestination();
            if (destination != null)
                destinationString = destination.getName();
        }
        statement.setString(3, destinationString);
        Location loc = portal.getSignPos();
        statement.setString(4, loc.getWorld().getName());
        statement.setInt(5, loc.getBlockX());
        statement.setInt(6, loc.getBlockY());
        statement.setInt(7, loc.getBlockZ());
        statement.setString(8, portal.getAllFlagsString());
        statement.setString(9, portal.getOwnerUUID().toString());

        if (isInterServer) {
            statement.setString(10, Stargate.serverName);
            statement.setBoolean(11, true);
        }

        return statement;
    }

    public PreparedStatement compileRemoveStatement(Connection conn, IPortal portal, Type type) throws SQLException {
        PreparedStatement output = conn.prepareStatement(
                "DELETE FROM " + getName(type)
                        + " WHERE name = ? AND network = ?");
        output.setString(1, portal.getName());
        output.setString(2, portal.getNetwork().getName());
        return output;
    }

    public PreparedStatement compileRefreshPortalStatement(Connection conn, IPortal portal, Type type) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(
                "UPDATE " + getName(type)
                        + " SET server = ?"
                        + " WHERE network = ? AND name = ?;");

        statement.setString(1, Stargate.serverName);
        statement.setString(2, portal.getNetwork().getName());
        statement.setString(3, portal.getName());
        return statement;
    }

    public PreparedStatement changePortalOnlineStatus(Connection conn, IPortal portal, boolean isOnline, Type type) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(
                "UPDATE " + getName(type)
                        + " SET isOnline = ?"
                        + " WHERE network = ? AND name = ?;");

        statement.setBoolean(1, isOnline);
        statement.setString(2, portal.getNetwork().getName());
        statement.setString(3, portal.getName());
        return statement;
    }
}
