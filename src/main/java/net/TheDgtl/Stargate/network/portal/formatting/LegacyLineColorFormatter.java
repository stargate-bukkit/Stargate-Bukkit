package net.TheDgtl.Stargate.network.portal.formatting;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class LegacyLineColorFormatter extends AbstractLineColorFormatter {


    public LegacyLineColorFormatter(Material signMaterial) {
        super(signMaterial);
    }

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
        return Stargate.getLegacySignColor();
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
