package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockChangeRequest;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.PortalRegistry;
import net.knarcraft.stargate.portal.property.PortalLocation;
import net.knarcraft.stargate.portal.property.PortalOptions;
import net.knarcraft.stargate.portal.property.PortalOwner;
import net.knarcraft.stargate.portal.property.PortalStrings;
import net.knarcraft.stargate.portal.property.gate.Gate;
import net.knarcraft.stargate.portal.property.gate.GateHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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
    public static void saveAllPortals(@NotNull World world) {
        Stargate.getStargateConfig().addManagedWorld(world.getName());
        String saveFileLocation = Stargate.getPortalFolder() + "/" + world.getName() + ".db";

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(saveFileLocation, false));

            for (Portal portal : PortalRegistry.getAllPortals()) {
                //Skip portals in other worlds
                if (portal.getWorld() == null) {
                    Stargate.logSevere(String.format("Could not save portal %s because its world is null",
                            portal.getName()));
                } else {
                    String worldName = portal.getWorld().getName();
                    if (!worldName.equalsIgnoreCase(world.getName())) {
                        continue;
                    }
                    //Save the portal
                    savePortal(bufferedWriter, portal);
                }
            }

            bufferedWriter.close();
        } catch (Exception exception) {
            Stargate.logSevere(String.format("Exception while writing stargates to %s: %s", saveFileLocation, exception));
        }
    }

    /**
     * Saves one portal
     *
     * @param bufferedWriter <p>The buffered writer to write to</p>
     * @param portal         <p>The portal to save</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private static void savePortal(@NotNull BufferedWriter bufferedWriter, @NotNull Portal portal) throws IOException {
        StringBuilder builder = new StringBuilder();
        BlockLocation button = portal.getStructure().getButton();

        //WARNING: Because of the primitive save format, any change in order will break everything!
        builder.append(portal.getName()).append(':');
        builder.append(portal.getSignLocation()).append(':');
        builder.append((button != null) ? button.toString() : "").append(':');

        //Add removes config values to keep indices consistent
        builder.append(0).append(':');
        builder.append(0).append(':');

        builder.append(portal.getYaw()).append(':');
        builder.append(portal.getTopLeft()).append(':');
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
    private static void savePortalOptions(@NotNull Portal portal, @NotNull StringBuilder builder) {
        PortalOptions options = portal.getOptions();
        builder.append(':');
        builder.append(options.isHidden()).append(':');
        builder.append(options.isAlwaysOn()).append(':');
        builder.append(options.isPrivate()).append(':');
        if (portal.getWorld() != null) {
            builder.append(portal.getWorld().getName()).append(':');
        } else {
            builder.append(':');
        }
        builder.append(options.isFree()).append(':');
        builder.append(options.isBackwards()).append(':');
        builder.append(options.isShown()).append(':');
        builder.append(options.isNoNetwork()).append(':');
        builder.append(options.isRandom()).append(':');
        builder.append(options.isBungee()).append(':');
        builder.append(options.isSilent()).append(':');
        builder.append(options.hasNoSign());
    }

    /**
     * Loads all portals for the given world
     *
     * @param world <p>The world to load portals for</p>
     * @return <p>True if portals could be loaded</p>
     */
    public static boolean loadAllPortals(@NotNull World world) {
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
    private static boolean loadPortals(@NotNull World world, @NotNull File database) {
        int lineIndex = 0;
        try {
            Scanner scanner = new Scanner(database);
            boolean needsToSaveDatabase = false;
            while (scanner.hasNextLine()) {
                //Read the line and do whatever needs to be done
                needsToSaveDatabase = readPortalLine(scanner, ++lineIndex, world) || needsToSaveDatabase;
            }
            scanner.close();

            //Do necessary tasks after all portals have loaded
            Stargate.debug("PortalFileHelper::loadPortals", String.format("Finished loading portals for %s. " +
                    "Starting post loading tasks", world));
            doPostLoadTasks(world, needsToSaveDatabase);
            return true;
        } catch (Exception exception) {
            Stargate.logSevere(String.format("Exception while reading stargates from %s: %d! Message: %s",
                    database.getName(), lineIndex, exception.getMessage()));
        }
        return false;
    }

    /**
     * Reads one file line containing information about one portal
     *
     * @param scanner   <p>The scanner to read</p>
     * @param lineIndex <p>The index of the read line</p>
     * @param world     <p>The world for which portals are currently being read</p>
     * @return <p>True if the read portal has changed and the world's database needs to be saved</p>
     */
    private static boolean readPortalLine(@NotNull Scanner scanner, int lineIndex, @NotNull World world) {
        String line = scanner.nextLine().trim();

        //Ignore empty and comment lines
        if (line.startsWith("#") || line.isEmpty()) {
            return false;
        }

        //Check if the min. required portal data is present
        String[] portalData = line.split(":");
        if (portalData.length < 8) {
            Stargate.logInfo(String.format("Invalid line - %s", lineIndex));
            return false;
        }

        //Load the portal defined in the current line
        return loadPortal(portalData, world, lineIndex);
    }

    /**
     * Performs tasks which must be run after portals have loaded
     *
     * <p>This will open always on portals, print info about loaded stargates and re-draw portal signs for loaded
     * portals.</p>
     *
     * @param world               <p>The world portals have been loaded for</p>
     * @param needsToSaveDatabase <p>Whether the portal database's file needs to be updated</p>
     */
    private static void doPostLoadTasks(@NotNull World world, boolean needsToSaveDatabase) {
        //Open any always-on portals. Do this here as it should be more efficient than in the loop.
        PortalHandler.verifyAllPortals();
        int portalCount = PortalRegistry.getAllPortals().size();
        int openCount = PortalHandler.openAlwaysOpenPortals();

        //Print info about loaded stargates so that admins can see if all stargates loaded
        Stargate.logInfo(String.format("{%s} Loaded %d stargates with %d set as always-on", world.getName(),
                portalCount, openCount));


        if (Stargate.getGateConfig().applyStartupFixes()) {
            //Re-draw the signs in case a bug in the config prevented the portal from loading and has been fixed since
            Stargate.debug("PortalFileHelper::doPostLoadTasks::update",
                    String.format("Updating portal signs/buttons for %s", world));
            for (Portal portal : PortalRegistry.getAllPortals()) {
                if (portal.isRegistered() && portal.getWorld() != null && portal.getWorld().equals(world) &&
                        world.getWorldBorder().isInside(portal.getSignLocation())) {
                    portal.drawSign();
                    updatePortalButton(portal);
                    Stargate.debug("UpdateSignsButtons", String.format("Updated sign and button for portal %s",
                            portal.getName()));
                }
            }
        }
        //Save the portals to disk to update with any changes
        Stargate.debug("PortalFileHelper::doPostLoadTasks", String.format("Saving database for world %s", world));
        if (needsToSaveDatabase) {
            saveAllPortals(world);
        }
    }

    /**
     * Loads one portal from a data array
     *
     * @param portalData <p>The array describing the portal</p>
     * @param world      <p>The world to create the portal in</p>
     * @param lineIndex  <p>The line index to report in case the user needs to fix an error</p>
     * @return <p>True if the portal's data has changed and its database needs to be updated</p>
     */
    private static boolean loadPortal(@NotNull String[] portalData, @NotNull World world, int lineIndex) {
        //Load min. required portal data
        String name = portalData[0];
        BlockLocation button = (!portalData[2].isEmpty()) ? new BlockLocation(world, portalData[2]) : null;

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
            return false;
        }

        //Load extra portal data
        String destination = (portalData.length > 8) ? portalData[8] : "";
        String network = (portalData.length > 9 && !portalData[9].isEmpty()) ? portalData[9] :
                Stargate.getDefaultNetwork();
        String ownerString = (portalData.length > 10) ? portalData[10] : "";

        //Get the owner from the owner string
        PortalOwner owner = new PortalOwner(ownerString);

        //Create the new portal
        PortalStrings portalStrings = new PortalStrings(name, network, destination);
        Portal portal = new Portal(portalLocation, button, portalStrings, gate, owner,
                PortalHandler.getPortalOptions(portalData));

        //Register the portal, and close it in case it wasn't properly closed when the server stopped
        boolean buttonLocationChanged = updateButtonVector(portal);
        PortalHandler.registerPortal(portal);
        if (Stargate.getGateConfig().applyStartupFixes()) {
            portal.getPortalOpener().closePortal(true);
        }
        return buttonLocationChanged;
    }

    /**
     * Updates a portal's button if it does not match the correct material
     *
     * @param portal <p>The portal update the button of</p>
     */
    private static void updatePortalButton(@NotNull Portal portal) {
        BlockLocation buttonLocation = getButtonLocation(portal);
        if (buttonLocation == null) {
            return;
        }

        if (portal.getOptions().isAlwaysOn()) {
            //Clear button if it exists
            if (MaterialHelper.isButtonCompatible(buttonLocation.getType())) {
                Material newMaterial = decideRemovalMaterial(buttonLocation, portal);
                Stargate.addBlockChangeRequest(new BlockChangeRequest(buttonLocation, newMaterial, null));
            }
        } else {
            //Replace button if the material is not a button
            if (!MaterialHelper.isButtonCompatible(buttonLocation.getType())) {
                generatePortalButton(portal, DirectionHelper.getBlockFaceFromYaw(portal.getYaw()));
            }
        }
    }

    /**
     * Decides the material to use for removing a portal's button/sign
     *
     * @param location <p>The location of the button/sign to replace</p>
     * @param portal   <p>The portal the button/sign belongs to</p>
     * @return <p>The material to use for removing the button/sign</p>
     */
    @NotNull
    public static Material decideRemovalMaterial(@NotNull BlockLocation location, @NotNull Portal portal) {
        //Get the blocks to each side of the location
        Location leftLocation = location.getRelativeLocation(-1, 0, 0, portal.getYaw());
        Location rightLocation = location.getRelativeLocation(1, 0, 0, portal.getYaw());

        //If the block is water or is waterlogged, assume the portal is underwater
        if (isUnderwater(leftLocation) || isUnderwater(rightLocation)) {
            return Material.WATER;
        } else {
            return Material.AIR;
        }
    }

    /**
     * Checks whether the given location is underwater
     *
     * <p>If the location has a water block, or a block which is waterlogged, it will be considered underwater.</p>
     *
     * @param location <p>The location to check</p>
     * @return <p>True if the location is underwater</p>
     */
    private static boolean isUnderwater(@NotNull Location location) {
        BlockData blockData = location.getBlock().getBlockData();
        return blockData.getMaterial() == Material.WATER ||
                (blockData instanceof Waterlogged waterlogged && waterlogged.isWaterlogged());
    }

    /**
     * Updates the button vector for the given portal
     *
     * <p>As the button vector isn't saved, it is null when the portal is loaded. This method allows it to be
     * explicitly set when necessary.</p>
     *
     * @param portal <p>The portal to update the button vector for</p>
     * @return <p>True if the calculated button location is not the same as the one in the portal file</p>
     */
    private static boolean updateButtonVector(@NotNull Portal portal) {
        for (RelativeBlockVector control : portal.getGate().getLayout().getControls()) {
            BlockLocation controlLocation = portal.getLocation().getTopLeft().getRelativeLocation(control,
                    portal.getYaw());
            BlockLocation buttonLocation = controlLocation.getRelativeLocation(
                    new RelativeBlockVector(0, 0, 1), portal.getYaw());
            if (!buttonLocation.equals(portal.getLocation().getSignLocation())) {
                portal.getLocation().setButtonVector(control);

                BlockLocation oldButtonLocation = portal.getStructure().getButton();
                if (oldButtonLocation != null && !oldButtonLocation.equals(buttonLocation)) {
                    Stargate.addBlockChangeRequest(new BlockChangeRequest(oldButtonLocation, Material.AIR, null));
                    portal.getStructure().setButton(buttonLocation);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generates a button for a portal
     *
     * @param portal       <p>The portal to generate button for</p>
     * @param buttonFacing <p>The direction the button should be facing</p>
     */
    public static void generatePortalButton(@NotNull Portal portal, @NotNull BlockFace buttonFacing) {
        //Go one block outwards to find the button's location rather than the control block's location
        BlockLocation button = getButtonLocation(portal);

        // If the button location is null here, it is assumed that the button generation wasn't necessary
        if (button == null) {
            return;
        }

        if (!MaterialHelper.isButtonCompatible(button.getType())) {
            @NotNull List<Material> possibleMaterials = MaterialHelper.specifiersToMaterials(
                    portal.getGate().getPortalButtonMaterials()).stream().toList();
            Material buttonType = ListHelper.getRandom(possibleMaterials);

            Directional buttonData = (Directional) Bukkit.createBlockData(buttonType);
            buttonData.setFacing(buttonFacing);
            button.getBlock().setBlockData(buttonData);
        }
        portal.getStructure().setButton(button);
    }

    /**
     * Gets the location of a portal's button
     *
     * @param portal <p>The portal to find the button for</p>
     * @return <p>The location of the portal's button</p>
     */
    @Nullable
    private static BlockLocation getButtonLocation(@NotNull Portal portal) {
        BlockLocation topLeft = portal.getTopLeft();
        RelativeBlockVector buttonVector = portal.getLocation().getButtonVector();

        if (buttonVector == null) {
            return null;
        }

        return topLeft.getRelativeLocation(buttonVector.addOut(1), portal.getYaw());
    }

}
