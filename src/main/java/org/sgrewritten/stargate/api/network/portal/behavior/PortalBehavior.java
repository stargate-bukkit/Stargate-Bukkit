package org.sgrewritten.stargate.api.network.portal.behavior;

import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;

public interface PortalBehavior {

    /**
     * This method will be triggered whenever a portal button is clicked (left or right)
     * @param event <p>Event information on this click</p>
     */
    void onButtonClick(@NotNull PlayerInteractEvent event);

    /**
     * This method will be triggered whenever a portal sig nis clicked (left or right)
     * @param event <p>Event information on the click</p>
     */
    void onSignClick(@NotNull PlayerInteractEvent event);

    /**
     * This method will be triggered whenever the portal is destroyed
     */
    void onDestroy();

    /**
     * This method will be triggered whenever the portal deems it reasonable to update itself.
     * Can be triggered from multiple causes, for example a portal gets destroyed in the network.
     */
    void update();

    /**
     * @return The destination portal this behavior points to
     */
    @Nullable Portal getDestination();

    /**
     * @return <p>The sign lines that should be drawn by any portal with this behavior</p>
     */
    @NotNull LineData @NotNull[] getLines();

    /**
     * @return <p>The {@link PortalFlag} this behavior relates to</p>
     */
    @NotNull PortalFlag getAttachedFlag();

    /**
     * Assign this behavior to a portal
     * @param portal <p>The portal to assign this behavior to</p>
     */
    @ApiStatus.Internal
    void assignPortal(@NotNull RealPortal portal);

    /**
     * @return <p>Get the name of the destination this behavior is pointing towards</p>
     */
    @Nullable String getDestinationName();
}
