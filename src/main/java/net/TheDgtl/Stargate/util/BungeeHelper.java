package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.RegistryAPI;
import net.TheDgtl.Stargate.network.portal.Portal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class BungeeHelper {


    /*
     * Used in bungee / waterfall
     */
    private static final HashMap<String, Portal> bungeeQueue = new HashMap<>();

    /**
     * @param dataFolder
     * @param internalFolder
     */
    public static void loadBungeeServerName(String dataFolder, String internalFolder) {
        Stargate.log(Level.FINEST, dataFolder);
        File path = new File(dataFolder, internalFolder);
        if (!path.exists() && path.mkdir()) {
            try {
                Files.setAttribute(path.toPath(), "dos:hidden", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(path, "serverUUID.txt");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new FileNotFoundException("serverUUID.txt was not found and could not be created");
                }
                BufferedWriter writer = FileHelper.getBufferedWriter(file);
                writer.write(UUID.randomUUID().toString());
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            BufferedReader reader = FileHelper.getBufferedReader(file);
            Stargate.serverUUID = UUID.fromString(reader.readLine());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToQueue(RegistryAPI registry, String playerName, String portalName, String netName, boolean isInterServer) {
        Network network = registry.getNetwork(netName, isInterServer);


        /*
         * In some cases, there might be issues with a portal being deleted in a server, but still present in the
         * inter-server database. Therefore, we have to check for that...
         */
        if (network == null) {
            // Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = network.
            String msg = String.format("Inter-server network ''%s'' could not be found", netName);
            Stargate.log(Level.WARNING, msg);
        }
        Portal portal = network == null ? null : network.getPortal(portalName);
        if (portal == null) {
            // Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = gate.
            String msg = String.format("Inter-server portal ''%s'' in network ''%s'' could not be found", portalName, netName);
            Stargate.log(Level.WARNING, msg);
        }
        bungeeQueue.put(playerName, portal);
    }


    public static Portal pullFromQueue(String playerName) {
        return bungeeQueue.remove(playerName);
    }
}
