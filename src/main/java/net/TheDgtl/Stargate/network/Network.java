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

    private final static Map<GateStructureType, Map<BlockLocation, Portal>> portalFromStructureTypeMap =
            new EnumMap<>(GateStructureType.class);

    /**
     * Instantiates a new network
     *
     * @param name           <p>The name of the new network</p>
     * @param database       <p>The database to use for saving network data</p>
     * @param queryGenerator <p>The generator to use for generating SQL queries</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public Network(String name, Database database, SQLQueryGenerator queryGenerator) throws NameErrorException {
        if (name.trim().isEmpty() || (name.length() >= Stargate.MAX_TEXT_LENGTH)) {
            throw new NameErrorException(TranslatableMessage.INVALID_NAME);
        }
        this.name = name;
        this.database = database;
        this.sqlMaker = queryGenerator;
        nameToPortalMap = new HashMap<>();
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
     * Registers the existence of the given structure type in the given locations
     *
     * <p>Basically stores the portals that exist at the given locations, but using the structure type as the key to be
     * able to check locations for the given structure type.</p>
     *
     * @param structureType <p>The structure type to register</p>
     * @param locationsMap  <p>The locations and the corresponding portals to register</p>
     */
    public void registerLocations(GateStructureType structureType, Map<BlockLocation, Portal> locationsMap) {
        if (!portalFromStructureTypeMap.containsKey(structureType)) {
            portalFromStructureTypeMap.put(structureType, new HashMap<>());
        }
        portalFromStructureTypeMap.get(structureType).putAll(locationsMap);
    }

    /**
     * Un-registers all portal blocks with the given structure type, at the given block location
     *
     * @param structureType <p>The type of structure to un-register</p>
     * @param blockLocation <p>The location to un-register</p>
     */
    public void unRegisterLocation(GateStructureType structureType, BlockLocation blockLocation) {
        Map<BlockLocation, Portal> map = portalFromStructureTypeMap.get(structureType);
        if (map != null) {
            Stargate.log(Level.FINEST, "Unregistering portal " + map.get(blockLocation).getName() +
                    " with structType " + structureType + " at location " + blockLocation.toString());
            map.remove(blockLocation);
        }
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
     * @param saveToDatabase <p>Whether to also save the portal to the database</p>
     */
    public void addPortal(Portal portal, boolean saveToDatabase) {
        if (portal instanceof RealPortal) {
            RealPortal physicalPortal = (RealPortal) portal;
            for (GateStructureType key : physicalPortal.getGate().getFormat().portalParts.keySet()) {
                List<BlockLocation> locations = physicalPortal.getGate().getLocations(key);
                this.registerLocations(key, generateLocationMap(locations, portal));
            }
        }
        if (saveToDatabase) {
            savePortal(portal);
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
     * Gets the portal with the given structure type at the given location
     *
     * @param location      <p>The location to check for portal structures</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    public static Portal getPortal(Location location, GateStructureType structureType) {
        return getPortal(new BlockLocation(location), structureType);
    }

    /**
     * Gets the portal with any of the given structure types at the given location
     *
     * @param location       <p>The location to check for portal structures</p>
     * @param structureTypes <p>The structure types to look for</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    public static Portal getPortal(Location location, GateStructureType[] structureTypes) {
        return getPortal(new BlockLocation(location), structureTypes);
    }

    /**
     * Checks if any of the given blocks belong to a portal
     *
     * @param blocks <p>The blocks to check</p>
     * @return <p>True if any of the given blocks belong to a portal</p>
     */
    public static boolean isInPortal(List<Block> blocks) {
        for (Block block : blocks) {
            if (getPortal(block.getLocation(), GateStructureType.values()) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks one block away from the given location to check if it's adjacent to a portal structure
     *
     * <p>Checks North, west, south, east direction. Not up / down, as it is currently
     * not necessary and a waste of resources.</p>
     *
     * @param location      <p>The location to check for adjacency</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>True if the given location is adjacent to a location containing the given structure type</p>
     */
    public static boolean isNextToPortal(Location location, GateStructureType structureType) {
        BlockVector adjacentVector = new BlockVector(1, 0, 0);
        for (int i = 0; i < 4; i++) {
            Location adjacentLocation = location.clone().add(adjacentVector);
            if (getPortal(adjacentLocation, structureType) != null) {
                return true;
            }
            adjacentVector.rotateAroundY(Math.PI / 2);
        }
        return false;
    }

    /**
     * Get the portal with the given structure type at the given location
     *
     * @param blockLocation <p>The location the portal is located at</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>The found portal, or null if no such portal exists</p>
     */
    static public Portal getPortal(BlockLocation blockLocation, GateStructureType structureType) {
        if (!(portalFromStructureTypeMap.containsKey(structureType))) {
            return null;
        }
        return portalFromStructureTypeMap.get(structureType).get(blockLocation);
    }

    /**
     * Get the portal with any of the given structure types at the given location
     *
     * @param blockLocation  <p>The location the portal is located at</p>
     * @param structureTypes <p>The structure types to look for</p>
     * @return <p>The found portal, or null if no such portal exists</p>
     */
    static public Portal getPortal(BlockLocation blockLocation, GateStructureType[] structureTypes) {
        for (GateStructureType key : structureTypes) {
            Portal portal = getPortal(blockLocation, key);
            if (portal != null)
                return portal;
        }
        return null;
    }

    /**
     * Saves the given portal to the database
     *
     * @param portal <p>The portal to save</p>
     */
    protected void savePortal(Portal portal) {
        savePortal(database, portal, PortalType.LOCAL);
    }

    /**
     * Saves the given portal to the database
     *
     * @param database   <p>The database to save to</p>
     * @param portal     <p>The portal to save</p>
     * @param portalType <p>The type of portal to save</p>
     */
    protected void savePortal(Database database, Portal portal, PortalType portalType) {
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
