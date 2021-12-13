package net.TheDgtl.Stargate.network.portal;

import java.util.EnumSet;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.network.Network;

/**
 * A placeholder portal that can be given values and then saved into the
 * database. Does not have any other function
 * 
 * @author Thorin
 *
 */
public class PlaceholderPortal implements Portal{
    private String name;
    private Network network;
    private String destination = "";
    private EnumSet<PortalFlag> flags;
    private Location signLoc;
    private String gateFileName;
    private UUID ownerUUID;

    public PlaceholderPortal(String name, Network network, String destination, EnumSet<PortalFlag> flags, Location signLoc, String gateFileName, UUID ownerUUID) {
        this.name = name;
        if(destination != null)
            this.destination = destination;
        this.flags = flags;
        this.signLoc = signLoc;
        this.network = network;
        this.gateFileName = gateFileName;
        this.ownerUUID = ownerUUID;
    }
    
    public PlaceholderPortal(String name, Network network, String destination) {
        this.name = name;
        if(destination != null)
            this.destination = destination;
        this.network = network;
    }

    @Override
    public void destroy() {}

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isOpenFor(Entity target) {
        return false;
    }

    @Override
    public void teleportHere(Entity target, RealPortal origin) {}

    @Override
    public void doTeleport(Entity target) {}

    @Override
    public void close(boolean forceClose) {}

    @Override
    public void open(Player player) {}

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void overrideDestination(Portal destination) {}

    @Override
    public Network getNetwork() {
        return this.network;
    }

    @Override
    public void setNetwork(Network targetNetwork) {
        this.network = targetNetwork;
    }

    @Override
    public boolean hasFlag(PortalFlag flag) {
        return flags.contains(flag);
    }

    @Override
    public String getAllFlagsString() {
        return Portal.flagsToString(flags);
    }

    @Override
    public Location getSignLocation() {
        return signLoc;
    }

    @Override
    public String getDesignName() {
        return gateFileName;
    }

    @Override
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public void update() {}

    @Override
    public Portal loadDestination() {
        Portal destination = network.getPortal(this.destination);
        if(destination != null)
            return destination;
        return new PlaceholderPortal(this.destination,network,"");
    }
}
