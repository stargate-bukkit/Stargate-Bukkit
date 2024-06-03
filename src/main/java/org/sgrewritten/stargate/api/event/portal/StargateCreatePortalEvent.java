package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;

/**
 * This event should be called whenever a stargate is created
 *
 * <p>This event can be used to deny or change the cost of a stargate creation.</p>
 */
@SuppressWarnings("unused")
public class StargateCreatePortalEvent extends StargatePortalEvent{

    private static final HandlerList handlers = new HandlerList();
    private final String[] lines;
    private boolean deny;
    private String denyReason;
    private double cost;

    /**
     * Instantiates a new stargate creation event
     *
     * <p>This is called asynchronously!</p>
     *
     * @param player     <p>The player creating the stargate</p>
     * @param portal     <p>The created portal</p>
     * @param lines      <p>The lines of the sign creating the star gate</p>
     * @param deny       <p>Whether to deny the creation of the new gate</p>
     * @param denyReason <p>The reason stargate creation was denied</p>
     * @param cost       <p>The cost of creating the new star gate</p>
     */
    public StargateCreatePortalEvent(@NotNull OfflinePlayer player, @NotNull Portal portal, @NotNull String[] lines, boolean deny,
                                     String denyReason, double cost) {
        super(portal, false);
        this.lines = lines;
        this.cost = cost;
        this.deny = deny;
        this.denyReason = denyReason;
    }

    /**
     * Gets a given line from the sign creating the star gate
     *
     * @param index <p>The line number to get</p>
     * @return <p>The text on the given line</p>
     * @throws IndexOutOfBoundsException <p>If given a line index less than zero or above three</p>
     */
    public String getLine(int index) throws IndexOutOfBoundsException {
        return lines[index];
    }

    /**
     * Add flag to related portal and save to storage
     *
     * @param flag <p>The flag to add</p>
     */
    public void addFlag(PortalFlag flag) throws UnsupportedOperationException {
        this.getPortal().addFlag(flag);
    }

    /**
     * Remove flag to related portal and save to storage
     *
     * @param flag <p>The flag to remove</p>
     */
    public void removeFlag(PortalFlag flag) throws UnsupportedOperationException {
        this.getPortal().removeFlag(flag);
    }

    /**
     * Gets the cost of creating the stargate
     *
     * @return <p>The cost of creating the stargate</p>
     */
    public double getCost() {
        return cost;
    }

    /**
     * Sets the cost of creating the stargate
     *
     * @param cost <p>The new cost of creating the stargate</p>
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



    /**
     * Gets whether the entity should be denied access
     *
     * @return <p>Whether the entity should be denied access</p>
     */
    public boolean getDeny() {
        return this.deny;
    }

    /**
     * Sets whether to deny access to the entity
     *
     * @param deny <p>Whether to deny access to the entity</p>
     */
    public void setDeny(boolean deny) {
        this.deny = deny;
    }

    /**
     * Gets the reason the stargate access was denied
     *
     * @return <p>The reason the stargate access was denied</p>
     */
    public String getDenyReason() {
        return denyReason;
    }

    /**
     * Sets the reason the stargate access was denied
     *
     * <p>Set to null for a generic message. Set to empty for no message.</p>
     *
     * @param denyReason <p>The new reason why the stargate access was denied</p>
     */
    public void setDenyReason(String denyReason) {
        this.denyReason = denyReason;
    }
}