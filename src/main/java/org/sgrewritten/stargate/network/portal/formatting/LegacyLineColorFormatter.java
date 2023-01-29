package org.sgrewritten.stargate.network.portal.formatting;

import org.bukkit.ChatColor;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.network.portal.formatting.FormattableObject;
import org.sgrewritten.stargate.api.network.portal.formatting.LineFormatter;

public class LegacyLineColorFormatter implements LineFormatter {

    private ChatColor getColor() {
        return Stargate.getInstance().getLegacySignColor();
    }


    @Override
    public String formatFormattableObject(FormattableObject formattableObject) {
        return getColor() + formattableObject.getHighlighting().getHighlightedName(formattableObject.getName());
    }
}
