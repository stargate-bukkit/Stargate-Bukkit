package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import net.knarcraft.stargate.portal.property.PortalLocation;
import net.knarcraft.stargate.portal.property.PortalOption;
import net.knarcraft.stargate.portal.property.PortalOptions;
import net.knarcraft.stargate.portal.property.PortalOwner;
import net.knarcraft.stargate.portal.property.PortalStrings;
import net.knarcraft.stargate.portal.property.PortalStructure;
import net.knarcraft.stargate.portal.property.gate.Gate;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * This class represents a portal in space which points to one or several portals
 */
public class Portal {

    private final String name;
    private final String cleanName;
    private final String network;
    private final String cleanNetwork;

    private final PortalOwner portalOwner;
    private boolean isRegistered;

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
     * @param portalStrings  <p>The portal's string values, such as name, network and destination</p>
     * @param gate           <p>The gate type to use for this portal</p>
     * @param portalOwner    <p>The portal's owner</p>
     * @param options        <p>A map containing all possible portal options, with true for the ones enabled</p>
     */
    public Portal(@NotNull PortalLocation portalLocation, @Nullable BlockLocation button,
                  @NotNull PortalStrings portalStrings, @NotNull Gate gate, @NotNull PortalOwner portalOwner,
                  @NotNull Map<PortalOption, Boolean> options) {
        this.location = portalLocation;
        this.network = portalStrings.network();
        this.name = portalStrings.name();
        this.portalOwner = portalOwner;
        this.options = new PortalOptions(options, !portalStrings.destination().isEmpty());
        this.signDrawer = new PortalSignDrawer(this);
        this.portalOpener = new PortalOpener(this, portalStrings.destination());
        this.structure = new PortalStructure(this, gate, button);
        this.portalActivator = portalOpener.getPortalActivator();
        this.cleanName = cleanString(name);
        this.cleanNetwork = cleanString(network);
    }

    /**
     * Checks if this portal is registered
     *
     * @return <p>True if this portal is registered</p>
     */
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * Sets whether this portal is registered
     *
     * @param isRegistered <p>True if this portal is registered</p>
     */
    public void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    /**
     * Gets the location data for this portal
     *
     * @return <p>This portal's location data</p>
     */
    @NotNull
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
    @NotNull
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
    @NotNull
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
    @NotNull
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
    @Nullable
    public Player getActivePlayer() {
        return portalActivator.getActivePlayer();
    }

    /**
     * Gets the network this portal belongs to
     *
     * @return <p>The network this portal belongs to</p>
     */
    @NotNull
    public String getNetwork() {
        return network;
    }

    /**
     * Gets the clean name of the network this portal belongs to
     *
     * @return <p>The clean network name</p>
     */
    @NotNull
    public String getCleanNetwork() {
        return cleanNetwork;
    }

    /**
     * Gets the time this portal was triggered (activated/opened)
     *
     * <p>The time is given in the equivalent of a Unix timestamp. It's used to decide when a portal times out and
     * automatically closes/deactivates.</p>
     *
     * @return <p>The time this portal was triggered (activated/opened)</p>
     */
    public long getTriggeredTime() {
        return portalOpener.getTriggeredTime();
    }

    /**
     * Gets the name of this portal
     *
     * @return <p>The name of this portal</p>
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets the clean name of this portal
     *
     * @return <p>The clean name of this portal</p>
     */
    @NotNull
    public String getCleanName() {
        return cleanName;
    }

    /**
     * Gets the portal opener used by this portal
     *
     * <p>The portal opener is responsible for opening and closing this portal.</p>
     *
     * @return <p>This portal's portal opener</p>
     */
    @NotNull
    public PortalOpener getPortalOpener() {
        return portalOpener;
    }

    /**
     * Gets the name of this portal's destination portal
     *
     * @return <p>The name of this portal's destination portal</p>
     */
    @NotNull
    public String getDestinationName() {
        return portalOpener.getPortalActivator().getDestinationName();
    }

    /**
     * Gets the gate type used by this portal
     *
     * @return <p>The gate type used by this portal</p>
     */
    @NotNull
    public Gate getGate() {
        return structure.getGate();
    }

    /**
     * Gets this portal's owner
     *
     * <p>The owner is the player which created the portal.</p>
     *
     * @return <p>This portal's owner</p>
     */
    @NotNull
    public PortalOwner getOwner() {
        return portalOwner;
    }

    /**
     * Checks whether a given player is the owner of this portal
     *
     * @param player <p>The player to check</p>
     * @return <p>True if the player is the owner of this portal</p>
     */
    public boolean isOwner(@NotNull Player player) {
        if (this.portalOwner.getUUID() != null) {
            return player.getUniqueId().compareTo(this.portalOwner.getUUID()) == 0;
        } else {
            return player.getName().equalsIgnoreCase(this.portalOwner.getName());
        }
    }

    /**
     * Gets the world this portal belongs to
     *
     * @return <p>The world this portal belongs to</p>
     */
    @Nullable
    public World getWorld() {
        return location.getWorld();
    }

    /**
     * Gets the location of this portal's sign
     *
     * @return <p>The location of this portal's sign</p>
     */
    @NotNull
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
    @NotNull
    public BlockLocation getTopLeft() {
        return this.location.getTopLeft();
    }

    /**
     * Gets the block at the given location relative to this portal's top-left block
     *
     * @param vector <p>The relative block vector explaining the position of the block</p>
     * @return <p>The block at the given relative position</p>
     */
    @NotNull
    public BlockLocation getBlockAt(@NotNull RelativeBlockVector vector) {
        return getTopLeft().getRelativeLocation(vector, getYaw());
    }

    /**
     * Cleans a string by removing color codes, lower-casing and replacing spaces with underscores
     *
     * @param string <p>The string to clean</p>
     * @return <p>The clean string</p>
     */
    @NotNull
    public static String cleanString(@NotNull String string) {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string)).toLowerCase();
    }

    @Override
    @NotNull
    public String toString() {
        return String.format("Portal [id=%s, network=%s name=%s, type=%s]", getSignLocation(), network, name,
                structure.getGate().getFilename());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cleanName == null) ? 0 : cleanName.hashCode());
        result = prime * result + ((cleanNetwork == null) ? 0 : cleanNetwork.hashCode());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Portal other = (Portal) object;
        if (cleanName == null) {
            if (other.cleanName != null) {
                return false;
            }
        } else if (!cleanName.equalsIgnoreCase(other.cleanName)) {
            return false;
        }
        //If none of the portals have a name, check if the network is the same
        if (cleanNetwork == null) {
            return other.cleanNetwork == null;
        } else {
            return cleanNetwork.equalsIgnoreCase(other.cleanNetwork);
        }
    }

}
