package org.sgrewritten.stargate.migration;

import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.util.FileHelper;

import java.util.HashMap;

public class DataMigration_1_0_12 extends DataMigration {
    private static HashMap<String, String> CONFIG_CONVERSIONS;

    public DataMigration_1_0_12() {
        if (CONFIG_CONVERSIONS == null) {
            loadConfigConversions();
        }
    }

    @Override
    public void run() {
    }

    @Override
    public int getConfigVersion() {
        return 7;
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
        FileHelper.readInternalFileToMap("/migration/config-migrations-1_0_12.properties", CONFIG_CONVERSIONS);
    }
}
