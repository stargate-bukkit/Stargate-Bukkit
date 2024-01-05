package org.sgrewritten.stargate.migration;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.google.common.io.Files;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.database.SQLiteDatabase;
import org.sgrewritten.stargate.network.StargateNetwork;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

class DataMigration1014Test {

    private DataMigration_1_0_14 migration;
    private SQLiteDatabase database;
    private static final File sqlDatabaseFile = new File("src/test/resources", "alpha-1_0_0_11.db");
    private static final File oldSqlDatabaseFile = new File("src/test/resources", "alpha-1_0_0_11.old");
    private static final String UUID_STRING = "9a091c5a-b320-4123-8e5c-867edebc455b";
    private StargateAPIMock stargateAPI;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        migration = new DataMigration_1_0_14();
        Files.copy(sqlDatabaseFile, oldSqlDatabaseFile);
        database = new SQLiteDatabase(sqlDatabaseFile);
        this.stargateAPI = new StargateAPIMock();

        @NotNull ServerMock server = MockBukkit.mock();
        PlayerMock player = new PlayerMock(server, "player", UUID.fromString(UUID_STRING));
        server.addPlayer(player);
    }

    @AfterEach
    void tearDown() {
        Assertions.assertTrue(sqlDatabaseFile.delete());
        Assertions.assertTrue(oldSqlDatabaseFile.renameTo(sqlDatabaseFile));
        MockBukkit.unmock();
    }

    @Test
    void run_CheckFlagFix() throws SQLException {
        migration.run(database, stargateAPI);
        Assertions.assertTrue(portalHasFlag(PortalFlag.CUSTOM_NETWORK, "portal", "network"));
        Assertions.assertTrue(portalHasFlag(PortalFlag.PERSONAL_NETWORK, "portal1", UUID_STRING));
        Assertions.assertTrue(portalHasFlag(PortalFlag.DEFAULT_NETWORK, "portal2", StargateNetwork.DEFAULT_NETWORK_ID));
    }

    private boolean portalHasFlag(PortalFlag flag, String portalName, String networkName) throws SQLException {
        try (Connection connection = database.getConnection()) {
            String query = "SELECT flags FROM PortalView WHERE name = ? AND network = ?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, portalName);
            statement.setString(2, networkName);
            ResultSet resultSet = statement.executeQuery();
            boolean result = PortalFlag.parseFlags(resultSet.getString(1)).contains(flag);
            statement.close();
            return result;
        }
    }
}