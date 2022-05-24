package net.TheDgtl.Stargate.database;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.config.TableNameConfiguration;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.InvalidStructureException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.gate.GateFormatHandler;
import net.TheDgtl.Stargate.network.InterServerNetwork;
import net.TheDgtl.Stargate.network.LocalNetwork;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.PersonalNetwork;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.RegistryAPI;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.PortalPosition;
import net.TheDgtl.Stargate.network.portal.PositionType;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;
import net.TheDgtl.Stargate.util.portal.PortalCreationHelper;

import org.bukkit.Bukkit;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * An API for interacting with the portal database
 */
public class PortalDatabaseAPI implements StorageAPI {

    private final Database database;
    private final SQLQueryGenerator sqlQueryGenerator;
    private final boolean useInterServerNetworks;
    private final StargateLogger logger;

    /**
     * Instantiates a new stargate registry
     *
     * @param stargate <p>The Stargate instance to use</p>
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public PortalDatabaseAPI(Stargate stargate) throws SQLException {
        this(loadDatabase(stargate), ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE), ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE), stargate);
    }

    /**
     * Instantiates a new stargate registry
     *
     * @param database            <p>The database used for storing portals</p>
     * @param usingBungee         <p>Whether BungeeCord support is enabled</p>
     * @param usingRemoteDatabase <p>Whether a remote database, not a flatfile database is used</p>
     * @param logger              <p>The Stargate logger to use for logging</p>
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public PortalDatabaseAPI(Database database, boolean usingBungee, boolean usingRemoteDatabase,
                             StargateLogger logger) throws SQLException {
        this.logger = logger;
        this.database = database;
        useInterServerNetworks = usingRemoteDatabase && usingBungee;
        String PREFIX = usingRemoteDatabase ? ConfigurationHelper.getString(ConfigurationOption.BUNGEE_INSTANCE_NAME) : "";
        String serverPrefix = usingRemoteDatabase ? Stargate.serverUUID.toString() : "";
        TableNameConfiguration config = new TableNameConfiguration(PREFIX, serverPrefix.replace("-", ""));
        DriverEnum databaseEnum = usingRemoteDatabase ? DriverEnum.MYSQL : DriverEnum.SQLITE;
        this.sqlQueryGenerator = new SQLQueryGenerator(config, logger, databaseEnum);
        createTables();
    }

    /**
     * Instantiates a new stargate registry
     *
     * @param database            <p>The database used for storing portals</p>
     * @param usingBungee         <p>Whether BungeeCord support is enabled</p>
     * @param usingRemoteDatabase <p>Whether a remote database, not a flatfile database is used</p>
     * @param logger              <p>The Stargate logger to use for logging</p>
     * @param config              <p>The table name configuration to use</p>
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public PortalDatabaseAPI(Database database, boolean usingBungee, boolean usingRemoteDatabase,
                             StargateLogger logger, TableNameConfiguration config) throws SQLException {
        this.logger = logger;
        this.database = database;
        useInterServerNetworks = usingRemoteDatabase && usingBungee;
        DriverEnum databaseEnum = usingRemoteDatabase ? DriverEnum.MYSQL : DriverEnum.SQLITE;
        this.sqlQueryGenerator = new SQLQueryGenerator(config, logger, databaseEnum);
        createTables();
    }

    @Override
    public void loadFromStorage() {
        try {
            logger.logMessage(Level.FINER, "Loading portals from base database");
            loadAllPortals(database, PortalType.LOCAL, Stargate.getRegistry());
            if (useInterServerNetworks) {
                logger.logMessage(Level.FINER, "Loading portals from inter-server bungee database");
                loadAllPortals(database, PortalType.INTER_SERVER, Stargate.getRegistry());
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void startInterServerConnection() {
        try {
            Connection conn = database.getConnection();
            PreparedStatement statement = sqlQueryGenerator.generateUpdateServerInfoStatus(conn, Stargate.serverUUID,
                    Stargate.serverName);
            statement.execute();
            statement.close();
            conn.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void endInterServerConnection() {
        //TODO: Why is this method here if it does nothing
    }

    @Override
    public Network createNetwork(String networkName, Set<PortalFlag> flags) throws NameErrorException {
        if (flags.contains(PortalFlag.FANCY_INTER_SERVER)) {
            return new InterServerNetwork(networkName, database, sqlQueryGenerator);
        }
        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            UUID uuid = UUID.fromString(networkName);
            return new PersonalNetwork(uuid, database, sqlQueryGenerator);
        } else {
            return new LocalNetwork(networkName, database, sqlQueryGenerator);
        }
    }

    @Override
    public boolean savePortalToStorage(RealPortal portal, PortalType portalType) {
        /* An SQL transaction is used here to make sure partial data is never added to the database. */
        Connection connection = null;
        try {
            connection = database.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement savePortalStatement = sqlQueryGenerator.generateAddPortalStatement(connection, portal, portalType);
            savePortalStatement.execute();
            savePortalStatement.close();

            PreparedStatement addFlagStatement = sqlQueryGenerator.generateAddPortalFlagRelationStatement(connection, portalType);
            addFlags(addFlagStatement, portal);
            addFlagStatement.close();

            PreparedStatement addPositionStatement = sqlQueryGenerator.generateAddPortalPositionStatement(connection, portalType);
            addPortalPositions(addPositionStatement, portal);
            addPositionStatement.close();

            connection.commit();
            connection.setAutoCommit(true);
            connection.close();
            return true;
        } catch (SQLException exception) {
            try {
                if (connection != null) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public void removePortalFromStorage(Portal portal, PortalType portalType) {
        /* An SQL transaction is used here to make sure portals are never partially removed from the database. */
        Connection conn = null;
        try {
            conn = database.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement removeFlagsStatement = sqlQueryGenerator.generateRemoveFlagStatement(conn, portalType);
            removeFlagsStatement.setString(1, portal.getName());
            removeFlagsStatement.setString(2, portal.getNetwork().getName());
            removeFlagsStatement.execute();
            removeFlagsStatement.close();

            PreparedStatement removePositionsStatement = sqlQueryGenerator.generateRemovePortalPositionsStatement(conn, portalType);
            removePositionsStatement.setString(1, portal.getName());
            removePositionsStatement.setString(2, portal.getNetwork().getName());
            removePositionsStatement.execute();
            removePositionsStatement.close();

            PreparedStatement statement = sqlQueryGenerator.generateRemovePortalStatement(conn, portal, portalType);
            statement.execute();
            statement.close();

            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.setAutoCommit(false);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
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
        if (ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE)) {
            if (ConfigurationHelper.getBoolean(ConfigurationOption.SHOW_HIKARI_CONFIG)) {
                return new MySqlDatabase(stargate);
            }

            DriverEnum driver = DriverEnum.valueOf(ConfigurationHelper.getString(ConfigurationOption.BUNGEE_DRIVER).toUpperCase());
            String bungeeDatabaseName = ConfigurationHelper.getString(ConfigurationOption.BUNGEE_DATABASE);
            int port = ConfigurationHelper.getInteger(ConfigurationOption.BUNGEE_PORT);
            String address = ConfigurationHelper.getString(ConfigurationOption.BUNGEE_ADDRESS);
            String username = ConfigurationHelper.getString(ConfigurationOption.BUNGEE_USERNAME);
            String password = ConfigurationHelper.getString(ConfigurationOption.BUNGEE_PASSWORD);
            boolean useSSL = ConfigurationHelper.getBoolean(ConfigurationOption.BUNGEE_USE_SSL);

            switch (driver) {
                case MARIADB:
                case MYSQL:
                    return new MySqlDatabase(driver, address, port, bungeeDatabaseName, username, password, useSSL);
                default:
                    throw new SQLException("Unsupported driver: Stargate currently supports MariaDb and MySql for remote databases");
            }
        } else {
            String databaseName = ConfigurationHelper.getString(ConfigurationOption.DATABASE_NAME);
            File file = new File(stargate.getDataFolder().getAbsoluteFile(), databaseName + ".db");
            return new SQLiteDatabase(file);
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
        PreparedStatement portalPositionsStatement = sqlQueryGenerator.generateCreatePortalPositionTableStatement(connection, PortalType.LOCAL);
        runStatement(portalPositionsStatement);
        PreparedStatement portalPositionIndex = sqlQueryGenerator.generateCreatePortalPositionIndex(connection);
        runStatement(portalPositionIndex);


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
        PreparedStatement interPortalPositionsStatement = sqlQueryGenerator.generateCreatePortalPositionTableStatement(connection, PortalType.INTER_SERVER);
        runStatement(interPortalPositionsStatement);
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
     * Loads all portals from the given database of the given portal type into the given registry
     *
     * @param database   <p>The database to load from</p>
     * @param portalType <p>The portal type to load</p>
     * @throws SQLException <p>If an SQL error occurs</p>
     */
    private void loadAllPortals(Database database, PortalType portalType, RegistryAPI registry) throws SQLException {
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
                registry.createNetwork(targetNetwork, flags);
            } catch (NameErrorException ignored) {
            }
            Network network = registry.getNetwork(targetNetwork, isBungee);

            if (portalType == PortalType.INTER_SERVER) {
                String serverUUID = resultSet.getString("homeServerId");
                logger.logMessage(Level.FINEST, "serverUUID = " + serverUUID);
                if (!serverUUID.equals(Stargate.serverUUID.toString())) {
                    String serverName = resultSet.getString("serverName");
                    Portal virtualPortal = new VirtualPortal(serverName, name, network, flags, ownerUUID);
                    try {
                        network.addPortal(virtualPortal, false);
                    } catch (NameErrorException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
                GateFormat format = GateFormatHandler.getFormat(gateFileName);
                if (format == null) {
                    continue;
                }
                List<PortalPosition> portalPositions = getPortalPositions(networkName, name, portalType);
                Gate gate = new Gate(block.getLocation(), facing, flipZ, format, logger);
                if (ConfigurationHelper.getBoolean(ConfigurationOption.CHECK_PORTAL_VALIDITY)
                        && !gate.isValid(flags.contains(PortalFlag.ALWAYS_ON))) {
                    throw new InvalidStructureException();
                }
                gate.addPortalPositions(portalPositions);
                Portal portal = PortalCreationHelper.createPortal(network, name, destination, networkName, flags, gate, ownerUUID, logger);
                network.addPortal(portal, false);
                logger.logMessage(Level.FINEST, "Added as normal portal");
            } catch (NameErrorException e) {
                e.printStackTrace();
            } catch (InvalidStructureException e) {
                logger.logMessage(Level.WARNING, String.format(
                        "The portal %s in %snetwork %s located at %s is in an invalid state, and could therefore not be recreated",
                        name, (portalType == PortalType.INTER_SERVER ? "interserver" : ""), networkName,
                        block.getLocation()));
            } catch (GateConflictException ignored) {
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
    private List<PortalPosition> getPortalPositions(String networkName, String portalName, PortalType type) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement statement = sqlQueryGenerator.generateGetPortalPositionsStatement(connection, type);
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
        connection.close();
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
     * Adds a portal position to the portal positions table
     *
     * @param addPositionStatement <p>The prepared statement for adding a portal position</p>
     * @param portal               <p>The portal to add the portal positions of</p>
     * @throws SQLException <p>If unable to add the portal positions</p>
     */
    private void addPortalPositions(PreparedStatement addPositionStatement, RealPortal portal) throws SQLException {
        for (PortalPosition portalPosition : portal.getGate().getPortalPositions()) {
            addPositionStatement.setString(1, portal.getName());
            addPositionStatement.setString(2, portal.getNetwork().getName());
            addPositionStatement.setString(3, String.valueOf(portalPosition.getPositionLocation().getBlockX()));
            addPositionStatement.setString(4, String.valueOf(portalPosition.getPositionLocation().getBlockY()));
            addPositionStatement.setString(5, String.valueOf(-portalPosition.getPositionLocation().getBlockZ()));
            addPositionStatement.setString(6, portalPosition.getPositionType().name());
            addPositionStatement.execute();
        }
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
