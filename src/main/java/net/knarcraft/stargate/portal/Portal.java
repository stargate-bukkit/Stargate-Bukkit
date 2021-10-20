package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * This class represents a portal in space which points to one or several portals
 */
public class Portal {

    // Gate information
    private final String name;
    private final String network;
    private final String ownerName;
    private final UUID ownerUUID;

    private final PortalOptions options;
    private final PortalOpener portalOpener;
    private final PortalLocation location;
    private final PortalSignDrawer signDrawer;
    private final PortalStructure structure;
    private final PortalActivator portalActivator;

    /**
     * Instantiates a new portal
     *
     * @param portalLocation <p>Object containing locations of all relevant blocks</p>
     * @param button         <p>The location of the portal's open button</p>
     * @param destination    <p>The destination defined on the sign's destination line. "" for non-fixed gates</p>
     * @param name           <p>The name of the portal defined on the sign's first line</p>
     * @param network        <p>The network the portal belongs to, defined on the sign's third</p>
     * @param gate           <p>The gate type to use for this portal</p>
     * @param ownerUUID      <p>The UUID of the gate's owner</p>
     * @param ownerName      <p>The name of the gate's owner</p>
     * @param options        <p>A map containing all possible portal options, with true for the ones enabled</p>
     */
    public Portal(PortalLocation portalLocation, BlockLocation button, String destination, String name, String network,
                  Gate gate, UUID ownerUUID, String ownerName, Map<PortalOption, Boolean> options) {
        this.location = portalLocation;
        this.network = network;
        this.name = name;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.options = new PortalOptions(options, destination.length() > 0);
        this.signDrawer = new PortalSignDrawer(this);
        this.portalOpener = new PortalOpener(this, destination);
        this.structure = new PortalStructure(this, gate, button);
        this.portalActivator = portalOpener.getPortalActivator();
    }

    /**
     * Gets the location data for this portal
     *
     * @return <p>This portal's location data</p>
     */
    public PortalLocation getLocation() {
        return this.location;
    }

    /**
     * Gets the structure of this portal
     *
     * <p>The structure contains information about the portal's gate, button and real locations of frames and
     * entrances. The structure is also responsible for verifying built StarGates to make sure they match the gate.</p>
     *
     * @return <p>This portal's structure</p>
     */
    public PortalStructure getStructure() {
        return this.structure;
    }

    /**
     * Gets this portal's activator
     *
     * <p>The activator is responsible for activating/de-activating the portal and contains information about
     * available destinations and which player activated the portal.</p>
     *
     * @return <p>This portal's activator</p>
     */
    public PortalActivator getPortalActivator() {
        return this.portalActivator;
    }

    /**
     * Re-draws the sign on this portal
     */
    public void drawSign() {
        this.signDrawer.drawSign();
    }

    /**
     * Gets the portal options for this portal
     *
     * @return <p>This portal's portal options</p>
     */
    public PortalOptions getOptions() {
        return this.options;
    }

    /**
     * Gets whether this portal is currently open
     *
     * @return <p>Whether this portal is open</p>
     */
    public boolean isOpen() {
        return portalOpener.isOpen();
    }

    /**
     * Gets the player currently using this portal
     *
     * @return <p>The player currently using this portal</p>
     */
    public Player getActivePlayer() {
        return portalActivator.getActivePlayer();
    }

    /**
     * Gets the network this portal belongs to
     *
     * @return <p>The network this portal belongs to</p>
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Gets the time this portal was activated/opened
     *
     * <p>The time is given in the equivalent of a Unix timestamp. It's used to decide when a portal times out and
     * automatically closes.</p>
     *
     * @return <p>The time this portal was activated/opened</p>
     */
    public long getActivatedTime() {
        return portalOpener.getActivatedTime();
    }

    /**
     * Gets the name of this portal
     *
     * @return <p>The name of this portal</p>
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the portal opener used by this portal
     *
     * <p>The portal opener is responsible for opening and closing this portal.</p>
     *
     * @return <p>This portal's portal opener</p>
     */
    public PortalOpener getPortalOpener() {
        return portalOpener;
    }

    /**
     * Gets the name of this portal's destination portal
     *
     * @return <p>The name of this portal's destination portal</p>
     */
    public String getDestinationName() {
        return portalOpener.getPortalActivator().getDestinationName();
    }

    /**
     * Gets the gate type used by this portal
     *
     * @return <p>The gate type used by this portal</p>
     */
    public Gate getGate() {
        return structure.getGate();
    }

    /**
     * Gets the name of this portal's owner
     *
     * <p>The owner is the player which created the portal.</p>
     *
     * @return <p>The name of this portal's owner</p>
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Gets the UUID of this portal's owner
     *
     * <p>The owner is the player which created the portal.</p>
     *
     * @return <p>The UUID of this portal's owner</p>
     */
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * Checks whether a given player is the owner of this portal
     *
     * @param player <p>The player to check</p>
     * @return <p>True if the player is the owner of this portal</p>
     */
    public boolean isOwner(Player player) {
        if (this.ownerUUID != null) {
            return player.getUniqueId().compareTo(this.ownerUUID) == 0;
        } else {
            return player.getName().equalsIgnoreCase(this.ownerName);
        }
    }

    /**
     * Gets the world this portal belongs to
     *
     * @return <p>The world this portal belongs to</p>
     */
    public World getWorld() {
        return location.getWorld();
    }

    /**
     * Gets the location of this portal's sign
     *
     * @return <p>The location of this portal's sign</p>
     */
    public BlockLocation getSignLocation() {
        return this.location.getSignLocation();
    }

    /**
     * Gets the rotation (yaw) of this portal
     *
     * <p>The yaw is used to calculate all kinds of directions. See DirectionHelper to see how the yaw is used to
     * calculate to/from other direction types.</p>
     *
     * @return <p>The rotation (yaw) of this portal</p>
     */
    public float getYaw() {
        return this.location.getYaw();
    }

    /**
     * Gets the location of the top-left block of the portal
     *
     * @return <p>The location of the top-left portal block</p>
     */
    public BlockLocation getTopLeft() {
        return this.location.getTopLeft();
    }

    /**
     * Gets the block at the given location relative to this portal's top-left block
     *
     * @param vector <p>The relative block vector explaining the position of the block</p>
     * @return <p>The block at the given relative position</p>
     */
    public BlockLocation getBlockAt(RelativeBlockVector vector) {
        return getTopLeft().getRelativeLocation(vector, getYaw());
    }

    @Override
    public String toString() {
        return String.format("Portal [id=%s, network=%s name=%s, type=%s]", getSignLocation(), network, name,
                structure.getGate().getFilename());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((network == null) ? 0 : network.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Portal other = (Portal) object;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equalsIgnoreCase(other.name)) {
            return false;
        }
        //If none of the portals have a name, check if the network is the same
        if (network == null) {
            return other.network == null;
        } else {
            return network.equalsIgnoreCase(other.network);
        }
    }
}
