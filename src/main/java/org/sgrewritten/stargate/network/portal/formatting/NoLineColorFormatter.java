package org.sgrewritten.stargate.network.portal.formatting;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.formatting.FormattableObject;
import org.sgrewritten.stargate.api.network.portal.formatting.LineFormatter;

/**
 * A line formatter used for backwards compatibility before ChatColor was a thing
 *
 * @author Thorin
 */
public class NoLineColorFormatter implements LineFormatter {


    @Override
    public @NotNull String formatFormattableObject(FormattableObject formattableObject) {
        return formattableObject.getHighlighting().getHighlightedName(formattableObject.getName());
    }
}
