package org.sgrewritten.stargate.api.network;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;

import java.util.Iterator;
import java.util.stream.Stream;

public interface NetworkRegistry {


    /**
     * Use {@link NetworkManager} instead. This does not save to database, and is not cross server compatible
     *
     * @param network
     */
    @ApiStatus.Internal
    void registerNetwork(Network network);

    /**
     * Use {@link NetworkManager} instead. This does not save to database, and is not cross server compatible
     *
     * @param newId
     * @param oldId
     */
    @ApiStatus.Internal
    void renameNetwork(String newId, String oldId) throws InvalidNameException, UnimplementedFlagException, NameLengthException;

    /**
     * Use {@link NetworkManager} instead. This does not save to database, and is not cross server compatible
     *
     * @param network
     */
    void unregisterNetwork(Network network);


    /**
     * Gets the network with the given
     *
     * @param name <p>The name of the network to get</p>
     * @return <p>The network with the given name</p>
     */
    @Nullable Network getNetwork(String name);

    /**
     * Checks whether the given network name exists
     *
     * @param id <p>The network name to check</p>
     * @return <p>True if the network exists</p>
     */
    boolean networkExists(String id);

    void clear();

    void updatePortals();

    Stream<Network> stream();

    Iterator<Network> iterator();

    boolean networkNameExists(String name);

    Network getFromName(String name);

    void closeAllPortals();

    int size();
}
