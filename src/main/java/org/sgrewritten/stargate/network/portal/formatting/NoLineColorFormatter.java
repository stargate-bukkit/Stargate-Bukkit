package org.sgrewritten.stargate.network.portal.formatting;

import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.formatting.*;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.NetworkLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.PortalLineData;

import java.util.ArrayList;
import java.util.List;

/**
 * A line formatter used for backwards compatibility before ChatColor was a thing
 *
 * @author Thorin
 */
public class NoLineColorFormatter implements LineFormatter {

    @Override
    public SignLine convertToSignLine(LineData lineData) {
        return switch (lineData.getType()){
            case ERROR -> new TextLine(HighlightingStyle.SQUARE_BRACKETS.getHighlightedName(lineData.getText()), SignLineType.ERROR);
            case TEXT -> new TextLine(lineData.getText());
            case NETWORK ->getNetworkSignLine((NetworkLineData) lineData);
            case DESTINATION_PORTAL -> getPortalSignLine((PortalLineData) lineData, HighlightingStyle.LESSER_GREATER_THAN);
            case THIS_PORTAL -> getPortalSignLine((PortalLineData) lineData, HighlightingStyle.MINUS_SIGN);
            case PORTAL -> getPortalSignLine((PortalLineData) lineData, HighlightingStyle.NOTHING);
        };
    }

    private SignLine getNetworkSignLine(NetworkLineData lineData){
        Network network = lineData.getNetwork();
        HighlightingStyle highlightingStyle = network.getHighlightingStyle();
        return new NetworkLine(List.of(LegacyStargateComponent.of(highlightingStyle.getHighlightedName(network.getName()))),network);
    }

    private SignLine getPortalSignLine(PortalLineData lineData, HighlightingStyle highlightingStyle){
        Portal portal = lineData.getPortal();
        String portalName = portal == null ? lineData.getText() : portal.getName();
        return new PortalLine(List.of(LegacyStargateComponent.of(highlightingStyle.getHighlightedName(portalName))),portal,lineData.getType());
    }
}
