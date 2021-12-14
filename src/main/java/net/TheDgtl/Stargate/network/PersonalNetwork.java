package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.SQLQueryGenerator;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.logging.Level;

public class PersonalNetwork extends Network {
    private final String playerName;

    public PersonalNetwork(UUID id, Database database, SQLQueryGenerator sqlMaker) throws NameErrorException {
        super(id.toString(), database, sqlMaker);
        Stargate.log(Level.FINE, "Initialized personal network with UUID" + id);
        Stargate.log(Level.FINE, "Matching player name: " + Bukkit.getOfflinePlayer(id).getName());
        playerName = Bukkit.getOfflinePlayer(id).getName();
    }

    @Override
    public String getHighlightedName() {
        return HighlightingStyle.PERSONAL.getHighlightedName(playerName);
    }
}
