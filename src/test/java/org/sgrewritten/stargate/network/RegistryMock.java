package org.sgrewritten.stargate.network;

import org.sgrewritten.stargate.api.BlockHandlerResolver;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.gate.structure.GateStructureType;
import org.sgrewritten.stargate.container.ThreeTuple;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.database.StorageMock;
import org.sgrewritten.stargate.network.portal.BlockLocation;

import java.util.*;

public class RegistryMock extends StargateRegistry {
    Stack<TwoTuple<GateStructureType, BlockLocation>> previousUnregisteredLocations = new Stack<>();
    Stack<ThreeTuple<GateStructureType, BlockLocation, RealPortal>> previousRegisteredLocations = new Stack<>();

    public RegistryMock() {
        super(new StorageMock(), new BlockHandlerResolver(new StorageMock()));
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
        if(previousRegisteredLocations.isEmpty()){
            return null;
        }
        return previousRegisteredLocations.pop();
    }

    @Override
    public void unRegisterLocation(GateStructureType structureType, BlockLocation blockLocation) {
        previousUnregisteredLocations.push(new TwoTuple<>(structureType, blockLocation));
        super.unRegisterLocation(structureType,blockLocation);
    }

    public TwoTuple<GateStructureType, BlockLocation> getNextUnregisteredLocation() {
        if(previousUnregisteredLocations.isEmpty()){
            return null;
        }
        return previousUnregisteredLocations.pop();
    }
}