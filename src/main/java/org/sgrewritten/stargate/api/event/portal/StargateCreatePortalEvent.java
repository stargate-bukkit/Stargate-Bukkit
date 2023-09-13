package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * This event should be called whenever a stargate is created
 *
 * <p>This event can be used to deny or change the cost of a stargate creation.</p>
 */
@SuppressWarnings("unused")
public class StargateCreatePortalEvent extends DeniableStargatePortalEvent {

    private static final HandlerList handlers = new HandlerList();
    private final String[] lines;
    private int cost;

    /**
     * Instantiates a new stargate creation event
     *
     * @param player     <p>Thg player creating the stargate</p>
     * @param portal     <p>The created portal</p>
     * @param lines      <p>The lines of the sign creating the star gate</p>
     * @param deny       <p>Whether to deny the creation of the new gate</p>
     * @param denyReason <p>The reason stargate creation was denied</p>
     * @param cost       <p>The cost of creating the new star gate</p>
     */
    public StargateCreatePortalEvent(@NotNull Player player, @NotNull Portal portal, @NotNull String[] lines, boolean deny,
                                     String denyReason, int cost) {
        super(portal, player, deny, denyReason);

        this.lines = lines;
        this.cost = cost;
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
     * @param flag <p>The flag to add</p>
     */
    public void addFlag(Character flag) throws UnsupportedOperationException{
        this.getPortal().addFlag(flag);
    }

    /**
     * Remove flag to related portal and save to storage
     * @param flag
     */
    public void removeFlag(Character flag) throws UnsupportedOperationException{
        this.getPortal().removeFlag(flag);
    }

    /**
     * Gets the cost of creating the stargate
     *
     * @return <p>The cost of creating the stargate</p>
     */
    public int getCost() {
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

}