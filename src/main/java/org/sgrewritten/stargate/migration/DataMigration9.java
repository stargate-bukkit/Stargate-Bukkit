package org.sgrewritten.stargate.migration;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.property.PortalValidity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DataMigration9 extends DataMigration {
    private Properties configConversions = loadConfigConversions("/migration/config-migrations-9.properties");

    @Override
    public void run(@NotNull SQLDatabaseAPI base, StargateAPI stargateAPI) {

    }

    @Override
    public int getConfigVersion() {
        return 9;
    }

    @Override
    protected TwoTuple<String, Object> getNewConfigPair(TwoTuple<String, Object> oldPair) {
        if (!configConversions.contains(oldPair.getFirstValue())) {
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
}
