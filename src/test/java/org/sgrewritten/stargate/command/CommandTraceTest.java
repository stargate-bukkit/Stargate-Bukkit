package org.sgrewritten.stargate.command;

import be.seeseemelk.mockbukkit.MockBukkitInject;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.command.Command;
import org.bukkit.command.defaults.VersionCommand;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateExtension;
import org.sgrewritten.stargate.StargateInject;

@ExtendWith(StargateExtension.class)
class CommandTraceTest {

    @MockBukkitInject
    ServerMock server;
    @StargateInject
    private @NotNull Stargate plugin;
    private @NotNull PlayerMock sender;
    private CommandTrace traceCommand;
    private final Command fakeCommand = new VersionCommand("fake");

    @BeforeEach
    void setUp() {
        sender = server.addPlayer();
        traceCommand = new CommandTrace(plugin);
        sender.addAttachment(plugin, "sg.admin.trace", true);
    }

    @Test
    void onCommand() {
        Assertions.assertTrue(traceCommand.onCommand(sender, fakeCommand, "", new String[]{""}));
    }

    @Test
    void onCommandNoPermissions() {
        sender.addAttachment(plugin, "sg.admin.trace", false);
        Assertions.assertTrue(traceCommand.onCommand(sender, fakeCommand, "", new String[]{""}));
        String nextMessage = sender.nextMessage();
        Assertions.assertNotNull(nextMessage);
        Assertions.assertTrue(nextMessage.contains("Access denied!"));
    }

}
