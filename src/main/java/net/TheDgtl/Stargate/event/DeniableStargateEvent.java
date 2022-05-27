package net.TheDgtl.Stargate.event;

import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.entity.Entity;

/**
 * A Stargate event which can be denied
 */
public abstract class DeniableStargateEvent extends StargateEntityEvent {

    private boolean deny;
    private String denyReason;

    /**
     * Instantiates a new stargate player event
     *
     * @param portal           <p>The portal involved in this stargate event</p>
     * @param travellingEntity <p>The entity travelling through a portal</p>
     * @param deny             <p>Whether the stargate access should be denied</p>
     * @param denyReason       <p>The reason stargate access was denied</p>
     */
    DeniableStargateEvent(Portal portal, Entity travellingEntity, boolean deny, String denyReason) {
        super(portal, travellingEntity);
        this.deny = deny;
        this.denyReason = denyReason;
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
