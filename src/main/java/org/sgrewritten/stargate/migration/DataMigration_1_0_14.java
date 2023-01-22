package org.sgrewritten.stargate.migration;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.database.DatabaseDriver;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.SQLQuery;
import org.sgrewritten.stargate.database.SQLQueryHandler;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.util.ExceptionHelper;
import org.sgrewritten.stargate.util.FileHelper;
import org.sgrewritten.stargate.util.database.DatabaseHelper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataMigration_1_0_14 extends DataMigration {
    private static HashMap<String, String> CONFIG_CONVERSIONS;

    public DataMigration_1_0_14() {
        if (CONFIG_CONVERSIONS == null) {
            loadConfigConversions();
        }
    }

    @Override
    public void run(@NotNull SQLDatabaseAPI database) {
        boolean isInterserver = ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)
                && ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE);
        TableNameConfiguration nameConfiguration = DatabaseHelper.getTableNameConfiguration(ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE));
        try {
            new SQLDatabaseMigrator(database, nameConfiguration, new File("/migration/database/alpha-1_0_0_14"), isInterserver).run();;
        } catch (SQLException | IOException e) {
            Stargate.log(e);
        }
        
        try {
            addLackingNetworkFlags(database, StorageType.LOCAL, nameConfiguration);
            if(isInterserver) {
                addLackingNetworkFlags(database, StorageType.INTER_SERVER, nameConfiguration);
            }
        } catch (SQLException e) {
            Stargate.log(e);
        }
        
        changeDefaultNetworkID(database,nameConfiguration,isInterserver);
    }

    private void changeDefaultNetworkID(@NotNull SQLDatabaseAPI database, TableNameConfiguration nameConfiguration, boolean isInterserver) {
        try {
            runChangeDefaultNetworkIDStatement(database,nameConfiguration, StorageType.LOCAL);
            if(isInterserver) {
                runChangeDefaultNetworkIDStatement(database,nameConfiguration, StorageType.INTER_SERVER);
            }
        } catch (SQLException e) {
            Stargate.log(e);
        }
    }
    
    private void runChangeDefaultNetworkIDStatement(SQLDatabaseAPI database, TableNameConfiguration nameConfiguration,
            StorageType type) throws SQLException {
        Connection connection = null;
        try {
            connection = database.getConnection();
            SQLQuery query = type == StorageType.LOCAL ? SQLQuery.UPDATE_NETWORK_NAME
                    : SQLQuery.UPDATE_INTER_NETWORK_NAME;
            String queryString = nameConfiguration
                    .replaceKnownTableNames(SQLQueryHandler.getQuery(query, database.getDriver()));
            PreparedStatement statement = connection.prepareStatement(queryString);
            statement.setString(1, LocalNetwork.DEFAULT_NET_ID);
            statement.setString(2, ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK));
            statement.execute();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Override
    public int getConfigVersion() {
        return 7;
    }
    
    private void addLackingNetworkFlags(@NotNull SQLDatabaseAPI database,StorageType storageType,TableNameConfiguration nameConfiguration ) throws SQLException {
        Connection connection = null;
        try {
            connection = database.getConnection();
            String view = storageType == StorageType.LOCAL ? nameConfiguration.getPortalViewName() : nameConfiguration.getInterPortalViewName();
            Map<TwoTuple<String,String>,PortalFlag> portalsLackingFlagsMap = getLackingNetworkFlags(connection,view);
            insertNetworkFlags(connection,portalsLackingFlagsMap,nameConfiguration,storageType, database.getDriver());
        } finally {
            if(connection != null) {
                connection.close();
            }
        }
    }

    private void insertNetworkFlags(Connection connection, Map<TwoTuple<String, String>, PortalFlag> portalsLackingFlagsMap, TableNameConfiguration nameConfiguration, StorageType type, DatabaseDriver driver) throws SQLException {
        SQLQuery query = type == StorageType.LOCAL ? SQLQuery.INSERT_PORTAL_FLAG_RELATION : SQLQuery.INSERT_INTER_PORTAL_FLAG_RELATION;
        String queryString = nameConfiguration.replaceKnownTableNames(SQLQueryHandler.getQuery(query,driver));
        for(TwoTuple<String, String> key : portalsLackingFlagsMap.keySet()) {
            PortalFlag flag = portalsLackingFlagsMap.get(key);
            PreparedStatement statement = connection.prepareStatement(queryString);
            statement.setString(1, key.getFirstValue());
            statement.setString(2, key.getSecondValue());
            statement.setString(3, String.valueOf(flag.getCharacterRepresentation()));
            statement.execute();
            statement.close();
        }
    }

    private Map<TwoTuple<String,String>,PortalFlag> getLackingNetworkFlags(Connection connection, String view) throws SQLException {
        Map<TwoTuple<String,String>,PortalFlag> output = new HashMap<>();
        PreparedStatement statement = connection.prepareStatement("SELECT name,network,flags FROM " + view + ";");
        ResultSet resultSet = statement.executeQuery();
        if(resultSet == null) {
            statement.close();
            return output;
        }
        while(resultSet.next()) {
            if(NetworkType.getNetworkTypeFromFlags(PortalFlag.parseFlags(resultSet.getString("flags"))) == null) {
                PortalFlag flag = determineNetworkFlagFromNetworkName(resultSet.getString("network"));
                output.put(new TwoTuple<>(resultSet.getString("name"),resultSet.getString("network")), flag);
            }
        }
        return output;
    }
    
    private PortalFlag determineNetworkFlagFromNetworkName(String networkName) {
        if(ExceptionHelper.doesNotThrow(IllegalArgumentException.class,() -> UUID.fromString(networkName))) {
            return PortalFlag.PERSONAL_NETWORK;
        }
        if(networkName.toLowerCase().equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK).toLowerCase())) {
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

    private void loadConfigConversions() {
        CONFIG_CONVERSIONS = new HashMap<>();
        FileHelper.readInternalFileToMap("/migration/config-migrations-1_0_14.properties", CONFIG_CONVERSIONS);
    }
}
