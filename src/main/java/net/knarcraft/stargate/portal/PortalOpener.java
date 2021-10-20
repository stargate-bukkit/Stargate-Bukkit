package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockChangeRequest;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.event.StargateCloseEvent;
import net.knarcraft.stargate.event.StargateOpenEvent;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;

/**
 * The portal opener is responsible for opening and closing a portal
 */
public class PortalOpener {

    private boolean isOpen = false;
    private final Portal portal;
    private long openTime;
    private Player player;
    private final PortalActivator portalActivator;

    /**
     * Instantiates a new portal opener
     *
     * @param portal      <p>The portal this portal opener should open</p>
     * @param destination <p>The fixed destination defined on the portal's sign</p>
     */
    public PortalOpener(Portal portal, String destination) {
        this.portal = portal;
        this.portalActivator = new PortalActivator(portal, this, destination);
    }

    /**
     * Gets whether this portal activator's portal is currently open
     *
     * @return <p>Whether this portal activator's portal is open</p>
     */
    public boolean isOpen() {
        return isOpen || portal.getOptions().isAlwaysOn();
    }

    /**
     * Sets the time when this portal was activated
     *
     * @param openTime <p>Unix timestamp when portal was activated</p>
     */
    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }

    /**
     * Gets the destinations this portal activator has available
     *
     * @return <p>The available destinations</p>
     */
    public PortalActivator getPortalOpener() {
        return this.portalActivator;
    }

    /**
     * Open this portal
     *
     * @param force <p>Whether to force this portal open, even if it's already open for some player</p>
     */
    public void openPortal(boolean force) {
        openPortal(null, force);
    }

    /**
     * Open this portal
     *
     * @param force <p>Whether to force this portal open, even if it's already open for some player</p>
     */
    public void openPortal(Player openFor, boolean force) {
        //Call the StargateOpenEvent
        StargateOpenEvent event = new StargateOpenEvent(openFor, portal, force);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled() || (isOpen() && !event.getForce())) {
            return;
        }

        //Change the opening blocks to the correct type
        Material openType = portal.getGate().getPortalOpenBlock();
        Axis axis = (openType.createBlockData() instanceof Orientable) ? portal.getLocation().getRotationAxis() : null;
        for (BlockLocation inside : portal.getStructure().getEntrances()) {
            Stargate.blockChangeRequestQueue.add(new BlockChangeRequest(inside, openType, axis));
        }

        updatePortalOpenState(openFor);
    }

    /**
     * Updates this portal to be recognized as open and opens its destination portal
     *
     * @param openFor <p>The player to open this portal for</p>
     */
    private void updatePortalOpenState(Player openFor) {
        //Update the open state of this portal
        isOpen = true;
        openTime = System.currentTimeMillis() / 1000;
        Stargate.openPortalsQueue.add(portal);
        Stargate.activePortalsQueue.remove(portal);
        PortalOptions options = portal.getOptions();

        //Open remote portal
        if (!options.isAlwaysOn()) {
            player = openFor;

            Portal destination = portal.getPortalActivator().getDestination();
            //Only open destination if it's not-fixed or points at this portal
            if (!options.isRandom() && destination != null && (!destination.getOptions().isFixed() ||
                    destination.getDestinationName().equalsIgnoreCase(portal.getName())) && !destination.isOpen()) {
                destination.getPortalOpener().openPortal(openFor, false);
                destination.getPortalActivator().setDestination(portal);
                if (destination.getStructure().isVerified()) {
                    destination.drawSign();
                }
            }
        }
    }

    /**
     * Closes this portal
     *
     * @param force <p>Whether to force this portal closed, even if it's set as always on</p>
     */
    public void closePortal(boolean force) {
        if (!isOpen) {
            return;
        }
        //Call the StargateCloseEvent
        StargateCloseEvent event = new StargateCloseEvent(portal, force);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        force = event.getForce();

        //Only close always-open if forced to
        if (portal.getOptions().isAlwaysOn() && !force) {
            return;
        }

        //Close this gate, then the dest gate.
        Material closedType = portal.getGate().getPortalClosedBlock();
        for (BlockLocation inside : portal.getStructure().getEntrances()) {
            Stargate.blockChangeRequestQueue.add(new BlockChangeRequest(inside, closedType, null));
        }

        updatePortalClosedState();
        portalActivator.deactivate();
    }

    /**
     * Updates this portal to be recognized as closed and closes its destination portal
     */
    private void updatePortalClosedState() {
        //Update the closed state of this portal
        player = null;
        isOpen = false;
        Stargate.openPortalsQueue.remove(portal);
        Stargate.activePortalsQueue.remove(portal);

        //Close remote portal
        if (!portal.getOptions().isAlwaysOn()) {
            Portal end = portal.getPortalActivator().getDestination();

            if (end != null && end.isOpen()) {
                //Clear its destination first
                end.getPortalActivator().deactivate();
                end.getPortalOpener().closePortal(false);
            }
        }
    }

    /**
     * Gets whether this portal is open for the given player
     *
     * @param player <p>The player to check portal state for</p>
     * @return <p>True if this portal is open to the given player</p>
     */
    public boolean isOpenFor(Player player) {
        if (!isOpen) {
            return false;
        }
        if (portal.getOptions().isAlwaysOn() || this.player == null) {
            return true;
        }
        return player != null && player.getName().equalsIgnoreCase(this.player.getName());
    }

    /**
     * Gets the time this portal activator's portal opened
     *
     * @return <p>The time this portal activator's portal opened</p>
     */
    public long getOpenTime() {
        return openTime;
    }

}
