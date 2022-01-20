package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.BypassPermission;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.SQLQueryGenerator;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * A network of portals
 */
public class Network {

    protected Map<String, Portal> nameToPortalMap;
    protected Database database;
    protected String name;
    protected SQLQueryGenerator sqlMaker;
    private final StargateFactory factory;

    /**
     * Instantiates a new network
     *
     * @param name           <p>The name of the new network</p>
     * @param database       <p>The database to use for saving network data</p>
     * @param queryGenerator <p>The generator to use for generating SQL queries</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public Network(String name, Database database, SQLQueryGenerator queryGenerator, StargateFactory factory) throws NameErrorException {
        if (name.trim().isEmpty() || (name.length() >= Stargate.MAX_TEXT_LENGTH)) {
            throw new NameErrorException(TranslatableMessage.INVALID_NAME);
        }
        this.name = name;
        this.database = database;
        this.sqlMaker = queryGenerator;
        nameToPortalMap = new HashMap<>();
        this.factory = factory;
    }

    /**
     * Gets all portals belonging to this network
     *
     * @return <p>All portals belonging to this network</p>
     */
    public Collection<Portal> getAllPortals() {
        return nameToPortalMap.values();
    }

    /**
     * Gets the portal with the given name
     *
     * @param name <p>The name of the portal to get</p>
     * @return <p>The portal with the given name, or null if not found</p>
     */
    public Portal getPortal(String name) {
        if (name == null) {
            return null;
        }
        return nameToPortalMap.get(this.getPortalHash(name));
    }

