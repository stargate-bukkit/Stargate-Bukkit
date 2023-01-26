package org.sgrewritten.stargate.migration;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.SQLQuery;
import org.sgrewritten.stargate.database.SQLQueryExecutor;
import org.sgrewritten.stargate.database.SQLQueryGenerator;
import org.sgrewritten.stargate.database.SQLQueryHandler;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.GlobalPortalId;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.util.ExceptionHelper;
import org.sgrewritten.stargate.util.FileHelper;
import org.sgrewritten.stargate.util.database.DatabaseHelper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A data migrator to upgrade data and config to the 1.0.14 alpha
 */
public class DataMigration_1_0_14 extends DataMigration {

    private HashMap<String, String> CONFIG_CONVERSIONS;

    public DataMigration_1_0_14() {
        loadConfigConversions();
    }

    @Override
    public void run(@NotNull SQLDatabaseAPI database) {
        boolean isInterServer = ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)
                && ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE);
        Stargate.log(Level.INFO, "Running database migration 1.0.0.11 -> 1.0.0.14");
        TableNameConfiguration nameConfiguration = DatabaseHelper.getTableNameConfiguration(ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE));
        try {
            new SQLDatabaseMigrator(database, nameConfiguration, "/migration/database/alpha-1_0_0_14", isInterServer).run();
        } catch (SQLException | IOException e) {
            Stargate.log(e);
        }

        try {
            addNetworkTypeFlags(database, StorageType.LOCAL, nameConfiguration);
            if (isInterServer) {
                addNetworkTypeFlags(database, StorageType.INTER_SERVER, nameConfiguration);
            }
        } catch (SQLException e) {
            Stargate.log(e);
        }

        changeDefaultNetworkId(database, nameConfiguration, isInterServer);
    }

    /**
     * Changes the default network ids from the old values to the new ones
     *
     * @param database          <p>The database to use</p>
     * @param nameConfiguration <p>The table name configuration to use</p>
     */
    private void changeDefaultNetworkId(@NotNull SQLDatabaseAPI database, TableNameConfiguration nameConfiguration,
                                        boolean isInterServer) {
        try {
            runChangeDefaultNetworkIdStatement(database, nameConfiguration, StorageType.LOCAL);
            if (isInterServer) {
                runChangeDefaultNetworkIdStatement(database, nameConfiguration, StorageType.INTER_SERVER);
            }
        } catch (SQLException e) {
            Stargate.log(e);
        }
    }

    /**
     * Changes the default network id from the old value to the new one
     *
     * @param database          <p>The database to use</p>
     * @param nameConfiguration <p>The table name configuration to use</p>
     * @param type              <p>The type of portal to update for</p>
     * @throws SQLException <p>If unable to change the network id</p>
     */
    private void runChangeDefaultNetworkIdStatement(SQLDatabaseAPI database, TableNameConfiguration nameConfiguration,
                                                    StorageType type) throws SQLException {
        try (Connection connection = database.getConnection()) {
            SQLQuery query = type == StorageType.LOCAL ? SQLQuery.UPDATE_NETWORK_NAME :
                    SQLQuery.UPDATE_INTER_NETWORK_NAME;
            String queryString = nameConfiguration.replaceKnownTableNames(SQLQueryHandler.getQuery(query,
                    database.getDriver()));
            try (PreparedStatement statement = connection.prepareStatement(queryString)) {
                statement.setString(1, LocalNetwork.DEFAULT_NETWORK_ID);
                statement.setString(2, ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK));
                statement.execute();
            }
        }
    }

    @Override
    public int getConfigVersion() {
        return 7;
    }

    /**
     * Adds network type flags for all portals
     *
     * @param database          <p>The database to use</p>
     * @param storageType       <p>The type of portal to add network type flags for</p>
     * @param nameConfiguration <p>The table name configuration to use</p>
     * @throws SQLException <p>If unable to add the network type flags</p>
     */
    private void addNetworkTypeFlags(@NotNull SQLDatabaseAPI database, StorageType storageType,
                                     TableNameConfiguration nameConfiguration) throws SQLException {
        SQLQueryGenerator queryGenerator = new SQLQueryGenerator(nameConfiguration, Stargate.getInstance(),
                database.getDriver());
        try (Connection connection = database.getConnection()) {
            Map<GlobalPortalId, PortalFlag> portalNetworkTypeFlags = getNetworkTypeFlags(queryGenerator, connection,
                    storageType);
            insertNetworkTypeFlags(connection, queryGenerator, portalNetworkTypeFlags, storageType);
        }
    }

    /**
     * Adds network type flags for all portals, which wasn't used in older alpha versions
     *
     * @param connection             <p>The database connection to use</p>
     * @param queryGenerator         <p>The query generation to use</p>
     * @param portalNetworkTypeFlags <p>All portal type flags</p>
     * @param type                   <p>The type of portal to insert for</p>
     * @throws SQLException <p>If unable to insert any of the flags</p>
     */
    private void insertNetworkTypeFlags(Connection connection, SQLQueryGenerator queryGenerator,
                                        Map<GlobalPortalId, PortalFlag> portalNetworkTypeFlags,
                                        StorageType type) throws SQLException {
        SQLQueryExecutor executor = new SQLQueryExecutor(connection, queryGenerator);
        for (Map.Entry<GlobalPortalId, PortalFlag> entry : portalNetworkTypeFlags.entrySet()) {
            Set<PortalFlag> flagSet = new HashSet<>();
            flagSet.add(entry.getValue());
            executor.executeAddFlagRelation(type, entry.getKey(), flagSet);
        }
    }

    /**
     * Gets the network type flag for all portals
     *
     * @param sqlQueryGenerator <p>The SQL query generator to use</p>
     * @param connection        <p>The database connection to use</p>
     * @param type              <p>The type of portal to get network type flags for</p>
     * @return <p>A map between portal identifiers, and network type flags</p>
     * @throws SQLException <p>If unable to get the flags</p>
     */
    private Map<GlobalPortalId, PortalFlag> getNetworkTypeFlags(SQLQueryGenerator sqlQueryGenerator,
                                                                Connection connection, StorageType type) throws SQLException {
        Map<GlobalPortalId, PortalFlag> output = new HashMap<>();
        try (PreparedStatement statement = sqlQueryGenerator.generateGetAllPortalsStatement(connection, type)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet == null) {
                return output;
            }

            while (resultSet.next()) {
                if (NetworkType.getNetworkTypeFromFlags(PortalFlag.parseFlags(resultSet.getString("flags"))) != null) {
                    continue;
                }

                String networkId = resultSet.getString("network");
                PortalFlag flag = determineNetworkFlagFromNetworkName(networkId);
                GlobalPortalId id = new GlobalPortalId(resultSet.getString("name"), networkId);
                output.put(id, flag);
            }
        }
        return output;
    }

    /**
     * Determines the type of network the given network is, using the name of the network
     *
     * @param networkName <p>The name to check</p>
     * @return <p>The portal flag corresponding to the network's type</p>
     */
    private PortalFlag determineNetworkFlagFromNetworkName(String networkName) {
        if (ExceptionHelper.doesNotThrow(IllegalArgumentException.class, () -> Stargate.log(Level.FINEST,
                "Found personal network " + UUID.fromString(networkName)))) {
            return PortalFlag.PERSONAL_NETWORK;
        }
        if (networkName.equalsIgnoreCase(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            return PortalFlag.DEFAULT_NETWORK;
        }
        return PortalFlag.CUSTOM_NETWORK;
    }

    @Override
    protected TwoTuple<String, Object> getNewConfigPair(TwoTuple<String, Object> oldPair) {
        if (!CONFIG_CONVERSIONS.containsKey(oldPair.getFirstValue())) {
            return oldPair;
        }
        String newKey = CONFIG_CONVERSIONS.get(oldPair.getFirstValue());

        if (newKey == null) {
            return null;
        }

        return new TwoTuple<>(newKey, oldPair.getSecondValue());
    }

    /**
     * Loads the necessary config conversion for migrating
     */
    private void loadConfigConversions() {
        CONFIG_CONVERSIONS = new HashMap<>();
        FileHelper.readInternalFileToMap("/migration/config-migrations-1_0_14.properties", CONFIG_CONVERSIONS);
    }

}
