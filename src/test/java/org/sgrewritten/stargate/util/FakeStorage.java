package org.sgrewritten.stargate.util;

import org.sgrewritten.stargate.database.StorageAPI;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.InterServerNetwork;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalPosition;
import org.sgrewritten.stargate.network.portal.RealPortal;

public class FakeStorage implements StorageAPI{

    @Override
    public void loadFromStorage(RegistryAPI registry) throws StorageReadException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean savePortalToStorage(RealPortal portal, StorageType portalType) throws StorageWriteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removePortalFromStorage(Portal portal, StorageType portalType) throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPortalMetaData(Portal portal, String data, StorageType portalType) throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getPortalMetaData(Portal portal, StorageType portalType) throws StorageReadException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPortalPositionMetaData(RealPortal portal, PortalPosition portalPosition, String data,
            StorageType portalType) throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getPortalPositionMetaData(Portal portal, PortalPosition portalPosition, StorageType portalType)
            throws StorageReadException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Network createNetwork(String networkName, NetworkType type, boolean isInterserver)
            throws InvalidNameException, NameLengthException {
        if (isInterserver) {
            return new InterServerNetwork(networkName,type);
        }
        else {
            return new LocalNetwork(networkName, type);
        }
    }

    @Override
    public void startInterServerConnection() throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addFlagType(char flagChar) throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addPortalPositionType(String portalPositionTypeName) throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addFlag(Character flagChar, Portal portal, StorageType portalType) throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeFlag(Character flagChar, Portal portal, StorageType portalType) throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addPortalPosition(RealPortal portal, StorageType portalType, PortalPosition portalPosition)
            throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removePortalPosition(RealPortal portal, StorageType portalType, PortalPosition portalPosition)
            throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateNetworkName(String newName, String networkName, StorageType portalType)
            throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updatePortalName(String newName, String portalName, String networkName, StorageType portalType)
            throws StorageWriteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean netWorkExists(String netName, StorageType portalType) throws StorageReadException {
        // TODO Auto-generated method stub
        return false;
    }

}
