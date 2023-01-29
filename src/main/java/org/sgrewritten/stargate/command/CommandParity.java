package org.sgrewritten.stargate.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.database.property.StoredPropertiesAPI;
import org.sgrewritten.stargate.database.property.StoredProperty;

import java.util.Objects;
import java.util.logging.Level;

public class CommandParity implements CommandExecutor {

    private final @NotNull StoredPropertiesAPI properties;
    private final boolean doParityUpgrades;

    CommandParity(@NotNull StoredPropertiesAPI properties, boolean doParityUpgrades) {
        this.properties = Objects.requireNonNull(properties);
        this.doParityUpgrades = doParityUpgrades;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        String nagAuthor = properties.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE);
        if (!(sender instanceof ConsoleCommandSender) || nagAuthor == null || !nagAuthor.equals("true")) {
            return false;
        }
        if (!doParityUpgrades) {
            properties.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, "false");
            Stargate.log(Level.INFO, " Rejected parity upgrades.");
            return true;
        }

        return true;
    }

}
