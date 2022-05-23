package net.TheDgtl.Stargate.event;

import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event should be called whenever a stargate is created
 *
 * <p>This event can be used to deny or change the cost of a stargate creation.</p>
 */
@SuppressWarnings("unused")
public class StargateCreateEvent extends StargateEntityEvent {

    private static final HandlerList handlers = new HandlerList();
    private final String[] lines;
    private boolean deny;
    private String denyReason;
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
    public StargateCreateEvent(@NotNull Player player, @NotNull Portal portal, @NotNull String[] lines, boolean deny,
                               @NotNull String denyReason, int cost) {
        super(portal, player);
        
        this.lines = lines;
        this.deny = deny;
        this.denyReason = denyReason;
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
     * Gets whether the stargate creation should be denied
     *
     * @return <p>Whether the stargate creation should be denied</p>
     */
    public boolean getDeny() {
        return deny;
    }

    /**
     * Sets whether the stargate creation should be denied
     *
     * @param deny <p>Whether the stargate creation should be denied</p>
     */
    public void setDeny(boolean deny) {
        this.deny = deny;
    }

    /**
     * Gets the reason the stargate creation was denied
     *
     * @return <p>The reason the stargate creation was denied</p>
     */
    public String getDenyReason() {
        return denyReason;
    }

    /**
     * Sets the reason the stargate creation was denied
     *
     * @param denyReason <p>The new reason why the stargate creation was denied</p>
     */
    public void setDenyReason(@NotNull String denyReason) {
        this.denyReason = denyReason;
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