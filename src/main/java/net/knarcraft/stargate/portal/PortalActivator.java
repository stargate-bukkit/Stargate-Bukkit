package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.event.StargateActivateEvent;
import net.knarcraft.stargate.event.StargateDeactivateEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The portal activator activates/de-activates portals and keeps track of a portal's destinations
 *
 * <p>The portal activator is responsible for activating/de-activating the portal and contains information about
 * available destinations and which player activated the portal.</p>
 */
public class PortalActivator {

    private final Portal portal;
    private final PortalOpener opener;

    private List<String> destinations = new ArrayList<>();
    private String destination;
    private String lastDestination = "";
    private Player activePlayer;

    /**
     * Instantiates a new portal destinations object
     *
     * @param portal       <p>The portal which this this object stores destinations for</p>
     * @param portalOpener <p>The portal opener to trigger when the activation causes the portal to open</p>
     * @param destination  <p>The fixed destination specified on the portal's sign</p>
     */
    public PortalActivator(Portal portal, PortalOpener portalOpener, String destination) {
        this.portal = portal;
        this.opener = portalOpener;
        this.destination = destination;
    }

    /**
     * Gets the player which this activator's portal is currently activated for
     *
     * @return <p>The player this activator's portal is currently activated for</p>
     */
    public Player getActivePlayer() {
        return activePlayer;
    }

    /**
     * Gets the available portal destinations
     *
     * @return <p>The available portal destinations</p>
     */
    public List<String> getDestinations() {
        return new ArrayList<>(this.destinations);
    }

    /**
     * Gets the portal destination given a player
     *
     * @param player <p>Used for random gates to determine which destinations are available</p>
     * @return <p>The destination portal the player should teleport to</p>
     */
    public Portal getDestination(Player player) {
        String portalNetwork = portal.getCleanNetwork();
        if (portal.getOptions().isRandom()) {
            //Find possible destinations
            List<String> destinations = PortalHandler.getDestinations(portal, player, portalNetwork);
            if (destinations.size() == 0) {
                return null;
            }
            //Get one random destination
            String destination = destinations.get((new Random()).nextInt(destinations.size()));
            return PortalHandler.getByName(Portal.cleanString(destination), portalNetwork);
        } else {
            //Just return the normal fixed destination
            return PortalHandler.getByName(Portal.cleanString(destination), portalNetwork);
        }
    }

    /**
     * Gets the portal's destination
     *
     * <p>For random portals, getDestination must be given a player to decide which destinations are valid. Without a
     * player, or with a null player, behavior is only defined for a non-random gate.</p>
     *
     * @return <p>The portal destination</p>
     */
    public Portal getDestination() {
        return getDestination(null);
    }

    /**
     * Sets the destination of this portal activator's portal
     *
     * @param destination <p>The new destination of this portal activator's portal</p>
     */
    public void setDestination(Portal destination) {
        setDestination(destination.getName());
    }

    /**
     * Sets the destination of this portal activator's portal
     *
     * @param destination <p>The new destination of this portal activator's portal</p>
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Gets the name of the selected destination
     *
     * @return <p>The name of the selected destination</p>
     */
    public String getDestinationName() {
        return destination;
    }

    /**
     * Activates this activator's portal for the given player
     *
     * @param player <p>The player to activate the portal for</p>
     * @return <p>True if the portal was activated</p>
     */
    boolean activate(Player player) {
        //Clear previous destination data
        this.destination = "";
        this.destinations.clear();

        //Adds the active gate to the active queue to allow it to be remotely deactivated
        Stargate.getStargateConfig().getActivePortalsQueue().add(portal);

        //Set the given player as the active player
        activePlayer = player;

        String network = portal.getCleanNetwork();
        destinations = PortalHandler.getDestinations(portal, player, network);

        //Sort destinations if enabled
        if (Stargate.getGateConfig().sortNetworkDestinations()) {
            Collections.sort(destinations);
        }

        //Select last used destination if remember destination is enabled
        if (Stargate.getGateConfig().rememberDestination() && !lastDestination.isEmpty() &&
                destinations.contains(lastDestination)) {
            destination = lastDestination;
        }

        //Trigger an activation event to allow the cancellation to be cancelled
        return triggerStargateActivationEvent(player);
    }

