package net.TheDgtl.Stargate;

import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.IPortal;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * An API to facilitate addons and integrations.
 *
 * @author Thorin
 */
public class StargateAPI {
    /*
     * API specs currently being discussed on issue #77
     */

    /**
     * Search for a portal via a specified position.
     *
     * @param Location portalBlock (the specified position)
     * @return If found, the portal; else, null.
     */
    public IPortal getPortal(Location portalBlock) {
        return Network.getPortal(portalBlock, GateStructureType.values());
    }

    /**
     * Search for a portal via a specified portal part and a specified position.
     *
     * @param Location          portalBlock (the specified position)
     * @param GateStructureType structure (the type of part of the portal)
     * @return If found, the portal; else, null.
     */
    public IPortal getPortal(Location portalBlock, GateStructureType structure) {
        return Network.getPortal(portalBlock, structure);
    }

    /**
     * Search for a portal via specified portal parts and a specified position.
     *
     * @param Location            portalBlock (the specified position)
     * @param GateStructureType[] structures (the types of parts of the portal)
     * @return If found, the portal; else, null.
     */
    public IPortal getPortal(Location portalBlock, GateStructureType[] structures) {
        return Network.getPortal(portalBlock, structures);
    }

    /**
     * Search for a portal based its name and network.
     *
     * @param Network net (The portal's network)
     * @param String  portalName (The portal's name)
     * @return If found, the portal; else, null.
     */
    public IPortal getPortal(Network net, String portalName) {
        return net.getPortal(portalName);
    }

    /**
     * Search for a portal across the proxy based on its name and network.
     *
     * @param String  netName (The portal's network)
     * @param String  portalName (The portal's name)
     * @param boolean isBungee (The portal's scope)
     * @return If found, the portal; else, null.
     */
    public IPortal getPortal(String netName, String portalName, boolean isBungee) {
        Network net = Stargate.factory.getNetwork(netName, isBungee);
        if (net == null)
            return null;

        return net.getPortal(portalName);
    }

    /**
     * Checks a portal's network.
     *
     * @param IPortal portal (The portal)
     * @return The network within which the portal exists.
     */
    public Network getNetwork(IPortal portal) {
        return portal.getNetwork();
    }

    /**
     * Checks a portal's network, from across the proxy.
     *
     * @param String  networkName
     * @param boolean isBungee
     * @return The network found / otherwise null
     */
    public Network getNetwork(String networkName, boolean isBungee) {
        return Stargate.factory.getNetwork(networkName, isBungee);
    }

    /**
     * Change the network a portal is situated within (intended for non-fixed
     * gates). Note that, if targeting a fixed gate, its destination will also need
     * to be changed.
     *
     * @param IPortal portal
     * @param Network targetNet
     */
    public void changeNetwork(IPortal portal, Network targetNet) {
        portal.setNetwork(targetNet);
    }

    /**
     * TODO Currently not implemented
     *
     * @param config
     * @param location
     * @param openFacing
     */
    public void createPortal(Gate config, Location location, Vector openFacing) {

    }

    /**
     * Force a connection between two portals. They do not have to be in the same
     * network. Once entered, the portal's destination will be reset.
     *
     * @param IPortal     target (the portal that will have its destination
     *                    changed).
     * @param IPortal     destination (where that destination will be changed to).
     * @param destination
     */
    public void forceConnect(IPortal target, IPortal destination) {
        target.setOverrideDestination(destination);
    }

    public class InterFacePortal {
        private IPortal portal;
    }

}
