package net.TheDgtl.Stargate.event;

import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event should be called whenever a stargate is destroyed
 *
 * <p>This event can be used to deny or change the cost of a stargate destruction.</p>
 */
@SuppressWarnings("unused")
public class StargateDestroyEvent extends StargateEntityEvent {

    private static final HandlerList handlers = new HandlerList();
    private boolean deny;
    private String denyReason;
    private int cost;

    /**
     * Instantiates a new Stargate Destroy Event
     *
     * @param portal  <p>The destroyed portal</p>
     * @param player  <p>The player destroying the portal</p>
     * @param deny    <p>Whether the event should be denied (cancelled)</p>
     * @param denyMsg <p>The message to display if the event is denied</p>
     * @param cost    <p>The cost of destroying the portal</p>
     */
    public StargateDestroyEvent(@NotNull Portal portal, @NotNull Player player, boolean deny, @NotNull String denyMsg,
                                int cost) {
        super(portal, player);
        
        //TODO: Perhaps alter or add an event for a stargate destroyed by an explosion?
        this.deny = deny;
        this.denyReason = denyMsg;
        this.cost = cost;
    }

    /**
     * Gets whether this event should be denied
     *
     * @return <p>Whether this event should be denied</p>
     */
    public boolean getDeny() {
        return deny;
    }

    /**
     * Sets whether this event should be denied
     *
     * @param deny <p>Whether this event should be denied</p>
     */
    public void setDeny(boolean deny) {
        this.deny = deny;
    }

    /**
     * Gets the reason the event was denied
     *
     * @return <p>The reason the event was denied</p>
     */
    public String getDenyReason() {
        return denyReason;
    }

    /**
     * Sets the reason the event was denied
     *
     * @param denyReason <p>The reason the event was denied</p>
     */
    public void setDenyReason(@NotNull String denyReason) {
        this.denyReason = denyReason;
    }

    /**
     * Gets the cost of destroying the portal
     *
     * @return <p>The cost of destroying the portal</p>
     */
    public int getCost() {
        return cost;
    }

    /**
     * Sets the cost of destroying the portal
     *
     * @param cost <p>The cost of destroying the portal</p>
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}