    /**
     * Triggers a stargate activation event to allow other plugins to cancel the activation
     *
     * <p>The event may also end up changing destinations.</p>
     *
     * @param player <p>The player trying to activate this activator's portal</p>
     * @return <p>True if the portal was activated. False otherwise</p>
     */
    private boolean triggerStargateActivationEvent(Player player) {
        StargateActivateEvent event = new StargateActivateEvent(portal, player, destinations, destination);
        Stargate.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            Stargate.getStargateConfig().getActivePortalsQueue().remove(portal);
            return false;
        }

        //Update destinations in case they changed, and update the sign
        destination = event.getDestination();
        destinations = event.getDestinations();
        portal.drawSign();
        return true;
    }

    /**
     * Deactivates this portal
     */
    public void deactivate() {
        //Trigger a stargate deactivate event to allow other plugins to cancel the event
        StargateDeactivateEvent event = new StargateDeactivateEvent(portal);
        Stargate.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        //Un-mark the portal as activated
        Stargate.getStargateConfig().getActivePortalsQueue().remove(portal);

        //Fixed portals are active by definition, but should never be de-activated
        if (portal.getOptions().isFixed()) {
            return;
        }

        //Clear destinations and the active player before re-drawing the sign to show that it's deactivated
        destinations.clear();
        destination = "";
        activePlayer = null;
        portal.drawSign();
    }

    /**
     * Gets whether this portal activator's portal is active
     *
     * @return <p>Whether this portal activator's portal is active</p>
     */
    public boolean isActive() {
        return portal.getOptions().isFixed() || (destinations.size() > 0);
    }

    /**
     * Cycles destination for a non-fixed gate by one forwards step
     *
     * @param player <p>The player to cycle the gate for</p>
     */
    public void cycleDestination(Player player) {
        cycleDestination(player, 1);
    }

    /**
     * Cycles destination for a non-fixed gate
     *
     * @param player    <p>The player cycling destinations</p>
     * @param direction <p>The direction of the cycle (+1 for next, -1 for previous)</p>
     */
    public void cycleDestination(Player player, int direction) {
        //Only allow going exactly one step in either direction
        if (direction != 1 && direction != -1) {
            throw new IllegalArgumentException("The destination direction must be 1 or -1.");
        }

        boolean activate = false;
        if (!isActive() || getActivePlayer() != player) {
            //If not active or not active for the given player, and the activation is denied, just abort
            if (!activate(player)) {
                return;
            }
            activate = true;

            Stargate.debug("cycleDestination", "Network Size: " +
                    PortalHandler.getNetwork(portal.getCleanNetwork()).size());
            Stargate.debug("cycleDestination", "Player has access to: " + destinations.size());
        }

        //If no destinations are available, just tell the player and quit
        if (destinations.size() == 0) {
            if (!portal.getOptions().isSilent()) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("destEmpty"));
            }
            return;
        }

        //Cycle if destination remembering is disabled, if the portal was already active, or it has no last destination
        if (!Stargate.getGateConfig().rememberDestination() || !activate || lastDestination.isEmpty()) {
            cycleDestination(direction);
        }

        //Update the activated time to allow it to be deactivated after a timeout, and re-draw the sign to show the
        // selected destination
        opener.setTriggeredTime(System.currentTimeMillis() / 1000);
        portal.drawSign();
    }

    /**
     * Performs the actual destination cycling with no input checks
     *
     * @param direction <p>The direction of the cycle (+1 for next, -1 for previous)</p>
     */
    private void cycleDestination(int direction) {
        int index = destinations.indexOf(destination);
        index += direction;

        //Wrap around if the last destination has been reached
        if (index >= destinations.size()) {
            index = 0;
        } else if (index < 0) {
            index = destinations.size() - 1;
        }
        //Store selected destination
        destination = destinations.get(index);
        lastDestination = destination;
    }

}
