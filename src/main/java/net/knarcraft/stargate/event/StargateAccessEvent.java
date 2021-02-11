package net.knarcraft.stargate.event;

import net.knarcraft.stargate.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class StargateAccessEvent extends StargateEvent {

	private final Player player;
	private boolean deny;
	
	private static final HandlerList handlers = new HandlerList();


    /**
     * Gets a handler-list containing all event handlers
     * @return <p>A handler-list with all event handlers</p>
     */
	public static HandlerList getHandlerList() {
		return handlers;
	}

    /**
     * Instantiates a new stargate access event
     * @param player <p>The player involved in the vent</p>
     * @param portal <p>The portal involved in the event</p>
     * @param deny <p>Whether the event should be denied</p>
     */
	public StargateAccessEvent(Player player, Portal portal, boolean deny) {
		super("StargateAccessEvent", portal);
		
		this.player = player;
		this.deny = deny;
	}

    /**
     * Gets whether the player should be denied access
     * @return <p>Whether the player should be denied access</p>
     */
	public boolean getDeny() {
		return this.deny;
	}

    /**
     * Sets whether to deny the player
     * @param deny <p>Whether to deny the player</p>
     */
	public void setDeny(boolean deny) {
		this.deny = deny;
	}

    /**
     * Gets the player involved in this stargate access event
     * @return <p>The player involved in this event</p>
     */
	public Player getPlayer() {
		return this.player;
	}

    @Override
    public @org.jetbrains.annotations.NotNull HandlerList getHandlers() {
        return handlers;
    }
}
