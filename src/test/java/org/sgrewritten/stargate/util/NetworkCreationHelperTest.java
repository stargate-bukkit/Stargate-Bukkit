package org.sgrewritten.stargate.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class NetworkCreationHelperTest {

    private static Map<String, TwoTuple<PortalFlag, String>> interpretNameTest;
    private static Map<String, PortalFlag> insertNameRelatedFlagsTest;
    private static Map<String, String> parseNetworkNameTest;

    private static PlayerMock player;
    private static RegistryAPI registry;

    @BeforeAll
    static void setup() {
        ServerMock server = MockBukkit.mock();
        player = new PlayerMock(server, "playerName");
        String invalidPlayerName = "invalid";
        server.addPlayer(player);
        registry = new EmptyRegistry();
        String name = "name";

        interpretNameTest = new HashMap<>();
        interpretNameTest.put(HighlightingStyle.CURLY_BRACKETS.getHighlightedName(name), new TwoTuple<>(PortalFlag.PERSONAL_NETWORK, name));
        interpretNameTest.put(HighlightingStyle.SQUARE_BRACKETS.getHighlightedName(name), new TwoTuple<>(PortalFlag.FANCY_INTER_SERVER, name));
        interpretNameTest.put(name, new TwoTuple<>(null, name));
        interpretNameTest.put(HighlightingStyle.CURLY_BRACKETS.getHighlightedName(player.getName()), new TwoTuple<>(null, player.getName()));
        interpretNameTest.put(invalidPlayerName, new TwoTuple<>(null, invalidPlayerName));
        interpretNameTest.put(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK), new TwoTuple<>(null, ""));

        insertNameRelatedFlagsTest = new HashMap<>();
        insertNameRelatedFlagsTest.put(HighlightingStyle.CURLY_BRACKETS.getHighlightedName(name), PortalFlag.PERSONAL_NETWORK);
        insertNameRelatedFlagsTest.put(HighlightingStyle.SQUARE_BRACKETS.getHighlightedName(name), PortalFlag.FANCY_INTER_SERVER);

        parseNetworkNameTest = new HashMap<>();
        parseNetworkNameTest.put(HighlightingStyle.CURLY_BRACKETS.getHighlightedName(player.getName()), player.getUniqueId().toString());
        parseNetworkNameTest.put(HighlightingStyle.CURLY_BRACKETS.getHighlightedName(invalidPlayerName),
                Bukkit.getOfflinePlayer(invalidPlayerName).getUniqueId().toString());
        parseNetworkNameTest.put(HighlightingStyle.SQUARE_BRACKETS.getHighlightedName(name), name);
        parseNetworkNameTest.put(HighlightingStyle.LESSER_GREATER_THAN.getHighlightedName(name), name);
        parseNetworkNameTest.put(HighlightingStyle.ROUNDED_BRACKETS.getHighlightedName(name), name);
        parseNetworkNameTest.put(HighlightingStyle.MINUS_SIGN.getHighlightedName(name), name);

    }

    @Test
    void interpretNameTest() {
        for (String expectedName : interpretNameTest.keySet()) {
            TwoTuple<PortalFlag, String> data = interpretNameTest.get(expectedName);
            Set<PortalFlag> flags = EnumSet.noneOf(PortalFlag.class);
            PortalFlag flag = data.getFirstValue();
            if (flag != null) {
                flags.add(flag);
            }
            String resultingName = NetworkCreationHelper.interpretNetworkName(data.getSecondValue(), flags, player, registry);
            Assertions.assertEquals(expectedName, resultingName);
        }
    }

    @Test
    void getFlagsTest() {
        for (String nameToTest : insertNameRelatedFlagsTest.keySet()) {
            List<PortalFlag> flags = NetworkCreationHelper.getNameRelatedFlags(nameToTest);
            PortalFlag expectedFlag = insertNameRelatedFlagsTest.get(nameToTest);
            PortalFlag resultFlag = null;
            if (flags.size() > 0) {
                resultFlag = flags.get(0);
            }
            Assertions.assertTrue(flags.contains(expectedFlag), String.format("Expected flag %s, got flag %s. ", expectedFlag.toString(), (resultFlag == null) ? "null" : resultFlag.toString()));
        }
    }

    @Test
    void parseNameTest() {
        for (String nameToTest : parseNetworkNameTest.keySet()) {
            try {
                String result = NetworkCreationHelper.parseNetworkNameName(nameToTest);
                Assertions.assertEquals(parseNetworkNameTest.get(nameToTest), result);
            } catch (NameErrorException ignored) {
            }
        }
    }

}
