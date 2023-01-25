package org.sgrewritten.stargate.util;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.InterServerNetwork;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.NetworkType;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

class TranslatableMessageFormatterTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    @Test
    void formatUnimplementedConflictMessage() throws NameLengthException, InvalidNameException, UnimplementedFlagException {
        System.setProperty("bstats.relocatecheck", "false");
        Stargate plugin = MockBukkit.load(Stargate.class);
        Player player = server.addPlayer("network1");
        String expectedUnimplementedConflictMessage = "§e[Stargate] §fThe network1 Personal network has been temporarily separated from the network1 Custom network, but will soon be merged.";
        Assertions.assertEquals(expectedUnimplementedConflictMessage,
                TranslatableMessageFormatter.formatUnimplementedConflictMessage(
                        new InterServerNetwork(player.getUniqueId().toString(), NetworkType.PERSONAL),
                        new LocalNetwork("network1", NetworkType.CUSTOM), plugin.getLanguageManager()));
    }

    @Test
    void formatUnimplementedConflictMessage_NullCheck() throws NameLengthException, InvalidNameException, UnimplementedFlagException {
        System.setProperty("bstats.relocatecheck", "false");
        Stargate plugin = MockBukkit.load(Stargate.class);
        Player player = server.addPlayer("network1");
        String msg = TranslatableMessageFormatter.formatUnimplementedConflictMessage(
                new InterServerNetwork(player.getUniqueId().toString(), NetworkType.PERSONAL),
                null, plugin.getLanguageManager());
        Assertions.assertFalse(msg.contains("%"));
    }
}
