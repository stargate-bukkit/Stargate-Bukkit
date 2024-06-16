package org.sgrewritten.stargate.network.portal.formatting;

import org.bukkit.ChatColor;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.formatting.*;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.NetworkLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.PortalLineData;
import org.sgrewritten.stargate.colors.ColorRegistry;

import org.sgrewritten.stargate.api.container.Holder;
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
        List<Holder<StargateComponent>> components = new ArrayList<>(List.of(
                LegacyStargateComponent.of(getColor() + highlightingStyle.getPrefix()),
                LegacyStargateComponent.of(getColor() + (portal != null ? portal.getName() : lineData.getText())),
                LegacyStargateComponent.of(getColor() + highlightingStyle.getSuffix())
        ));
        return new PortalLine(components, portal, lineData.getType());
    }

    private List<Holder<StargateComponent>> formatLine(String line) {
        List<Holder<StargateComponent>> output = new ArrayList<>();
        output.add(LegacyStargateComponent.of(getColor() + line));
        return output;
    }

    private List<Holder<StargateComponent>> formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                LegacyStargateComponent.of(getColor() + highlightingStyle.getPrefix()),
                LegacyStargateComponent.of(getColor() + error),
                LegacyStargateComponent.of(getColor() + highlightingStyle.getSuffix())
        ));
    }

    private ChatColor getColor() {
        return ColorRegistry.LEGACY_SIGN_COLOR;
    }

    private SignLine formatNetworkName(NetworkLineData lineData) {
        Network network = lineData.getNetwork();
        List<Holder<StargateComponent>> components = new ArrayList<>(List.of(
                LegacyStargateComponent.of(getColor() + network.getHighlightingStyle().getPrefix()),
                LegacyStargateComponent.of(getColor() + (network == null ? "null" : network.getName())),
                LegacyStargateComponent.of(getColor() + network.getHighlightingStyle().getSuffix())
        ));
        return new NetworkLine(components, network);
    }

    private List<Holder<StargateComponent>> formatStringWithHighlighting(String aString, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                LegacyStargateComponent.of(getColor() + highlightingStyle.getPrefix()),
                LegacyStargateComponent.of(getColor() + aString),
                LegacyStargateComponent.of(getColor() + highlightingStyle.getSuffix())
        ));
    }
}
