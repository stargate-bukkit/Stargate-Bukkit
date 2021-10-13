package net.knarcraft.stargate.event;

import net.knarcraft.stargate.portal.Portal;
import org.bukkit.entity.Player;

/**
 * An abstract event describing any stargate event where a player is involved
 */
@SuppressWarnings("unused")
public abstract class StargatePlayerEvent extends StargateEvent {

    private final Player player;

    /**
     * Instantiates a new stargate player event
     *
     * @param portal <p>The portal involved in this stargate event</p>
     */
    StargatePlayerEvent(Portal portal, Player player) {
        super(portal);
        this.player = player;
    }

    /**
     * Gets the player creating the star gate
     *
     * @return <p>The player creating the star gate</p>
     */
    public Player getPlayer() {
        return player;
    }

}
