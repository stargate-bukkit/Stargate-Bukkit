package org.sgrewritten.stargate.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.network.RegistryMock;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import java.util.HashSet;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.fail;

class NetworkCreationHelperTest {
    private PlayerMock player;
    private RegistryAPI registry;
    private PermissionManager permissionManager;
    private String[] emptyNames;
    private ServerMock server;

    private static final String CENTRAL = "central";
    private static final String NETWORK1 = "network1";
    private static final String NETWORK2 = "network2";
    private static final String INVALID_NAME = "invalid";
    private static final String NAME = "name";
    private static final String PLAYER_NAME = "playerName";
    private MockPlugin plugin;

    @BeforeEach
    void setup() {
        server = MockBukkit.mock();
        player = new PlayerMock(server, PLAYER_NAME);
        plugin = MockBukkit.createMockPlugin();
        permissionManager = new StargatePermissionManager(player, new LanguageManagerMock());
        server.addPlayer(player);
        server.addPlayer("central");
        registry = new RegistryMock();
        emptyNames = new String[]{"", " ", "  "};
    }

    @AfterEach
    void teardown() {
        MockBukkit.unmock();
    }

    @Test
    void emptyDefinitionTest() throws TranslatableException {
        Stargate.log(Level.FINE, "############### EMPTY TEST #############");
        Assertions.assertNotNull(player.getUniqueId());
        for (String emptyName : emptyNames) {
            Network personalNetwork = NetworkCreationHelper.selectNetwork(emptyName, permissionManager, player, new HashSet<>(), registry);
            Assertions.assertEquals(NetworkType.PERSONAL, personalNetwork.getType());
            Assertions.assertEquals(player.getName(), personalNetwork.getName());
            Assertions.assertTrue(registry.networkExists(player.getUniqueId().toString(), false));
        }
        player.addAttachment(plugin, "sg.create.network.default", true);
        for (String emptyName : emptyNames) {
            Network defaultNetwork = NetworkCreationHelper.selectNetwork(emptyName, permissionManager, player, new HashSet<>(), registry);
            Assertions.assertEquals(NetworkType.DEFAULT, defaultNetwork.getType());
            Assertions.assertEquals(CENTRAL, defaultNetwork.getName());
            Assertions.assertTrue(registry.networkExists(LocalNetwork.DEFAULT_NETWORK_ID, false));
        }
    }


    @Test
    void explicitDefinitionTest_Default() throws TranslatableException {
        Network defaultNetwork = NetworkCreationHelper.selectNetwork(NetworkType.DEFAULT.getHighlightingStyle().getHighlightedName(CENTRAL), permissionManager, player, new HashSet<>(), registry);
        Assertions.assertEquals(NetworkType.DEFAULT, defaultNetwork.getType());
        Assertions.assertEquals(CENTRAL, defaultNetwork.getName());
    }

    @Test
    void explicitDefinitionTestDefaultTypeButWrongName() {
        Assertions.assertThrows(InvalidNameException.class, () -> NetworkCreationHelper.selectNetwork(
                NetworkType.DEFAULT.getHighlightingStyle().getHighlightedName(INVALID_NAME), permissionManager, player,
                new HashSet<>(), registry));
    }

