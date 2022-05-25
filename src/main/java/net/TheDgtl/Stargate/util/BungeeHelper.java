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

/**
 * A helper class for dealing with BungeeCord
 */
public final class BungeeHelper {

    private static final HashMap<String, Portal> bungeeQueue = new HashMap<>();

    private BungeeHelper() {

    }

    /**
     * Gets the server's unique ID
     *
     * <p>If the server this plugin runs on is missing an id, a new unique id will be generated.</p>
     *
     * @param dataFolder     <p>The folder containing plugin data</p>
     * @param internalFolder <p>The folder containing internal hidden files</p>
     */
    public static void getServerId(String dataFolder, String internalFolder) {
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

    /**
     * Adds a player to the BungeeCord teleportation queue
     *
     * @param registry      <p>The registry to use for looking up networks</p>
     * @param playerName    <p>The name of the player to add to the queue</p>
     * @param portalName    <p>The name of the portal the player is teleporting to</p>
     * @param networkName   <p>The name of the network the entry portal belongs to</p>
     * @param isInterServer <p>Whether the entry portal belongs to an inter-server network</p>
     */
    public static void addToQueue(RegistryAPI registry, String playerName, String portalName, String networkName,
                                  boolean isInterServer) {
        Network network = registry.getNetwork(networkName, isInterServer);

        /*
         * In some cases, there might be issues with a portal being deleted in a server, but still present in the
         * inter-server database. Therefore, we have to check for that...
         */
        if (network == null) {
            // Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = network.
            String msg = String.format("Inter-server network ''%s'' could not be found", networkName);
            Stargate.log(Level.WARNING, msg);
        }
        Portal portal = network == null ? null : network.getPortal(portalName);
        if (portal == null) {
            // Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = gate.
            String msg = String.format("Inter-server portal ''%s'' in network ''%s'' could not be found", portalName, networkName);
            Stargate.log(Level.WARNING, msg);
        }
        bungeeQueue.put(playerName, portal);
    }

    /**
     * Gets a portal from the BungeeCord teleportation queue
     *
     * @param playerName <p>The player to pull from the queue</p>
     * @return <p>The portal the player should be teleported to</p>
     */
    public static Portal pullFromQueue(String playerName) {
        return bungeeQueue.remove(playerName);
    }

}
