package org.sgrewritten.stargate.migration;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.util.FileHelper;
import org.sgrewritten.stargate.util.database.DatabaseHelper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

public class DataMigration_1_0_14 extends DataMigration {
    private static HashMap<String, String> CONFIG_CONVERSIONS;

    public DataMigration_1_0_14() {
        if (CONFIG_CONVERSIONS == null) {
            loadConfigConversions();
        }
    }

    @Override
    public void run(@NotNull SQLDatabaseAPI database) {
        boolean isInterver = ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)
                && ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE);
        TableNameConfiguration nameConfiguration = DatabaseHelper.getTableNameConfiguration(ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE));
        try {
            new SQLDatabaseMigrator(database, nameConfiguration, new File("/migration/database/alpha-1_0_0_14"), isInterver).run();;
        } catch (SQLException | IOException e) {
            Stargate.log(e);
        }
        
        try {
            addLackingNetworkFlags(database);
        } catch (SQLException e) {
            Stargate.log(e);
        }
    }

    @Override
    public int getConfigVersion() {
        return 7;
    }
    
    private void addLackingNetworkFlags(@NotNull SQLDatabaseAPI database) throws SQLException {
        Connection connection = null;
        try {
            connection = database.getConnection();
        } finally {
            if(connection != null) {
                connection.close();
            }
        }
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
