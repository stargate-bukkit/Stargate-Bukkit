package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.portaldata.PortalData;
import org.sgrewritten.stargate.util.database.PortalStorageHelper;
import org.sgrewritten.stargate.util.portal.PortalCreationHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * A helper tool for loading legacy portals from legacy storage
 */
public final class LegacyPortalStorageLoader {

    private static final Pattern DATABASE = Pattern.compile("\\.db$");

    private LegacyPortalStorageLoader() {

    }

    /**
     * Loads legacy portals in .db files from the given folder
     *
     * @param dir                <p>The folder containing legacy portals</p>
     * @param stargateAPI        <p>The stargate API</p>
     * @param defaultNetworkName <p> The default network name </p>
     * @return <p>The list of loaded and saved portals</p>
     * @throws IOException               <p>If unable to read one or more .db files</p>
     * @throws InvalidStructureException <p>If an encountered portal's structure is invalid</p>
     * @throws TranslatableException
     */
    public static @NotNull List<Portal> loadPortalsFromStorage(File dir,
                                                               String defaultNetworkName, StargateAPI stargateAPI)
            throws IOException, InvalidStructureException, TranslatableException {
        List<Portal> portals = new ArrayList<>();
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> name.endsWith(".db")) : new File[0];
        if (files == null) {
            return new ArrayList<>();
        }

        for (File file : files) {
            String worldName = DATABASE.matcher(file.getName()).replaceAll("");

            BufferedReader reader = FileHelper.getBufferedReader(file);
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    Stargate.log(Level.WARNING, "Could not load portal from world: " + worldName);
                    Stargate.log(Level.WARNING, "Ignoring world...");
                    continue;
                }
                try {
                    portals.add(readPortal(line, world, stargateAPI, defaultNetworkName));
                } catch (Exception e) {
                    Stargate.log(e);
                }
                line = reader.readLine();
            }
            reader.close();
        }
        return portals;
    }

    /**
     * Reads a portal line from a legacy portal .db file
     *
     * @param line  <p>The line to read</p>
     * @param world <p>The world the portal belongs to</p>
     * @return <p>The loaded portal</p>
     * @throws InvalidStructureException <p>If the portal's structure is invalid</p>
     * @throws TranslatableException     <p>If the portal's name is invalid</p>
     */
    private static Portal readPortal(String line, World world, StargateAPI stargateAPI,
                                     String defaultNetworkName) throws InvalidStructureException, TranslatableException {
        String[] portalProperties = line.split(":");
        PortalData portalData = PortalStorageHelper.loadPortalData(portalProperties, world, defaultNetworkName);
        try {
            Network network = stargateAPI.getNetworkManager().createNetwork(portalData.networkName(), portalData.flags(), false);
            Stargate.log(Level.INFO, "Created network with id: " + network.getId());
        } catch (InvalidNameException | NameLengthException | NameConflictException ignored) {
        }
        if (portalData.gateData().topLeft() == null) {
            throw new InvalidStructureException();
        }

        Network network = stargateAPI.getRegistry().getNetwork(portalData.networkName(),
                portalData.flags().contains(StargateFlag.INTERSERVER) ? StorageType.INTER_SERVER : StorageType.LOCAL);
        Stargate.log(Level.INFO, "fetched networkName: " + portalData.networkName());

        if (network == null) {
            Stargate.log(Level.SEVERE, "Unable to get network " + portalData.networkName() + " during legacy" +
                    "portal loading");
            return null;
        }

        Gate gate = new Gate(portalData.gateData(), stargateAPI.getRegistry());
        Location signLocation = LegacyDataHandler.loadLocation(world, portalProperties[1]);
        Location buttonLocation = LegacyDataHandler.loadLocation(world, portalProperties[2]);
        if (signLocation != null) {
            Stargate.log(Level.FINEST, "signLocation=" + signLocation);
            gate.addPortalPosition(signLocation, PositionType.SIGN, "Stargate");
        }
        if (buttonLocation != null && !portalData.flags().contains(StargateFlag.ALWAYS_ON)) {
            Stargate.log(Level.FINEST, "buttonLocation=" + buttonLocation);
            gate.addPortalPosition(buttonLocation, PositionType.BUTTON, "Stargate");
        }

        RealPortal portal = PortalCreationHelper.createPortal(network, portalData, gate, stargateAPI);

        Stargate.log(Level.FINE, String.format("Saving portal %s in network %s from old storage... ",
                portalData.name(), portalData.networkName()));
        stargateAPI.getNetworkManager().savePortal(portal, network);
        return portal;
    }

}
