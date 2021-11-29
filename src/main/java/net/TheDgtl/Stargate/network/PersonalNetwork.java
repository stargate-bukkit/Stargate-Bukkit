package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.network.portal.NameSurround;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PersonalNetwork extends Network {
    private final String playerName;

    public PersonalNetwork(UUID id, Database database, SQLQueryGenerator sqlMaker) throws NameError {
        super(id.toString(), database, sqlMaker);
        playerName = Bukkit.getOfflinePlayer(id).getName();
    }

    @Override
    public String concatName() {
        return NameSurround.NETWORK.getSurround(playerName);
    }
}
