package org.sgrewritten.stargate.network;

import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkRegistry;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.util.NameHelper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class StargateNetworkRegistry implements NetworkRegistry {
    private final Map<String, Network> networkMap = new HashMap<>();
    private final Map<String, Network> networkNameMap = new HashMap<>();
    private final Map<String, String> personalNetworkNameConflicts = new HashMap<>();

    @Override
    public void renameNetwork(String newId, String oldId) throws InvalidNameException, UnimplementedFlagException, NameLengthException {
        Network network = networkMap.remove(oldId);
        networkNameMap.remove(oldId);
        if(network == null){
            throw new InvalidNameException("Network does not exist, can not rename: " + oldId);
        }
        network.setID(NameHelper.getNormalizedName(newId));
        networkMap.put(network.getId(), network);
        networkNameMap.put(NameHelper.getNormalizedName(network.getName()), network);
    }

    @Override
    public void unregisterNetwork(Network network) {
        networkMap.remove(network.getId());
        String networkName = NameHelper.getNormalizedName(network.getName());
        networkNameMap.remove(networkName);
        String personalNetworkName = personalNetworkNameConflicts.get(networkName);
        if(personalNetworkName != null) {
            updateName(personalNetworkName);
            personalNetworkNameConflicts.remove(networkName);
        }
    }


    @Override
    public void registerNetwork(Network network) {
        networkMap.put(network.getId(), network);
        if(networkNameMap.containsKey(network.getName())) {
            String newName = updateName(network.getName());
            personalNetworkNameConflicts.put(network.getName(), newName);
        }
        networkNameMap.put(NameHelper.getNormalizedName(network.getName()), network);
    }

    /**
     * Update the name for the conflicting already registered network
     * @param name <p>The name of the networks that have conflicting names</p>
     * @return <p>The new name to apply to the conflicting network</p>
     */
    private String updateName(String name){
        // Name conflicts with a personal network (i.e. the name of the personal network will change)
        String normalizedName = NameHelper.getNormalizedName(name);
        Network network = networkNameMap.get(normalizedName);
        if(network != null && network.getType() == NetworkType.PERSONAL){
            networkNameMap.remove(normalizedName);
            networkNameMap.put(network.getName(), network);
        }
        return NameHelper.getNormalizedName(network.getName());
    }

    @Override
    public boolean networkExists(String networkName) {
        return getNetwork(networkName) != null;
    }

    @Override
    public Network getNetwork(String name) {
        String cleanName = NameHelper.getNormalizedName(NameHelper.getTrimmedName(name));
        return networkMap.get(cleanName);
    }

    @Override
    public void clear(){
        networkMap.clear();
        networkNameMap.clear();
    }

    @Override
    public void updatePortals(){
        for (Network network : networkMap.values()){
            network.updatePortals();
        }
    }

    @Override
    public Stream<Network> stream(){
        return networkMap.values().stream();
    }

    @Override
    public Iterator<Network> iterator() {
        return networkMap.values().iterator();
    }

    @Override
    public boolean networkNameExists(String name) {
        return networkNameMap.containsKey(NameHelper.getNormalizedName(name));
    }

    @Override
    public Network getFromName(String name){
        return networkNameMap.get(NameHelper.getNormalizedName(name));
    }

    @Override
    public void closeAllPortals() {
        for(Network network : networkMap.values()){
            for(Portal portal : network.getAllPortals()){
                portal.close(true);
            }
        }
    }

    @Override
    public int size() {
        return networkMap.size();
    }
}
