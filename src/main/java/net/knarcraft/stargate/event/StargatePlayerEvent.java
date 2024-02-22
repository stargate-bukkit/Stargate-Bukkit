package net.knarcraft.stargate.event;

import net.knarcraft.stargate.portal.Portal;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    StargatePlayerEvent(@NotNull Portal portal, @Nullable Player player) {
        super(portal);
        this.player = player;
    }

    /**
     * Gets the player creating the star gate
     *
     * @return <p>The player creating the star gate</p>
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

}
