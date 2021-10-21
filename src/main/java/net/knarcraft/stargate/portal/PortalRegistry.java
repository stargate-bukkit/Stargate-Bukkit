package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The portal registry keeps track of all registered portals and all their lookup blocks
 */
public class PortalRegistry {

    private static final Map<BlockLocation, Portal> lookupBlocks = new HashMap<>();
    private static final Map<BlockLocation, Portal> lookupEntrances = new HashMap<>();
    private static final Map<BlockLocation, Portal> lookupControls = new HashMap<>();

    private static final Map<String, Map<String, Portal>> portalLookupByNetwork = new HashMap<>();
    private static final Map<String, List<String>> allPortalNetworks = new HashMap<>();
    private static final Map<String, Portal> bungeePortals = new HashMap<>();

    private static final List<Portal> allPortals = new ArrayList<>();

    /**
     * Clears all portals and all data held by the portal registry
     */
    public static void clearPortals() {
        lookupBlocks.clear();
        portalLookupByNetwork.clear();
        lookupEntrances.clear();
        lookupControls.clear();
        allPortals.clear();
        allPortalNetworks.clear();
    }

    /**
     * Clears all portals loaded in a given world
     *
     * @param world <p>The world containing the portals to clear</p>
     */
    public static void clearPortals(World world) {
        //Storing the portals to clear is necessary to avoid a concurrent modification exception
        List<Portal> portalsToRemove = new ArrayList<>();
        allPortals.forEach((portal) -> {
            if (portal.getWorld().equals(world)) {
                portalsToRemove.add(portal);
            }
        });

        clearPortals(portalsToRemove);
    }

    /**
     * Clears a given list of portals from all relevant variables
     *
     * @param portalsToRemove <p>A list of portals to remove</p>
     */
    private static void clearPortals(List<Portal> portalsToRemove) {
        //Store the names of the portals to remove as some maps require the name, not the object
        List<String> portalNames = new ArrayList<>();
        portalsToRemove.forEach((portal) -> portalNames.add(portal.getName()));

        //Clear all the lookup locations for the portals
        lookupBlocks.keySet().removeIf((key) -> portalsToRemove.contains(lookupBlocks.get(key)));
        lookupEntrances.keySet().removeIf((key) -> portalsToRemove.contains(lookupEntrances.get(key)));
        lookupControls.keySet().removeIf((key) -> portalsToRemove.contains(lookupControls.get(key)));

        //Remove the portals from all networks, and then remove any empty networks. This is done for both network maps
        portalLookupByNetwork.keySet().forEach((network) -> portalLookupByNetwork.get(network).keySet().removeIf((key) ->
                portalsToRemove.contains(portalLookupByNetwork.get(network).get(key))));
        portalLookupByNetwork.keySet().removeIf((key) -> portalLookupByNetwork.get(key).isEmpty());
        allPortalNetworks.keySet().forEach((network) -> allPortalNetworks.get(network).removeIf(portalNames::contains));
        allPortalNetworks.keySet().removeIf((network) -> allPortalNetworks.get(network).isEmpty());

        //Finally, remove the portals from the portal list
        allPortals.removeIf(portalsToRemove::contains);
    }

    /**
     * Gets a copy of the list of all portals
     *
     * @return <p>A copy of the list of all portals</p>
     */
    public static List<Portal> getAllPortals() {
        return new ArrayList<>(allPortals);
    }

    /**
     * Gets a copy of the lookup map for finding a portal by its frame
     *
     * @return <p>A copy of the frame block lookup map</p>
     */
    public static Map<BlockLocation, Portal> getLookupBlocks() {
        return new HashMap<>(lookupBlocks);
    }

    /**
     * Gets a copy of the lookup map for finding a portal by its control block
     *
     * @return <p>A copy of the control block lookup map</p>
     */
    public static Map<BlockLocation, Portal> getLookupControls() {
        return new HashMap<>(lookupControls);
    }

    /**
     * Gets a copy of the lookup map for finding all portals in a network
     *
     * @return <p>A copy of the network portal lookup map</p>
     */
    public static Map<String, Map<String, Portal>> getPortalLookupByNetwork() {
        return new HashMap<>(portalLookupByNetwork);
    }

    /**
     * Gets a copy of all portal entrances available for lookup
     *
     * @return <p>A copy of all entrances to portal mappings</p>
     */
    public static Map<BlockLocation, Portal> getLookupEntrances() {
        return new HashMap<>(lookupEntrances);
    }

    /**
     * Gets a copy of all portal networks
     *
     * @return <p>A copy of all portal networks</p>
     */
    public static Map<String, List<String>> getAllPortalNetworks() {
        return new HashMap<>(allPortalNetworks);
    }

    /**
     * Gets a copy of all bungee portals
     *
     * @return <p>A copy of all bungee portals</p>
     */
    public static Map<String, Portal> getBungeePortals() {
        return new HashMap<>(bungeePortals);
    }

