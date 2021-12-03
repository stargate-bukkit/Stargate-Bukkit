package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.BypassPermission;
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Settings;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.SQLQueryGenerator;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.NameSurround;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

public class Network {
    protected HashMap<String, IPortal> portalList;
    protected Database database;
    protected String name;
    protected SQLQueryGenerator sqlMaker;


    final static EnumMap<GateStructureType, HashMap<BlockLocation, Portal>> portalFromPartsMap = new EnumMap<>(GateStructureType.class);

    public Network(String name, Database database, SQLQueryGenerator sqlMaker) throws NameErrorException {
        if (name.trim().isEmpty() || (name.length() == Stargate.MAX_TEXT_LENGTH))
            throw new NameErrorException(TranslatableMessage.INVALID_NAME);
        this.name = name;
        this.database = database;
        this.sqlMaker = sqlMaker;
        portalList = new HashMap<>();
    }

    public Collection<IPortal> getAllPortals() {
        return portalList.values();
    }

    public boolean portalExists(String name) {
        return (getPortal(name) != null);
    }

    public IPortal getPortal(String name) {
        if (name == null)
            return null;
        return portalList.get(this.compilePortalHash(name));
    }

    public void registerLocations(GateStructureType type, HashMap<BlockLocation, Portal> locationsMap) {
        if (!portalFromPartsMap.containsKey(type)) {
            portalFromPartsMap.put(type, new HashMap<>());
        }
        portalFromPartsMap.get(type).putAll(locationsMap);
    }

    public void unRegisterLocation(GateStructureType type, BlockLocation loc) {
        HashMap<BlockLocation, Portal> map = portalFromPartsMap.get(type);
        if (map != null) {
            Stargate.log(Level.FINEST, "Unregistering portal " + map.get(loc).getName() + " with structType " + type
                    + " at location " + loc.toString());
            map.remove(loc);
        }
    }

    public void removePortal(IPortal portal, boolean saveToDatabase) {
        portalList.remove(portal.getName());
        if (!saveToDatabase) {
            return;
        }

        try {
            removePortalFromDatabase(portal, PortalType.LOCAL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a portal and its associated data from the database
     *
     * @param portal     <p>The portal to remove</p>
     * @param portalType <p>The type of portal to remove</p>
     * @throws SQLException <p>If a database error occurs</p>
     */
    protected void removePortalFromDatabase(IPortal portal, PortalType portalType) throws SQLException {
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

    protected void savePortal(Database database, IPortal portal, PortalType portalType) {

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
     * Adds flags for the given portal to the database
     *
     * @param addFlagStatement <p>The statement used to add flags</p>
     * @param portal           <p>The portal to add the flags of</p>
     * @throws SQLException <p>If unable to set the flags</p>
     */
    private void addFlags(PreparedStatement addFlagStatement, IPortal portal) throws SQLException {
        for (Character character : portal.getAllFlagsString().toCharArray()) {
            Stargate.log(Level.FINER, "Adding flag " + character + " to portal: " + portal);
            addFlagStatement.setString(1, portal.getName());
            addFlagStatement.setString(2, portal.getNetwork().getName());
            addFlagStatement.setString(3, String.valueOf(character));
            addFlagStatement.execute();
        }
    }

    protected void savePortal(IPortal portal) {
        savePortal(database, portal, PortalType.LOCAL);
    }

    public void addPortal(IPortal portal, boolean saveToDatabase) {
        if (portal instanceof Portal) {
            Portal physicalPortal = (Portal) portal;
            for (GateStructureType key : physicalPortal.getGate().getFormat().portalParts.keySet()) {
                List<BlockLocation> locations = physicalPortal.getGate().getLocations(key);
                this.registerLocations(key, physicalPortal.generateLocationHashMap(locations));
            }
        }
        if (saveToDatabase) {
            savePortal(portal);
        }
        portalList.put(compilePortalHash(portal.getName()), portal);
    }

    private String compilePortalHash(String portalName) {
        String portalHash = portalName.toLowerCase();
        if (Settings.getBoolean(Setting.DISABLE_CUSTOM_COLORED_NAMES)) {
            portalHash = ChatColor.stripColor(portalHash);
        }
        return portalHash;
    }

    public boolean isPortalNameTaken(String name) {
        return portalList.containsKey(name);
    }

    public void updatePortals() {
        for (String portal : portalList.keySet()) {
            getPortal(portal).update();
        }
    }

    public HashSet<String> getAvailablePortals(Player actor, IPortal requester) {
        HashSet<String> tempPortalList = new HashSet<>(portalList.keySet());
        tempPortalList.remove(compilePortalHash(requester.getName()));
        if (!requester.hasFlag(PortalFlag.FORCE_SHOW)) {
            HashSet<String> removeList = new HashSet<>();
            for (String portalName : tempPortalList) {
                IPortal target = getPortal(portalName);
                if (target.hasFlag(PortalFlag.HIDDEN)
                        && (actor != null && !actor.hasPermission(BypassPermission.HIDDEN.getPermissionString())))
                    removeList.add(portalName);
                if (target.hasFlag(PortalFlag.PRIVATE) && actor != null
                        && !actor.hasPermission(BypassPermission.PRIVATE.getPermissionString())
                        && !actor.getUniqueId().equals(target.getOwnerUUID()))
                    removeList.add(portalName);
            }
            tempPortalList.removeAll(removeList);
        }
        return tempPortalList;
    }

    static public Portal getPortal(Location loc, GateStructureType key) {
        return getPortal(new BlockLocation(loc), key);
    }

    public static Portal getPortal(Location loc, GateStructureType[] keys) {

        return getPortal(new BlockLocation(loc), keys);
    }

    /**
     * Get a portal from location and the type of gateStructure targeted
     *
     * @param loc
     * @param key
     * @return
     */
    static public Portal getPortal(BlockLocation loc, GateStructureType key) {
        if (!(portalFromPartsMap.containsKey(key))) {
            return null;
        }
        return portalFromPartsMap.get(key).get(loc);
    }

    /**
     * Get a portal from location and the types of gateStructures targeted
     *
     * @param loc
     * @param keys
     * @return
     */
    static public Portal getPortal(BlockLocation loc, GateStructureType[] keys) {
        for (GateStructureType key : keys) {
            Portal portal = getPortal(loc, key);
            if (portal != null)
                return portal;
        }
        return null;
    }


    public static boolean isInPortal(List<Block> blocks, GateStructureType[] keys) {
        for (Block block : blocks) {
            if (getPortal(block.getLocation(), GateStructureType.values()) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks North, west, south, east direction. Not up / down, as it is currently
     * not necessary and a waste of resources
     *
     * @param loc The location to check for adjacency
     * @param key
     * @return Is adjacent to specified type of gateStructure.
     */
    public static boolean isNextToPortal(Location loc, GateStructureType key) {
        BlockVector adjacentVec = new BlockVector(1, 0, 0);
        for (int i = 0; i < 4; i++) {
            Location adjacentLoc = loc.clone().add(adjacentVec);
            if (getPortal(adjacentLoc, key) != null) {
                return true;
            }
            adjacentVec.rotateAroundY(Math.PI / 2);
        }
        return false;
    }

    public String concatName() {
        return NameSurround.NETWORK.getSurround(getName());
    }

    /**
     * Destroy network and every portal contained in it
     */
    public void destroy() {
        for (String portalName : portalList.keySet()) {
            IPortal portal = portalList.get(portalName);
            portal.destroy();
        }
        portalList.clear();
    }

    public String getName() {
        return name;
    }

    public int size() {
        return this.getAllPortals().size();
    }

}
