package net.TheDgtl.Stargate.network;

import java.util.HashMap;
import java.util.Set;

import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.network.portal.PortalFlag;

public interface NetworkRegistryAPI {
    /**
     * Creates a new network
     *
     * @param networkName <p>The name of the new network</p>
     * @param flags       <p>The flags containing the network's enabled options</p>
     * @throws NameErrorException <p>If the given network name is invalid</p>
     */
    public void createNetwork(String networkName, Set<PortalFlag> flags) throws NameErrorException;

    /**
     * Checks whether the given network name exists
     *
     * @param networkName <p>The network name to check</p>
     * @param isBungee    <p>Whether to look for a BungeeCord network</p>
     * @return <p>True if the network exists</p>
     */
    public boolean networkExists(String networkName, boolean isBungee);
    
    /**
     * Gets the network with the given
     *
     * @param name     <p>The name of the network to get</p>
     * @param isBungee <p>Whether the network is a BungeeCord network</p>
     * @return <p>The network with the given name</p>
     */
    public NetworkAPI getNetwork(String name, boolean isBungee);
    
    /**
     * Gets the map storing all BungeeCord networks
     *
     * @return <p>All BungeeCord networks</p>
     */
    public HashMap<String, InterServerNetwork> getBungeeNetworkList();
    
    /**
     * Gets the map storing all non-BungeeCord networks
     *
     * @return <p>All non-BungeeCord networks</p>
     */
    public HashMap<String, Network> getNetworkList();
}
