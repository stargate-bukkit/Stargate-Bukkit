package org.sgrewritten.stargate.command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.command.Command;
import org.bukkit.command.defaults.VersionCommand;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.database.property.PropertiesDatabase;
import org.sgrewritten.stargate.database.property.StoredProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

class CommandParityTest {

    private @NotNull ServerMock server;
    private @NotNull MockPlugin plugin;
    private @NotNull PlayerMock player;
    private CommandParity command;
    private PropertiesDatabase properties;
    private @NotNull ConsoleCommandSenderMock console;
    private Command fakeCommand = new VersionCommand("fake");

    @BeforeEach
    void setUp() throws FileNotFoundException, IOException {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        console = server.getConsoleSender();
        player = server.addPlayer();
        properties = new PropertiesDatabase(new File(plugin.getDataFolder(), "test.properties"));
        command = new CommandParity(properties, plugin.getDataFolder().getParentFile(), true);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void onCommand_NotConsole() {
        Assertions.assertFalse(command.onCommand(player, fakeCommand, "", new String[]{""}));
    }

    @Test
    void onCommand_ParityNotSet() {
        Assertions.assertFalse(command.onCommand(console, fakeCommand, "", new String[]{""}));
    }

    @Test
    void onCommand_ParityFalse() {
        properties.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, "false");
        Assertions.assertFalse(command.onCommand(console, fakeCommand, "", new String[]{""}));
    }

    @Test
    void onCommand_ParityTrue() {
        properties.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, "true");
        Assertions.assertTrue(command.onCommand(console, fakeCommand, "", new String[]{""}));
    }
    
}
