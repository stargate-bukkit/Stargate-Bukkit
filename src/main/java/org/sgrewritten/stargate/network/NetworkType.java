package org.sgrewritten.stargate.network;

import java.util.Set;

import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

public enum NetworkType {
    /**
     * A network that is directly linked to players
     */
    PERSONAL(HighlightingStyle.CURLY_BRACKETS, PortalFlag.PERSONAL_NETWORK),
    
    /**
     * The default network
     */
    DEFAULT(HighlightingStyle.SQUARE_BRACKETS, PortalFlag.DEFAULT_NETWORK),
    
    /**
     * A customised network
     */
    CUSTOM(HighlightingStyle.ROUNDED_BRACKETS, PortalFlag.CUSTOM_NETWORK), 
    
    /**
     * A terminal network
     */
    TERMINAL(HighlightingStyle.DOUBLE_GREATER_LESSER_THAN, PortalFlag.TERMINAL_NETWORK);

    private HighlightingStyle style;
    private PortalFlag flag;

    private NetworkType(HighlightingStyle style, PortalFlag flag) {
        this.style = style;
        this.flag = flag;
    }
    
    public HighlightingStyle getHighlightingStyle() {
        return style;
    }
    
    public PortalFlag getRelatedFlag() {
        return flag;
    }
    
    public static boolean styleGivesNetworkType(HighlightingStyle style) {
        return getNetworkTypeFromHighlight(style) != null;
    }

    public static void removeNetworkTypeRelatedFlags(Set<PortalFlag> flags) {
        for (NetworkType type : NetworkType.values()) {
            PortalFlag flagToRemove = type.getRelatedFlag();
            if (flagToRemove == null) {
                continue;
            }
            flags.remove(type.getRelatedFlag());
        }
    }

    public static NetworkType getNetworkTypeFromHighlight(HighlightingStyle highlight) {
        for(NetworkType type : NetworkType.values()) {
            if(type.getHighlightingStyle() == highlight) {
                return type;
            }
        }
        return null;
    }

    public static NetworkType getNetworkTypeFromFlags(Set<PortalFlag> flags) {
        for(NetworkType type: NetworkType.values()) {
            if(flags.contains(type.getRelatedFlag())) {
                return type;
            }
        }
        return null;
    }
}
