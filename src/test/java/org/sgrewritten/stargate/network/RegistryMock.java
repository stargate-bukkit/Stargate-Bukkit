package org.sgrewritten.stargate.network;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.BlockHandlerResolver;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.structure.GateStructureType;
import org.sgrewritten.stargate.container.ThreeTuple;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.database.StorageMock;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.portal.BlockLocation;
import org.sgrewritten.stargate.network.portal.PortalFlag;

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