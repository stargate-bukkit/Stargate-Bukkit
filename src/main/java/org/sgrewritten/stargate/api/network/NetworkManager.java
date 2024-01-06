package org.sgrewritten.stargate.api.network;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;

import java.util.Set;

/**
 * Interface for creating/accessing/removing/modifying portals and networks
 */
public interface NetworkManager {

    /**
     * Interprets a networkname and type, then selects it or creates it if it does not already exist
     *
     * @param name              <p> Initial name of the network</p>
     * @param permissionManager <p> A permission manager of the player</p>
     * @param player            <p> The player selecting the network</p>
     * @param flags             <p> flags of a portal this selection or creation comes from</p>
     * @return <p>The network the portal should be connected to</p>
     * @throws TranslatableException <p>If invalid input is given</p>
     */
    @NotNull Network selectNetwork(String name, PermissionManager permissionManager, OfflinePlayer player, Set<PortalFlag> flags) throws TranslatableException;

    /**
     * Gets the network with the given name, and creates it if it doesn't already exist
     *
     * @param name        <p>The name of the network to get</p>
     * @param type        <p>The type of network to get</p>
     * @param storageType <p>Whether or not the network works (or will work) across instances.
     * @return <p>The network the portal should be connected to</p>
     * @throws TranslatableException <p>If the network name is invalid</p>
     */
    @NotNull Network selectNetwork(String name, NetworkType type, StorageType storageType) throws TranslatableException;

    /**
     * Creates a new network assigned to this registry
     *
     * @param name        <p>The name of the new network</p>
     * @param type        <p>The type of network to create</p>
     * @param storageType <p>Whether to create it as a Interserver network</p>
     * @param isForced    <p>The authority for the creation </p>
     * @return <p> The network created </p>
     * @throws InvalidNameException       <p>If the given network name is invalid</p>
     * @throws NameLengthException
     * @throws NameConflictException
     * @throws UnimplementedFlagException
     */
    Network createNetwork(String name, NetworkType type, StorageType storageType, boolean isForced) throws InvalidNameException, UnimplementedFlagException, NameLengthException, NameConflictException;

    /**
     * Creates a new network assigned to this registry
     *
     * @param targetNetwork <p>The this network will attempt creation under</p>
     * @param flags         <p>The flags containing the network's enabled options</p>
     * @param isForced      <p>The authority for the creation </p>
     * @return <p> The network created </p>
     * @throws InvalidNameException       <p>If the given network name is invalid</p>
     * @throws NameLengthException
     * @throws NameConflictException
     * @throws UnimplementedFlagException
     */
    Network createNetwork(String targetNetwork, Set<PortalFlag> flags, boolean isForced) throws InvalidNameException, NameLengthException, NameConflictException, UnimplementedFlagException;

    /**
     * Rename the network to specified name, saves to storage
     *
     * @param network <p> The network to rename </p>
     * @param newName <p> The new name of the network </p>
     * @throws InvalidNameException
     * @throws NameLengthException
     * @throws UnimplementedFlagException
     */
    void rename(Network network, String newName) throws InvalidNameException, NameLengthException, UnimplementedFlagException;


    /**
     * Loads all portals from storage
     *
     * @param stargateAPI <p>The stargate api</p>
     */
    void loadPortals(StargateAPI stargateAPI);

    /**
     * @param portal  <p> The portal to rename</p>
     * @param newName <p> The new name of the portal </p>
     */
    void rename(Portal portal, String newName) throws NameConflictException;

    /**
     * Add portal to network and save to storage
     *
     * @param portal <p>The portal to be saved</p>
     * @param network <p>The network to assign the portal to</p>
     */
    void savePortal(RealPortal portal, Network network) throws NameConflictException;

    /**
     * Saves to database
     * @param portal <p>Destroy portal</p>
     */
    void destroyPortal(RealPortal portal);
}
