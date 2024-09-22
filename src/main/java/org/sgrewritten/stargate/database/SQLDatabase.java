package org.sgrewritten.stargate.database;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.event.portal.StargatePortalLoadEvent;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.database.property.StoredPropertiesAPI;
import org.sgrewritten.stargate.database.property.StoredProperty;
import org.sgrewritten.stargate.exception.*;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateNetwork;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.GlobalPortalId;
import org.sgrewritten.stargate.network.portal.StargatePortal;
import org.sgrewritten.stargate.network.portal.VirtualPortal;
import org.sgrewritten.stargate.network.portal.portaldata.PortalData;
import org.sgrewritten.stargate.thread.task.StargateRegionTask;
import org.sgrewritten.stargate.util.NetworkCreationHelper;
import org.sgrewritten.stargate.util.database.DatabaseHelper;
import org.sgrewritten.stargate.util.database.PortalStorageHelper;
import org.sgrewritten.stargate.util.portal.PortalCreationHelper;
import org.sgrewritten.stargate.util.portal.PortalHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import static javax.management.timer.Timer.ONE_WEEK;

/**
 * A generic SQL database used for loading and saving portal data
 */
public class SQLDatabase implements StorageAPI {

    private final StoredPropertiesAPI propertiesDatabase;
    private SQLDatabaseAPI database;
    private SQLQueryGenerator sqlQueryGenerator;
    private boolean useInterServerNetworks;
    private final Set<String> invalidGateFormats = new HashSet<>();

    /**
     * Instantiates a new stargate registry
     *
     * @param database <p>The database connected to this Stargate instance</p>
     * @throws StargateInitializationException <p>If unable to initialize the database</p>
     */
    public SQLDatabase(SQLDatabaseAPI database, StoredPropertiesAPI propertiesDatabase) throws StargateInitializationException {
        this.propertiesDatabase = propertiesDatabase;
        load(database);
    }

    /**
     * Instantiates a new stargate registry
     *
     * @param database            <p>The database used for storing portals</p>
     * @param usingBungee         <p>Whether BungeeCord support is enabled</p>
     * @param usingRemoteDatabase <p>Whether a remote database, not a flatfile database is used</p>
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public SQLDatabase(SQLDatabaseAPI database, boolean usingBungee, boolean usingRemoteDatabase, StoredPropertiesAPI propertiesDatabase) throws SQLException {
        this.propertiesDatabase = propertiesDatabase;
        load(database, usingBungee, usingRemoteDatabase);
    }

    /**
     * Instantiates a new stargate registry
     *
     * @param database            <p>The database used for storing portals</p>
     * @param usingBungee         <p>Whether BungeeCord support is enabled</p>
     * @param usingRemoteDatabase <p>Whether a remote database, not a flatfile database is used</p>
     * @param config              <p>The table name configuration to use</p>
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    public SQLDatabase(SQLDatabaseAPI database, boolean usingBungee, boolean usingRemoteDatabase,
                       TableNameConfiguration config, StoredPropertiesAPI propertiesDatabase) throws SQLException {
        this.database = database;
        this.propertiesDatabase = propertiesDatabase;
        useInterServerNetworks = usingRemoteDatabase && usingBungee;
        DatabaseDriver databaseEnum = usingRemoteDatabase ? DatabaseDriver.MYSQL : DatabaseDriver.SQLITE;
        this.sqlQueryGenerator = new SQLQueryGenerator(config, databaseEnum);
        DatabaseHelper.createTables(database, this.sqlQueryGenerator, useInterServerNetworks);
    }

    @Override
    public void loadFromStorage(StargateAPI stargateAPI) throws StorageReadException {
        try {
            Stargate.log(Level.FINER, "Loading portals from base database");
            loadAllPortals(database, StorageType.LOCAL, stargateAPI);
            if (useInterServerNetworks) {
                Stargate.log(Level.FINER, "Loading portals from inter-server bungee database");
                loadAllPortals(database, StorageType.INTER_SERVER, stargateAPI);
            }
            if (invalidGateFormats.isEmpty()) {
                propertiesDatabase.setProperty(StoredProperty.SCHEDULED_GATE_CLEARING, -1);
            } else if (Long.parseLong(propertiesDatabase.getProperty(StoredProperty.SCHEDULED_GATE_CLEARING)) > System.currentTimeMillis()) {
                propertiesDatabase.setProperty(StoredProperty.SCHEDULED_GATE_CLEARING, System.currentTimeMillis() + ONE_WEEK);
            }
        } catch (SQLException exception) {
            throw new StorageReadException(exception);
        }
    }

    @Override
    public boolean savePortalToStorage(RealPortal portal) throws StorageWriteException {
        StorageType portalType = portal.getStorageType();
        /* An SQL transaction is used here to make sure partial data is never added to the database. */
        Connection connection = null;
        try {
            connection = database.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement savePortalStatement = sqlQueryGenerator.generateAddPortalStatement(connection, portal, portalType)) {
                savePortalStatement.execute();
            }

