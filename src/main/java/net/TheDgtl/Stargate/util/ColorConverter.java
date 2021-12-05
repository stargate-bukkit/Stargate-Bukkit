package net.TheDgtl.Stargate.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.EnumMap;

public class ColorConverter {

    private static final EnumMap<DyeColor, ChatColor> dyeColorToChatColorMap = new EnumMap<>(DyeColor.class);

    static {
        dyeColorToChatColorMap.put(DyeColor.BLACK, ChatColor.of("#1D1D21"));
        dyeColorToChatColorMap.put(DyeColor.BLUE, ChatColor.of("#3C44AA"));
        dyeColorToChatColorMap.put(DyeColor.BROWN, ChatColor.of("#835432"));
        dyeColorToChatColorMap.put(DyeColor.CYAN, ChatColor.of("#169C9C"));
        dyeColorToChatColorMap.put(DyeColor.GRAY, ChatColor.of("#474F52"));
        dyeColorToChatColorMap.put(DyeColor.GREEN, ChatColor.of("#5E7C16"));
        dyeColorToChatColorMap.put(DyeColor.LIGHT_BLUE, ChatColor.of("#3AB3DA"));
        dyeColorToChatColorMap.put(DyeColor.LIGHT_GRAY, ChatColor.of("#9D9D97"));
        dyeColorToChatColorMap.put(DyeColor.LIME, ChatColor.of("#80C71F"));
        dyeColorToChatColorMap.put(DyeColor.MAGENTA, ChatColor.of("#C74EBD"));
        dyeColorToChatColorMap.put(DyeColor.ORANGE, ChatColor.of("#F9801D"));
        dyeColorToChatColorMap.put(DyeColor.PINK, ChatColor.of("#F38BAA"));
        dyeColorToChatColorMap.put(DyeColor.PURPLE, ChatColor.of("#8932B8"));
        dyeColorToChatColorMap.put(DyeColor.RED, ChatColor.of("#B02E26"));
        dyeColorToChatColorMap.put(DyeColor.WHITE, ChatColor.of("#F9FFFE"));
        dyeColorToChatColorMap.put(DyeColor.YELLOW, ChatColor.of("#FED83D"));
    }

    private static final EnumMap<Material, DyeColor> materialToColorsConversionMap = new EnumMap<>(Material.class);
    private static final EnumMap<DyeColor, Material> dyeColorToMaterialColorsConversionMap = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            Material dye = Material.getMaterial(color + "_DYE");
            materialToColorsConversionMap.put(dye, color);
            dyeColorToMaterialColorsConversionMap.put(color, dye);
        }
    }

    public static ChatColor getChatColorFromDyeColor(DyeColor color) {
        return dyeColorToChatColorMap.get(color);
    }

    public static DyeColor getDyeColorFromMaterial(Material mat) {
        return materialToColorsConversionMap.get(mat);
    }

    public static ChatColor getChatColorFromMaterial(Material mat) {
        return getChatColorFromDyeColor(getDyeColorFromMaterial(mat));
    }

    public static Material getMaterialFromDyeColor(DyeColor dye) {
        return dyeColorToMaterialColorsConversionMap.get(dye);
    }
}