    /**
     * Removes the given portal from this network
     *
     * @param portal             <p>The portal to remove</p>
     * @param removeFromDatabase <p>Whether to also remove the portal from the database</p>
     */
    public void removePortal(Portal portal, boolean removeFromDatabase) {
        nameToPortalMap.remove(this.getPortalHash(portal.getName()));
        if (!removeFromDatabase) {
            return;
        }

        try {
            removePortalFromDatabase(portal, PortalType.LOCAL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the given portal to this network
     *
     * @param portal         <p>The portal to add</p>
     * @param saveToDatabase <p>Whether to also save the portal to the database, only instances of RealPortal can be saved</p>
     */
    public void addPortal(Portal portal, boolean saveToDatabase) {
        if (portal instanceof RealPortal) {
            RealPortal physicalPortal = (RealPortal) portal;
            for (GateStructureType key : GateStructureType.values()) {
                List<BlockLocation> locations = physicalPortal.getGate().getLocations(key);
                if (locations == null)
                    continue;
                factory.registerLocations(key, generateLocationMap(locations, portal));
            }
        }
        if (portal instanceof RealPortal && saveToDatabase) {
            savePortal((RealPortal) portal);
        }
        nameToPortalMap.put(getPortalHash(portal.getName()), portal);
    }

    /**
     * Checks whether there is already a portal in this network with the given name
     *
     * @param name <p>The name to check for</p>
     * @return <p>True if an existing portal is already using the given name</p>
     */
    public boolean isPortalNameTaken(String name) {
        return nameToPortalMap.containsKey(name);
    }

    /**
     * Updates all portals in this network
     */
    public void updatePortals() {
        for (String portal : nameToPortalMap.keySet()) {
            getPortal(portal).update();
        }
    }

    /**
     * Gets names of all portals available to the given player from the given portal
     *
     * @param player    <p>The player to get portals </p>
     * @param requester <p>The portal the player is viewing other portals from</p>
     * @return <p>The names of all portals the player is allowed to see</p>
     */
    public Set<String> getAvailablePortals(Player player, Portal requester) {
        Set<String> tempPortalList = new HashSet<>(nameToPortalMap.keySet());
        tempPortalList.remove(getPortalHash(requester.getName()));
        if (!requester.hasFlag(PortalFlag.FORCE_SHOW)) {
            Set<String> removeList = new HashSet<>();
            for (String portalName : tempPortalList) {
                Portal target = getPortal(portalName);
                if (target.hasFlag(PortalFlag.HIDDEN) &&
                        (player != null && !player.hasPermission(BypassPermission.HIDDEN.getPermissionString()))) {
                    removeList.add(portalName);
                }
                if (target.hasFlag(PortalFlag.PRIVATE) && player != null &&
                        !player.hasPermission(BypassPermission.PRIVATE.getPermissionString()) &&
                        !player.getUniqueId().equals(target.getOwnerUUID())) {
                    removeList.add(portalName);
                }
            }
            tempPortalList.removeAll(removeList);
        }
        return tempPortalList;
    }

    /**
     * Gets the highlighted name of this network
     *
     * @return <p>The highlighted name of this network</p>
     */
    public String getHighlightedName() {
        return HighlightingStyle.NETWORK.getHighlightedName(getName());
    }

    /**
     * Destroys this network and every portal contained in it
     */
    public void destroy() {
        for (String portalName : nameToPortalMap.keySet()) {
            Portal portal = nameToPortalMap.get(portalName);
            portal.destroy();
        }
        nameToPortalMap.clear();
    }

    /**
     * Gets the name of this network
     *
     * @return <p>The name of this network</p>
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the current number of portals in this network
     *
     * @return <p>The size of this network</p>
     */
    public int size() {
        return this.getAllPortals().size();
    }

    /**
     * Saves the given portal to the database
     *
     * @param portal <p>The portal to save</p>
     */
    protected void savePortal(RealPortal portal) {
        savePortal(database, portal, PortalType.LOCAL);
    }

    /**
     * Saves the given portal to the database
     *
     * @param database   <p>The database to save to</p>
     * @param portal     <p>The portal to save</p>
     * @param portalType <p>The type of portal to save</p>
     */
    protected void savePortal(Database database, RealPortal portal, PortalType portalType) {
        /* An SQL transaction is used here to make sure partial data is never added to the database. */
        Connection connection = null;
        try {
            connection = database.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement savePortalStatement = sqlMaker.generateAddPortalStatement(connection, portal, portalType);
            savePortalStatement.execute();
            savePortalStatement.close();

            PreparedStatement addFlagStatement = sqlMaker.generateAddPortalFlagRelationStatement(connection, portalType);
            addFlags(addFlagStatement, portal);
            addFlagStatement.close();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException exception) {
            try {
                if (connection != null) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            exception.printStackTrace();
        }
    }

    /**
     * Removes a portal and its associated data from the database
     *
     * @param portal     <p>The portal to remove</p>
     * @param portalType <p>The type of portal to remove</p>
     * @throws SQLException <p>If a database error occurs</p>
     */
    protected void removePortalFromDatabase(Portal portal, PortalType portalType) throws SQLException {
        /* An SQL transaction is used here to make sure portals are never partially removed from the database. */
        Connection conn = null;
        try {
            conn = database.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement removeFlagsStatement = sqlMaker.generateRemoveFlagStatement(conn, portalType);
            removeFlagsStatement.setString(1, portal.getName());
            removeFlagsStatement.setString(2, portal.getNetwork().getName());
            removeFlagsStatement.execute();
            removeFlagsStatement.close();

            PreparedStatement statement = sqlMaker.generateRemovePortalStatement(conn, portal, portalType);
            statement.execute();
            statement.close();

            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
                conn.setAutoCommit(false);
                conn.close();
            }
        }
    }

    /**
     * Gets a map between the given block locations and the given portal
     *
     * @param locations <p>The locations related to the portal</p>
     * @param portal    <p>The portal with blocks at the given locations</p>
     * @return <p>The resulting location to portal mapping</p>
     */
    private Map<BlockLocation, Portal> generateLocationMap(List<BlockLocation> locations, Portal portal) {
        Map<BlockLocation, Portal> output = new HashMap<>();
        for (BlockLocation location : locations) {
            output.put(location, portal);
        }
        return output;
    }

    /**
     * Gets a "hash" of a portal's name
     *
     * <p>This basically just lower-cases the name, and strips color if enabled. This is to make portal names
     * case-agnostic and optionally color-agnostic.</p>
     *
     * @param portalName <p>The name to "hash"</p>
     * @return <p>The "hashed" name</p>
     */
    private String getPortalHash(String portalName) {
        String portalHash = portalName.toLowerCase();
        if (Settings.getBoolean(Setting.DISABLE_CUSTOM_COLORED_NAMES)) {
            portalHash = ChatColor.stripColor(portalHash);
        }
        return portalHash;
    }

    /**
     * Adds flags for the given portal to the database
     *
     * @param addFlagStatement <p>The statement used to add flags</p>
     * @param portal           <p>The portal to add the flags of</p>
     * @throws SQLException <p>If unable to set the flags</p>
     */
    private void addFlags(PreparedStatement addFlagStatement, Portal portal) throws SQLException {
        for (Character character : portal.getAllFlagsString().toCharArray()) {
            Stargate.log(Level.FINER, "Adding flag " + character + " to portal: " + portal);
            addFlagStatement.setString(1, portal.getName());
            addFlagStatement.setString(2, portal.getNetwork().getName());
            addFlagStatement.setString(3, String.valueOf(character));
            addFlagStatement.execute();
        }
    }

}
