package org.sgrewritten.stargate.manager;

import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.network.NetworkType;

import java.util.Set;

public class UnrestrictedPermissionManager implements PermissionManager {
    @Override
    public Set<PortalFlag> returnDisallowedFlags(Set<PortalFlag> flags) {
        return Set.of();
    }

    @Override
    public boolean hasAccessPermission(RealPortal portal) {
        return true;
    }

    @Override
    public boolean hasCreatePermissions(RealPortal portal) {
        return true;
    }

    @Override
    public boolean hasDestroyPermissions(RealPortal portal) {
        return true;
    }

    @Override
    public boolean hasOpenPermissions(RealPortal entrance, Portal exit) {
        return true;
    }

    @Override
    public boolean hasTeleportPermissions(RealPortal entrance) {
        return true;
    }

    @Override
    public boolean canCreateInNetwork(String network, NetworkType type) {
        return true;
    }

    @Override
    public String getDenyMessage() {
        throw new UnsupportedOperationException("Operation not supported");
    }
}
