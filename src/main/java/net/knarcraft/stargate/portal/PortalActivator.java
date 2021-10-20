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
 * The portal destinations contain information about a portal's destinations, and is responsible for cycling destinations
 *
 * <p>The activator is responsible for activating/de-activating the portal and contains information about
 * available destinations and which player activated the portal.</p>
 */
public class PortalActivator {

    private String destination;
    private String lastDestination = "";
    private List<String> destinations = new ArrayList<>();
    private final Portal portal;
    private final PortalOpener activator;
    private Player activePlayer;

    /**
     * Instantiates a new portal destinations object
     *
     * @param portal      <p>The portal which this this object stores destinations for</p>
     * @param activator   <p>The activator to use when a player activates a portal</p>
     * @param destination <p></p>
     */
    public PortalActivator(Portal portal, PortalOpener activator, String destination) {
        this.portal = portal;
        this.activator = activator;
        this.destination = destination;
    }

    /**
     * Gets the player currently using this portal activator's portal
     *
     * @return <p>The player currently using this portal activator's portal</p>
     */
    public Player getActivePlayer() {
        return activePlayer;
    }

    /**
     * Gets the destinations of this portal
     *
     * @return <p>The destinations of this portal</p>
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
        if (portal.getOptions().isRandom()) {
            destinations = PortalHandler.getDestinations(portal, player, portal.getNetwork());
            if (destinations.size() == 0) {
                return null;
            }
            String destination = destinations.get((new Random()).nextInt(destinations.size()));
            destinations.clear();
            return PortalHandler.getByName(destination, portal.getNetwork());
        }
        return PortalHandler.getByName(destination, portal.getNetwork());
    }

    /**
     * Activates this portal for the given player
     *
     * @param player <p>The player to activate the portal for</p>
     * @return <p>True if the portal was activated</p>
     */
    boolean activate(Player player) {
        this.destination = "";
        this.destinations.clear();
        Stargate.activePortalsQueue.add(portal);
        activePlayer = player;
        String network = portal.getNetwork();
        destinations = PortalHandler.getDestinations(portal, player, network);
        if (Stargate.sortNetworkDestinations) {
            Collections.sort(destinations);
        }
        if (Stargate.rememberDestination && !lastDestination.isEmpty() && destinations.contains(lastDestination)) {
            destination = lastDestination;
        }

        StargateActivateEvent event = new StargateActivateEvent(portal, player, destinations, destination);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            Stargate.activePortalsQueue.remove(portal);
            return false;
        }
        destination = event.getDestination();
        destinations = event.getDestinations();
        portal.drawSign();
        return true;
    }

    /**
     * Deactivates this portal
     */
    public void deactivate() {
        StargateDeactivateEvent event = new StargateDeactivateEvent(portal);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        Stargate.activePortalsQueue.remove(portal);
        if (portal.getOptions().isFixed()) {
            return;
        }
        destinations.clear();
        destination = "";
        activePlayer = null;
        portal.drawSign();
    }

    /**
     * Gets whether this portal is active
     *
     * @return <p>Whether this portal is active</p>
     */
    public boolean isActive() {
        return portal.getOptions().isFixed() || (destinations.size() > 0);
    }

    /**
     * Gets the portal destination
     *
     * <p>If this portal is random, a player should be given to get correct destinations.</p>
     *
     * @return <p>The portal destination</p>
     */
    public Portal getDestination() {
        return getDestination(null);
    }

    /**
     * Sets the destination of this portal
     *
     * @param destination <p>The new destination of this portal</p>
     */
    public void setDestination(Portal destination) {
        setDestination(destination.getName());
    }

    /**
     * Sets the destination of this portal
     *
     * @param destination <p>The new destination of this portal</p>
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Gets the name of the destination of this portal
     *
     * @return <p>The name of this portal's destination</p>
     */
    public String getDestinationName() {
        return destination;
    }

    /**
     * Cycles destination for a network gate forwards
     *
     * @param player <p>The player to cycle the gate for</p>
     */
    public void cycleDestination(Player player) {
        cycleDestination(player, 1);
    }

    /**
     * Cycles destination for a network gate
     *
     * @param player    <p>The player cycling destinations</p>
     * @param direction <p>The direction of the cycle (+1 for next, -1 for previous)</p>
     */
    public void cycleDestination(Player player, int direction) {
        if (direction != 1 && direction != -1) {
            throw new IllegalArgumentException("The destination direction must be 1 or -1.");
        }

        boolean activate = false;
        if (!isActive() || getActivePlayer() != player) {
            //If the stargate activate event is cancelled, return
            if (!activate(player)) {
                return;
            }
            Stargate.debug("cycleDestination", "Network Size: " +
                    PortalHandler.getNetwork(portal.getNetwork()).size());
            Stargate.debug("cycleDestination", "Player has access to: " + destinations.size());
            activate = true;
        }

        if (destinations.size() == 0) {
            Stargate.sendErrorMessage(player, Stargate.getString("destEmpty"));
            return;
        }

        if (!Stargate.rememberDestination || !activate || lastDestination.isEmpty()) {
            cycleDestination(direction);
        }
        activator.setOpenTime(System.currentTimeMillis() / 1000);
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
