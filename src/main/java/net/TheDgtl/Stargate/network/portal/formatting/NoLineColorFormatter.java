package net.TheDgtl.Stargate.network.portal.formatting;

import net.TheDgtl.Stargate.network.portal.IPortal;

/**
 * A line formatter used for backwards compatibility before ChatColor was a thing
 *
 * @author Thorin
 */
public class NoLineColorFormatter implements LineFormatter {

    @Override
    public String formatPortalName(IPortal portal, HighlightingStyle highlightingStyle) {
        return highlightingStyle.getHighlightedName(portal.getName());
    }

    @Override
    public String formatLine(String line) {
        return line;
    }

    @Override
    public String formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return highlightingStyle.getHighlightedName(error);
    }

}
