package org.sgrewritten.stargate.database;

import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.exception.StargateInitializationException;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.InterServerNetwork;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.PersonalNetwork;
import org.sgrewritten.stargate.network.PortalType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.BungeePortal;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalData;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.PortalPosition;
import org.sgrewritten.stargate.network.portal.RealPortal;
import org.sgrewritten.stargate.network.portal.VirtualPortal;
import org.sgrewritten.stargate.util.database.DatabaseHelper;
import org.sgrewritten.stargate.util.database.PortalStorageHelper;
import org.sgrewritten.stargate.util.portal.PortalCreationHelper;

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
 * A generic SQL database used for loading and saving portal data
 */
public class SQLDatabase implements StorageAPI {

    private SQLDatabaseAPI database;
    private SQLQueryGenerator sqlQueryGenerator;
    private boolean useInterServerNetworks;
    private StargateLogger logger;

    /**
     * Instantiates a new stargate registry
     *
     * @param stargate <p>The Stargate instance to use</p>
     * @throws StargateInitializationException <p>If unable to initialize the database</p>
     */
    public SQLDatabase(SQLDatabaseAPI database, Stargate stargate) throws StargateInitializationException {
        load(database, stargate);
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
    public SQLDatabase(SQLDatabaseAPI database, boolean usingBungee, boolean usingRemoteDatabase,
                       StargateLogger logger) throws SQLException {
        load(database, usingBungee, usingRemoteDatabase, logger);
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
    public SQLDatabase(SQLDatabaseAPI database, boolean usingBungee, boolean usingRemoteDatabase,
                       StargateLogger logger, TableNameConfiguration config) throws SQLException {
        this.logger = logger;
        this.database = database;
        useInterServerNetworks = usingRemoteDatabase && usingBungee;
        DatabaseDriver databaseEnum = usingRemoteDatabase ? DatabaseDriver.MYSQL : DatabaseDriver.SQLITE;
        this.sqlQueryGenerator = new SQLQueryGenerator(config, logger, databaseEnum);
        DatabaseHelper.createTables(database, this.sqlQueryGenerator, useInterServerNetworks);
    }

    @Override
    public void loadFromStorage(RegistryAPI registry) {
        try {
            logger.logMessage(Level.FINER, "Loading portals from base database");
            loadAllPortals(database, PortalType.LOCAL, registry);
            if (useInterServerNetworks) {
                logger.logMessage(Level.FINER, "Loading portals from inter-server bungee database");
                loadAllPortals(database, PortalType.INTER_SERVER, registry);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
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

            DatabaseHelper.runStatement(sqlQueryGenerator.generateRemoveFlagsStatement(conn, portalType, portal));
            DatabaseHelper.runStatement(sqlQueryGenerator.generateRemovePortalPositionsStatement(conn, portalType, portal));
            DatabaseHelper.runStatement(sqlQueryGenerator.generateRemovePortalStatement(conn, portal, portalType));

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
     * Loads all portals from the given database of the given portal type into the given registry
     *
     * @param database   <p>The database to load from</p>
     * @param portalType <p>The portal type to load</p>
     * @throws SQLException <p>If an SQL error occurs</p>
     */
    private void loadAllPortals(SQLDatabaseAPI database, PortalType portalType, RegistryAPI registry) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement statement = sqlQueryGenerator.generateGetAllPortalsStatement(connection, portalType);

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            PortalData portalData = PortalStorageHelper.loadPortalData(resultSet, portalType);
            if (portalData == null) {
                continue;
            }
            boolean isBungee = portalData.flags.contains(PortalFlag.FANCY_INTER_SERVER);
            Stargate.log(Level.FINEST,
                    "Trying to add portal " + portalData.name + ", on network " + portalData.networkName + ",isInterServer = " + isBungee);

            String targetNetwork = portalData.networkName;
            if (portalData.flags.contains(PortalFlag.BUNGEE)) {
                targetNetwork = BungeePortal.getLegacyNetworkName();
            }

            try {
                registry.createNetwork(targetNetwork, portalData.flags);
            } catch (NameErrorException ignored) {
            }
            Network network = registry.getNetwork(targetNetwork, isBungee);

            //TODO Check if portalType is necessary to keep track of // there's already flags.contains(PortalFlag.FANCY_INTERSERVER)
            if (portalType == PortalType.INTER_SERVER) {
                if (!portalData.serverUUID.equals(Stargate.getServerUUID())) {
                    Portal virtualPortal = new VirtualPortal(portalData.serverName, portalData.name, network, portalData.flags, portalData.ownerUUID);
                    try {
                        network.addPortal(virtualPortal, false);
                    } catch (NameErrorException ignored) {
                    }
                    Stargate.log(Level.FINEST, "Added as virtual portal");
                    continue;
                }
            }

            if (portalData.destination == null || portalData.destination.trim().isEmpty()) {
                portalData.flags.add(PortalFlag.NETWORKED);
            }

            try {
                List<PortalPosition> portalPositions = getPortalPositions(portalData);
                Gate gate = new Gate(portalData, logger);
                if (ConfigurationHelper.getBoolean(ConfigurationOption.CHECK_PORTAL_VALIDITY)
                        && !gate.isValid(portalData.flags.contains(PortalFlag.ALWAYS_ON))) {
                    throw new InvalidStructureException();
                }
                gate.addPortalPositions(portalPositions);
                Portal portal = PortalCreationHelper.createPortal(network, portalData, gate, logger);
                network.addPortal(portal, false);
                Stargate.log(Level.FINEST, "Added as normal portal");
            } catch (NameErrorException e) {
                e.printStackTrace();
            } catch (InvalidStructureException e) {
                Stargate.log(Level.WARNING, String.format(
                        "The portal %s in %snetwork %s located at %s is in an invalid state, and could therefore not be recreated",
                        portalData.name, (portalType == PortalType.INTER_SERVER ? "inter-server-" : ""), portalData.networkName,
                        portalData.topLeft));
            } catch (GateConflictException ignored) {
            }
        }
        statement.close();
        connection.close();
    }

    /**
     * Gets all portal positions for the given portal
     *
     * @param portalData <p>The portal data to use for the query</p>
     * @return <p>The portal positions belonging to the portal</p>
     * @throws SQLException <p>If the SQL query fails to successfully execute</p>
     */
    private List<PortalPosition> getPortalPositions(PortalData portalData) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement statement = sqlQueryGenerator.generateGetPortalPositionsStatement(connection, portalData.portalType);
        statement.setString(1, portalData.networkName);
        statement.setString(2, portalData.name);

        List<PortalPosition> portalPositions = new ArrayList<>();
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            portalPositions.add(PortalStorageHelper.loadPortalPosition(resultSet));
        }
        statement.close();
        connection.close();
        return portalPositions;
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
            PortalStorageHelper.addPortalPosition(addPositionStatement, portal, portalPosition);
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
        for (Character flagCharacter : portal.getAllFlagsString().toCharArray()) {
            addFlag(addFlagStatement, portal, flagCharacter);
        }
    }

    private void addFlag(PreparedStatement addFlagStatement, Portal portal, Character flagCharacter) throws SQLException {
        Stargate.log(Level.FINER, "Adding flag " + flagCharacter + " to portal: " + portal);
        addFlagStatement.setString(1, portal.getName());
        addFlagStatement.setString(2, portal.getNetwork().getName());
        addFlagStatement.setString(3, String.valueOf(flagCharacter));
        addFlagStatement.execute();
    }

    @Override
    public void load(SQLDatabaseAPI database, Stargate stargate) throws StargateInitializationException {
        try {
            load(database, ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE),
                    ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE), stargate);
        } catch (SQLException exception) {
            logger.logMessage(Level.SEVERE, exception.getMessage());
            throw new StargateInitializationException(exception.getMessage());
        }
    }

    private void load(SQLDatabaseAPI database, boolean usingBungee, boolean usingRemoteDatabase, StargateLogger logger)
            throws SQLException {
        this.logger = logger;
        this.database = database;
        useInterServerNetworks = usingRemoteDatabase && usingBungee;
        TableNameConfiguration config = DatabaseHelper.getTableNameConfiguration(usingRemoteDatabase);
        DatabaseDriver databaseEnum = usingRemoteDatabase ? DatabaseDriver.MYSQL : DatabaseDriver.SQLITE;
        this.sqlQueryGenerator = new SQLQueryGenerator(config, logger, databaseEnum);
        DatabaseHelper.createTables(database, this.sqlQueryGenerator, useInterServerNetworks);
    }

    @Override
    public Network createNetwork(String networkName, Set<PortalFlag> flags) throws NameErrorException {
        if (flags.contains(PortalFlag.FANCY_INTER_SERVER)) {
            return new InterServerNetwork(networkName);
        }
        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            UUID uuid = UUID.fromString(networkName);
            return new PersonalNetwork(uuid);
        } else {
            return new LocalNetwork(networkName);
        }
    }

    @Override
    public void startInterServerConnection() {
        try {
            Connection conn = database.getConnection();
            DatabaseHelper.runStatement(sqlQueryGenerator.generateUpdateServerInfoStatus(conn, Stargate.getServerUUID(),
                    Stargate.getServerName()));
            conn.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void addFlagType(Character flagChar){
        Connection connection;
        try {
            connection = database.getConnection();
            PreparedStatement addStatement = sqlQueryGenerator.generateAddFlagStatement(connection);
            addStatement.setString(1, String.valueOf(flagChar));
            DatabaseHelper.runStatement(addStatement);
            connection.close();
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public void addFlag(Character flagChar, Portal portal, PortalType portalType) {
        Connection connection;
        try {
            connection = database.getConnection();
            PreparedStatement statement = sqlQueryGenerator.generateGetAllFlagsStatement(connection);
            ResultSet resultSet = statement.executeQuery();
            List<String> knownFlags = new ArrayList<>();
            while (resultSet.next()) {
                knownFlags.add(resultSet.getString("character"));
            }
            if (!knownFlags.contains(String.valueOf(flagChar))) {
                PreparedStatement addFlagStatement = sqlQueryGenerator.generateAddPortalFlagRelationStatement(connection,
                        portalType);
                addFlag(addFlagStatement, portal, flagChar);
                addFlagStatement.close();
                connection.close();
            }
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
        
    }

    @Override
    public void addPortalPositionType(String portalPositionTypeName) {
        Connection connection;
        try {
            connection = database.getConnection();
            PreparedStatement statement = sqlQueryGenerator.generateGetAllPortalPositionTypesStatement(connection);
            ResultSet resultSet = statement.executeQuery();
            List<String> knownPositionTypes = new ArrayList<>();
            while (resultSet.next()) {
                knownPositionTypes.add(resultSet.getString("positionName"));
            }
            if (!knownPositionTypes.contains(portalPositionTypeName)) {
                PreparedStatement addStatement = sqlQueryGenerator.generateAddPortalPositionTypeStatement(connection);
                addStatement.setString(1, portalPositionTypeName);
                DatabaseHelper.runStatement(addStatement);
            }
            connection.close();
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
        
    }

    @Override
    public void addPortalPosition(RealPortal portal, PortalType portalType, PortalPosition portalPosition) {
        Connection connection;
        try {
            connection = database.getConnection();
            PreparedStatement addPositionStatement = sqlQueryGenerator.generateAddPortalPositionStatement(connection, portalType);
            PortalStorageHelper.addPortalPosition(addPositionStatement, portal, portalPosition);
            addPositionStatement.close();
            connection.close();
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
        
    }

    @Override
    public void removeFlag(Character flagChar, Portal portal, PortalType portalType) {
        Connection connection;
        try {
            connection = database.getConnection();
            DatabaseHelper.runStatement(sqlQueryGenerator.generateRemoveFlagStatement(connection, portalType, portal, flagChar));
            connection.close();
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public void removePortalPosition(RealPortal portal, PortalType portalType, PortalPosition portalPosition) throws StorageWriteException {
        Connection connection;
        try {
            connection = database.getConnection();
            DatabaseHelper.runStatement(sqlQueryGenerator.generateRemovePortalPositionStatement(connection, portalType, portal, portalPosition));
            connection.close();
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public void setPortalMetaData(Portal portal, String data, PortalType portalType) throws StorageWriteException {
        Connection connection;
        try {
            connection = database.getConnection();
            DatabaseHelper.runStatement(sqlQueryGenerator.generateSetPortalMetaStatement(connection, portal, data, portalType));
            connection.close();
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public String getPortalMetaData(Portal portal, PortalType portalType) throws StorageReadException {
        Connection connection;
        try {
            connection = database.getConnection();
            PreparedStatement statement = sqlQueryGenerator.generateGetPortalStatement(connection, portal, portalType);
            ResultSet set = statement.executeQuery();
            if (!set.next()) {
                return null;
            }
            String data = set.getString("metaData");
            statement.close();
            connection.close();
            return data;
        } catch (SQLException e) {
            throw new StorageReadException(e);
        }
    }

    @Override
    public void setPortalPositionMetaData(RealPortal portal, PortalPosition portalPosition, String data, PortalType portalType) throws StorageWriteException {
        Connection connection;
        try {
            connection = database.getConnection();
            DatabaseHelper.runStatement(sqlQueryGenerator.generateSetPortalPositionMeta(connection, portal, portalPosition, data, portalType));
            connection.close();
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public String getPortalPositionMetaData(Portal portal, PortalPosition portalPosition, PortalType portalType) throws StorageReadException {
        Connection connection;
        try {
            connection = database.getConnection();
            PreparedStatement statement = sqlQueryGenerator.generateGetPortalPositionStatement(connection, portal, portalPosition, portalType);
            ResultSet set = statement.executeQuery();
            if (!set.next()) {
                return null;
            }
            String data = set.getString("metaData");
            statement.close();
            connection.close();
            return data;
        } catch (SQLException e) {
            throw new StorageReadException(e);
        }
    }

}
