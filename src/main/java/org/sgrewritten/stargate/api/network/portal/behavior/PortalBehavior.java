package org.sgrewritten.stargate.api.network.portal.behavior;

import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;

public interface PortalBehavior {

    void onButtonClick(@NotNull PlayerInteractEvent event);

    void onSignClick(@NotNull PlayerInteractEvent event);

    void onDestroy();

    void update();

    @Nullable Portal getDestination();

    @NotNull LineData @NotNull[] getLines();

    @NotNull PortalFlag getAttachedFlag();

    void assignPortal(@NotNull RealPortal portal);

    @Nullable String getDestinationName();
}
