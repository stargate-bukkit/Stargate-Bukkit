package org.sgrewritten.stargate.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.FakeStargate;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.manager.PermissionManager;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import java.util.HashSet;

class NetworkCreationHelperTest {
    private static PlayerMock player;
    private static RegistryAPI registry;
    private static PermissionManager permissionManager;
    private static String[] emptyNames;
    private static FakeStargate plugin;
    private static ServerMock server;
    
    private static final String CENTRAL = "central";
    private static final String INVALID_NAME = "invalid";
    private static final String NAME = "name";

    @BeforeAll
    static void setup() {
        server = MockBukkit.mock();
        player = new PlayerMock(server, "playerName");
        plugin = (FakeStargate) MockBukkit.load(FakeStargate.class);
        permissionManager = new StargatePermissionManager(player, new FakeLanguageManager());
        server.addPlayer(player);
        registry = new StargateRegistry(new FakeStorage());
        
        emptyNames = new String[]{"", " ", "  "};
    }
    
    @AfterAll
    static void teardown() {
        MockBukkit.unmock();
    }

    @Test
    public void emptyDefinitionTest() throws InvalidNameException, TranslatableException {
        System.out.println("############### EMPTY TEST #############");
        Assertions.assertTrue(player.getUniqueId() != null);
        for(String emptyName : emptyNames) {
            Network personalNetwork = NetworkCreationHelper.selectNetwork(emptyName, permissionManager, player, new HashSet<>(), registry);
            Assertions.assertEquals(NetworkType.PERSONAL, personalNetwork.getType());
            Assertions.assertEquals(player.getName(), personalNetwork.getName());
            Assertions.assertTrue(registry.networkExists(player.getUniqueId().toString(), false));
        }
        player.addAttachment(plugin, "sg.create.network.default", true);
        for(String emptyName : emptyNames) {
            Network defaultNetwork = NetworkCreationHelper.selectNetwork(emptyName, permissionManager, player, new HashSet<>(), registry);
            Assertions.assertEquals(NetworkType.DEFAULT, defaultNetwork.getType());
            Assertions.assertEquals(CENTRAL, defaultNetwork.getName());
            Assertions.assertTrue(registry.networkExists(LocalNetwork.DEFAULT_NET_ID, false));
        }
    }

    
    @Test
    public void explicitDefinitionTest() throws InvalidNameException, TranslatableException{
        System.out.println("############### EXPLICIT TEST #############");
        Network defaultNetwork = NetworkCreationHelper.selectNetwork(NetworkType.DEFAULT.getHighlightingStyle().getHighlightedName(CENTRAL), permissionManager, player, new HashSet<>(), registry);
        Assertions.assertEquals(NetworkType.DEFAULT, defaultNetwork.getType());
        Assertions.assertEquals(CENTRAL, defaultNetwork.getName());

        /*Assertions.assertThrows(NameErrorException.class, () -> {
            NetworkCreationHelper.selectNetwork(NetworkType.DEFAULT.getHighlightingStyle().getHighlightedName(INVALID_NAME), permissionManager, player, new HashSet<>(), registry);
        });
        */
        String highlightedPlayername = NetworkType.PERSONAL.getHighlightingStyle().getHighlightedName(player.getName());
        Network personalNetwork = NetworkCreationHelper.selectNetwork(highlightedPlayername, permissionManager, player, new HashSet<>(), registry);
        Assertions.assertEquals(NetworkType.PERSONAL, personalNetwork.getType());
        Assertions.assertEquals(player.getName(), personalNetwork.getName());
        
        String customNetworkName = NetworkType.CUSTOM.getHighlightingStyle().getHighlightedName(NAME);
        Network customNetwork = NetworkCreationHelper.selectNetwork(customNetworkName, permissionManager, player, new HashSet<>(), registry);
        Assertions.assertEquals(NetworkType.CUSTOM, customNetwork.getType());
        Assertions.assertEquals(NAME, customNetwork.getName());
        
        Network changedNameFromDefault = NetworkCreationHelper.selectNetwork(NetworkType.CUSTOM.getHighlightingStyle().getHighlightedName(CENTRAL), permissionManager, player, new HashSet<>(), registry);
        Assertions.assertEquals(NetworkType.CUSTOM, changedNameFromDefault.getType());
        Assertions.assertEquals(CENTRAL + 1, changedNameFromDefault.getName());
    
    }
    
    @Test
    public void implicitDefinitionTest() throws InvalidNameException, TranslatableException{
        System.out.println("############### IMPLICIT TEST #############");
        String name = "name";
        HighlightingStyle[] values = HighlightingStyle.values();
        // Affirm default network exist
        Network defaultNetwork = NetworkCreationHelper.selectNetwork(NetworkType.DEFAULT.getHighlightingStyle().getHighlightedName(CENTRAL), permissionManager, player, new HashSet<>(), registry);
        for(int i = 0; i < values.length; i++) {
            HighlightingStyle style = values[i];
            if(NetworkType.styleGivesNetworkType(style)) {
                continue;
            }
            String personalNetworkName = name + i + "p";
            String explicitPersonalNetworkName = NetworkType.PERSONAL.getHighlightingStyle().getHighlightedName(personalNetworkName);
            String implicitPersonalNetworkName = style.getHighlightedName(personalNetworkName);
            Player player = server.addPlayer(personalNetworkName);
            // Create a personal network explicitly, then fetch it implicitly
            Network explicitPersonalNetwork = NetworkCreationHelper.selectNetwork(explicitPersonalNetworkName, new StargatePermissionManager(player, new FakeLanguageManager()), player, new HashSet<>(), registry);
            Network implicitPersonalNetwork = NetworkCreationHelper.selectNetwork(implicitPersonalNetworkName, new StargatePermissionManager(player, new FakeLanguageManager()), player, new HashSet<>(), registry);
            Assertions.assertEquals(explicitPersonalNetwork, implicitPersonalNetwork);
            Assertions.assertEquals(NetworkType.PERSONAL,implicitPersonalNetwork.getType());
            Assertions.assertEquals(personalNetworkName, implicitPersonalNetwork.getName());
            
            String implicitDefaultNetworkName = style.getHighlightedName(CENTRAL);
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
}
