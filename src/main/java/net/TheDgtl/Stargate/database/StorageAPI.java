package net.TheDgtl.Stargate.database;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;

import java.sql.SQLException;
import java.util.Set;

public interface StorageAPI {

    /**
     * Loads all portals from storage and adds them to the portal registry
     */
    void loadFromStorage();

    /**
     * "Starts" the inter-server connection by setting this server's portals as online
     */
    void startInterServerConnection();

    /**
     * Creates a new network unassigned to a registry
     *
     * @param networkName <p>The name of the new network</p>
     * @param flags       <p>The flag set used to look for network flags</p>
     * @return The network that was created
     * @throws NameErrorException <p>If the given network name is invalid</p>
     */
    Network createNetwork(String networkName, Set<PortalFlag> flags) throws NameErrorException;

    /**
     * Saves the given portal to storage
     *
     * @param portal     <p>The portal to save</p>
     * @param portalType <p>The type of portal to save</p>
     */
    boolean savePortalToStorage(RealPortal portal, PortalType portalType);

    /**
     * Removes a portal and its associated data from storage
     *
     * @param portal     <p>The portal to remove</p>
     * @param portalType <p>The type of portal to remove</p>
     */
    void removePortalFromStorage(Portal portal, PortalType portalType);

    /**
     * Loads all settings 
     * 
     * @param stargate <p>An instance of stargate</p>
     * @throws SQLException 
     */
    void load(Stargate stargate) throws SQLException;


}
