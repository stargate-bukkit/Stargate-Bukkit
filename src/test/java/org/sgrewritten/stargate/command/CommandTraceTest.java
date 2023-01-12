package org.sgrewritten.stargate.command;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.Stargate;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

class CommandTraceTest {

    private @NotNull Stargate plugin;
    private @NotNull ServerMock server;
    private @NotNull PlayerMock sender;
    private CommandTrace traceCommand;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        sender = server.addPlayer();
        System.setProperty("bstats.relocatecheck", "false");
        plugin = MockBukkit.load(Stargate.class);
        traceCommand = new CommandTrace(plugin);
        sender.addAttachment(plugin,"sg.admin.trace",true);
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void onCommand() {
        Assertions.assertTrue(traceCommand.onCommand(sender, null, "", new String[]{""}));
    }
    
    void onCommand_NoPerms() {
        sender.addAttachment(plugin,"sg.admin.trace",false);
        Assertions.assertTrue(traceCommand.onCommand(sender, null, "", new String[]{""}));
        Assertions.assertTrue(sender.nextMessage().contains("Access denied!"));
    }

}
