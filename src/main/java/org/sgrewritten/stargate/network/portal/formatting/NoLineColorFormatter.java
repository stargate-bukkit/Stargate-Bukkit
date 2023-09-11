package org.sgrewritten.stargate.network.portal.formatting;

import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.format.StargateComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * A line formatter used for backwards compatibility before ChatColor was a thing
 *
 * @author Thorin
 */
public class NoLineColorFormatter implements LineFormatter {

    @Override
    public List<StargateComponent> formatPortalName(Portal portal, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(highlightingStyle.getPrefix()),
                new StargateComponent((portal != null) ? portal.getName() : "null"),
                new StargateComponent(highlightingStyle.getSuffix())
        ));
    }

    @Override
    public List<StargateComponent> formatLine(String line) {
        return new ArrayList<>(List.of(new StargateComponent(line)));
    }

    @Override
    public List<StargateComponent> formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(highlightingStyle.getPrefix()),
                new StargateComponent(error),
                new StargateComponent(highlightingStyle.getSuffix())
        ));
    }

    @Override
    public List<StargateComponent> formatNetworkName(Network network, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(highlightingStyle.getPrefix()),
                new StargateComponent((network != null) ? network.getName() : "null"),
                new StargateComponent(highlightingStyle.getSuffix())
        ));
    }

    @Override
    public List<StargateComponent> formatStringWithHighlighting(String aString, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(highlightingStyle.getPrefix()),
                new StargateComponent(aString),
                new StargateComponent(highlightingStyle.getSuffix())
        ));
    }

}
