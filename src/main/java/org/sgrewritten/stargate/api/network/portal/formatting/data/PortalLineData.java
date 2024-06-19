package org.sgrewritten.stargate.api.network.portal.formatting.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;

import java.util.Objects;

public class PortalLineData implements LineData {

    private Portal portal;
    private final SignLineType portalType;
    private final String portalName;

    public PortalLineData(@NotNull Portal portal, SignLineType portalType){
        this.portal = Objects.requireNonNull(portal);
        this.portalName = portal.getName();
        this.portalType = Objects.requireNonNull(portalType);
    }

    public PortalLineData(@NotNull String portalName, SignLineType portalType){
        this.portalName = Objects.requireNonNull(portalName);
        this.portalType = Objects.requireNonNull(portalType);
    }

    public @Nullable Portal getPortal() {
        return this.portal;
    }

    @Override
    public @NotNull SignLineType getType() {
        return portalType;
    }

    @Override
    public @NotNull String getText() {
        return portalName;
    }

    @Override
    public String toString(){
        return "PortalLineData(" + getText() + ")";
    }
}
