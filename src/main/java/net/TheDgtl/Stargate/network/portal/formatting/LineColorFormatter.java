package net.TheDgtl.Stargate.network.portal.formatting;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;
import net.TheDgtl.Stargate.util.ColorConverter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * A line formatter that applies coloring to formatted lines
 */
public class LineColorFormatter implements LineFormatter {

    private static final ChatColor GRAY_SELECTOR_COLOR = ChatColor.of("#808080");
    private static final ChatColor ERROR_COLOR = ChatColor.RED;
    private static Map<PortalFlag, ChatColor[]> flagColors;

    private final DyeColor dyeColor;
    private final boolean isLightSign;

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
        this.dyeColor = dyeColor;
        this.isLightSign = isLightSign(signMaterial);
    }

    @Override
    public String formatPortalName(Portal portal, HighlightingStyle highlightingStyle) {
        String name = portal.getName();

        ChatColor nameColor;
        ChatColor selectorColor;

        if (Settings.getInteger(Setting.NAME_STYLE) == 3) {
            nameColor = getDefaultColor(isLightSign);
        } else {
            nameColor = getNameColor(portal, isLightSign);
        }

        switch (Settings.getInteger(Setting.NAME_STYLE)) {
            case 1:
                selectorColor = getDefaultColor(isLightSign);
                break;
            case 2:
                selectorColor = getNameColor(portal, isLightSign);
                break;
            case 4:
                selectorColor = getDefaultColor(!isLightSign);
                break;
            default:
                selectorColor = GRAY_SELECTOR_COLOR;
        }

        String coloredName = nameColor + name + selectorColor;
        return selectorColor + highlightingStyle.getHighlightedName(coloredName);
    }

    @Override
    public String formatLine(String line) {
        return getColor(isLightSign) + line;
    }

    @Override
    public String formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return getColor(isLightSign) + highlightingStyle.getHighlightedName(ERROR_COLOR + error + getColor(isLightSign));
    }

    /**
     * Checks whether the given sign material represents a light sign or a dark sign
     *
     * @param signMaterial <p>The sign material to check</p>
     * @return <p>True if the material represents a light sign</p>
     */
    static protected boolean isLightSign(Material signMaterial) {
        switch (signMaterial) {
            // Dark signs
            case DARK_OAK_WALL_SIGN:
            case WARPED_WALL_SIGN:
            case CRIMSON_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
                return false;
            default:
                return true;
        }
    }

    /**
     * Gets the color to use for normal portals for the given sign type
     *
     * @param isLightSign <p>Whether to get the color for a light sign or a dark sign</p>
     * @return <p>The color to use for normal portals</p>
     */
    private ChatColor getColor(boolean isLightSign) {
        if (dyeColor != null && dyeColor != DyeColor.BLACK) {
            return ColorConverter.getChatColorFromDyeColor(dyeColor);
        } else {
            return getDefaultColor(isLightSign);
        }
    }

    /**
     * Gets the default color to use for the given type of sign
     *
     * @param isLightSign <p>Whether to get the default color for a light sign or a dark sign</p>
     * @return <p>The default color to use for the given type of sign</p>
     */
    private ChatColor getDefaultColor(boolean isLightSign) {
        return isLightSign ? Stargate.defaultLightSignColor : Stargate.defaultDarkColor;
    }

    /**
     * Gets the color to use for displaying the given portal's name
     *
     * @param portal      <p>The portal to display the name of</p>
     * @param isLightSign <p>Whether the sign is a light color as opposed to a dark color</p>
     * @return <p>The color to use for displaying the portal's name</p>
     */
    private ChatColor getNameColor(Portal portal, boolean isLightSign) {
        Stargate.log(Level.FINER, " Gate " + portal.getName() + " has flags: " + portal.getAllFlagsString());
        ChatColor[] colors = getNameColors(portal);
        if (isLightSign) {
            return colors[0];
        } else {
            return colors[1];
        }
    }

    /**
     * Gets the name colors to use for the given portal's sign
     *
     * @param portal <p>The portal to get colors for</p>
     * @return <p>The colors used to draw the portal's name</p>
     */
    private ChatColor[] getNameColors(Portal portal) {
        if (portal instanceof VirtualPortal) {
            return flagColors.get(PortalFlag.FANCY_INTER_SERVER);
        } else if (portal.hasFlag(PortalFlag.PRIVATE)) {
            return flagColors.get(PortalFlag.PRIVATE);
        } else if (portal.hasFlag(PortalFlag.FREE)) {
            return flagColors.get(PortalFlag.FREE);
        } else if (portal.hasFlag(PortalFlag.HIDDEN)) {
            return flagColors.get(PortalFlag.HIDDEN);
        } else if (portal.hasFlag(PortalFlag.FORCE_SHOW)) {
            return flagColors.get(PortalFlag.FORCE_SHOW);
        } else if (portal.hasFlag(PortalFlag.BACKWARDS)) {
            return flagColors.get(PortalFlag.BACKWARDS);
        } else {
            return new ChatColor[]{getColor(true), getColor(false)};
        }
    }

    /**
     * Loads all used flag colors to reduce some overhead
     */
    private static void loadFlagColors() {
        flagColors = new HashMap<>();
        flagColors.put(PortalFlag.BACKWARDS, new ChatColor[]{ChatColor.of("#240023"), ChatColor.of("#b3baff")});
        flagColors.put(PortalFlag.FORCE_SHOW, new ChatColor[]{ChatColor.of("#002422"), ChatColor.of("#b3fffc")});
        flagColors.put(PortalFlag.HIDDEN, new ChatColor[]{ChatColor.of("#292800"), ChatColor.of("#fffcb3")});
        flagColors.put(PortalFlag.FREE, new ChatColor[]{ChatColor.of("#002402"), ChatColor.of("#b3ffb8")});
        flagColors.put(PortalFlag.PRIVATE, new ChatColor[]{ChatColor.of("#210000"), ChatColor.of("#ffb3b3")});
        flagColors.put(PortalFlag.FANCY_INTER_SERVER, new ChatColor[]{ChatColor.of("#240023"), ChatColor.of("#FFE0FE")});
    }

}
