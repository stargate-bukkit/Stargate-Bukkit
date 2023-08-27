package org.sgrewritten.stargate.util.colors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.util.FileHelper;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ColorProperty {
    private static final Map<PortalFlag, Short> flagColorHues = new EnumMap<>(PortalFlag.class);
    private static final Map<Material, TwoTuple<Short, Short>> signSaturationMap = new EnumMap<>(Material.class);
    private static final Map<Material, TwoTuple<Short, Short>> signBrightnessMap = new EnumMap<>(Material.class);

    static {
        loadFlagHues();
        loadSignSaturationBrightness();
    }


    /**
     * Load all flag hues from internal storage
     */
    private static void loadFlagHues() {
        Map<String, String> flagColorsString = new HashMap<>();
        FileHelper.readInternalFileToMap("/colors/flagColors.properties", flagColorsString);
        for (String key : flagColorsString.keySet()) {
            flagColorHues.put(PortalFlag.valueOf(key), Short.valueOf(flagColorsString.get(key)));
        }
    }

    /**
     * Load saturation and brightness for each sign
     */
    private static void loadSignSaturationBrightness() {
        /*
         * Look into the /colors/signTextSaturationBrightness.properties for more info on how the data is stored
         */
        Map<String, String> signSaturationBrightnessMapStrings = new HashMap<>();
        FileHelper.readInternalFileToMap("/colors/signTextSaturationBrightness.properties", signSaturationBrightnessMapStrings);
        for (String key : signSaturationBrightnessMapStrings.keySet()) {
            String saturationBrightnessString = signSaturationBrightnessMapStrings.get(key);
            JsonElement json = JsonParser.parseString(saturationBrightnessString);
            JsonArray array = json.getAsJsonArray();
            JsonArray text = array.get(0).getAsJsonArray();
            JsonArray highlight = array.get(1).getAsJsonArray();
            Short textSaturation = text.get(0).getAsShort();
            Short textBrightness = text.get(1).getAsShort();
            Short highlightSaturation = highlight.get(0).getAsShort();
            Short highlightBrightness = highlight.get(1).getAsShort();
            try {
                signSaturationMap.put(Material.valueOf(key), new TwoTuple<>(textSaturation, highlightSaturation));
                signBrightnessMap.put(Material.valueOf(key), new TwoTuple<>(textBrightness, highlightBrightness));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    /**
     * Create a color from the hue and internal saturation/brightness values gotten from the sign type
     *
     * @param signMaterial <p> The material of the sign </p>
     * @param hue          <p> The hue tto be applied </p>
     * @param isHighlight  <p> Whether this to take internal values from pointer/highlight or not </p>
     * @return <p> A color optimised for the sign, given the hue </p>
     */
    public static ChatColor getColorFromHue(Material signMaterial, short hue, boolean isHighlight) {
        if(!signSaturationMap.containsKey(signMaterial)){
            Stargate.log(Level.WARNING,"Coloring for sign material " + signMaterial + " is not implemented, please contact development");
            return ChatColor.BLACK;
        }

        float saturation = (hue != -1) ? (float) getSaturationFromSignMaterial(signMaterial, isHighlight) / 100 : 0f;
        float brightness = (float) getBrightnessFromSignMaterial(signMaterial, isHighlight) / 100;

        return ColorConverter.colorToChatColor(java.awt.Color.getHSBColor((float) hue / 360, saturation, brightness));
    }

    /**
     * Get a saturation from a sign material
     *
     * @param signMaterial <p> A saturation from a signmaterial </p>
     * @param isHighlight  <p> Whether this to take internal values from pointer/highlight or not </p>
     * @return <p> A saturation </p>
     */
    private static short getSaturationFromSignMaterial(Material signMaterial, boolean isHighlight) {
        if (isHighlight) {
            return signSaturationMap.get(signMaterial).getSecondValue();
        }
        return signSaturationMap.get(signMaterial).getFirstValue();
    }

    /**
     * Get a brightness from a sign material
     *
     * @param signMaterial <p> A brightness from a signmaterial </p>
     * @param isHighlight  <p> Whether this to take internal values from pointer/highlight or not </p>
     * @return <p> A brightness </p>
     */
    private static short getBrightnessFromSignMaterial(Material signMaterial, boolean isHighlight) {
        Stargate.log(Level.FINEST,"Sign material of " + signMaterial);
        if (isHighlight) {
            return signBrightnessMap.get(signMaterial).getSecondValue();
        }
        return signBrightnessMap.get(signMaterial).getFirstValue();
    }

    /**
     * A stupid way to avoid localisation
     *
     * @return <p> A map of all the hues for the flags </p>
     */
    public static Map<PortalFlag, Short> getFlagColorHues() {
        return flagColorHues;
    }
}
