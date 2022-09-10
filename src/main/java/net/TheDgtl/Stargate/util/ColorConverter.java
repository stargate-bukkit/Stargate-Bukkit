package net.TheDgtl.Stargate.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.EnumMap;

/**
 * A converter for converting between different types of colors
 */
public final class ColorConverter {

    private static final EnumMap<Material, DyeColor> materialToColorsConversionMap = new EnumMap<>(Material.class);
    private static final EnumMap<DyeColor, Material> dyeColorToMaterialColorsConversionMap = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            Material dye = Material.getMaterial(color + "_DYE");
            materialToColorsConversionMap.put(dye, color);
            dyeColorToMaterialColorsConversionMap.put(color, dye);
        }
    }

    private ColorConverter() {

    }

    /**
     * Gets the corresponding chat color for the given dye color
     *
     * @param dyeColor <p>The dye color to convert into a chat color</p>
     * @return <p>The chat color corresponding to the given dye color</p>
     */
    public static ChatColor getChatColorFromDyeColor(DyeColor dyeColor) {
        Color color = dyeColor.getColor();
        return ChatColor.of(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
    }

    /**
     * Gets the dye color corresponding to the given dye material
     *
     * @param material <p>The material to get the dye color from</p>
     * @return <p>The dye color corresponding to the dye material</p>
     */
    public static DyeColor getDyeColorFromMaterial(Material material) {
        return materialToColorsConversionMap.get(material);
    }

    /**
     * Gets the dye material corresponding to the given dye color
     *
     * @param dye <p>The dye to get the dye material from</p>
     * @return <p>The dye material </p>
     */
    public static Material getMaterialFromDyeColor(DyeColor dye) {
        return dyeColorToMaterialColorsConversionMap.get(dye);
    }

    public static ChatColor getInvertedChatColorFromDyeColor(DyeColor dyeColor) {
        Color color = dyeColor.getColor();
        return ChatColor.of(String.format("#%02X%02X%02X", 255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue()));
    }
    
    public static ChatColor invertBrightness(ChatColor initialColor) {
        java.awt.Color convertedInitialColor = initialColor.getColor();
        float[] hsb = java.awt.Color.RGBtoHSB(convertedInitialColor.getRed(), convertedInitialColor.getGreen(), convertedInitialColor.getBlue(),  new float[3]);
        hsb[2] = (float) 1 - hsb[2];
        java.awt.Color color = java.awt.Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        return ChatColor.of(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(),color.getBlue()));
    }
}