            try (PreparedStatement addFlagStatement = sqlQueryGenerator.generateAddPortalFlagRelationStatement(connection, portalType)) {
                addFlags(addFlagStatement, portal);
            }

            try (PreparedStatement addPositionStatement = sqlQueryGenerator.generateAddPortalPositionStatement(connection, portalType)) {
                addPortalPositions(addPositionStatement, portal);
            }
            connection.commit();
            connection.setAutoCommit(true);
            connection.close();
            if (portal instanceof StargatePortal stargatePortal) {
                stargatePortal.setSavedToStorage();
            }
            return true;
        } catch (SQLException exception) {
            try {
                if (connection != null) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                throw new StorageWriteException(e);
            }
            throw new StorageWriteException(exception);
        }
    }

    @Override
    public void removePortalFromStorage(Portal portal) throws StorageWriteException {
        /* An SQL transaction is used here to make sure portals are never partially removed from the database. */
        StorageType portalType = portal.getStorageType();
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
                    throw new StorageWriteException(ex);
                }
            }
            throw new StorageWriteException(e);
        }
    }

    /**
     * Loads all portals from the given database of the given portal type into the given registry
     *
     * @param database    <p>The database to load from</p>
     * @param portalType  <p>The portal type to load</p>
     * @param stargateAPI <p>The stargate registry</p>
     * @throws SQLException <p>If an SQL error occurs</p>
     */
    private void loadAllPortals(SQLDatabaseAPI database, StorageType portalType, StargateAPI stargateAPI) throws SQLException {
        PortalLoadData portalLoadData;
        try (Connection connection = database.getConnection()) {
            PreparedStatement statement = sqlQueryGenerator.generateGetAllPortalsStatement(connection, portalType);

            ResultSet resultSet = statement.executeQuery();
            portalLoadData = loadPortalsInQuery(resultSet, portalType);
            statement.close();
        }
        String scheduledGateFormatClearing = propertiesDatabase.getProperty(StoredProperty.SCHEDULED_GATE_CLEARING);
        if (scheduledGateFormatClearing != null && Long.parseLong(scheduledGateFormatClearing) > System.currentTimeMillis()) {
            removeGateFormats(portalLoadData.invalidGates, portalType);
        } else {
            invalidGateFormats.addAll(portalLoadData.invalidGates);
        }

        for (PortalData portalData : portalLoadData.loadedPortals) {
            loadPortal(portalData, stargateAPI);
        }
    }

    private PortalLoadData loadPortalsInQuery(ResultSet resultSet, StorageType portalType) throws SQLException {
        List<PortalData> portalDataList = new ArrayList<>();
        Set<String> worldsToRemove = new HashSet<>();
        Set<String> gateFormatsToRemove = new HashSet<>();
        while (resultSet.next()) {
            try {
                PortalData portalData = PortalStorageHelper.loadPortalData(resultSet, portalType);
                portalDataList.add(portalData);
            } catch (PortalLoadException e) {
                switch (e.getFailureType()) {
                    case WORLD -> worldsToRemove.add(resultSet.getString("world"));
                    case GATE_FORMAT -> gateFormatsToRemove.add(resultSet.getString("gateFileName"));
                }
            }
        }
        return new PortalLoadData(portalDataList, worldsToRemove, gateFormatsToRemove);
    }

    private void removeGateFormats(Collection<String> gateFormatsToRemove, StorageType storageType) throws SQLException {
        try (Connection connection = database.getConnection()) {
            for (String gateFormat : gateFormatsToRemove) {
                try (PreparedStatement preparedStatement = sqlQueryGenerator.generateRemoveGateStatement(connection, gateFormat, storageType)) {
                    preparedStatement.execute();
                }
            }
        }
    }

    /**
     * Loads one portal from the given result set into the given registry
     *
     * @param stargateAPI <p>The stargate API</p>
     * @throws SQLException <p>If an SQL error occurs</p>
     */
    private void loadPortal(PortalData portalData, StargateAPI stargateAPI) throws SQLException {
        if (portalData == null) {
            return;
        }

        Network network = getNetwork(portalData, stargateAPI.getRegistry(), stargateAPI.getNetworkManager());
        if (network == null) {
            Stargate.log(Level.WARNING, "Unable to get network " + portalData.networkName());
            return;
        }

        //If the loaded portal is virtual, register it to the network, and not as a normal one
        if (registerVirtualPortal(portalData.portalType(), portalData, network)) {
            return;
        }

        if (portalData.destination() == null || portalData.destination().trim().isEmpty()) {
            portalData.flags().add(StargateFlag.NETWORKED);
        }

        final List<PortalPosition> portalPositions = getPortalPositions(portalData, portalData.flags().contains(PortalFlag.LEGACY_INTERSERVER) ? portalData.networkName() : network.getId());

        //Actually register the gate and its positions
        new StargateRegionTask(portalData.gateData().topLeft()) {
            @Override
            public void run() {
                try {
                    registerPortalGate(portalData, network, stargateAPI, portalPositions);
                } catch (TranslatableException e) {
                    Stargate.log(e);
                } catch (InvalidStructureException e) {
                    Stargate.log(Level.WARNING, String.format(
                            "The portal %s in %snetwork %s located at %s is in an invalid state, and could therefore not be recreated",
                            portalData.name(), (portalData.portalType() == StorageType.INTER_SERVER ? "inter-server-" : ""), portalData.networkName(),
                            portalData.gateData().topLeft()));
                }
            }
        }.runNow();
    }

    /**
     * Registers the gate and portal positions for the given portal data
     *
     * @param portalData  <p>The portal data to register positions for</p>
     * @param network     <p>The network the portal belongs to</p>
     * @param stargateAPI <p>The portal stargate API</p>
     * @throws SQLException              <p>If unable to interact with the database</p>
     * @throws InvalidStructureException <p>If the portal's gate is invalid</p>
     * @throws TranslatableException     <p>If some input is invalid</p>
     */
    private void registerPortalGate(PortalData portalData, Network network, StargateAPI stargateAPI, List<PortalPosition> portalPositions) throws
            InvalidStructureException, TranslatableException {
        Gate gate = new Gate(portalData.gateData(), stargateAPI.getRegistry());

        gate.addPortalPositions(portalPositions);
        RealPortal portal = PortalCreationHelper.createPortal(network, portalData, gate, stargateAPI);
        if (!PortalHelper.portalValidityCheck(portal, stargateAPI.getNetworkManager())) {
            return;
        }
        if (portal instanceof StargatePortal stargatePortal) {
            stargatePortal.setSavedToStorage();
        }
        gate.assignPortal(portal);
        network.addPortal(portal);
        StargatePortalLoadEvent event = new StargatePortalLoadEvent(portal);
        Bukkit.getPluginManager().callEvent(event);

        Stargate.log(Level.FINEST, "Added as normal portal: " + network.getId() + ":" + portal.getName());
    }

    /**
     * Registers the given portal as a virtual portal if
     *
     * @param portalType <p>The type of the loaded portal</p>
     * @param portalData <p>The data for the loaded portal</p>
     * @param network    <p>The network the portal belongs to</p>
     * @return <p>True if the portal was registered as a virtual portal</p>
     */
    private boolean registerVirtualPortal(StorageType portalType, PortalData portalData, Network network) {
        if (portalType == StorageType.INTER_SERVER && !portalData.serverUUID().equals(Stargate.getServerUUID())) {
            Portal virtualPortal = new VirtualPortal(portalData.serverName(), portalData.name(), network, portalData.flags(),
                    portalData.ownerUUID());
            try {
                network.addPortal(virtualPortal);
            } catch (NameConflictException exception) {
                Stargate.log(exception);
            }
            Stargate.log(Level.FINEST, "Added as virtual portal");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the network of the portal in the given portal data
     *
     * @param portalData <p>The portal data of a portal in the network</p>
     * @param registry   <p>The stargate registry to use</p>
     * @return <p>The resulting network, or null if invalid</p>
     */
    private Network getNetwork(PortalData portalData, RegistryAPI registry, NetworkManager networkManager) {
        StorageType storageType = portalData.flags().contains(StargateFlag.INTERSERVER) ? StorageType.INTER_SERVER : StorageType.LOCAL;
        String targetNetwork = portalData.networkName();
        if (portalData.flags().contains(StargateFlag.LEGACY_INTERSERVER)) {
            targetNetwork = ConfigurationHelper.getString(ConfigurationOption.LEGACY_BUNGEE_NETWORK);
        }
        Stargate.log(Level.FINEST,
                "Trying to add portal " + portalData.name() + ", on network " + targetNetwork + ",storageType = " + storageType);
        try {
            boolean isForced = portalData.flags().contains(StargateFlag.DEFAULT_NETWORK);
            Network network = networkManager.createNetwork(targetNetwork, portalData.flags(), isForced);

            if (NetworkCreationHelper.getDefaultNamesTaken().contains(network.getId().toLowerCase())) {
                String newValidName = registry.getValidNewName(network);
                targetNetwork = newValidName;
                networkManager.rename(network, newValidName);
            }
        } catch (NameConflictException ignored) {
        } catch (TranslatableException e) {
            Stargate.log(e);
            return null;
        }
        return registry.getNetwork(targetNetwork, storageType);
    }

    /**
     * Gets all portal positions for the given portal
     *
     * @param portalData <p>The portal data to use for the query</p>
     * @param id
     * @return <p>The portal positions belonging to the portal</p>
     * @throws SQLException <p>If the SQL query fails to successfully execute</p>
     */
    private List<PortalPosition> getPortalPositions(PortalData portalData, String id) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement statement = sqlQueryGenerator.generateGetPortalPositionsStatement(connection, portalData.portalType());
        statement.setString(1, id);
        statement.setString(2, portalData.name());

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
        addFlagStatement.setString(2, portal.getNetwork().getId());
        addFlagStatement.setString(3, String.valueOf(flagCharacter));
        addFlagStatement.execute();
    }

    /**
     * Loads the database from the given API, and prepares necessary tables
     *
     * @param database <p>The database API to get a connection from</p>
     * @throws StargateInitializationException <p>If unable to initializes the database and tables</p>
     */
    public void load(SQLDatabaseAPI database) throws StargateInitializationException {
        try {
            load(database, ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE),
                    ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE));
        } catch (SQLException exception) {
            throw new StargateInitializationException(exception);
        }
    }

    private void load(SQLDatabaseAPI database, boolean usingBungee, boolean usingRemoteDatabase)
            throws SQLException {
        this.database = database;
        useInterServerNetworks = usingRemoteDatabase && usingBungee;
        TableNameConfiguration config = DatabaseHelper.getTableNameConfiguration(usingRemoteDatabase);
        DatabaseDriver databaseEnum = usingRemoteDatabase ? DatabaseDriver.MYSQL : DatabaseDriver.SQLITE;
        this.sqlQueryGenerator = new SQLQueryGenerator(config, databaseEnum);
        DatabaseHelper.createTables(database, this.sqlQueryGenerator, useInterServerNetworks);
    }

    @Override
    public Network createNetwork(String networkName, NetworkType type, StorageType storageType) throws InvalidNameException, NameLengthException, UnimplementedFlagException {
        return new StargateNetwork(networkName, type, storageType);
    }

    @Override
    public void startInterServerConnection() throws StorageWriteException {
        try (Connection conn = database.getConnection()) {
            DatabaseHelper.runStatement(sqlQueryGenerator.generateUpdateServerInfoStatus(conn, Stargate.getServerUUID(),
                    Stargate.getServerName()));
        } catch (SQLException exception) {
            throw new StorageWriteException(exception);
        }
    }

    @Override
    public void addFlagType(char flagChar) throws StorageWriteException {
        try (Connection connection = database.getConnection()) {
            PreparedStatement addStatement = sqlQueryGenerator.generateAddFlagStatement(connection);
            addStatement.setString(1, String.valueOf(flagChar));
            DatabaseHelper.runStatement(addStatement);
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public void addFlag(Character flagChar, Portal portal, StorageType portalType) throws StorageWriteException {
        try (Connection connection = database.getConnection()) {
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
            }
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }

    }

    @Override
    public void addPortalPositionType(String portalPositionTypeName) throws StorageWriteException {
        try (Connection connection = database.getConnection()) {
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
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }

    }

    @Override
    public void addPortalPosition(RealPortal portal, StorageType portalType, PortalPosition portalPosition) throws StorageWriteException {
        try (Connection connection = database.getConnection()) {
            PreparedStatement addPositionStatement = sqlQueryGenerator.generateAddPortalPositionStatement(connection, portalType);
            PortalStorageHelper.addPortalPosition(addPositionStatement, portal, portalPosition);
            addPositionStatement.close();
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }

    }

    @Override
    public void removeFlag(Character flagChar, Portal portal, StorageType portalType) throws StorageWriteException {
        try (Connection connection = database.getConnection()) {
            DatabaseHelper.runStatement(sqlQueryGenerator.generateRemoveFlagStatement(connection, portalType, portal, flagChar));
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public void removePortalPosition(RealPortal portal, StorageType portalType, PortalPosition portalPosition) throws StorageWriteException {
        try (Connection connection = database.getConnection()) {
            DatabaseHelper.runStatement(sqlQueryGenerator.generateRemovePortalPositionStatement(connection, portalType, portal, portalPosition));
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public void setPortalMetaData(Portal portal, String data, StorageType portalType) throws StorageWriteException {
        try (Connection connection = database.getConnection()) {
            DatabaseHelper.runStatement(sqlQueryGenerator.generateSetPortalMetaStatement(connection, portal, data, portalType));
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public String getPortalMetaData(Portal portal, StorageType portalType) throws StorageReadException {
        try (Connection connection = database.getConnection()) {
            PreparedStatement statement = sqlQueryGenerator.generateGetPortalStatement(connection, portal, portalType);
            ResultSet set = statement.executeQuery();
            if (!set.next()) {
                return null;
            }
            String data = set.getString("metaData");
            statement.close();
            return data;
        } catch (SQLException e) {
            throw new StorageReadException(e);
        }
    }

    @Override
    public void setPortalPositionMetaData(RealPortal portal, PortalPosition portalPosition, String data, StorageType portalType) throws StorageWriteException {
        try (Connection connection = database.getConnection()) {
            DatabaseHelper.runStatement(sqlQueryGenerator.generateSetPortalPositionMeta(connection, portal, portalPosition, data, portalType));
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public String getPortalPositionMetaData(Portal portal, PortalPosition portalPosition, StorageType portalType) throws StorageReadException {
        try (Connection connection = database.getConnection()) {
            PreparedStatement statement = sqlQueryGenerator.generateGetPortalPositionStatement(connection, portal, portalPosition, portalType);
            ResultSet set = statement.executeQuery();
            if (!set.next()) {
                return null;
            }
            String data = set.getString("metaData");
            statement.close();
            return data;
        } catch (SQLException e) {
            throw new StorageReadException(e);
        }
    }

    @Override
    public void updateNetworkName(String newName, String networkName, StorageType portalType) throws StorageWriteException {

        try (Connection connection = database.getConnection()) {
            DatabaseHelper.runStatement(sqlQueryGenerator.generateUpdateNetworkNameStatement(connection, newName, networkName, portalType));
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public void updatePortalName(String newName, GlobalPortalId globalPortalId, StorageType portalType) throws StorageWriteException {
        try (Connection connection = database.getConnection()) {
            DatabaseHelper
                    .runStatement(sqlQueryGenerator.generateUpdatePortalNameStatement(connection, newName,
                            globalPortalId.portalId(), globalPortalId.networkId(), portalType));
        } catch (SQLException e) {
            throw new StorageWriteException(e);
        }
    }

    @Override
    public boolean netWorkExists(String netName, StorageType portalType) throws StorageReadException {
        try (Connection connection = database.getConnection()) {
            PreparedStatement statement = sqlQueryGenerator.generateGetAllPortalsOfNetwork(connection, netName, portalType);
            ResultSet resultSet = statement.getResultSet();
            return (resultSet != null) && resultSet.next();
        } catch (SQLException e) {
            throw new StorageReadException(e);
        }
    }

    @Override
    public Set<String> getScheduledGatesClearing() {
        return this.invalidGateFormats;
    }

    @Override
    public void loadPortalsInWorld(World world, StorageType storageType, StargateAPI stargateAPI) throws StorageReadException, StorageWriteException {
        PortalLoadData portalLoadData;
        try (Connection connection = database.getConnection()) {
            ResultSet resultSet = sqlQueryGenerator.generateLoadPortalsInWorldStatement(connection, world, storageType).executeQuery();
            portalLoadData = loadPortalsInQuery(resultSet, storageType);
        } catch (SQLException e) {
            throw new StorageReadException(e);
        }

        String scheduledGateFormatClearing = propertiesDatabase.getProperty(StoredProperty.SCHEDULED_GATE_CLEARING);
        if (scheduledGateFormatClearing != null && Long.parseLong(scheduledGateFormatClearing) > System.currentTimeMillis()) {
            try {
                removeGateFormats(portalLoadData.invalidGates, storageType);
            } catch (SQLException e) {
                throw new StorageWriteException(e);
            }
        } else {
            invalidGateFormats.addAll(portalLoadData.invalidGates);
        }

        for (PortalData portalData : portalLoadData.loadedPortals) {
            try {
                loadPortal(portalData, stargateAPI);
            } catch (SQLException e) {
                throw new StorageReadException(e);
            }
        }
    }

    private record PortalLoadData(Collection<PortalData> loadedPortals, Collection<String> invalidWorlds,
                                  Collection<String> invalidGates) {

    }
}
