package org.sgrewritten.stargate.network.portal.formatting;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.network.portal.formatting.LineFormatter;
import org.sgrewritten.stargate.api.network.portal.formatting.NetworkLine;
import org.sgrewritten.stargate.api.network.portal.formatting.PortalLine;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLine;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;
import org.sgrewritten.stargate.api.network.portal.formatting.StargateComponent;
import org.sgrewritten.stargate.api.network.portal.formatting.TextLine;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.NetworkLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.PortalLineData;
import org.sgrewritten.stargate.colors.ColorConverter;
import org.sgrewritten.stargate.colors.ColorRegistry;
import org.sgrewritten.stargate.colors.ColorSelector;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.VirtualPortal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class LineColorFormatter implements LineFormatter {
    private static final ChatColor ERROR_COLOR = ChatColor.RED;
    private final DyeColor dyeColor;
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

        color = this.getColor();
        pointerColor = this.getPointerColor();
    }


    @Override
    public SignLine convertToSignLine(LineData lineData) {
        return switch (lineData.getType()) {
            case ERROR ->
                    new TextLine(formatErrorLine(lineData.getText(), HighlightingStyle.SQUARE_BRACKETS), SignLineType.ERROR);
            case TEXT -> new TextLine(formatLine(lineData.getText()));
            case NETWORK -> formatNetworkName((NetworkLineData) lineData);
            case DESTINATION_PORTAL ->
                    formatPortalName((PortalLineData) lineData, HighlightingStyle.LESSER_GREATER_THAN);
            case THIS_PORTAL -> formatPortalName((PortalLineData) lineData, HighlightingStyle.MINUS_SIGN);
            case PORTAL -> formatPortalName((PortalLineData) lineData, HighlightingStyle.NOTHING);
        };
    }

    private SignLine formatPortalName(PortalLineData lineData, HighlightingStyle highlightingStyle) {
        ChatColor pointerColorTemp = this.pointerColor;
        Portal portal = lineData.getPortal();
        if (ConfigurationHelper.getInteger(ConfigurationOption.POINTER_BEHAVIOR) == 2 && getFlagColor(portal) != null) {
            pointerColorTemp = getFlagColor(portal);
        }
        String portalName = (portal != null) ? portal.getName() : lineData.getText();
        List<StargateComponent> components = new ArrayList<>(List.of(
                new StargateComponent(pointerColorTemp + highlightingStyle.getPrefix()),
                new StargateComponent(color + portalName),
                new StargateComponent(pointerColorTemp + highlightingStyle.getSuffix())
        ));
        return new PortalLine(components, portal, lineData.getType());
    }

    private SignLine formatNetworkName(NetworkLineData networkLineData) {
        Network network = networkLineData.getNetwork();
        HighlightingStyle highlightingStyle = network.getHighlightingStyle();
        String networkName = network.getName();
        String bold = (network.getStorageType() == StorageType.INTER_SERVER) ? ChatColor.BOLD.toString() : "";
        List<StargateComponent> components = new ArrayList<>(List.of(
                new StargateComponent(pointerColor + bold + highlightingStyle.getPrefix()),
                new StargateComponent(color + bold + networkName),
                new StargateComponent(pointerColor + bold + highlightingStyle.getSuffix())
        ));
        return new NetworkLine(components, network);
    }

    private List<StargateComponent> formatStringWithHighlighting(String aString, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(pointerColor + highlightingStyle.getPrefix()),
                new StargateComponent(color + aString),
                new StargateComponent(pointerColor + highlightingStyle.getSuffix())
        ));
    }

    private List<StargateComponent> formatLine(String line) {
        return new ArrayList<>(List.of(new StargateComponent(color + line)));
    }

    private List<StargateComponent> formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return new ArrayList<>(List.of(
                new StargateComponent(ERROR_COLOR + highlightingStyle.getPrefix()),
                new StargateComponent(ERROR_COLOR + error),
                new StargateComponent(ERROR_COLOR + highlightingStyle.getSuffix())
        ));
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
        return ColorRegistry.DEFAULT_COLORS.get(ColorSelector.TEXT);
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
            return ColorRegistry.DEFAULT_COLORS.get(ColorSelector.POINTER);
        }
        return ColorRegistry.DEFAULT_COLORS.get(ColorSelector.TEXT);
    }

    /**
     * @return <p> If the default color should not be applied </p>
     */
    private boolean shouldUseDyeColor() {
        return (dyeColor != null && dyeColor != ColorRegistry.DEFAULT_DYE_COLOR);
    }

    /**
     * Get flag color
     *
     * @param portal <p> The portal to check flags from </p>
     * @return <p> A color corresponding to a portals flag. </p>
     */
    private ChatColor getFlagColor(Portal portal) {
        StargateFlag[] flagPriority = new StargateFlag[]{StargateFlag.PRIVATE, StargateFlag.FREE, StargateFlag.HIDDEN,
                StargateFlag.FORCE_SHOW, StargateFlag.BACKWARDS};

        if (portal == null) {
            return null;
        }
        if (portal instanceof VirtualPortal) {
            return ColorRegistry.FLAG_COLORS.get(StargateFlag.INTERSERVER);
        }
        for (StargateFlag flag : flagPriority) {
            if (portal.hasFlag(flag)) {
                return ColorRegistry.FLAG_COLORS.get(flag);
            }
        }
        return null;
    }
}
