package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockChangeRequest;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.event.StargateCloseEvent;
import net.knarcraft.stargate.event.StargateOpenEvent;
import net.knarcraft.stargate.portal.property.PortalOptions;
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
    private long triggeredTime;
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
     * Gets whether this portal opener's portal is currently open
     *
     * @return <p>Whether this portal opener's portal is open</p>
     */
    public boolean isOpen() {
        return isOpen || portal.getOptions().isAlwaysOn();
    }

    /**
     * Sets the time when this portal was triggered (activated/opened)
     *
     * @param triggeredTime <p>Unix timestamp when portal was triggered</p>
     */
    public void setTriggeredTime(long triggeredTime) {
        this.triggeredTime = triggeredTime;
    }

    /**
     * Gets the portal activator belonging to this portal opener
     *
     * @return <p>The portal activator belonging to this portal opener</p>
     */
    public PortalActivator getPortalActivator() {
        return this.portalActivator;
    }

    /**
     * Open this portal opener's portal
     *
     * @param force <p>Whether to force the portal open, even if it's already open for some player</p>
     */
    public void openPortal(boolean force) {
        openPortal(null, force);
    }

    /**
     * Open this portal opener's portal
     *
     * @param openFor <p>The player to open the portal for</p>
     * @param force   <p>Whether to force the portal open, even if it's already open for some player</p>
     */
    public void openPortal(Player openFor, boolean force) {
        //Call the StargateOpenEvent to allow the opening to be cancelled
        StargateOpenEvent event = new StargateOpenEvent(openFor, portal, force);
        Stargate.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled() || (isOpen() && !event.getForce())) {
            return;
        }

        //Get the material to change the opening to
        Material openType = portal.getGate().getPortalOpenBlock();
        //Adjust orientation if applicable
        Axis axis = (openType.createBlockData() instanceof Orientable) ? portal.getLocation().getRotationAxis() : null;

        //Change the entrance blocks to the correct type
        for (BlockLocation inside : portal.getStructure().getEntrances()) {
            Stargate.addBlockChangeRequest(new BlockChangeRequest(inside, openType, axis));
        }

        //Update the portal state to make is actually open
        updatePortalOpenState(openFor);
    }

    /**
     * Updates this portal opener's portal to be recognized as open and opens its destination portal
     *
     * @param openFor <p>The player to open this portal opener's portal for</p>
     */
    private void updatePortalOpenState(Player openFor) {
        //Update the open state of this portal
        isOpen = true;
        triggeredTime = System.currentTimeMillis() / 1000;

        //Change state from active to open
        Stargate.getStargateConfig().getOpenPortalsQueue().add(portal);
        Stargate.getStargateConfig().getActivePortalsQueue().remove(portal);

        PortalOptions options = portal.getOptions();

        //If this portal is always open, opening the destination is not necessary
        if (options.isAlwaysOn()) {
            return;
        }

        //Update the player the portal is open for
        this.player = openFor;

        Portal destination = portal.getPortalActivator().getDestination();
        if (destination == null) {
            return;
        }

        boolean thisIsDestination = Portal.cleanString(destination.getDestinationName()).equals(portal.getCleanName());
        //Only open destination if it's not-fixed or points at this portal, and is not already open
        if (!options.isRandom() && (!destination.getOptions().isFixed() || thisIsDestination) && !destination.isOpen()) {
            //Open the destination portal
            destination.getPortalOpener().openPortal(openFor, false);
            //Set the destination portal to this opener's portal
            destination.getPortalActivator().setDestination(portal);

            //Update the destination's sign if it's verified
            if (destination.getStructure().isVerified()) {
                destination.drawSign();
            }
        }
    }

    /**
     * Closes this portal opener's portal
     *
     * @param force <p>Whether to force the portal closed, even if it's set as always on</p>
     */
    public void closePortal(boolean force) {
        //No need to close a portal which is already closed
        if (!isOpen()) {
            return;
        }

        //Call the StargateCloseEvent to allow other plugins to cancel the closing, or change whether to force it closed
        StargateCloseEvent event = new StargateCloseEvent(portal, force);
        Stargate.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        //Only close an always-open portal if forced to
        if (portal.getOptions().isAlwaysOn() && !event.getForce()) {
            return;
        }

        //Close the portal by requesting the opening blocks to change
        Material closedType = portal.getGate().getPortalClosedBlock();
        for (BlockLocation entrance : portal.getStructure().getEntrances()) {
            Stargate.addBlockChangeRequest(new BlockChangeRequest(entrance, closedType, null));
        }

        //Update the portal state to make it actually closed
        updatePortalClosedState();

        //Finally, deactivate the portal
        portalActivator.deactivate();
    }

    /**
     * Updates this portal to be recognized as closed and closes its destination portal
     */
    private void updatePortalClosedState() {
        //Unset the stored player and set the portal to closed
        player = null;
        isOpen = false;

        //Un-mark the portal as active and open
        Stargate.getStargateConfig().getOpenPortalsQueue().remove(portal);
        Stargate.getStargateConfig().getActivePortalsQueue().remove(portal);

        //Close the destination portal if not always open
        if (!portal.getOptions().isAlwaysOn()) {
            Portal destination = portal.getPortalActivator().getDestination();

            if (destination != null && destination.isOpen()) {
                //De-activate and close the destination portal
                destination.getPortalActivator().deactivate();
                destination.getPortalOpener().closePortal(false);
            }
        }
    }

    /**
     * Gets whether this portal opener's portal is open for the given player
     *
     * @param player <p>The player to check portal state for</p>
     * @return <p>True if this portal opener's portal is open to the given player</p>
     */
    public boolean isOpenFor(Player player) {
        //If closed, it's closed for everyone
        if (!isOpen) {
            return false;
        }
        //If always on, or player is null which only happens with an always on portal, allow the player to pass
        if (portal.getOptions().isAlwaysOn() || this.player == null) {
            return true;
        }
        //If the player is the player which the portal opened for, allow it to pass
        return player != null && player.getName().equalsIgnoreCase(this.player.getName());
    }

    /**
     * Gets the time this portal opener's portal was triggered (activated/opened)
     *
     * @return <p>The time this portal opener's portal was triggered</p>
     */
    public long getTriggeredTime() {
        return triggeredTime;
    }

}
