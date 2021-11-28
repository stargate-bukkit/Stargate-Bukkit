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

public class SQLQuerryMaker {
    private String tableName;
    private String bungeeTableName;
    private String interserverTableName;


    public SQLQuerryMaker(String tableName) {
        this.tableName = tableName;
    }

    public SQLQuerryMaker(String tableName, String bungeeTableName, String interserverTableName) {
        String instanceName = Setting.getString(Setting.BUNGEE_INSTANCE_NAME);
        this.tableName = tableName + instanceName;
        this.bungeeTableName = bungeeTableName + instanceName;
        this.interserverTableName = interserverTableName;
    }

    public enum Type {
        LOCAL, BUNGEE, INTERSERVER;
    }

    private String getName(Type type) {
        switch (type) {
            case LOCAL:
                return tableName;
            case BUNGEE:
                return bungeeTableName;
            case INTERSERVER:
                return interserverTableName;
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
                + " network TEXT, name TEXT, desti TEXT, world TEXT,"
                + " x INTEGER, y INTEGER, z INTEGER, flags TEXT, ownerUUID TEXT,"
                + ((type == Type.INTERSERVER) ? " server TEXT, isOnline INTEGER," : "")
                + " UNIQUE(network,name) );";
        Stargate.log(Level.FINEST, "sql querry: " + statementMsg);
        return conn.prepareStatement(statementMsg);
    }

    public PreparedStatement compileAddStatement(Connection conn, IPortal portal, Type type) throws SQLException {
        boolean isInterserver = (type == Type.INTERSERVER);
        PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO " + getName(type)
                        + " (network,name,desti,world,x,y,z,flags,ownerUUID" + (isInterserver ? ",server,isOnline" : "") + ")"
                        + " VALUES(?,?,?,?,?,?,?,?,?" + (isInterserver ? ",?,?" : "") + ");");

        statement.setString(1, portal.getNetwork().getName());
        statement.setString(2, portal.getName());
        String destiStr = null;
        if (portal instanceof Portal) {
            IPortal desti = ((Portal) portal).loadDestination();
            if (desti != null)
                destiStr = desti.getName();
        }
        statement.setString(3, destiStr);
        Location loc = portal.getSignPos();
        statement.setString(4, loc.getWorld().getName());
        statement.setInt(5, loc.getBlockX());
        statement.setInt(6, loc.getBlockY());
        statement.setInt(7, loc.getBlockZ());
        statement.setString(8, portal.getAllFlagsString());
        statement.setString(9, portal.getOwnerUUID().toString());

        if (isInterserver) {
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