    /**
     * Gets names of all portals within a network
     *
     * @param network <p>The network to get portals from</p>
     * @return <p>A list of portal names</p>
     */
    public static List<String> getNetwork(String network) {
        return allPortalNetworks.get(network.toLowerCase());
    }

    /**
     * Un-registers the given portal
     *
     * @param portal    <p>The portal to un-register</p>
     * @param removeAll <p>Whether to remove the portal from the list of all portals</p>
     */
    public static void unregisterPortal(Portal portal, boolean removeAll) {
        Stargate.debug("Unregister", "Unregistering gate " + portal.getName());
        portal.getPortalOpener().closePortal(true);

        String portalName = portal.getName().toLowerCase();
        String networkName = portal.getNetwork().toLowerCase();

        //Remove portal from lookup blocks
        for (BlockLocation block : portal.getStructure().getFrame()) {
            lookupBlocks.remove(block);
        }

        //Remove registered info about the lookup controls and blocks
        lookupBlocks.remove(portal.getSignLocation());
        lookupControls.remove(portal.getSignLocation());

        BlockLocation button = portal.getStructure().getButton();
        if (button != null) {
            lookupBlocks.remove(button);
            lookupControls.remove(button);
        }

        //Remove entrances
        for (BlockLocation entrance : portal.getStructure().getEntrances()) {
            lookupEntrances.remove(entrance);
        }

        //Remove the portal from the list of all portals
        if (removeAll) {
            allPortals.remove(portal);
        }

        if (portal.getOptions().isBungee()) {
            //Remove the bungee listing
            bungeePortals.remove(portalName);
        } else {
            //Remove from network lists
            portalLookupByNetwork.get(networkName).remove(portalName);
            allPortalNetworks.get(networkName).remove(portalName);

            //Update all portals in the same network with this portal as its destination
            for (String originName : allPortalNetworks.get(networkName)) {
                Portal origin = PortalHandler.getByName(originName, portal.getNetwork());
                if (origin == null || !origin.getDestinationName().equalsIgnoreCase(portalName) ||
                        !origin.getStructure().isVerified()) {
                    continue;
                }
                //Update the portal's sign
                if (origin.getOptions().isFixed()) {
                    origin.drawSign();
                }
                //Close portal without destination
                if (origin.getOptions().isAlwaysOn()) {
                    origin.getPortalOpener().closePortal(true);
                }
            }
        }

        //Clear sign data
        if (portal.getSignLocation().getBlock().getBlockData() instanceof WallSign) {
            Sign sign = (Sign) portal.getSignLocation().getBlock().getState();
            sign.setLine(0, portal.getName());
            sign.setLine(1, "");
            sign.setLine(2, "");
            sign.setLine(3, "");
            sign.update();
        }

        PortalHandler.saveAllPortals(portal.getWorld());
    }

    /**
     * Registers a portal
     *
     * @param portal <p>The portal to register</p>
     */
    static void registerPortal(Portal portal) {
        portal.getOptions().setFixed(portal.getDestinationName().length() > 0 || portal.getOptions().isRandom() ||
                portal.getOptions().isBungee());

        String portalName = portal.getName().toLowerCase();
        String networkName = portal.getNetwork().toLowerCase();

        //Bungee portals are stored in their own list
        if (portal.getOptions().isBungee()) {
            bungeePortals.put(portalName, portal);
        } else {
            //Check if network exists in the lookup list. If not, register the new network
            if (!portalLookupByNetwork.containsKey(networkName)) {
                Stargate.debug("register", "Network " + portal.getNetwork() +
                        " not in lookupNamesNet, adding");
                portalLookupByNetwork.put(networkName, new HashMap<>());
            }
            //Check if this network exists in the network list. If not, register the network
            if (!allPortalNetworks.containsKey(networkName)) {
                Stargate.debug("register", "Network " + portal.getNetwork() +
                        " not in allPortalsNet, adding");
                allPortalNetworks.put(networkName, new ArrayList<>());
            }

            //Register the portal
            portalLookupByNetwork.get(networkName).put(portalName, portal);
            allPortalNetworks.get(networkName).add(portalName);
        }

        //Register all frame blocks to the lookup list
        for (BlockLocation block : portal.getStructure().getFrame()) {
            lookupBlocks.put(block, portal);
        }
        //Register the sign and button to the lookup lists
        lookupBlocks.put(portal.getSignLocation(), portal);
        lookupControls.put(portal.getSignLocation(), portal);

        BlockLocation button = portal.getStructure().getButton();
        if (button != null) {
            lookupBlocks.put(button, portal);
            lookupControls.put(button, portal);
        }

        //Register entrances to the lookup list
        for (BlockLocation entrance : portal.getStructure().getEntrances()) {
            lookupEntrances.put(entrance, portal);
        }

        allPortals.add(portal);
    }

}
