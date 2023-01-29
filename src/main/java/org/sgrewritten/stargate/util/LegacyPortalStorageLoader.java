package org.sgrewritten.stargate.util;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.gate.control.AlwaysOnControlMechanism;
import org.sgrewritten.stargate.gate.control.ButtonControlMechanism;
import org.sgrewritten.stargate.gate.control.SignControlMechanism;
import org.sgrewritten.stargate.network.portal.PortalData;
import org.sgrewritten.stargate.util.database.PortalStorageHelper;
import org.sgrewritten.stargate.util.portal.PortalCreationHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * A helper tool for loading legacy portals from legacy storage
 */
public final class LegacyPortalStorageLoader {

    private LegacyPortalStorageLoader() {

    }

    /**
     * Loads legacy portals in .db files from the given folder
     *
     * @param portalSaveLocation <p>The folder containing legacy portals</p>
     * @param server             <p>The server this plugin is running on</p>
     * @param registry           <p>The stargate registry to save portals to</p>
     * @param logger             <p>The logger used for logging</p>
     * @return <p>The list of loaded and saved portals</p>
     * @throws IOException               <p>If unable to read one or more .db files</p>
     * @throws InvalidStructureException <p>If an encountered portal's structure is invalid</p>
     * @throws InvalidNameException      <p>If the name of a portal is invalid</p>
     * @throws TranslatableException
     */
    public static List<Portal> loadPortalsFromStorage(String portalSaveLocation, Server server,
                                                      RegistryAPI registry, StargateLogger logger, String defaultNetworkName, LanguageManager languageManager, StargateEconomyAPI economyManager)
            throws IOException, InvalidStructureException, InvalidNameException, TranslatableException {
        List<Portal> portals = new ArrayList<>();
        File dir = new File(portalSaveLocation);
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> name.endsWith(".db")) : new File[0];
        if (files == null) {
            return null;
        }

        for (File file : files) {
            String worldName = file.getName().replaceAll("\\.db$", "");

            BufferedReader reader = FileHelper.getBufferedReader(file);
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                portals.add(readPortal(line, server.getWorld(worldName), registry, logger, defaultNetworkName, languageManager, economyManager));

                line = reader.readLine();
            }
            reader.close();
        }
        return portals;
    }

    /**
     * Reads a portal line from a legacy portal .db file
     *
     * @param line     <p>The line to read</p>
     * @param world    <p>The world the portal belongs to</p>
     * @param registry <p>The portal registry to save the portal to</p>
     * @param logger   <p>The logger used for logging</p>
     * @return <p>The loaded portal</p>
     * @throws InvalidStructureException <p>If the portal's structure is invalid</p>
     * @throws TranslatableException     <p>If the portal's name is invalid</p>
     */
    private static Portal readPortal(String line, World world, RegistryAPI registry, StargateLogger logger,
                                     String defaultNetworkName, LanguageManager languageManager,
                                     StargateEconomyAPI economyManager) throws InvalidStructureException, TranslatableException {
        String[] portalProperties = line.split(":");
        PortalData portalData = PortalStorageHelper.loadPortalData(portalProperties, world, defaultNetworkName);
        try {
            registry.createNetwork(portalData.networkName, portalData.flags, false);
        } catch (InvalidNameException | NameLengthException | NameConflictException ignored) {
        }
        if (portalData.topLeft == null) {
            throw new InvalidStructureException();
        }

        Network network = registry.getNetwork(portalData.networkName,
                portalData.flags.contains(PortalFlag.FANCY_INTER_SERVER));

        if (network == null) {
            logger.logMessage(Level.SEVERE, "Unable to get network " + portalData.networkName + " during legacy" +
                    "portal loading");
            return null;
        }

        Gate gate = new Gate(portalData, registry, languageManager);
        Location signLocation = LegacyDataHandler.loadLocation(world, portalProperties[1]);
        Location buttonLocation = LegacyDataHandler.loadLocation(world, portalProperties[2]);
        if (signLocation != null) {
            logger.logMessage(Level.FINEST, "signLocation=" + signLocation);
            SignControlMechanism mechanism = new SignControlMechanism(gate.getRelativeVector(signLocation).toBlockVector(), gate, languageManager);
            gate.setPortalControlMechanism(mechanism);
            gate.addPortalPosition(mechanism);
        }
        if (buttonLocation != null) {
            logger.logMessage(Level.FINEST, "buttonLocation=" + buttonLocation);
            if (portalData.flags.contains(PortalFlag.ALWAYS_ON)) {
                AlwaysOnControlMechanism mechanism = new AlwaysOnControlMechanism();
                gate.setPortalControlMechanism(mechanism);
            } else {
                ButtonControlMechanism mechanism = new ButtonControlMechanism(gate.getRelativeVector(buttonLocation).toBlockVector(), gate);
                gate.addPortalPosition(mechanism);
                gate.setPortalControlMechanism(mechanism);
            }
        }

        Portal portal = PortalCreationHelper.createPortal(network, portalData, gate, languageManager, registry, economyManager);

        // Add the portal to its network and store it to the database
        logger.logMessage(Level.FINE, String.format("Saving portal %s in network %s from old storage... ",
                portalData.name, portalData.networkName));
        network.addPortal(portal, true);

        return portal;
    }

}
