package org.sgrewritten.stargate.network;

import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

public enum NetworkType {
    /**
     * A network that is directly linked to players
     */
    PERSONAL(HighlightingStyle.CURLY_BRACKETS),
    
    /**
     * The default network
     */
    DEFAULT(HighlightingStyle.SQUARE_BRACKETS),
    
    /**
     * A customised network
     */
    CUSTOM(HighlightingStyle.ROUNDED_BRACKETS);

    private HighlightingStyle style;

    private NetworkType(HighlightingStyle style) {
        this.style = style;
    }
    
    HighlightingStyle getHighlightingStyle() {
        return style;
    }
}
