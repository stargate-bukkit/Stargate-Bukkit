package net.knarcraft.stargate.event;

import net.knarcraft.stargate.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event should be called whenever a player attempts to access a stargate
 */
@SuppressWarnings("unused")
public class StargateAccessEvent extends StargatePlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private boolean deny;

    /**
     * Instantiates a new stargate access event
     *
     * @param player <p>The player involved in the vent</p>
     * @param portal <p>The portal involved in the event</p>
     * @param deny   <p>Whether the event should be denied</p>
     */
    public StargateAccessEvent(Player player, Portal portal, boolean deny) {
        super("StargateAccessEvent", portal, player);

        this.deny = deny;
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets whether the player should be denied access
     *
     * @return <p>Whether the player should be denied access</p>
     */
    public boolean getDeny() {
        return this.deny;
    }

    /**
     * Sets whether to deny the player
     *
     * @param deny <p>Whether to deny the player</p>
     */
    public void setDeny(boolean deny) {
        this.deny = deny;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

}
