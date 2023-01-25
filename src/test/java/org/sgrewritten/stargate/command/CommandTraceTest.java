package org.sgrewritten.stargate.command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.command.Command;
import org.bukkit.command.defaults.VersionCommand;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.Stargate;

class CommandTraceTest {

    private @NotNull Stargate plugin;
    private @NotNull ServerMock server;
    private @NotNull PlayerMock sender;
    private CommandTrace traceCommand;
    private Command fakeCommand = new VersionCommand("fake");

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        sender = server.addPlayer();
        System.setProperty("bstats.relocatecheck", "false");
        plugin = MockBukkit.load(Stargate.class);
        traceCommand = new CommandTrace(plugin);
        sender.addAttachment(plugin, "sg.admin.trace", true);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void onCommand() {
        Assertions.assertTrue(traceCommand.onCommand(sender, fakeCommand, "", new String[]{""}));
    }

    @Test
    void onCommand_NoPerms() {
        sender.addAttachment(plugin, "sg.admin.trace", false);
        Assertions.assertTrue(traceCommand.onCommand(sender, fakeCommand, "", new String[]{""}));
        Assertions.assertTrue(sender.nextMessage().contains("Access denied!"));
    }

}
