package net.knarcraft.stargate.event;

import net.knarcraft.stargate.Portal;
import org.bukkit.entity.Player;

/**
 * An abstract event describing any stargate event where a player is involved
 */
public abstract class StargatePlayerEvent extends StargateEvent {

    private final Player player;

    /**
     * Instantiates a new stargate player event
     *
     * @param event  <p>UNUSED</p>
     * @param portal <p>The portal involved in this stargate event</p>
     */
    StargatePlayerEvent(String event, Portal portal, Player player) {
        super(event, portal);
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
