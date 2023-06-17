package org.sgrewritten.stargate.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

import java.util.List;

/**
 * This event should be called whenever a player activates a stargate
 *
 * <p>Activation of a stargate happens when a player right-clicks the sign of a stargate.
 * This event can be used to overwrite the selected destination, and all destinations the player can see.</p>
 */
@SuppressWarnings("unused")
public class StargateActivateEvent extends StargateEntityEvent {

    private static final HandlerList handlers = new HandlerList();
    private List<String> destinations;
    private String destination;

    /**
     * Instantiates a new stargate activate event
     *
     * @param portal       <p>The activated portal</p>
     * @param entity       <p>The entity activating the portal</p>
     * @param destinations <p>The destinations available to the player using the portal</p>
     * @param destination  <p>The currently selected destination</p>
     */
    public StargateActivateEvent(@NotNull Portal portal, @NotNull Entity entity, @NotNull List<String> destinations,
                                 String destination) {
        super(portal, entity);

        this.destinations = destinations;
        this.destination = destination;
    }

    /**
     * Gets the destinations available for the portal
     *
     * @return <p>The destinations available for the portal</p>
     */
    public List<String> getDestinations() {
        return destinations;
    }

    /**
     * Sets the destinations available to the player using the portal
     *
     * @param destinations <p>The new list of available destinations</p>
     */
    public void setDestinations(@NotNull List<String> destinations) {
        this.destinations = destinations;
    }

    /**
     * Gets the selected destination
     *
     * @return <p>The selected destination</p>
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets (changes) the selected destination
     *
     * @param destination <p>The new selected destination</p>
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

}