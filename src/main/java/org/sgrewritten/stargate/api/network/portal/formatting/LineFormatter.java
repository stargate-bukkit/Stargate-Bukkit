package org.sgrewritten.stargate.api.network.portal.formatting;

import org.jetbrains.annotations.NotNull;

/**
 * A formatter for formatting a line on a sign
 */
public interface LineFormatter {

    @NotNull String formatFormattableObject(FormattableObject formattableObject);

}