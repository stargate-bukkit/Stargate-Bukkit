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
import net.TheDgtl.Stargate.exception.InvalidStructureException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.PortalPosition;
import net.TheDgtl.Stargate.network.portal.PositionType;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;
import net.TheDgtl.Stargate.util.PortalCreationHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

/**
 * TODO: The StargateFactory class does more than one task which is terrible design. Need to refactor this class. Also,
 *  it's not a factory, but rather a network+portal handler/registry.
 */
public class StargateFactory {

    private final HashMap<String, Network> networkList = new HashMap<>();
    private final HashMap<String, InterServerNetwork> bungeeNetworkList = new HashMap<>();
    private final Map<GateStructureType, Map<BlockLocation, Portal>> portalFromStructureTypeMap = new EnumMap<>(GateStructureType.class);

    private final Database database;
    private final SQLQueryGenerator sqlQueryGenerator;
    private final boolean useInterServerNetworks;
    private final StargateLogger logger;

    /**
     * Instantiates a new stargate factory
     *
     * @param stargate <p>The Stargate instance to use</p>
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public StargateFactory(Stargate stargate) throws SQLException {
        this(loadDatabase(stargate), Settings.getBoolean(Setting.USING_BUNGEE),
                Settings.getBoolean(Setting.USING_REMOTE_DATABASE), stargate);
    }

    /**
     * Instantiates a new stargate factory
     *
     * @param database            <p>The database used for storing portals</p>
     * @param usingBungee         <p>Whether BungeeCord support is enabled</p>
     * @param usingRemoteDatabase <p>Whether a remote database, not a flatfile database is used</p>
     * @param logger              <p>The Stargate logger to use for logging</p>
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public StargateFactory(Database database, boolean usingBungee, boolean usingRemoteDatabase,
                           StargateLogger logger) throws SQLException {
        this.logger = logger;
        this.database = database;
        useInterServerNetworks = usingRemoteDatabase && usingBungee;
        String PREFIX = usingRemoteDatabase ? Settings.getString(Setting.BUNGEE_INSTANCE_NAME) : "";
        String serverPrefix = usingRemoteDatabase ? Stargate.serverUUID.toString() : "";
        TableNameConfig config = new TableNameConfig(PREFIX, serverPrefix.replace("-", ""));
        DriverEnum databaseEnum = usingRemoteDatabase ? DriverEnum.MYSQL : DriverEnum.SQLITE;
        this.sqlQueryGenerator = new SQLQueryGenerator(config, logger, databaseEnum);
        createTables();
    }

    /**
     * Loads all portals from the database
     *
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public void loadFromDatabase() throws SQLException {
        logger.logMessage(Level.FINER, "Loading portals from base database");
        loadAllPortals(database, PortalType.LOCAL);
        if (useInterServerNetworks) {
            logger.logMessage(Level.FINER, "Loading portals from inter-server bungee database");
            loadAllPortals(database, PortalType.INTER_SERVER);
        }

        updatePortals(networkList);
        updatePortals(bungeeNetworkList);
    }

    /**
     * "Starts" the inter-server connection by setting this server's portals as online
     *
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public void startInterServerConnection() throws SQLException {
        Connection conn = database.getConnection();
        PreparedStatement statement = sqlQueryGenerator.generateUpdateServerInfoStatus(conn, Stargate.serverUUID, Stargate.serverName);
        statement.execute();
        statement.close();

        for (InterServerNetwork net : bungeeNetworkList.values()) {
            for (Portal portal : net.getAllPortals()) {
                //Virtual portal = portals on other servers
                if (portal instanceof VirtualPortal) {
                    continue;
                }

                setInterServerPortalOnlineStatus(portal, true);
            }
        }
        conn.close();
    }

    /**
     * "Ends" the inter-server connection by setting this server's portals as offline
     *
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public void endInterServerConnection() throws SQLException {
        for (InterServerNetwork interServerNetwork : bungeeNetworkList.values()) {
            for (Portal portal : interServerNetwork.getAllPortals()) {
                //Virtual portal = portals on other servers
                if (portal instanceof VirtualPortal) {
                    continue;
                }

                setInterServerPortalOnlineStatus(portal, false);
            }
        }
    }

    /**
     * Creates a new network
     *
     * @param networkName <p>The name of the new network</p>
     * @param flags       <p>The flag set used to look for network flags</p>
     * @throws NameErrorException <p>If the given network name is invalid</p>
     */
    public void createNetwork(String networkName, Set<PortalFlag> flags) throws NameErrorException {
        if (networkExists(networkName, flags.contains(PortalFlag.FANCY_INTER_SERVER))) {
            throw new NameErrorException(null);
        }
        if (flags.contains(PortalFlag.FANCY_INTER_SERVER)) {
            InterServerNetwork interServerNetwork = new InterServerNetwork(networkName, database, sqlQueryGenerator, this);
            String networkHash = interServerNetwork.getName().toLowerCase();
            if (Settings.getBoolean(Setting.DISABLE_CUSTOM_COLORED_NAMES)) {
                networkHash = ChatColor.stripColor(networkHash);
            }
            bungeeNetworkList.put(networkHash, interServerNetwork);
            return;
        }
        Network network;
        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            UUID uuid = UUID.fromString(networkName);
            network = new PersonalNetwork(uuid, database, sqlQueryGenerator, this);
        } else {
            network = new Network(networkName, database, sqlQueryGenerator, this);
        }
        networkList.put(networkName, network);
    }

    /**
     * Checks whether the given network name exists
     *
     * @param networkName <p>The network name to check</p>
     * @param isBungee    <p>Whether to look for a BungeeCord network</p>
     * @return <p>True if the network exists</p>
     */
    public boolean networkExists(String networkName, boolean isBungee) {
        return getNetwork(networkName, isBungee) != null;
    }

    /**
     * Gets the network with the given
     *
     * @param name     <p>The name of the network to get</p>
     * @param isBungee <p>Whether the network is a BungeeCord network</p>
     * @return <p>The network with the given name</p>
     */
    public Network getNetwork(String name, boolean isBungee) {
        return getNetworkMap(isBungee).get(name);
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
            if (portal != null) {
                return portal;
            }
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
    public boolean isPartOfPortal(List<Block> blocks) {
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

    /**
     * Gets the map storing all networks of the given type
     *
     * @param getBungee <p>Whether to get BungeeCord networks</p>
     * @return <p>A network name -> network map</p>
     */
    private Map<String, ? extends Network> getNetworkMap(boolean getBungee) {
        if (getBungee) {
            return bungeeNetworkList;
        } else {
            return networkList;
        }
    }

    /**
     * Loads the database
     *
     * @param stargate <p>The Stargate instance to use for initialization</p>
     * @return <p>The loaded database</p>
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    private static Database loadDatabase(Stargate stargate) throws SQLException {
        //TODO: The StargateFactory should not be the class to load the database!
        if (Settings.getBoolean(Setting.USING_REMOTE_DATABASE)) {
            if (Settings.getBoolean(Setting.SHOW_HIKARI_CONFIG)) {
                return new MySqlDatabase(stargate);
            }

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

    /**
     * Updates all portals in the given networks
     *
     * @param networkMap <p>A map of networks</p>
     */
    private void updatePortals(Map<String, ? extends Network> networkMap) {
        for (Network network : networkMap.values()) {
            network.updatePortals();
        }
    }

    /**
     * Executes and closes the given statement
     *
     * @param statement <p>The statement to execute</p>
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
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
        PreparedStatement localPortalsStatement = sqlQueryGenerator.generateCreatePortalTableStatement(connection, PortalType.LOCAL);
        runStatement(localPortalsStatement);
        PreparedStatement flagStatement = sqlQueryGenerator.generateCreateFlagTableStatement(connection);
        runStatement(flagStatement);
        addMissingFlags(connection, sqlQueryGenerator);

        PreparedStatement portalPositionTypesStatement = sqlQueryGenerator.generateCreatePortalPositionTypeTableStatement(connection);
        runStatement(portalPositionTypesStatement);
        addMissingPositionTypes(connection, sqlQueryGenerator);
        PreparedStatement portalPositionsStatement = sqlQueryGenerator.generateCreatePortalPositionTableStatement(connection);
        runStatement(portalPositionsStatement);

        PreparedStatement lastKnownNameStatement = sqlQueryGenerator.generateCreateLastKnownNameTableStatement(connection);
        runStatement(lastKnownNameStatement);
        PreparedStatement portalRelationStatement = sqlQueryGenerator.generateCreateFlagRelationTableStatement(connection, PortalType.LOCAL);
        runStatement(portalRelationStatement);
        PreparedStatement portalViewStatement = sqlQueryGenerator.generateCreatePortalViewStatement(connection, PortalType.LOCAL);
        runStatement(portalViewStatement);

        if (!useInterServerNetworks) {
            connection.close();
            return;
        }

        PreparedStatement serverInfoStatement = sqlQueryGenerator.generateCreateServerInfoTableStatement(connection);
        runStatement(serverInfoStatement);
        PreparedStatement interServerPortalsStatement = sqlQueryGenerator.generateCreatePortalTableStatement(connection, PortalType.INTER_SERVER);
        runStatement(interServerPortalsStatement);
        PreparedStatement interServerRelationStatement = sqlQueryGenerator.generateCreateFlagRelationTableStatement(connection, PortalType.INTER_SERVER);
        runStatement(interServerRelationStatement);
        PreparedStatement interPortalViewStatement = sqlQueryGenerator.generateCreatePortalViewStatement(connection, PortalType.INTER_SERVER);
        runStatement(interPortalViewStatement);
        connection.close();
    }

    /**
     * Adds any flags not already in the database
     *
     * @param connection        <p>The database connection to use</p>
     * @param sqlQueryGenerator <p>The SQL Query Generator to use for generating queries</p>
     * @throws SQLException <p>If unable to get from, or update the database</p>
     */
    private void addMissingFlags(Connection connection, SQLQueryGenerator sqlQueryGenerator) throws SQLException {
        PreparedStatement statement = sqlQueryGenerator.generateGetAllFlagsStatement(connection);
        PreparedStatement addStatement = sqlQueryGenerator.generateAddFlagStatement(connection);

        ResultSet resultSet = statement.executeQuery();
        List<String> knownFlags = new ArrayList<>();
        while (resultSet.next()) {
            knownFlags.add(resultSet.getString("character"));
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
     * Adds any position types not already in the database
     *
     * @param connection        <p>The database connection to use</p>
     * @param sqlQueryGenerator <p>The SQL Query Generator to use for generating queries</p>
     * @throws SQLException <p>If unable to get from, or update the database</p>
     */
    private void addMissingPositionTypes(Connection connection, SQLQueryGenerator sqlQueryGenerator) throws SQLException {
        PreparedStatement statement = sqlQueryGenerator.generateGetAllPortalPositionTypesStatement(connection);
        PreparedStatement addStatement = sqlQueryGenerator.generateAddPortalPositionTypeStatement(connection);

        ResultSet resultSet = statement.executeQuery();
        List<String> knownPositionTypes = new ArrayList<>();
        while (resultSet.next()) {
            knownPositionTypes.add(resultSet.getString("positionName"));
        }
        for (PositionType type : PositionType.values()) {
            if (!knownPositionTypes.contains(type.toString())) {
                addStatement.setString(1, type.toString());
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
        PreparedStatement statement = sqlQueryGenerator.generateGetAllPortalsStatement(connection, portalType);

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            String name = resultSet.getString("name");
            String networkName = resultSet.getString("network");

            //Skip null rows
            if (name == null && networkName == null) {
                continue;
            }

            String destination = resultSet.getString("destination");
            //Make sure to treat no destination as empty, not a null string
            if (resultSet.wasNull()) {
                destination = "";
            }
            String worldName = resultSet.getString("world");
            int topLeftX = resultSet.getInt("x");
            int topLeftY = resultSet.getInt("y");
            int topLeftZ = resultSet.getInt("z");
            String flagString = resultSet.getString("flags");
            UUID ownerUUID = UUID.fromString(resultSet.getString("ownerUUID"));
            String gateFileName = resultSet.getString("gateFileName");
            boolean flipZ = resultSet.getBoolean("flipZ");
            BlockFace facing = getBlockFaceFromOrdinal(Integer.parseInt(resultSet.getString("facing")));

            Set<PortalFlag> flags = PortalFlag.parseFlags(flagString);

            boolean isBungee = flags.contains(PortalFlag.FANCY_INTER_SERVER);
            logger.logMessage(Level.FINEST, "Trying to add portal " + name + ", on network " + networkName +
                    ",isInterServer = " + isBungee);

            String targetNetwork = networkName;
            if (flags.contains(PortalFlag.BUNGEE)) {
                targetNetwork = "§§§§§§#BUNGEE#§§§§§§";
            }

            try {
                createNetwork(targetNetwork, flags);
            } catch (NameErrorException ignored) {
            }
            Network network = getNetwork(targetNetwork, isBungee);

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
            Block block = world.getBlockAt(topLeftX, topLeftY, topLeftZ);

            if (destination == null || destination.trim().isEmpty()) {
                flags.add(PortalFlag.NETWORKED);
            }

            try {
                GateFormat format = GateFormat.getFormat(gateFileName);
                List<PortalPosition> portalPositions = getPortalPositions(networkName, name);
                Gate gate = new Gate(block.getLocation(), facing, flipZ, format, portalPositions, logger);
                Portal portal = PortalCreationHelper.createPortal(network, name, destination, networkName, flags, gate, ownerUUID);
                network.addPortal(portal, false);
                logger.logMessage(Level.FINEST, "Added as normal portal");
                if (isBungee) {
                    setInterServerPortalOnlineStatus(portal, true);
                }
            } catch (NameErrorException e) {
                e.printStackTrace();
            } catch (InvalidStructureException e) {
                logger.logMessage(Level.WARNING, String.format(
                        "The portal %s in %snetwork %s located at %s is in an invalid state, and could therefore not be recreated",
                        name, (portalType == PortalType.INTER_SERVER ? "interserver" : ""), networkName,
                        block.getLocation()));
            }
        }
        statement.close();
        connection.close();
    }

    /**
     * Gets all portal positions for the given portal
     *
     * @param networkName <p>The name of the network the portal belongs to</p>
     * @param portalName  <p>The name of the portal</p>
     * @return <p>The portal positions belonging to the portal</p>
     * @throws SQLException <p>If the SQL query fails to successfully execute</p>
     */
    private List<PortalPosition> getPortalPositions(String networkName, String portalName) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement statement = sqlQueryGenerator.generateGetPortalPositionsStatement(connection);
        statement.setString(1, networkName);
        statement.setString(2, portalName);

        List<PortalPosition> portalPositions = new ArrayList<>();
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            int xCoordinate = Integer.parseInt(resultSet.getString("xCoordinate"));
            int yCoordinate = Integer.parseInt(resultSet.getString("yCoordinate"));
            int zCoordinate = -Integer.parseInt(resultSet.getString("zCoordinate"));
            BlockVector positionVector = new BlockVector(xCoordinate, yCoordinate, zCoordinate);
            PositionType positionType = PositionType.valueOf(resultSet.getString("positionName"));
            portalPositions.add(new PortalPosition(positionType, positionVector));
        }
        statement.close();
        return portalPositions;
    }

    /**
     * Gets the correct block face from the given ordinal
     *
     * @param ordinal <p>The ordinal to get the block face from</p>
     * @return <p>The corresponding block face, or null</p>
     */
    private BlockFace getBlockFaceFromOrdinal(int ordinal) {
        for (BlockFace blockFace : BlockFace.values()) {
            if (blockFace.ordinal() == ordinal) {
                return blockFace;
            }
        }
        return null;
    }

    /**
     * Updates the online state of an inter-server portal
     *
     * @param portal   <p>The inter-server portal to update</p>
     * @param isOnline <p>Whether the inter-server portal is currently online</p>
     * @throws SQLException <p>If an SQL error occurs</p>
     */
    private void setInterServerPortalOnlineStatus(Portal portal, boolean isOnline) throws SQLException {
        Connection conn = database.getConnection();
        PreparedStatement statement = sqlQueryGenerator.generateSetPortalOnlineStatusStatement(conn, portal, isOnline);
        statement.execute();
        statement.close();
        conn.close();
    }

}
