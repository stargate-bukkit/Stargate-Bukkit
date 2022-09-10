package net.TheDgtl.Stargate.network.portal.formatting;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.container.TwoTuple;
import net.TheDgtl.Stargate.network.InterServerNetwork;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;
import net.TheDgtl.Stargate.util.ColorConverter;
import net.TheDgtl.Stargate.util.FileHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * A line formatter that applies coloring to formatted lines
 */
public class LineColorFormatter extends AbstractLineColorFormatter {

    private static final ChatColor GRAY_SELECTOR_COLOR = ChatColor.of("#808080");
    private static final ChatColor ERROR_COLOR = ChatColor.RED;
    private static final Map<PortalFlag, ChatColor> flagColors = new EnumMap<>(PortalFlag.class);

    private DyeColor dyeColor;

    static {
        loadFlagColors();
    }

    /**
     * Instantiates a new line color formatter for a sign
     *
     * @param dyeColor     <p>The color of the dye applied to the sign</p>
     * @param signMaterial <p>The material used for the sign</p>
     */
    public LineColorFormatter(DyeColor dyeColor, Material signMaterial) {
        super(signMaterial);
        this.dyeColor = dyeColor;
    }

    @Override
    public String formatPortalName(Portal portal, HighlightingStyle highlightingStyle) {
        ChatColor pointerColor = getPointerColor();
        if(ConfigurationHelper.getInteger(ConfigurationOption.POINTER_BEHAVIOR) == 2 && getFlagColor(portal) != null) {
            pointerColor =  getFlagColor(portal);
        }
        ChatColor listingColor = getColor();
        String portalName = (portal != null) ? portal.getName() : "null";
        return pointerColor + highlightingStyle.getHighlightedName(listingColor + portalName + pointerColor);
    }

    @Override
    public String formatNetworkName(Network network, HighlightingStyle highlightingStyle) {
        String networkName = (network != null) ? network.getName() : "null";
        return getPointerColor() + highlightingStyle.getHighlightedName(getColor() + networkName + getPointerColor());
    }

    @Override
    public String formatStringWithHiglighting(String aString, HighlightingStyle highlightingStyle) {
        return getPointerColor() + highlightingStyle.getHighlightedName(getColor() + aString + getPointerColor());
    }

    @Override
    public String formatLine(String line) {
        return getColor() + line;
    }

    @Override
    public String formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return ERROR_COLOR + highlightingStyle.getHighlightedName(error);
    }

    @Override
    public void onSignDyeing(DyeColor signColor) {
        this.dyeColor = signColor;
    }
    
    private ChatColor getColor() {
        if (shouldUseDyeColor()) {
            return ColorConverter.getChatColorFromDyeColor(dyeColor);
        } else {
            return Stargate.getDefaultSignColor();
        }
    }

    private ChatColor getPointerColor() {
        if(shouldUseDyeColor()) {
            if(ConfigurationHelper.getInteger(ConfigurationOption.POINTER_BEHAVIOR) == 3) {
                return ColorConverter.getInvertedChatColorFromDyeColor(dyeColor);
            }
            return ColorConverter.getChatColorFromDyeColor(dyeColor);
        }
        ChatColor color = getColor();
        if(ConfigurationHelper.getInteger(ConfigurationOption.POINTER_BEHAVIOR) == 3) {
            return ColorConverter.invertBrightness(color);
        }
        return color;
    }
    
    private boolean shouldUseDyeColor() {
        return (dyeColor != null && dyeColor != DyeColor.BLACK);
    }

    /**
     * Get flag color 
     * @param portal <p> The portal to check flags from </p>
     * @return A color corresponding to a portals flag..
     */
    private ChatColor getFlagColor(Portal portal) {
        PortalFlag[] flagPriority = new PortalFlag[] { PortalFlag.PRIVATE, PortalFlag.FREE, PortalFlag.HIDDEN,
                PortalFlag.FORCE_SHOW, PortalFlag.BACKWARDS };
        
        if(portal == null) {
            return null;
        }
        if (portal instanceof VirtualPortal) {
            return flagColors.get(PortalFlag.FANCY_INTER_SERVER);
        }
        for (PortalFlag flag : flagPriority) {
            if (portal.hasFlag(flag)) {
                return flagColors.get(flag);
            }
        }
        return null;
    }

    private static void loadFlagColors() {
        Map<String,String> flagColorsString = new HashMap<>();
        FileHelper.readInternalFileToMap("flagColors.properties", flagColorsString);
        for(String key : flagColorsString.keySet()) {
            flagColors.put(PortalFlag.valueOf(key), ChatColor.of(flagColorsString.get(key)));
        }
    }
}
