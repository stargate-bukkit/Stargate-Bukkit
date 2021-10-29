package net.knarcraft.stargate.event;

import net.knarcraft.stargate.portal.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event should be called whenever a player attempts to access a stargate
 *
 * <p>This event is triggered whenever a player enters or activates a stargate. This event can be used to override 
 * whether the player should be allowed to access the stargate.</p>
 */
@SuppressWarnings("unused")
public class StargateAccessEvent extends StargatePlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private boolean deny;

    /**
     * Instantiates a new stargate access event
     *
     * @param player <p>The player involved in the event</p>
     * @param portal <p>The portal involved in the event</p>
     * @param deny   <p>Whether the stargate access should be denied</p>
     */
    public StargateAccessEvent(Player player, Portal portal, boolean deny) {
        super(portal, player);

        this.deny = deny;
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
     * Sets whether to deny access to the player
     *
     * @param deny <p>Whether to deny access to the player</p>
     */
    public void setDeny(boolean deny) {
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

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

}
