package org.sgrewritten.stargate.network;

import org.bukkit.Bukkit;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;
import org.sgrewritten.stargate.util.NetworkCreationHelper;

import java.util.UUID;
import java.util.logging.Level;

/**
 * A network that belongs to a certain player and uses the player's name
 */
public class PersonalNetwork extends LocalNetwork {

    private final String playerName;

    /**
     * Instantiates a new personal network
     *
     * @param uuid <p>The UUID of the player this network belongs to</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public PersonalNetwork(UUID uuid) throws NameErrorException {
        super(uuid.toString());
        Stargate.log(Level.FINER, "Initialized personal network with UUID " + uuid);
        Stargate.log(Level.FINER, "Matching player name: " + Bukkit.getOfflinePlayer(uuid).getName());
        String possiblePlayerName = Bukkit.getOfflinePlayer(uuid).getName();
        if (possiblePlayerName != null
                && (NetworkCreationHelper.getDefaultNamesTaken().contains(possiblePlayerName.toLowerCase())
                || NetworkCreationHelper.getBannedNames().contains(possiblePlayerName.toLowerCase()))) {
            possiblePlayerName = uuid.toString().split("-")[0];
        }
        playerName = possiblePlayerName;
        //TODO: The player name isn't accessible, making all the above code unnecessary
    }

    @Override
    public HighlightingStyle getHighlightingStyle() {
        return HighlightingStyle.PERSONAL;
    }

}
