package org.sgrewritten.stargate.network.portal.formatting;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.network.InterServerNetwork;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.VirtualPortal;
import org.sgrewritten.stargate.util.colors.ColorConverter;
import org.sgrewritten.stargate.util.colors.ColorProperty;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

public class LineColorFormatter implements LineFormatter {
    private static final ChatColor ERROR_COLOR = ChatColor.RED;
    private final DyeColor dyeColor;
    private final Material signMaterial;
    private final Map<PortalFlag, ChatColor> flagColors;

    private final ChatColor color;
    private final ChatColor pointerColor;

    /**
     * Instantiates a new line color formatter for a sign
     *
     * @param dyeColor     <p>The color of the dye applied to the sign</p>
     * @param signMaterial <p>The material used for the sign</p>
     */
    public LineColorFormatter(DyeColor dyeColor, Material signMaterial) {
        Stargate.log(Level.FINER, "Instantiating a new LineColorFormatter with DyeColor " + dyeColor + " and sign Material " + signMaterial);
        this.dyeColor = dyeColor;
        this.signMaterial = signMaterial;

        color = this.getColor();
        pointerColor = this.getPointerColor();

        flagColors = compileFlagColors();
    }

    @Override
    public String formatPortalName(Portal portal, HighlightingStyle highlightingStyle) {
        ChatColor pointerColor = this.pointerColor;
        if (ConfigurationHelper.getInteger(ConfigurationOption.POINTER_BEHAVIOR) == 2 && getFlagColor(portal) != null) {
            pointerColor = getFlagColor(portal);
        }
        String portalName = (portal != null) ? portal.getName() : "null";
        return pointerColor + highlightingStyle.getHighlightedName(color + portalName + pointerColor);
    }

    @Override
    public String formatNetworkName(Network network, HighlightingStyle highlightingStyle) {
        String networkName = (network != null) ? network.getName() : "null";
        String bold = (network instanceof InterServerNetwork) ? ChatColor.BOLD.toString() : "";
        return pointerColor + bold + highlightingStyle.getHighlightedName(color + bold + networkName + pointerColor + bold);
    }

    @Override
    public String formatStringWithHighlighting(String aString, HighlightingStyle highlightingStyle) {
        return pointerColor + highlightingStyle.getHighlightedName(color + aString + pointerColor);
    }

    @Override
    public String formatLine(String line) {
        return color + line;
    }

    @Override
    public String formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return ERROR_COLOR + highlightingStyle.getHighlightedName(error);
    }

    /**
     * Get text color
     *
     * @return A color to be used on text
     */
    private ChatColor getColor() {
        if (shouldUseDyeColor()) {
            return ColorConverter.getChatColorFromDyeColor(dyeColor);
        }
        return ColorProperty.getColorFromHue(this.signMaterial, Stargate.getDefaultSignHue(), false);
    }

    /**
     * Get pointer / highlighting color
     *
     * @return A color to be used on pointer / highlighting
     */
    private ChatColor getPointerColor() {
        if (shouldUseDyeColor()) {
            if (ConfigurationHelper.getInteger(ConfigurationOption.POINTER_BEHAVIOR) == 3) {
                return ColorConverter.getInvertedChatColor(ColorConverter.getChatColorFromDyeColor(dyeColor));
            }
            return ColorConverter.getChatColorFromDyeColor(dyeColor);
        }
        if (ConfigurationHelper.getInteger(ConfigurationOption.POINTER_BEHAVIOR) == 3) {
            return ColorProperty.getColorFromHue(this.signMaterial, Stargate.getDefaultSignHue(), true);
        }
        return ColorProperty.getColorFromHue(this.signMaterial, Stargate.getDefaultSignHue(), false);
    }

    /**
     * @return <p> If the default color should not be applied </p>
     */
    private boolean shouldUseDyeColor() {
        return (dyeColor != null && dyeColor != Stargate.getDefaultSignDyeColor(signMaterial));
    }

    /**
     * Get flag color
     *
     * @param portal <p> The portal to check flags from </p>
     * @return <p> A color corresponding to a portals flag. </p>
     */
    private ChatColor getFlagColor(Portal portal) {
        PortalFlag[] flagPriority = new PortalFlag[]{PortalFlag.PRIVATE, PortalFlag.FREE, PortalFlag.HIDDEN,
                PortalFlag.FORCE_SHOW, PortalFlag.BACKWARDS};

        if (portal == null) {
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

    /**
     * Compile a map of all the flag-colors, good idea to use, as it avoids having to convert too much between hsb and rgb
     *
     * @return <p> A map of all the flag-colors </p>
     */
    private Map<PortalFlag, ChatColor> compileFlagColors() {
        Map<PortalFlag, ChatColor> flagColors = new EnumMap<>(PortalFlag.class);
        for (PortalFlag key : ColorProperty.getFlagColorHues().keySet()) {
            flagColors.put(key, ColorProperty.getColorFromHue(this.signMaterial, ColorProperty.getFlagColorHues().get(key), false));
        }
        return flagColors;
    }
}
