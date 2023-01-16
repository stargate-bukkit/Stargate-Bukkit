package org.sgrewritten.stargate.command;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.database.property.StoredPropertiesAPI;
import org.sgrewritten.stargate.database.property.StoredProperty;

public class CommandParity implements CommandExecutor {
    
    private @NotNull File repoToMoveFilesTo;
    private @NotNull StoredPropertiesAPI properties;
    private boolean doParityUpgrades;

    CommandParity(@NotNull StoredPropertiesAPI properties, @NotNull File repoToMoveFilesTo, boolean doParityUpgrades){
        this.repoToMoveFilesTo = Objects.requireNonNull(repoToMoveFilesTo);
        this.properties = Objects.requireNonNull(properties);
        this.doParityUpgrades = doParityUpgrades;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        String nagAuthor = properties.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE);
        if(!(sender instanceof ConsoleCommandSender) || nagAuthor == null || !nagAuthor.equals("true")) {
            return false;
        }
        if(!doParityUpgrades) {
            properties.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, "false");
            Stargate.log(Level.INFO," Rejected parity upgrades.");
            return true;
        }
        
        return true;
    }
    
}
