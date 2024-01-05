package org.sgrewritten.stargate.network.portal.formatting;

import org.bukkit.ChatColor;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.format.StargateComponent;

import java.util.ArrayList;
import java.util.List;

public class LegacyLineColorFormatter implements LineFormatter {

    @Override
    public List<StargateComponent> formatPortalName(Portal portal, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(getColor() + highlightingStyle.getPrefix()),
                new StargateComponent(getColor() + (portal != null ? portal.getName() : "null")),
                new StargateComponent(getColor() + highlightingStyle.getSuffix())
        ));
    }

    @Override
    public List<StargateComponent> formatLine(String line) {
        List<StargateComponent> output = new ArrayList<>();
        output.add(new StargateComponent(getColor() + line));
        return output;
    }

    @Override
    public List<StargateComponent> formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(getColor() + highlightingStyle.getPrefix()),
                new StargateComponent(getColor() + error),
                new StargateComponent(getColor() + highlightingStyle.getSuffix())
        ));
    }

    private ChatColor getColor() {
        return Stargate.getInstance().getLegacySignColor();
    }

    @Override
    public List<StargateComponent> formatNetworkName(Network network, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(getColor() + highlightingStyle.getPrefix()),
                new StargateComponent(getColor() + (network == null ? "null" : network.getName())),
                new StargateComponent(getColor() + highlightingStyle.getSuffix())
        ));
    }

    @Override
    public List<StargateComponent> formatStringWithHighlighting(String aString, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(getColor() + highlightingStyle.getPrefix()),
                new StargateComponent(getColor() + aString),
                new StargateComponent(getColor() + highlightingStyle.getSuffix())
        ));
    }

}
