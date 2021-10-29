package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.portal.Gate;
import net.knarcraft.stargate.portal.GateHandler;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.PortalLocation;
import net.knarcraft.stargate.portal.PortalOptions;
import net.knarcraft.stargate.portal.PortalOwner;
import net.knarcraft.stargate.portal.PortalRegistry;
import org.bukkit.World;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import static net.knarcraft.stargate.portal.PortalSignDrawer.markPortalWithInvalidGate;

/**
 * Helper class for saving and loading portal save files
 */
public final class PortalFileHelper {

    private PortalFileHelper() {

    }

    /**
     * Saves all portals for the given world
     *
     * @param world <p>The world to save portals for</p>
     */
    public static void saveAllPortals(World world) {
        Stargate.getStargateConfig().addManagedWorld(world.getName());
        String saveFileLocation = Stargate.getPortalFolder() + "/" + world.getName() + ".db";

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(saveFileLocation, false));

            for (Portal portal : PortalRegistry.getAllPortals()) {
                //Skip portals in other worlds
                String worldName = portal.getWorld().getName();
                if (!worldName.equalsIgnoreCase(world.getName())) {
                    continue;
                }
                //Save the portal
                savePortal(bufferedWriter, portal);
            }

            bufferedWriter.close();
        } catch (Exception e) {
            Stargate.logSevere(String.format("Exception while writing stargates to %s: %s", saveFileLocation, e));
        }
    }

    /**
     * Saves one portal
     *
     * @param bufferedWriter <p>The buffered writer to write to</p>
     * @param portal         <p>The portal to save</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private static void savePortal(BufferedWriter bufferedWriter, Portal portal) throws IOException {
        StringBuilder builder = new StringBuilder();
        BlockLocation button = portal.getStructure().getButton();

        //WARNING: Because of the primitive save format, any change in order will break everything!
        builder.append(portal.getName()).append(':');
        builder.append(portal.getSignLocation().toString()).append(':');
        builder.append((button != null) ? button.toString() : "").append(':');

        //Add removes config values to keep indices consistent
        builder.append(0).append(':');
        builder.append(0).append(':');

        builder.append(portal.getYaw()).append(':');
        builder.append(portal.getTopLeft().toString()).append(':');
        builder.append(portal.getGate().getFilename()).append(':');

        //Only save the destination name if the gate is fixed as it doesn't matter otherwise
        builder.append(portal.getOptions().isFixed() ? portal.getDestinationName() : "").append(':');

        builder.append(portal.getNetwork()).append(':');

        //Name is saved as a fallback if the UUID is unavailable
        builder.append(portal.getOwner().getIdentifier());

        //Save all the portal options
        savePortalOptions(portal, builder);

        bufferedWriter.append(builder.toString());
        bufferedWriter.newLine();
    }

    /**
     * Saves all portal options for the given portal
     *
     * @param portal  <p>The portal to save</p>
     * @param builder <p>The string builder to append to</p>
     */
    private static void savePortalOptions(Portal portal, StringBuilder builder) {
        PortalOptions options = portal.getOptions();
        builder.append(':');
        builder.append(options.isHidden()).append(':');
        builder.append(options.isAlwaysOn()).append(':');
        builder.append(options.isPrivate()).append(':');
        builder.append(portal.getWorld().getName()).append(':');
        builder.append(options.isFree()).append(':');
        builder.append(options.isBackwards()).append(':');
        builder.append(options.isShown()).append(':');
        builder.append(options.isNoNetwork()).append(':');
        builder.append(options.isRandom()).append(':');
        builder.append(options.isBungee());
    }

    /**
     * Loads all portals for the given world
     *
     * @param world <p>The world to load portals for</p>
     * @return <p>True if portals could be loaded</p>
     */
    public static boolean loadAllPortals(World world) {
        String location = Stargate.getPortalFolder();

        File database = new File(location, world.getName() + ".db");

        if (database.exists()) {
            return loadPortals(world, database);
        } else {
            Stargate.logInfo(String.format("{%s} No stargates for world ", world.getName()));
        }
        return false;
    }

    /**
     * Loads all the given portals
     *
     * @param world    <p>The world to load portals for</p>
     * @param database <p>The database file containing the portals</p>
     * @return <p>True if the portals were loaded successfully</p>
     */
    private static boolean loadPortals(World world, File database) {
        int lineIndex = 0;
        try {
            Scanner scanner = new Scanner(database);
            while (scanner.hasNextLine()) {
                //Read the line and do whatever needs to be done
                readPortalLine(scanner, ++lineIndex, world);
            }
            scanner.close();

            //Do necessary tasks after all portals have loaded
            doPostLoadTasks(world);
            return true;
        } catch (Exception e) {
            Stargate.logSevere(String.format("Exception while reading stargates from %s: %d", database.getName(),
                    lineIndex));
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Reads one file line containing information about one portal
     *
     * @param scanner   <p>The scanner to read</p>
     * @param lineIndex <p>The index of the read line</p>
     * @param world     <p>The world for which portals are currently being read</p>
     */
    private static void readPortalLine(Scanner scanner, int lineIndex, World world) {
        String line = scanner.nextLine().trim();

        //Ignore empty and comment lines
        if (line.startsWith("#") || line.isEmpty()) {
            return;
        }

        //Check if the min. required portal data is present
        String[] portalData = line.split(":");
        if (portalData.length < 8) {
            Stargate.logInfo(String.format("Invalid line - %s", lineIndex));
            return;
        }

        //Load the portal defined in the current line
        loadPortal(portalData, world, lineIndex);
    }

    /**
     * Performs tasks which must be run after portals have loaded
     *
     * <p>This will open always on portals, print info about loaded stargates and re-draw portal signs for loaded
     * portals.</p>
     *
     * @param world <p>The world portals have been loaded for</p>
     */
    private static void doPostLoadTasks(World world) {
        //Open any always-on portals. Do this here as it should be more efficient than in the loop.
        PortalHandler.verifyAllPortals();
        int portalCount = PortalRegistry.getAllPortals().size();
        int openCount = PortalHandler.openAlwaysOpenPortals();

        //Print info about loaded stargates so that admins can see if all stargates loaded
        Stargate.logInfo(String.format("{%s} Loaded %d stargates with %d set as always-on", world.getName(),
                portalCount, openCount));

        //Re-draw the signs in case a bug in the config prevented the portal from loading and has been fixed since
        for (Portal portal : PortalRegistry.getAllPortals()) {
            portal.drawSign();
        }
    }

    /**
     * Loads one portal from a data array
     *
     * @param portalData <p>The array describing the portal</p>
     * @param world      <p>The world to create the portal in</p>
     * @param lineIndex  <p>The line index to report in case the user needs to fix an error</p>
     */
    private static void loadPortal(String[] portalData, World world, int lineIndex) {
        //Load min. required portal data
        String name = portalData[0];
        BlockLocation button = (portalData[2].length() > 0) ? new BlockLocation(world, portalData[2]) : null;

        //Load the portal's location
        PortalLocation portalLocation = new PortalLocation();
        portalLocation.setSignLocation(new BlockLocation(world, portalData[1]));
        portalLocation.setYaw(Float.parseFloat(portalData[5]));
        portalLocation.setTopLeft(new BlockLocation(world, portalData[6]));

        //Check if the portal's gate type exists and is loaded
        Gate gate = GateHandler.getGateByName(portalData[7]);
        if (gate == null) {
            //Mark the sign as invalid to reduce some player confusion
            markPortalWithInvalidGate(portalLocation, portalData[7], lineIndex);
            return;
        }

        //Load extra portal data
        String destination = (portalData.length > 8) ? portalData[8] : "";
        String network = (portalData.length > 9 && !portalData[9].isEmpty()) ? portalData[9] : Stargate.getDefaultNetwork();
        String ownerString = (portalData.length > 10) ? portalData[10] : "";

        //Get the owner from the owner string
        PortalOwner owner = new PortalOwner(ownerString);

        //Create the new portal
        Portal portal = new Portal(portalLocation, button, destination, name, network, gate, owner,
                PortalHandler.getPortalOptions(portalData));

        //Register the portal, and close it in case it wasn't properly closed when the server stopped
        PortalHandler.registerPortal(portal);
        portal.getPortalOpener().closePortal(true);
    }

}
