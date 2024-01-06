package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

public class StargateListPortalEvent extends DeniableStargatePortalEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Portal listedPortal;

    /**
     * Instantiates a new stargate event
     *
     * @param portal <p>The portal involved in this stargate event</p>
     */
    public StargateListPortalEvent(@NotNull Portal portal, Entity entity, Portal listedPortal, boolean deny) {
        super(portal, entity, deny, "", false);
        this.listedPortal = listedPortal;
    }

    /**
     * @return <p>The portal that will be listed</p>
     */
    public Portal getListedPortal() {
        return listedPortal;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
