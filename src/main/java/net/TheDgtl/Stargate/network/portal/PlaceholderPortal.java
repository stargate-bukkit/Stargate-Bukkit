package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.util.PortalHelper;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;
import java.util.UUID;

/**
 * A placeholder portal that can be given values and then saved into the
 * database. Does not have any other function
 *
 * @author Thorin
 */
public class PlaceholderPortal implements RealPortal {

    private final String name;
    private Network network;
    private String destination = "";
    private Set<PortalFlag> flags;
    private UUID ownerUUID;
    private Gate gate;

    /**
     * Instantiates a new placeholder portal
     *
     * @param name         <p>The name of the portal</p>
     * @param network      <p>The network the portal belongs to</p>
     * @param destination  <p>The fixed destination of this portal, or null</p>
     * @param flags        <p>The flags enabled for the portal</p>
     * @param ownerUUID    <p>The UUID of the portal's owner</p>
     * @param gate         <p>The gate construct linked to the portal</p>
     */
    public PlaceholderPortal(String name, Network network, String destination, Set<PortalFlag> flags,
                             UUID ownerUUID, Gate gate) {
        this.name = name;
        if (destination != null) {
            this.destination = destination;
        }
        this.flags = flags;
        this.network = network;
        this.ownerUUID = ownerUUID;
        this.gate = gate;
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
    public void drawControlMechanisms() {}

    @Override
    public void setSignColor(DyeColor color) {}

    @Override
    public void onButtonClick(PlayerInteractEvent event) {}

    @Override
    public Gate getGate() {
        return this.gate;
    }

    @Override
    public void close(long relatedOpenTime) {}

    @Override
    public Location getExit() {
        return null;
    }

    @Override
    public Location getSignLocation() {
        return null;
    }

}
