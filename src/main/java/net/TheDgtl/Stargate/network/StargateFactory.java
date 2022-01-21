package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.config.TableNameConfig;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.DriverEnum;
import net.TheDgtl.Stargate.database.MySqlDatabase;
import net.TheDgtl.Stargate.database.SQLQueryGenerator;
import net.TheDgtl.Stargate.database.SQLiteDatabase;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.BungeePortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;
import net.TheDgtl.Stargate.util.PortalCreationHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class StargateFactory {

    private final HashMap<String, Network> networkList = new HashMap<>();
    private final HashMap<String, InterServerNetwork> bungeeNetList = new HashMap<>();

    private final Database database;

    private final SQLQueryGenerator sqlMaker;
    private final boolean useInterServerNetworks;
    private final StargateLogger logger;

    private final Map<GateStructureType, Map<BlockLocation, Portal>> portalFromStructureTypeMap =
            new EnumMap<>(GateStructureType.class);

    public StargateFactory(Stargate stargate) throws SQLException {
        this(loadDatabase(stargate), Settings.getBoolean(Setting.USING_BUNGEE),
                Settings.getBoolean(Setting.USING_REMOTE_DATABASE), stargate);
    }

    public StargateFactory(Database database, boolean usingBungee, boolean usingRemoteDatabase, StargateLogger logger) throws SQLException {
        this.logger = logger;
        this.database = database;
        useInterServerNetworks = usingRemoteDatabase && usingBungee;
        String PREFIX = usingRemoteDatabase ? Settings.getString(Setting.BUNGEE_INSTANCE_NAME) : "";
        String serverPrefix = usingRemoteDatabase ? Stargate.serverUUID.toString() : "";
        TableNameConfig config = new TableNameConfig(PREFIX, serverPrefix.replace("-", ""));
        DriverEnum databaseEnum = usingRemoteDatabase ? DriverEnum.MYSQL : DriverEnum.SQLITE;
        this.sqlMaker = new SQLQueryGenerator(config, logger, databaseEnum);
        createTables();
    }

    public void loadFromDatabase() throws SQLException {
        logger.logMessage(Level.FINER, "Loading portals from base database");
        loadAllPortals(database, PortalType.LOCAL);
        if (useInterServerNetworks) {
            logger.logMessage(Level.FINER, "Loading portals from inter-server bungee database");
            loadAllPortals(database, PortalType.INTER_SERVER);
        }

        refreshPortals(networkList);
        refreshPortals(bungeeNetList);
    }

    private static Database loadDatabase(Stargate stargate) throws SQLException {
        if (Settings.getBoolean(Setting.USING_REMOTE_DATABASE)) {
            if (Settings.getBoolean(Setting.SHOW_HIKARI_CONFIG))
                return new MySqlDatabase(stargate);

            DriverEnum driver = DriverEnum.valueOf(Settings.getString(Setting.BUNGEE_DRIVER).toUpperCase());
            String bungeeDatabaseName = Settings.getString(Setting.BUNGEE_DATABASE);
            int port = Settings.getInteger(Setting.BUNGEE_PORT);
            String address = Settings.getString(Setting.BUNGEE_ADDRESS);
            String username = Settings.getString(Setting.BUNGEE_USERNAME);
            String password = Settings.getString(Setting.BUNGEE_PASSWORD);
            boolean useSSL = Settings.getBoolean(Setting.BUNGEE_USE_SSL);

            switch (driver) {
                case MARIADB:
                case MYSQL:
                    return new MySqlDatabase(driver, address, port, bungeeDatabaseName, username, password, useSSL);
                default:
                    throw new SQLException("Unsupported driver: Stargate currently supports MariaDb and MySql for remote databases");
            }
        } else {
            String databaseName = Settings.getString(Setting.DATABASE_NAME);
            File file = new File(stargate.getDataFolder().getAbsoluteFile(), databaseName + ".db");
            return new SQLiteDatabase(file);
        }
    }

    private void refreshPortals(HashMap<String, ? extends Network> networksList) {
        for (Network net : networksList.values()) {
            net.updatePortals();
        }
    }

    private void runStatement(PreparedStatement statement) throws SQLException {
        statement.execute();
        statement.close();
    }

    /**
     * Creates all necessary database tables
     *
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    private void createTables() throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement localPortalsStatement = sqlMaker.generateCreatePortalTableStatement(connection, PortalType.LOCAL);
        runStatement(localPortalsStatement);
        PreparedStatement flagStatement = sqlMaker.generateCreateFlagTableStatement(connection);
        runStatement(flagStatement);
        addMissingFlags(connection, sqlMaker);

        PreparedStatement portalPositionTypesStatement = sqlMaker.generateCreatePortalPositionTypeTableStatement(connection);
        runStatement(portalPositionTypesStatement);
        //TODO: Add portal position types (buttons, signs). Should probably create an enum for this

        PreparedStatement lastKnownNameStatement = sqlMaker.generateCreateLastKnownNameTableStatement(connection);
        runStatement(lastKnownNameStatement);
        PreparedStatement portalRelationStatement = sqlMaker.generateCreateFlagRelationTableStatement(connection, PortalType.LOCAL);
        runStatement(portalRelationStatement);
        PreparedStatement portalViewStatement = sqlMaker.generateCreatePortalViewStatement(connection, PortalType.LOCAL);
        runStatement(portalViewStatement);

        if (!useInterServerNetworks) {
            connection.close();
            return;
        }

        PreparedStatement serverInfoStatement = sqlMaker.generateCreateServerInfoTableStatement(connection);
        runStatement(serverInfoStatement);
        PreparedStatement interServerPortalsStatement = sqlMaker.generateCreatePortalTableStatement(connection, PortalType.INTER_SERVER);
        runStatement(interServerPortalsStatement);
        PreparedStatement interServerRelationStatement = sqlMaker.generateCreateFlagRelationTableStatement(connection, PortalType.INTER_SERVER);
        runStatement(interServerRelationStatement);
        PreparedStatement interPortalViewStatement = sqlMaker.generateCreatePortalViewStatement(connection, PortalType.INTER_SERVER);
        runStatement(interPortalViewStatement);

        PreparedStatement portalPositionsStatement = sqlMaker.generateCreatePortalPositionTableStatement(connection);
        runStatement(portalPositionsStatement);
        connection.close();
    }

    /**
     * Adds any flags not already in the database
     *
     * @param connection <p>The database connection to use</p>
     * @param sqlMaker   <p>The SQL Query Generator to use for generating queries</p>
     * @throws SQLException <p>If unable to get from, or update the database</p>
     */
    private void addMissingFlags(Connection connection, SQLQueryGenerator sqlMaker) throws SQLException {
        PreparedStatement statement = sqlMaker.generateGetAllFlagsStatement(connection);
        PreparedStatement addStatement = sqlMaker.generateAddFlagStatement(connection);

        ResultSet set = statement.executeQuery();
        List<String> knownFlags = new ArrayList<>();
        while (set.next()) {
            knownFlags.add(set.getString("character"));
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
     * Loads all portals from the given database of the given portal type
     *
     * @param database   <p>The database to load from</p>
     * @param portalType <p>The portal type to load</p>
     * @throws SQLException <p>If an SQL error occurs</p>
     */
    private void loadAllPortals(Database database, PortalType portalType) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement statement = sqlMaker.generateGetAllPortalsStatement(connection, portalType);

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            String name = resultSet.getString("name");
            String netName = resultSet.getString("network");

            //Skip null rows
            if (name == null && netName == null) {
                continue;
            }

            String destination = resultSet.getString("destination");
            //Make sure to treat no destination as empty, not a null string
            if (resultSet.wasNull()) {
                destination = "";
            }
            String worldName = resultSet.getString("world");
            int x = resultSet.getInt("x");
            int y = resultSet.getInt("y");
            int z = resultSet.getInt("z");
            String flagString = resultSet.getString("flags");
            UUID ownerUUID = UUID.fromString(resultSet.getString("ownerUUID"));

            Set<PortalFlag> flags = PortalFlag.parseFlags(flagString);

            boolean isBungee = flags.contains(PortalFlag.FANCY_INTER_SERVER);
            logger.logMessage(Level.FINEST, "Trying to add portal " + name + ", on network " + netName +
                    ",isInterServer = " + isBungee);

            String targetNet = netName;
            if (flags.contains(PortalFlag.BUNGEE)) {
                targetNet = "§§§§§§#BUNGEE#§§§§§§";
            }

            try {
                createNetwork(targetNet, flags);
            } catch (NameErrorException ignored) {
            }
            Network network = getNetwork(targetNet, isBungee);

            if (portalType == PortalType.INTER_SERVER) {
                String serverUUID = resultSet.getString("homeServerId");
                logger.logMessage(Level.FINEST, "serverUUID = " + serverUUID);
                if (!serverUUID.equals(Stargate.serverUUID.toString())) {
                    String serverName = resultSet.getString("serverName");
                    Portal virtualPortal = new VirtualPortal(serverName, name, network, flags, ownerUUID);
                    network.addPortal(virtualPortal, false);
                    logger.logMessage(Level.FINEST, "Added as virtual portal");
                    continue;
                }
            }

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                continue;
            }
            Block block = world.getBlockAt(x, y, z);
            String[] virtualSign = {name, destination, netName};

            if (destination == null || destination.trim().isEmpty()) {
                flags.add(PortalFlag.NETWORKED);
            }

            try {
                //TODO: This needs to be changed as we will save the top-left location and the sign will be seen as one 
                // of potentially several interfaces rather than an identifier
                Portal portal = PortalCreationHelper.createPortalFromSign(network, virtualSign, block, flags, ownerUUID);
                network.addPortal(portal, false);
                logger.logMessage(Level.FINEST, "Added as normal portal");
                if (isBungee) {
                    setInterServerPortalOnlineStatus(portal, true);
                }
            } catch (GateConflictException | NoFormatFoundException ignored) {
            } catch (NameErrorException e) {
                e.printStackTrace();
            }
        }
        statement.close();
        connection.close();
    }

    private void setInterServerPortalOnlineStatus(Portal portal, boolean isOnline) throws SQLException {
        Connection conn = database.getConnection();
        PreparedStatement statement = sqlMaker.generateSetPortalOnlineStatusStatement(conn, portal, isOnline);
        statement.execute();
        statement.close();
        conn.close();
    }

    public void startInterServerConnection() throws SQLException {
        Connection conn = database.getConnection();
        PreparedStatement statement = sqlMaker.generateUpdateServerInfoStatus(conn, Stargate.serverUUID, Stargate.serverName);
        statement.execute();
        statement.close();

        for (InterServerNetwork net : bungeeNetList.values()) {
            for (Portal portal : net.getAllPortals()) {
                /*
                 * Virtual portal = portals on other servers
                 */
                if (portal instanceof VirtualPortal)
                    continue;

                setInterServerPortalOnlineStatus(portal, true);
            }
        }
        conn.close();
    }

    public void endInterServerConnection() throws SQLException {
        for (InterServerNetwork net : bungeeNetList.values()) {
            for (Portal portal : net.getAllPortals()) {
                /*
                 * Virtual portal = portals on other servers
                 */
                if (portal instanceof VirtualPortal)
                    continue;

                setInterServerPortalOnlineStatus(portal, false);
            }
        }
    }

    public void createNetwork(String netName, Set<PortalFlag> flags) throws NameErrorException {
        if (netExists(netName, flags.contains(PortalFlag.FANCY_INTER_SERVER)))
            throw new NameErrorException(null);
        if (flags.contains(PortalFlag.FANCY_INTER_SERVER)) {
            InterServerNetwork net = new InterServerNetwork(netName, database, sqlMaker, this);
            String netHash = net.getName().toLowerCase();
            if (Settings.getBoolean(Setting.DISABLE_CUSTOM_COLORED_NAMES)) {
                netHash = ChatColor.stripColor(netHash);
            }
            bungeeNetList.put(netHash, net);
            return;
        }
        Network net;
        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            UUID id = UUID.fromString(netName);
            net = new PersonalNetwork(id, database, sqlMaker, this);
        } else {
            net = new Network(netName, database, sqlMaker, this);
        }
        networkList.put(netName, net);
    }

    public boolean netExists(String netName, boolean isBungee) {
        return (getNetwork(netName, isBungee) != null);
    }

    public Network getNetwork(String name, boolean isBungee) {
        return getNetMap(isBungee).get(name);
    }

    private HashMap<String, ? extends Network> getNetMap(boolean isBungee) {
        if (isBungee) {
            return bungeeNetList;
        } else {
            return networkList;
        }
    }

    final HashMap<String, BungeePortal> bungeeList = new HashMap<>();

    public BungeePortal getBungeeGate(String name) {
        return bungeeList.get(name);
    }


    /**
     * Get the portal with the given structure type at the given location
     *
     * @param blockLocation <p>The location the portal is located at</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>The found portal, or null if no such portal exists</p>
     */
    public Portal getPortal(BlockLocation blockLocation, GateStructureType structureType) {
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
    public Portal getPortal(BlockLocation blockLocation, GateStructureType[] structureTypes) {
        for (GateStructureType key : structureTypes) {
            Portal portal = getPortal(blockLocation, key);
            if (portal != null)
                return portal;
        }
        return null;
    }


    /**
     * Gets the portal with the given structure type at the given location
     *
     * @param location      <p>The location to check for portal structures</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    public Portal getPortal(Location location, GateStructureType structureType) {
        return getPortal(new BlockLocation(location), structureType);
    }

    /**
     * Gets the portal with any of the given structure types at the given location
     *
     * @param location       <p>The location to check for portal structures</p>
     * @param structureTypes <p>The structure types to look for</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    public Portal getPortal(Location location, GateStructureType[] structureTypes) {
        return getPortal(new BlockLocation(location), structureTypes);
    }


    /**
     * Checks if any of the given blocks belong to a portal
     *
     * @param blocks <p>The blocks to check</p>
     * @return <p>True if any of the given blocks belong to a portal</p>
     */
    public boolean isInPortal(List<Block> blocks) {
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
    public boolean isNextToPortal(Location location, GateStructureType structureType) {
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
            Stargate.log(Level.FINER, "Unregistering portal " + map.get(blockLocation).getName() +
                    " with structType " + structureType + " at location " + blockLocation.toString());
            map.remove(blockLocation);
        }
    }
}
