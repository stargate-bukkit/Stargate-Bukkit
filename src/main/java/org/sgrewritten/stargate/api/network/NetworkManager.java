package org.sgrewritten.stargate.api.network;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.network.NetworkType;

import java.util.Set;

public interface NetworkManager {

    /**
     * Interprets a networkname and type, then selects it or creates it if it does not already exist
     *
     * @param name              <p> Initial name of the network</p>
     * @param permissionManager <p> A permission manager of the player</p>
     * @param player            <p> The player selecting the network</p>
     * @param flags             <p> flags of a portal this selection or creation comes from</p>
     * @return <p>The network the portal should be connected to</p>
     * @throws TranslatableException <p>If invalid input is given</p>
     */
    Network selectNetwork(String name, PermissionManager permissionManager, OfflinePlayer player, Set<PortalFlag> flags) throws TranslatableException;

    /**
     * Gets the network with the given name, and creates it if it doesn't already exist
     *
     * @param name          <p>The name of the network to get</p>
     * @param type          <p>The type of network to get</p>
     * @param isInterServer <p>Whether or not the network works (or will work) across instances.
     * @return <p>The network the portal should be connected to</p>
     * @throws TranslatableException <p>If the network name is invalid</p>
     */
    Network selectNetwork(String name, NetworkType type, boolean isInterServer) throws TranslatableException;

}
