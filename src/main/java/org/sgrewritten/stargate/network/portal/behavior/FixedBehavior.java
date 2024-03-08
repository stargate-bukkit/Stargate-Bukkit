package org.sgrewritten.stargate.network.portal.behavior;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.NetworkLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.PortalLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.TextLineData;

import java.util.Arrays;
import java.util.logging.Level;

public class FixedBehavior extends AbstractPortalBehavior{
    private final String destinationName;
    private Portal destination;

    public FixedBehavior(LanguageManager languageManager, @NotNull String destinationName) {
        super(languageManager);
        this.destinationName = destinationName;
    }

    @Override
    public void update() {
        this.destination = portal.getNetwork().getPortal(destinationName);
    }

    @Override
    public @Nullable Portal getDestination() {
        return this.destination;
    }

    @Override
    public @NotNull LineData @NotNull [] getLines() {
        LineData[] lines = new LineData[4];
        lines[0] = new PortalLineData(portal, SignLineType.THIS_PORTAL);
        lines[2] = new NetworkLineData(portal.getNetwork());
        if (getDestination() != null) {
            lines[1] = new PortalLineData(getDestination(), SignLineType.DESTINATION_PORTAL);
            lines[3] = new TextLineData();
        } else {
            lines[1] = new PortalLineData(destinationName, SignLineType.DESTINATION_PORTAL);
            lines[3] = new TextLineData(super.languageManager.getString(TranslatableMessage.DISCONNECTED), SignLineType.ERROR);
        }
        return lines;
    }

    @Override
    public @NotNull PortalFlag getAttachedFlag() {
        return PortalFlag.FIXED;
    }
}
