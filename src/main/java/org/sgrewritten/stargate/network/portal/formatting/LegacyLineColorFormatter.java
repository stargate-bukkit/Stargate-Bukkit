package org.sgrewritten.stargate.network.portal.formatting;

import org.bukkit.ChatColor;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.formatting.LineFormatter;
import org.sgrewritten.stargate.api.network.portal.formatting.NetworkLine;
import org.sgrewritten.stargate.api.network.portal.formatting.PortalLine;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLine;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;
import org.sgrewritten.stargate.api.network.portal.formatting.StargateComponent;
import org.sgrewritten.stargate.api.network.portal.formatting.TextLine;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.NetworkLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.PortalLineData;
import org.sgrewritten.stargate.colors.ColorRegistry;

import java.util.ArrayList;
import java.util.List;

public class LegacyLineColorFormatter implements LineFormatter {
    @Override
    public SignLine convertToSignLine(LineData lineData){
        return switch (lineData.getType()){
            case ERROR -> new TextLine(formatErrorLine(lineData.getText(), HighlightingStyle.SQUARE_BRACKETS), SignLineType.ERROR);
            case TEXT -> new TextLine(formatLine(lineData.getText()));
            case NETWORK -> formatNetworkName((NetworkLineData) lineData);
            case DESTINATION_PORTAL -> formatPortalName((PortalLineData) lineData, HighlightingStyle.LESSER_GREATER_THAN);
            case THIS_PORTAL -> formatPortalName((PortalLineData) lineData,HighlightingStyle.MINUS_SIGN);
            case PORTAL -> formatPortalName((PortalLineData) lineData,HighlightingStyle.NOTHING);
        };
    }

    private SignLine formatPortalName(PortalLineData lineData, HighlightingStyle highlightingStyle) {
        Portal portal = lineData.getPortal();
        List<StargateComponent> components = new ArrayList<>(List.of(
                new StargateComponent(getColor() + highlightingStyle.getPrefix()),
                new StargateComponent(getColor() + (portal != null ? portal.getName() : lineData.getText())),
                new StargateComponent(getColor() + highlightingStyle.getSuffix())
        ));
        return new PortalLine(components, portal, lineData.getType());
    }

    private List<StargateComponent> formatLine(String line) {
        List<StargateComponent> output = new ArrayList<>();
        output.add(new StargateComponent(getColor() + line));
        return output;
    }

    private List<StargateComponent> formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(getColor() + highlightingStyle.getPrefix()),
                new StargateComponent(getColor() + error),
                new StargateComponent(getColor() + highlightingStyle.getSuffix())
        ));
    }

    private ChatColor getColor() {
        return ColorRegistry.LEGACY_SIGN_COLOR;
    }

    private SignLine formatNetworkName(NetworkLineData lineData) {
        Network network = lineData.getNetwork();
        List<StargateComponent> components = new ArrayList<>(List.of(
                new StargateComponent(getColor() + network.getHighlightingStyle().getPrefix()),
                new StargateComponent(getColor() + (network == null ? "null" : network.getName())),
                new StargateComponent(getColor() + network.getHighlightingStyle().getSuffix())
        ));
        return new NetworkLine(components, network);
    }

    private List<StargateComponent> formatStringWithHighlighting(String aString, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(getColor() + highlightingStyle.getPrefix()),
                new StargateComponent(getColor() + aString),
                new StargateComponent(getColor() + highlightingStyle.getSuffix())
        ));
    }
}
