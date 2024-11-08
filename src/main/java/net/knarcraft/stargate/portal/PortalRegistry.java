package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.DynmapManager;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.utility.PortalFileHelper;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        bungeePortals.clear();
    }

    /**
     * Clears all portals loaded in a given world
     *
     * @param world <p>The world containing the portals to clear</p>
     */
    public static void clearPortals(@NotNull World world) {
        //Storing the portals to clear is necessary to avoid a concurrent modification exception
        List<Portal> portalsToRemove = new ArrayList<>();
        allPortals.forEach((portal) -> {
            if (portal.getWorld() != null && portal.getWorld().equals(world)) {
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
    private static void clearPortals(@NotNull List<Portal> portalsToRemove) {
        //Store the names of the portals to remove as some maps require the name, not the object
        List<String> portalNames = new ArrayList<>();
        portalsToRemove.forEach((portal) -> portalNames.add(portal.getCleanName()));

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
    @NotNull
    public static List<Portal> getAllPortals() {
        return new ArrayList<>(allPortals);
    }

    /**
     * Gets a portal that the given frame block belongs to
     *
     * @param blockLocation <p>The location that might be a frame block</p>
     * @return <p>The portal the frame block belongs to, or null</p>
     */
    @Nullable
    public static Portal getPortalFromFrame(@NotNull BlockLocation blockLocation) {
        return lookupBlocks.get(blockLocation);
    }

    /**
     * Gets the portal that the given control block belongs to
     *
     * @param blockLocation <p>The location that might be a portal control block</p>
     * @return <p>The portal the control block belongs to, or null</p>
     */
    @Nullable
    public static Portal getPortalFromControl(@NotNull BlockLocation blockLocation) {
        return lookupControls.get(blockLocation);
    }

    /**
     * Gets the portal identified by the given network name and portal name
     *
     * @param networkName <p>The name of the network the portal belongs to</p>
     * @param portalName  <p>The name of the portal</p>
     * @return <p>The portal, or null if no such network and/or portal exists</p>
     */
    @Nullable
    public static Portal getPortalInNetwork(@NotNull String networkName, @NotNull String portalName) {
        Map<String, Portal> portalsInNetwork = portalLookupByNetwork.get(Portal.cleanString(networkName));
        if (portalsInNetwork == null) {
            return null;
        }
        return portalsInNetwork.get(Portal.cleanString(portalName));
    }

    /**
     * Gets a portal from the location of a possible entrance
     *
     * @param blockLocation <p>A location that might be a portal's entrance</p>
     * @return <p>A portal, or null</p>
     */
    @Nullable
    public static Portal getPortalFromEntrance(@NotNull BlockLocation blockLocation) {
        return lookupEntrances.get(blockLocation);
    }

    /**
     * Gets a copy of all portal networks
     *
     * @return <p>A copy of all portal networks</p>
     */
    @NotNull
    public static Map<String, List<String>> getAllPortalNetworks() {
        return new HashMap<>(allPortalNetworks);
    }

    /**
     * Gets the BungeeCord portal with the given name
     *
     * @param portalName <p>The name of the portal to get</p>
     * @return <p>The portal, or null</p>
     */
    @Nullable
    public static Portal getBungeePortal(@NotNull String portalName) {
        return bungeePortals.get(Portal.cleanString(portalName));
    }

    /**
     * Gets names of all portals within a network
     *
     * @param network <p>The network to get portals from</p>
     * @return <p>A list of portal names</p>
     */
    @Nullable
    public static List<String> getNetwork(@NotNull String network) {
        return allPortalNetworks.get(network.toLowerCase());
    }

    /**
     * Un-registers the given portal
     *
     * @param portal    <p>The portal to un-register</p>
     * @param removeAll <p>Whether to remove the portal from the list of all portals</p>
     */
    public static void unregisterPortal(@NotNull Portal portal, boolean removeAll) {
        Stargate.debug("Unregister", "Unregistering gate " + portal.getName());
        portal.getPortalActivator().deactivate();
        portal.getPortalOpener().closePortal(true);

        String portalName = portal.getCleanName();
        String networkName = portal.getCleanNetwork();

        clearLookupMaps(portal, removeAll);

        if (portal.getOptions().isBungee()) {
            //Remove the bungee listing
            bungeePortals.remove(portalName);
        } else {
            //Remove from network lists
            portalLookupByNetwork.get(networkName).remove(portalName);
            allPortalNetworks.get(networkName).remove(portalName);

            //Update all portals in the same network with this portal as its destination
            for (String originName : allPortalNetworks.get(networkName)) {
                Portal origin = PortalHandler.getByName(originName, portal.getCleanNetwork());
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

        //Mark the portal's sign as unregistered
        new PortalSignDrawer(portal).drawUnregisteredSign();

        if (portal.getWorld() != null) {
            PortalFileHelper.saveAllPortals(portal.getWorld());
        }
        portal.setRegistered(false);
        DynmapManager.removePortalMarker(portal);
    }

    /**
     * Clears the given portal's presence from lookup maps
     *
     * @param portal    <p>The portal to clear</p>
     * @param removeAll <p>Whether to remove the portal from the list of all portals</p>
     */
    private static void clearLookupMaps(@NotNull Portal portal, boolean removeAll) {
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
    }

    /**
     * Registers a portal
     *
     * @param portal <p>The portal to register</p>
     */
    public static void registerPortal(@NotNull Portal portal) {
        portal.getOptions().setFixed(!portal.getDestinationName().isEmpty() || portal.getOptions().isRandom() ||
                portal.getOptions().isBungee());

        String portalName = portal.getCleanName();
        String networkName = portal.getCleanNetwork();

        //Bungee portals are stored in their own list
        if (portal.getOptions().isBungee()) {
            bungeePortals.put(portalName, portal);
        } else {
            //Check if network exists in the lookup list. If not, register the new network
            if (!portalLookupByNetwork.containsKey(networkName)) {
                Stargate.debug("register", String.format("Network %s not in lookupNamesNet, adding",
                        portal.getNetwork()));
                portalLookupByNetwork.put(networkName, new HashMap<>());
            }
            //Check if this network exists in the network list. If not, register the network
            if (!allPortalNetworks.containsKey(networkName)) {
                Stargate.debug("register", String.format("Network %s not in allPortalsNet, adding",
                        portal.getNetwork()));
                allPortalNetworks.put(networkName, new ArrayList<>());
            }

            //Register the portal
            portalLookupByNetwork.get(networkName).put(portalName, portal);

            if (!allPortalNetworks.get(networkName).contains(portalName)) {
                allPortalNetworks.get(networkName).add(portalName);
            } else {
                Stargate.logSevere(String.format("Portal %s on network %s was registered twice. Check your portal " +
                        "database for duplicates.", portal.getName(), portal.getNetwork()));
            }
        }

        //Register all frame blocks to the lookup list
        for (BlockLocation block : portal.getStructure().getFrame()) {
            lookupBlocks.put(block, portal);
        }
        //Register the sign and button to the lookup lists
        if (!portal.getOptions().hasNoSign()) {
            lookupBlocks.put(portal.getSignLocation(), portal);
            lookupControls.put(portal.getSignLocation(), portal);
        }

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
        portal.setRegistered(true);
        DynmapManager.addPortalMarker(portal);
    }

}
