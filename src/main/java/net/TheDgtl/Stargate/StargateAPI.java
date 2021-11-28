package net.TheDgtl.Stargate;

import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.IPortal;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * An API to facilitate addons and integrations
 *
 * <p>API specs currently being discussed on issue #77</p>
 *
 * @author Thorin
 */
@SuppressWarnings("unused")
public class StargateAPI {

    /**
     * Gets a portal at the given location
     *
     * <p>If a portal's frame, iris or control-blocks have a block at the given location, the portal will be
     * returned.</p>
     *
     * @param portalBlock <p>The location to search for a portal</p>
     * @return <p>A portal, or null if no portal was found</p>
     */
    public IPortal getPortal(Location portalBlock) {
        return Network.getPortal(portalBlock, GateStructureType.values());
    }

    /**
     * Get a portal at the given location, only if the location has the given gate structure type
     *
     * <p>If a portal has a block of the given structure type (control block, frame or iris) at the given location, the
     * portal will be returned.</p>
     *
     * @param portalBlock   <p>The location to search for a portal</p>
     * @param structureType <p>The type of gate structure to look for</p>
     * @return <p>A portal, or null if no portal was found</p>
     */
    public IPortal getPortal(Location portalBlock, GateStructureType structureType) {
        return Network.getPortal(portalBlock, structureType);
    }

    /**
     * Get a portal at the given location, only if the location has one of the given gate structure types
     *
     * <p>If a portal has a block of the given structure type (control block, frame or iris) at the given location, the
     * portal will be returned.</p>
     *
     * @param portalBlock    <p>The location to search for a portal</p>
     * @param structureTypes <p>The types of gate structures to look for</p>
     * @return <p>A portal, or null if no portal was found</p>
     */
    public IPortal getPortal(Location portalBlock, GateStructureType[] structureTypes) {
        return Network.getPortal(portalBlock, structureTypes);
    }

    /**
     * Get a portal in the given network, with the given name
     *
     * @param network    <p>The network to search</p>
     * @param portalName <p>The name of the portal</p>
     * @return <p>A portal, or null if no portal was found</p>
     */
    public IPortal getPortal(Network network, String portalName) {
        return network.getPortal(portalName);
    }

    /**
     * Get a portal across the proxy network, given its name and network
     *
     * @param networkName <p>The name of the network to search</p>
     * @param portalName  <p>The name of the portal to look for</p>
     * @param isBungee    <p>Whether to search for a BungeeCord-connected portal</p>
     * @return <p>A portal, or null if no portal was found</p>
     */
    public IPortal getPortal(String networkName, String portalName, boolean isBungee) {
        Network network = Stargate.factory.getNetwork(networkName, isBungee);
        if (network == null) {
            return null;
        }

        return network.getPortal(portalName);
    }

    /**
     * Gets a network given one of the portals within the network
     *
     * @param portal <p>A portal within the target network</p>
     * @return <p>The network the portal belongs to</p>
     */
    public Network getNetwork(IPortal portal) {
        return portal.getNetwork();
    }

    /**
     * Gets a network across the proxy network
     *
     * @param networkName <p>The name of the network to get</p>
     * @param isBungee    <p>Whether to search for a network across BungeeCord</p>
     * @return <p>The network, or null if no such network was found</p>
     */
    public Network getNetwork(String networkName, boolean isBungee) {
        return Stargate.factory.getNetwork(networkName, isBungee);
    }

    /**
     * Changes the network a portal belongs to
     *
     * <p>Change the network a portal is situated within (intended for non-fixed
     * gates). Note that, if targeting a fixed gate, its destination will also need
     * to be changed.</p>
     *
     * @param portal        <p>The portal to change the network of</p>
     * @param targetNetwork <p>The target network the portal should be moved to</p>
     */
    public void changeNetwork(IPortal portal, Network targetNetwork) {
        portal.setNetwork(targetNetwork);
    }

    /**
     * Creates a new portal
     *
     * @param gate       <p>The gate type to use for the new portal</p>
     * @param location   <p>???</p>
     * @param openFacing <p>???</p>
     */
    public void createPortal(Gate gate, Location location, Vector openFacing) {
        //TODO Currently not implemented
    }

    /**
     * Forces two portals to temporarily connect to each-other
     *
     * <p>Force a connection between two portals. They do not have to be in the same
     * network. Once entered, the portal's destination will be reset.</p>
     *
     * @param target      <p>The portal to change the destination of</p>
     * @param destination <p>The portal's new destination</p>
     */
    public void forceConnect(IPortal target, IPortal destination) {
        target.setOverrideDestination(destination);
    }

}
