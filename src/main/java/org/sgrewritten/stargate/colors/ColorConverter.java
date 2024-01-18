package org.sgrewritten.stargate.colors;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.EnumMap;

/**
 * A converter for converting between different types of colors
 */
public final class ColorConverter {

    private static final String STRING_TO_INSERT = "#%02X%02X%02X";
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
        return ChatColor.of(String.format(STRING_TO_INSERT, color.getRed(), color.getGreen(), color.getBlue()));
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

    /**
     * Invert the chatColor.
     *
     * @param initialColor <p>The color to invert</p>
     * @return <p>The inverted color</p>
     */
    public static ChatColor getInvertedChatColor(ChatColor initialColor) {
        java.awt.Color color = initialColor.getColor();
        return ChatColor.of(String.format(STRING_TO_INSERT, 255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue()));
    }

    /**
     * Convert from java.awt.Color to net.md_5.bungee.api.ChatColor
     *
     * @param color <p>A color to convert</p>
     * @return <p>A converted color</p>
     */
    public static ChatColor colorToChatColor(java.awt.Color color) {
        return ChatColor.of(String.format(STRING_TO_INSERT, color.getRed(), color.getGreen(), color.getBlue()));
    }

    /**
     * Get the hue of a rgb format color by converting it into hsb
     *
     * @param color <p>A color to check the hue of</p>
     * @return <p>A hue of the color</p>
     */
    public static short getHue(ChatColor color) {
        java.awt.Color convertedInitialColor = color.getColor();
        float[] hsb = java.awt.Color.RGBtoHSB(convertedInitialColor.getRed(), convertedInitialColor.getGreen(),
                convertedInitialColor.getBlue(), new float[3]);

        if (hsb[1] == 0f || hsb[2] == 0f) {
            return -1;
        }

        return (short) Math.round(hsb[0] * 360);
    }

    /**
     * Gets the dye-color with the closest similarity of the chat-color
     *
     * @param color <p>Any chat color</p>
     * @return <p>A dye color similar to the chat-color</p>
     */
    public static DyeColor getClosestDyeColor(ChatColor color) {
        int minProximity = -1;
        DyeColor closestColor = null;
        for (DyeColor dyeColor : DyeColor.values()) {
            int proximity = getColorProximity(color, dyeColor);
            if (proximity < minProximity || minProximity == -1) {
                minProximity = proximity;
                closestColor = dyeColor;
            }
        }
        return closestColor;
    }

    /**
     * Get an index on how similar the colors are
     *
     * @param color    <p>A chatColor</p>
     * @param dyeColor <p>A dyeColor</p>
     * @return <p> The proximity of the two colors </p>
     */
    private static int getColorProximity(ChatColor color, DyeColor dyeColor) {
        Color bukkitColor = dyeColor.getColor();
        java.awt.Color javaColor = color.getColor();
        return Math.abs(javaColor.getRed() - bukkitColor.getRed()) + Math.abs(javaColor.getGreen() -
                bukkitColor.getGreen()) + Math.abs(javaColor.getBlue() - bukkitColor.getBlue());
    }
}
