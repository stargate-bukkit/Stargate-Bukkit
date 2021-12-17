package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.SQLQueryGenerator;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.logging.Level;

/**
 * A network that belongs to a certain player and uses the player's name
 */
public class PersonalNetwork extends Network {
    
    private final String playerName;

    /**
     * Instantiates a new personal network
     *
     * @param uuid           <p>The UUID of the player this network belongs to</p>
     * @param database       <p>The database to use for saving network data</p>
     * @param queryGenerator <p>The generator to use for generating SQL queries</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public PersonalNetwork(UUID uuid, Database database, SQLQueryGenerator queryGenerator, StargateFactory factory) throws NameErrorException {
        super(uuid.toString(), database, queryGenerator, factory);
        Stargate.log(Level.FINE, "Initialized personal network with UUID" + uuid);
        Stargate.log(Level.FINE, "Matching player name: " + Bukkit.getOfflinePlayer(uuid).getName());
        playerName = Bukkit.getOfflinePlayer(uuid).getName();
    }

    @Override
    public String getHighlightedName() {
        return HighlightingStyle.PERSONAL.getHighlightedName(playerName);
    }

}
