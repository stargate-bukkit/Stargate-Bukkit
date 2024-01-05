package org.sgrewritten.stargate.network;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.BlockHandlerResolver;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.container.ThreeTuple;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.database.StorageMock;

import java.util.Map;
import java.util.Stack;

public class RegistryMock extends StargateRegistry {
    Stack<TwoTuple<GateStructureType, BlockLocation>> previousUnregisteredLocations = new Stack<>();
    Stack<ThreeTuple<GateStructureType, BlockLocation, RealPortal>> previousRegisteredLocations = new Stack<>();

    Stack<PortalPosition> nextRegisteredPortalPosition = new Stack<>();
    Stack<BlockLocation> nextUnRegisteredPortalPosition = new Stack<>();

    public RegistryMock() {
        super(new StorageMock(), new BlockHandlerResolver(new StorageMock()));
    }

    public RegistryMock(StorageAPI storage, BlockHandlerResolver resolver) {
        super(storage, resolver);
    }

    @Override
    public void registerLocations(GateStructureType structureType, Map<BlockLocation, RealPortal> locationsMap) {
        super.registerLocations(structureType, locationsMap);
        for (BlockLocation location : locationsMap.keySet()) {
            previousRegisteredLocations.push(new ThreeTuple<>(structureType, location, locationsMap.get(location)));
        }
    }

    @Override
    public void registerLocation(GateStructureType structureType, BlockLocation location, RealPortal portal) {
        previousRegisteredLocations.push(new ThreeTuple<>(structureType, location, portal));
        super.registerLocation(structureType, location, portal);
    }

    public ThreeTuple<GateStructureType, BlockLocation, RealPortal> getNextRegisteredLocation() {
        if (previousRegisteredLocations.isEmpty()) {
            return null;
        }
        return previousRegisteredLocations.pop();
    }

    @Override
    public void unRegisterLocation(GateStructureType structureType, BlockLocation blockLocation) {
        previousUnregisteredLocations.push(new TwoTuple<>(structureType, blockLocation));
        super.unRegisterLocation(structureType, blockLocation);
    }

    public TwoTuple<GateStructureType, BlockLocation> getNextUnregisteredLocation() {
        if (previousUnregisteredLocations.isEmpty()) {
            return null;
        }
        return previousUnregisteredLocations.pop();
    }

    @Override
    public void registerPortalPosition(PortalPosition portalPosition, Location location, RealPortal portal) {
        super.registerPortalPosition(portalPosition, location, portal);
        nextRegisteredPortalPosition.push(portalPosition);
    }

    public @Nullable PortalPosition getNextRegisteredPortalPosition() {
        if (nextRegisteredPortalPosition.isEmpty()) {
            return null;
        }
        return nextRegisteredPortalPosition.pop();
    }

    @Override
    public void removePortalPosition(Location location) {
        super.removePortalPosition(location);
        nextUnRegisteredPortalPosition.push(new BlockLocation(location));
    }

    public @Nullable BlockLocation getNextRemovedPortalPosition() {
        if (nextUnRegisteredPortalPosition.isEmpty()) {
            return null;
        }
        return nextUnRegisteredPortalPosition.pop();
    }
}