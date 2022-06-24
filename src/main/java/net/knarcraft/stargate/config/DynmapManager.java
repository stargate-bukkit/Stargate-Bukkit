package net.knarcraft.stargate.config;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalRegistry;
import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

/**
 * A manager for dealing with everything Dynmap
 */
public final class DynmapManager {

    private static MarkerSet markerSet;
    private static MarkerIcon portalIcon;

    private DynmapManager() {

    }

    /**
     * Initializes the dynmap manager
     *
     * @param dynmapAPI <p>A reference</p>
     */
    public static void initialize(DynmapAPI dynmapAPI) {
        if (dynmapAPI == null || dynmapAPI.getMarkerAPI() == null) {
            markerSet = null;
            portalIcon = null;
        } else {
            markerSet = dynmapAPI.getMarkerAPI().createMarkerSet("stargate", "Stargate", null, false);
            markerSet.setHideByDefault(Stargate.getStargateConfig().hideDynmapIcons());
            portalIcon = dynmapAPI.getMarkerAPI().getMarkerIcon("portal");
        }
    }

    /**
     * Adds all portal markers for all current portals
     */
    public static void addAllPortalMarkers() {
        if (markerSet == null || Stargate.getStargateConfig().isDynmapDisabled()) {
            //Remove any existing markers if dynmap has been disabled after startup
            if (markerSet != null) {
                markerSet.getMarkers().forEach(GenericMarker::deleteMarker);
            }
            return;
        }
        markerSet.setHideByDefault(Stargate.getStargateConfig().hideDynmapIcons());
        //Remove all existing markers for a clean start
        markerSet.getMarkers().forEach(GenericMarker::deleteMarker);

        for (Portal portal : PortalRegistry.getAllPortals()) {
            addPortalMarker(portal);
        }
    }

    /**
     * Adds a portal marker for the given portal
     *
     * @param portal <p>The portal to add a marker for</p>
     */
    public static void addPortalMarker(Portal portal) {
        if (markerSet == null || Stargate.getStargateConfig().isDynmapDisabled()) {
            return;
        }
        World world = portal.getWorld();
        if (portal.getOptions().isHidden() || world == null) {
            return;
        }

        Location location = portal.getBlockAt(portal.getGate().getLayout().getExit());
        Marker marker = markerSet.createMarker(getPortalMarkerId(portal), portal.getName(), world.getName(),
                location.getX(), location.getY(), location.getZ(), portalIcon, false);
        if (marker == null) {
            Stargate.logWarning(String.format(
                    """
                            Unable to create marker for portal
                            Portal marker id: %s
                            Portal name: %s
                            Portal world: %s
                            Portal location: %s,%s,%s""",
                    getPortalMarkerId(portal), portal.getName(), world.getName(), location.getX(), location.getY(),
                    location.getZ()));
            return;
        }
        String networkPrompt;
        if (portal.getOptions().isBungee()) {
            networkPrompt = "Server";
        } else {
            networkPrompt = "Network";
        }
        String markerDescription = String.format("<b>Name:</b> %s<br /><b>%s:</b> %s<br /><b>Destination:</b> " +
                        "%s<br /><b>Owner:</b> %s<br />", portal.getName(), networkPrompt, portal.getNetwork(),
                portal.getDestinationName(), portal.getOwner().getName());
        marker.setDescription(markerDescription);
        marker.setLabel(portal.getName(), true);
        marker.setMarkerIcon(portalIcon);
    }

    /**
     * Removes the portal marker for the given portal
     *
     * @param portal <p>The portal to remove the marker for</p>
     */
    public static void removePortalMarker(Portal portal) {
        if (markerSet == null || Stargate.getStargateConfig().isDynmapDisabled()) {
            return;
        }
        Marker marker = markerSet.findMarker(getPortalMarkerId(portal));
        if (marker != null) {
            marker.deleteMarker();
        }
    }

    /**
     * Gets the id used for the given portal's marker
     *
     * @param portal <p>The portal to get a marker id for</p>
     * @return <p></p>
     */
    private static String getPortalMarkerId(Portal portal) {
        return portal.getNetwork() + "-:-" + portal.getName();
    }

}
