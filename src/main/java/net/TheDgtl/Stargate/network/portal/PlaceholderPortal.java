package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.util.PortalHelper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.UUID;

/**
 * A placeholder portal that can be given values and then saved into the
 * database. Does not have any other function
 *
 * @author Thorin
 */
public class PlaceholderPortal implements Portal {

    private final String name;
    private Network network;
    private String destination = "";
    private EnumSet<PortalFlag> flags;
    private Location signLocation;
    private String gateFileName;
    private UUID ownerUUID;

    /**
     * Instantiates a new placeholder portal
     *
     * @param name         <p>The name of the portal</p>
     * @param network      <p>The network the portal belongs to</p>
     * @param destination  <p>The fixed destination of this portal, or null</p>
     * @param flags        <p>The flags enabled for the portal</p>
     * @param signLocation <p>The location of this portal's sign</p>
     * @param gateFileName <p>The file name of this portal's gate format</p>
     * @param ownerUUID    <p>The UUID of the portal's owner</p>
     */
    public PlaceholderPortal(String name, Network network, String destination, EnumSet<PortalFlag> flags,
                             Location signLocation, String gateFileName, UUID ownerUUID) {
        this.name = name;
        if (destination != null) {
            this.destination = destination;
        }
        this.flags = flags;
        this.signLocation = signLocation;
        this.network = network;
        this.gateFileName = gateFileName;
        this.ownerUUID = ownerUUID;
    }

    /**
     * Instantiates a new placeholder portal
     *
     * @param name        <p>The name of the portal</p>
     * @param network     <p>The network the portal belongs to</p>
     * @param destination <p>The fixed destination of this portal, or null</p>
     */
    public PlaceholderPortal(String name, Network network, String destination) {
        this.name = name;
        if (destination != null) {
            this.destination = destination;
        }
        this.network = network;
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
    public void teleportHere(Entity target, RealPortal origin) {
    }

    @Override
    public void doTeleport(Entity target) {
    }

    @Override
    public void close(boolean forceClose) {
    }

    @Override
    public void open(Player player) {
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void overrideDestination(Portal destination) {
    }

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
        return PortalHelper.flagsToString(flags);
    }

    @Override
    public Location getSignLocation() {
        return signLocation;
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
    public void update() {
    }

    @Override
    public Portal loadDestination() {
        Portal destination = network.getPortal(this.destination);
        if (destination != null) {
            return destination;
        }
        return new PlaceholderPortal(this.destination, network, "");
    }

    @Override
    public void setOwner(UUID targetPlayer) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
