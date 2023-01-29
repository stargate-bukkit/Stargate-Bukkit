package org.sgrewritten.stargate.network.portal.formatting;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.portal.formatting.FormattableObject;
import org.sgrewritten.stargate.api.network.portal.formatting.LineFormatter;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.util.colors.ColorConverter;
import org.sgrewritten.stargate.util.colors.ColorProperty;

import java.util.logging.Level;

public class LineColorFormatter implements LineFormatter {
    private static final ChatColor ERROR_COLOR = ChatColor.RED;
    private final DyeColor dyeColor;
    private final Material signMaterial;

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

    }

    @Override
    public String formatFormattableObject(FormattableObject formattableObject) {
        ChatColor pointerColor = this.pointerColor;
        ChatColor objectSpecifiedColor = formattableObject.getPointerColor(signMaterial);
        if (ConfigurationHelper.getInteger(ConfigurationOption.POINTER_BEHAVIOR) == 2 && objectSpecifiedColor != null) {
            pointerColor = objectSpecifiedColor;
        }
        ChatColor color = this.color;
        if (formattableObject.getOverrideColor() != null) {
            color = formattableObject.getOverrideColor();
        }

        return pointerColor + formattableObject.getHighlighting().getHighlightedName(color + formattableObject.getName() + pointerColor);
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


}
