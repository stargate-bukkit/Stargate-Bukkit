package org.sgrewritten.stargate.api.network.portal.formatting.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.formatting.LineFormatter;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;

/**
 * Used as input arguments for {@link LineFormatter}
 */
public interface LineData{

    /**
     * @return <p>The line type of this line data</p>
     */
    @NotNull SignLineType getType();

    /**
     *
     * @return <p>The unformatted text for this line data</p>
     */
    @NotNull String getText();
}
