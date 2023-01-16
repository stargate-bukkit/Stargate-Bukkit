package org.sgrewritten.stargate.network.portal.formatting;

import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.formatting.HighlightingStyle;
import org.sgrewritten.stargate.api.network.portal.formatting.LineFormatter;

/**
 * A line formatter used for backwards compatibility before ChatColor was a thing
 *
 * @author Thorin
 */
public class NoLineColorFormatter implements LineFormatter {

    @Override
    public String formatPortalName(Portal portal, HighlightingStyle highlightingStyle) {
        return highlightingStyle.getHighlightedName((portal != null) ? portal.getName() : "null");
    }

    @Override
    public String formatLine(String line) {
        return line;
    }

    @Override
    public String formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return highlightingStyle.getHighlightedName(error);
    }

    @Override
    public String formatNetworkName(Network network, HighlightingStyle highlightingStyle) {
        return highlightingStyle.getHighlightedName((network != null) ? network.getName() : "null");
    }

    @Override
    public String formatStringWithHiglighting(String aString, HighlightingStyle highlightingStyle) {
        return highlightingStyle.getHighlightedName(aString);
    }

}
