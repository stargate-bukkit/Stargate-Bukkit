package org.sgrewritten.stargate.network.portal.formatting;

import org.bukkit.ChatColor;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;

public class LegacyLineColorFormatter implements LineFormatter {

    @Override
    public String formatPortalName(Portal portal, HighlightingStyle highlightingStyle) {
        return getColor() + highlightingStyle.getHighlightedName((portal != null) ? portal.getName() : "null");
    }

    @Override
    public String formatLine(String line) {
        return getColor() + line;
    }

    @Override
    public String formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return getColor() + highlightingStyle.getHighlightedName(error);
    }

    private ChatColor getColor() {
        return Stargate.getInstance().getLegacySignColor();
    }

    @Override
    public String formatNetworkName(Network network, HighlightingStyle highlightingStyle) {
        return getColor() + highlightingStyle.getHighlightedName((network != null) ? network.getName() : "null");
    }

    @Override
    public String formatStringWithHiglighting(String aString, HighlightingStyle highlightingStyle) {
        return getColor() + highlightingStyle.getHighlightedName(aString);
    }

}