    @ParameterizedTest
    @EnumSource
    void explicitDefinitionTestPersonalName(NetworkType networkType) throws TranslatableException {
        Network network1 = NetworkCreationHelper.selectNetwork(NetworkType.PERSONAL.getHighlightingStyle().getHighlightedName(player.getName()), permissionManager, player, new HashSet<>(), registry);
        if (networkType == NetworkType.DEFAULT) {
            Assertions.assertThrows(TranslatableException.class, () -> NetworkCreationHelper.selectNetwork(networkType.getHighlightingStyle().getHighlightedName(player.getName()), permissionManager, player, new HashSet<>(), registry));
        } else if (networkType == NetworkType.TERMINAL) {
            Assertions.assertThrows(UnimplementedFlagException.class, () -> NetworkCreationHelper.selectNetwork(networkType.getHighlightingStyle().getHighlightedName(player.getName()), permissionManager, player, new HashSet<>(), registry));
        } else if (networkType == NetworkType.CUSTOM) {
            Assertions.assertThrows(NameConflictException.class, () -> NetworkCreationHelper.selectNetwork(networkType.getHighlightingStyle().getHighlightedName(player.getName()), permissionManager, player, new HashSet<>(), registry));
        } else {
            Network network2 = NetworkCreationHelper.selectNetwork(networkType.getHighlightingStyle().getHighlightedName(player.getName()), permissionManager, player, new HashSet<>(), registry);
            Assertions.assertEquals(network1, network2);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {PLAYER_NAME, CENTRAL})
    void explicitDefinitionTestPersonal(String name) throws TranslatableException {
        String highlightedPlayerName = NetworkType.PERSONAL.getHighlightingStyle().getHighlightedName(name);
        Network personalNetwork = NetworkCreationHelper.selectNetwork(highlightedPlayerName, permissionManager, player, new HashSet<>(), registry);
        Assertions.assertEquals(NetworkType.PERSONAL, personalNetwork.getType());
        if (name.equals(CENTRAL)) {
            // Name should not be "central", as it conflicts with the default network name
            Assertions.assertNotEquals(CENTRAL, personalNetwork.getName());
            Assertions.assertNotNull(personalNetwork.getName());
        } else {
            Assertions.assertEquals(name, personalNetwork.getName());
        }
    }

    @Test
    void explicitDefinitionTestCustom() throws TranslatableException {
        String customNetworkName = NetworkType.CUSTOM.getHighlightingStyle().getHighlightedName(NAME);
        Network customNetwork = NetworkCreationHelper.selectNetwork(customNetworkName, permissionManager, player, new HashSet<>(), registry);
        Assertions.assertEquals(NetworkType.CUSTOM, customNetwork.getType());
        Assertions.assertEquals(NAME, customNetwork.getName());

        Network changedNameFromDefault = NetworkCreationHelper.selectNetwork(NetworkType.CUSTOM.getHighlightingStyle().getHighlightedName(CENTRAL), permissionManager, player, new HashSet<>(), registry);
        Assertions.assertEquals(NetworkType.CUSTOM, changedNameFromDefault.getType());
        Assertions.assertEquals(CENTRAL + 1, changedNameFromDefault.getName());
    }

    @Test
    void implicitDefinitionTest() throws TranslatableException {
        Stargate.log(Level.FINE, "############### IMPLICIT TEST #############");
        String name = "name";
        HighlightingStyle[] values = HighlightingStyle.values();
        // Affirm default network exist
        Network defaultNetwork = NetworkCreationHelper.selectNetwork(NetworkType.DEFAULT.getHighlightingStyle().getHighlightedName(CENTRAL), permissionManager, player, new HashSet<>(), registry);
        for (int i = 0; i < values.length; i++) {
            HighlightingStyle style = values[i];
            if (NetworkType.styleGivesNetworkType(style)) {
                continue;
            }
            String personalNetworkName = name + i + "p";
            String explicitPersonalNetworkName = NetworkType.PERSONAL.getHighlightingStyle().getHighlightedName(personalNetworkName);
            String implicitPersonalNetworkName = style.getHighlightedName(personalNetworkName);
            Player player = server.addPlayer(personalNetworkName);
            // Create a personal network explicitly, then fetch it implicitly
            Network explicitPersonalNetwork = NetworkCreationHelper.selectNetwork(explicitPersonalNetworkName, new StargatePermissionManager(player, new LanguageManagerMock()), player, new HashSet<>(), registry);
            Network implicitPersonalNetwork = NetworkCreationHelper.selectNetwork(implicitPersonalNetworkName, new StargatePermissionManager(player, new LanguageManagerMock()), player, new HashSet<>(), registry);
            Assertions.assertEquals(explicitPersonalNetwork, implicitPersonalNetwork);
            Assertions.assertEquals(NetworkType.PERSONAL, implicitPersonalNetwork.getType());
            Assertions.assertEquals(personalNetworkName, implicitPersonalNetwork.getName());

            Network implicitDefaultNetwork = NetworkCreationHelper.selectNetwork(CENTRAL, permissionManager, player, new HashSet<>(), registry);
            Assertions.assertEquals(defaultNetwork, implicitDefaultNetwork);
            Assertions.assertEquals(NetworkType.DEFAULT, implicitDefaultNetwork.getType());
            Assertions.assertEquals(CENTRAL, implicitDefaultNetwork.getName());

            String customName = name + i + "c";
            String explicitCustomNetworkName = NetworkType.CUSTOM.getHighlightingStyle().getHighlightedName(customName);
            String implicitCustomNetworkName = style.getHighlightedName(customName);
            Network explicitCustomNetwork = NetworkCreationHelper.selectNetwork(explicitCustomNetworkName, permissionManager, player, new HashSet<>(), registry);
            Network implicitCustomNetwork = NetworkCreationHelper.selectNetwork(implicitCustomNetworkName, permissionManager, player, new HashSet<>(), registry);
            Assertions.assertEquals(explicitCustomNetwork, implicitCustomNetwork);
            Assertions.assertEquals(NetworkType.CUSTOM, implicitCustomNetwork.getType());
            Assertions.assertEquals(customName, implicitCustomNetwork.getName());

            String customNameV2 = name + i + "c2";
            String implicitCustomNetworkNameV2 = style.getHighlightedName(customNameV2);
            Network customNetworkV2 = NetworkCreationHelper.selectNetwork(implicitCustomNetworkNameV2, permissionManager, player, new HashSet<>(), registry);
            Assertions.assertEquals(NetworkType.CUSTOM, customNetworkV2.getType());
            Assertions.assertEquals(customNameV2, customNetworkV2.getName());
        }
    }

    @ParameterizedTest
    @EnumSource(value = NetworkType.class, names = {"CUSTOM", "PERSONAL"})
    void isInterServerToLocalConflictTest(NetworkType type) throws NameLengthException, NameConflictException, InvalidNameException, UnimplementedFlagException {

        String network1id = NETWORK1;
        String network2id = NETWORK2;
        String invertedNetwork2id;
        if (type == NetworkType.PERSONAL) {
            network1id = server.addPlayer(NETWORK1).getUniqueId().toString();
            network2id = server.addPlayer(NETWORK2).getUniqueId().toString();
            invertedNetwork2id = NETWORK2;
        } else {
            server.addPlayer(NETWORK2);
            Player player1 = server.getPlayer(NETWORK2);
            if (player1 != null) {
                invertedNetwork2id = player1.getUniqueId().toString();
            } else {
                fail();
                throw new IllegalStateException("Test cannot continue");
            }
        }
        Network local1 = registry.createNetwork(network1id, type, false, false);
        Network inter1 = registry.createNetwork(network1id, type, true, false);

        Assertions.assertNotNull(NetworkCreationHelper.getInterserverLocalConflict(inter1, registry));
        Assertions.assertNotNull(NetworkCreationHelper.getInterserverLocalConflict(local1, registry));

        Network inter2 = registry.createNetwork(network2id, type, true, false);

        Assertions.assertNull(NetworkCreationHelper.getInterserverLocalConflict(inter2, registry));
        // Assert there will be a conflict when a network of different type is being created
        Network local2 = registry.createNetwork(invertedNetwork2id, type == NetworkType.PERSONAL ? NetworkType.CUSTOM : NetworkType.PERSONAL,
                false, false);
        Assertions.assertNotNull(NetworkCreationHelper.getInterserverLocalConflict(inter2, registry));
        Assertions.assertNotNull(NetworkCreationHelper.getInterserverLocalConflict(local2, registry));

    }
}
