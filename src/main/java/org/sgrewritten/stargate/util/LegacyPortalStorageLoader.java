package org.sgrewritten.stargate.util;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.portaldata.PortalData;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.PositionType;
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
     * @param stargateAPI       <p>The stargate API</p>
     * @param defaultNetworkName <p> The default network name </p>
     * @return <p>The list of loaded and saved portals</p>
     * @throws IOException               <p>If unable to read one or more .db files</p>
     * @throws InvalidStructureException <p>If an encountered portal's structure is invalid</p>
     * @throws TranslatableException
     */
    public static List<Portal> loadPortalsFromStorage(String portalSaveLocation, Server server,
                                                      String defaultNetworkName, RegistryAPI registry, StargateAPI stargateAPI)
            throws IOException, InvalidStructureException, TranslatableException {
        List<Portal> portals = new ArrayList<>();
        File dir = new File(portalSaveLocation);
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> name.endsWith(".db")) : new File[0];
        if (files == null) {
            return null;
        }

        for (File file : files) {
            String worldName = file.getName().replaceAll("\\.db$", "");

            BufferedReader reader = FileHelper.getBufferedReader(file);
            // Fix encoding issue / can't convert properly from ansi to utf8
            String line = reader.readLine();
            // Convert to utf-8 if ansi is used
            while (line != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                portals.add(readPortal(line, server.getWorld(worldName),stargateAPI, registry, defaultNetworkName));

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
     * @return <p>The loaded portal</p>
     * @throws InvalidStructureException <p>If the portal's structure is invalid</p>
     * @throws TranslatableException     <p>If the portal's name is invalid</p>
     */
    private static Portal readPortal(String line, World world, StargateAPI stargateAPI, RegistryAPI registry,
                                     String defaultNetworkName) throws InvalidStructureException, TranslatableException {
        String[] portalProperties = line.split(":");
        PortalData portalData = PortalStorageHelper.loadPortalData(portalProperties, world, defaultNetworkName);
        try {
            registry.createNetwork(portalData.networkName(), portalData.flags(), false);
        } catch (InvalidNameException | NameLengthException | NameConflictException ignored) {
        }
        if (portalData.gateData().topLeft() == null) {
            throw new InvalidStructureException();
        }

        Network network = registry.getNetwork(portalData.networkName(),
                portalData.flags().contains(PortalFlag.FANCY_INTER_SERVER));

        if (network == null) {
            Stargate.log(Level.SEVERE, "Unable to get network " + portalData.networkName() + " during legacy" +
                    "portal loading");
            return null;
        }

        Gate gate = new Gate(portalData.gateData(), registry);
        Location signLocation = LegacyDataHandler.loadLocation(world, portalProperties[1]);
        Location buttonLocation = LegacyDataHandler.loadLocation(world, portalProperties[2]);
        if (signLocation != null) {
            Stargate.log(Level.FINEST, "signLocation=" + signLocation);
            gate.addPortalPosition(signLocation, PositionType.SIGN, "Stargate");
        }
        if (buttonLocation != null && !portalData.flags().contains(PortalFlag.ALWAYS_ON)) {
            Stargate.log(Level.FINEST, "buttonLocation=" + buttonLocation);
            gate.addPortalPosition(buttonLocation, PositionType.BUTTON, "Stargate");
        }

        Portal portal = PortalCreationHelper.createPortal(network, portalData, gate, stargateAPI);

        // Add the portal to its network and store it to the database
        Stargate.log(Level.FINE, String.format("Saving portal %s in network %s from old storage... ",
                portalData.name(), portalData.networkName()));
        network.addPortal(portal, true);

        return portal;
    }

}
