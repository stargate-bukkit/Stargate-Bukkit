package org.sgrewritten.stargate.migration;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.property.PortalValidity;
import org.sgrewritten.stargate.util.FileHelper;
import org.sgrewritten.stargate.util.database.DatabaseHelper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class DataMigration9 extends DataMigration {
    private final Properties configConversions = loadConfigConversions("/migration/config-migrations-9.properties");

    @Override
    public void run(@NotNull SQLDatabaseAPI database, StargateAPI stargateAPI) {
        TableNameConfiguration tableNameConfiguration = DatabaseHelper.getTableNameConfiguration(ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE));
        boolean isInterServer = ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)
                && ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE);
        try {
            if (isInterServer) {
                runChangeWorldNameToUUID(database, tableNameConfiguration, StorageType.INTER_SERVER);
            }
            runChangeWorldNameToUUID(database, tableNameConfiguration, StorageType.LOCAL);
        } catch (SQLException | IOException e) {
            Stargate.log(e);
        }
    }

    @Override
    public int getConfigVersion() {
        return 9;
    }

    @Override
    protected TwoTuple<String, Object> getNewConfigPair(TwoTuple<String, Object> oldPair) {
        if (!configConversions.containsKey(oldPair.getFirstValue())) {
            return oldPair;
        }
        String newKey = configConversions.getProperty(oldPair.getFirstValue());
        if (oldPair.getFirstValue().equals("checkPortalValidity")) {
            return new TwoTuple<>(newKey, (((boolean) oldPair.getSecondValue()) ? PortalValidity.REMOVE : PortalValidity.IGNORE).toString());
        }
        return new TwoTuple<>(newKey, oldPair.getSecondValue());
    }

    @Override
    public String getVersionFrom() {
        return "1.0.0.15";
    }

    @Override
    public String getVersionTo() {
        return "1.0.0.16";
    }

    private static Properties loadConfigConversions(String file) {
        try (InputStream inputStream = Stargate.class.getResourceAsStream(file)) {
            Properties output = new Properties();
            output.load(inputStream);
            return output;
        } catch (IOException e) {
            throw new RuntimeException(e);
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
    private void runChangeWorldNameToUUID(SQLDatabaseAPI database, TableNameConfiguration nameConfiguration,
                                          StorageType type) throws SQLException, IOException {
        String directory = "/migration/database/v-9/";
        String fileNameUpdate = directory + (type == StorageType.LOCAL ? "update_world_local.sql" : "update_world_inter_server.sql");
        String fileNameGet = directory + (type == StorageType.LOCAL ? "get_world_local.sql" : "get_world_inter_server.sql");
        try (Connection connection = database.getConnection()) {
            Set<String> worldNames = new HashSet<>();
            try (InputStream stream = Stargate.class.getResourceAsStream(fileNameGet)) {
                String query = nameConfiguration.replaceKnownTableNames(FileHelper.readStreamToString(stream));
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        worldNames.add(resultSet.getString("world"));
                    }
                }
            }
            try (InputStream stream = Stargate.class.getResourceAsStream(fileNameUpdate)) {
                String query = nameConfiguration.replaceKnownTableNames(FileHelper.readStreamToString(stream));
                for (String worldName : worldNames) {
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        continue;
                    }
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, world.getUID().toString());
                        preparedStatement.setString(2, worldName);
                        preparedStatement.execute();
                    }
                }
            }
        }
    }
}