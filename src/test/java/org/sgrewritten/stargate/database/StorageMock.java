package org.sgrewritten.stargate.database;

import org.bukkit.World;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.container.ThreeTuple;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateNetwork;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.GlobalPortalId;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class StorageMock implements StorageAPI {

    final Stack<ThreeTuple<RealPortal, StorageType, PortalPosition>> nextAddedPortalPosition = new Stack<>();
    final Stack<ThreeTuple<RealPortal, StorageType, PortalPosition>> nextRemovedPortalPosition = new Stack<>();

    @Override
    public void loadFromStorage(StargateAPI stargateAPI) {

    }

    @Override
    public boolean savePortalToStorage(RealPortal portal) {
        return false;
    }

    @Override
    public void removePortalFromStorage(Portal portal) {

    }

    @Override
    public void setPortalMetaData(Portal portal, String data, StorageType portalType) {

    }

    @Override
    public String getPortalMetaData(Portal portal, StorageType portalType) {
        return null;
    }

    @Override
    public void setPortalPositionMetaData(RealPortal portal, PortalPosition portalPosition, String data,
                                          StorageType portalType) {

    }

    @Override
    public String getPortalPositionMetaData(Portal portal, PortalPosition portalPosition, StorageType portalType) {
        return null;
    }

    @Override
    public Network createNetwork(String networkName, NetworkType type, StorageType storageType)
            throws InvalidNameException, NameLengthException, UnimplementedFlagException {
        return new StargateNetwork(networkName, type, storageType);
    }

    @Override
    public void startInterServerConnection() {

    }

    @Override
    public void addFlagType(char flagChar) {

    }

    @Override
    public void addPortalPositionType(String portalPositionTypeName) {

    }

    @Override
    public void addFlag(Character flagChar, Portal portal, StorageType portalType) {

    }

    @Override
    public void removeFlag(Character flagChar, Portal portal, StorageType portalType) {

    }

    @Override
    public void addPortalPosition(RealPortal portal, StorageType portalType, PortalPosition portalPosition) {
        this.nextAddedPortalPosition.push(new ThreeTuple<>(portal, portalType, portalPosition));
    }

    public ThreeTuple<RealPortal, StorageType, PortalPosition> getNextAddedPortalPosition() {
        if (nextAddedPortalPosition.isEmpty()) {
            return null;
        }
        return this.nextAddedPortalPosition.pop();
    }

    @Override
    public void removePortalPosition(RealPortal portal, StorageType portalType, PortalPosition portalPosition) {
        this.nextRemovedPortalPosition.push(new ThreeTuple<>(portal, portalType, portalPosition));
    }

    public ThreeTuple<RealPortal, StorageType, PortalPosition> getNextRemovedPortalPosition() {
        if (nextRemovedPortalPosition.isEmpty()) {
            return null;
        }
        return this.nextRemovedPortalPosition.pop();
    }

    @Override
    public void updateNetworkName(String newName, String networkName, StorageType portalType) {

    }

    @Override
    public void updatePortalName(String newName, GlobalPortalId portalId, StorageType portalType) {
    }

    @Override
    public boolean netWorkExists(String netName, StorageType portalType) {
        return false;
    }

    @Override
    public Set<String> getScheduledGatesClearing() {
        return new HashSet<>();
    }

    @Override
    public void loadPortalsInWorld(World world, StorageType storageType, StargateAPI stargateAPI) throws StorageReadException, StorageWriteException {

    }

}
