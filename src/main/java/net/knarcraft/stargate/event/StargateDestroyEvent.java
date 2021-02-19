package net.knarcraft.stargate.event;

import net.knarcraft.stargate.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event represents an event where a star gate is destroyed or attempted to be destroyed
 */
@SuppressWarnings("unused")
public class StargateDestroyEvent extends StargatePlayerEvent {

    private boolean deny;
    private String denyReason;
    private int cost;

    private static final HandlerList handlers = new HandlerList();

    /**
     * Instantiates a new Stargate Destroy Event
     * @param portal <p>The portal destroyed</p>
     * @param player <p>The player destroying the portal</p>
     * @param deny <p>Whether the event should be denied (cancelled)</p>
     * @param denyMsg <p>The message to display if the event is denied</p>
     * @param cost <p>The cost of destroying the portal</p>
     */
    public StargateDestroyEvent(Portal portal, Player player, boolean deny, String denyMsg, int cost) {
        super("StargateDestroyEvent", portal, player);
        this.deny = deny;
        this.denyReason = denyMsg;
        this.cost = cost;
    }

    /**
     * Gets whether this event should be denied
     * @return <p>Whether this event should be denied</p>
     */
    public boolean getDeny() {
        return deny;
    }

    /**
     * Sets whether this event should be denied
     * @param deny <p>Whether this event should be denied</p>
     */
    public void setDeny(boolean deny) {
        this.deny = deny;
    }

    /**
     * Gets the reason the event was denied
     * @return <p>The reason the event was denied</p>
     */
    public String getDenyReason() {
        return denyReason;
    }

    /**
     * Sets the reason the event was denied
     * @param denyReason <p>The reason the event was denied</p>
     */
    public void setDenyReason(String denyReason) {
        this.denyReason = denyReason;
    }

    /**
     * Gets the cost of destroying the portal
     * @return <p>The cost of destroying the portal</p>
     */
    public int getCost() {
        return cost;
    }

    /**
     * Sets the cost of destroying the portal
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
