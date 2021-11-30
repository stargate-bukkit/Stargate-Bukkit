package net.TheDgtl.Stargate.database;

import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FakePortal implements IPortal {

    private Location signLocation;
    private String portalName;
    private Network network;
    private UUID ownerUUID;

    public FakePortal(Location signLocation, String portalName, Network network, UUID ownerUUID) {
        this.signLocation = signLocation;
        this.portalName = portalName;
        this.network = network;
        this.ownerUUID = ownerUUID;
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isOpenFor(Entity target) {
        return false;
    }

    @Override
    public void teleportHere(Entity target, Portal origin) {

    }

    @Override
    public void doTeleport(Entity target) {

    }

    @Override
    public void drawControlMechanism() {

    }

    @Override
    public void close(boolean force) {

    }

    @Override
    public void open(Player player) {

    }

    @Override
    public String getName() {
        return this.portalName;
    }

    @Override
    public void setNetwork(Network targetNet) {

    }

    @Override
    public void setOverrideDestination(IPortal destination) {

    }

    @Override
    public Network getNetwork() {
        return this.network;
    }

    @Override
    public boolean hasFlag(PortalFlag flag) {
        return false;
    }

    @Override
    public String getAllFlagsString() {
        return null;
    }

    @Override
    public Location getSignPos() {
        return this.signLocation;
    }

    @Override
    public String getDesignName() {
        return null;
    }

    @Override
    public UUID getOwnerUUID() {
        return ownerUUID;
    }
}
