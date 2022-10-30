package net.TheDgtl.Stargate.util.colors;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.TheDgtl.Stargate.container.TwoTuple;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.util.FileHelper;
import net.md_5.bungee.api.ChatColor;

public class ColorProperty {
    private static final Map<PortalFlag, Short> flagColorHues = new EnumMap<>(PortalFlag.class);
    private static final Map<Material,TwoTuple<Short,Short>> signSaturationMap = new EnumMap<>(Material.class);
    private static final Map<Material,TwoTuple<Short,Short>> signBrightnessMap = new EnumMap<>(Material.class);
    static {
        loadFlagHues();
        loadSignSaturationBrightness();
    }


    /**
     * Load all flag hues from internal storage
     */
    private static void loadFlagHues() {
        Map<String,String> flagColorsString = new HashMap<>();
        FileHelper.readInternalFileToMap("/colors/flagColors.properties", flagColorsString);
        for(String key : flagColorsString.keySet()) {
            flagColorHues.put(PortalFlag.valueOf(key), Short.valueOf(flagColorsString.get(key)));
        }
    }
    
    /**
     * Load saturation and brightness for each sign
     */
    private static void loadSignSaturationBrightness() {
        /**
         * Look into the /colors/signTextSaturationBrightness.properties for more info on how the data is stored
         */
        Map<String,String> signSaturationBrightnessMapStrings = new HashMap<>();
        FileHelper.readInternalFileToMap("/colors/signTextSaturationBrightness.properties", signSaturationBrightnessMapStrings);
        for(String key : signSaturationBrightnessMapStrings.keySet()) {
            String saturationBrightnessString = signSaturationBrightnessMapStrings.get(key);
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(saturationBrightnessString);
            JsonArray array = json.getAsJsonArray();
            JsonArray text = array.get(0).getAsJsonArray();
            JsonArray highligt = array.get(1).getAsJsonArray();
            Short textSaturation = text.get(0).getAsShort();
            Short textBrightness = text.get(1).getAsShort();
            Short highligtSaturation = highligt.get(0).getAsShort();
            Short highligtBrightness = highligt.get(1).getAsShort();
            try {
                signSaturationMap.put(Material.valueOf(key), new TwoTuple<>(textSaturation, highligtSaturation));
                signBrightnessMap.put(Material.valueOf(key), new TwoTuple<>(textBrightness, highligtBrightness));
            } catch (IllegalArgumentException ignored) {}
        }
    }
    
    /**
     * Create a color from the hue and internal saturation/brightness values gotten from the sign type
     * @param signMaterial <p> The material of the sign </p>
     * @param hue   <p> The hue tto be applied </p>
     * @param isHighlight <p> Whether this to take internal values from pointer/highlight or not </p>
     * @return <p> A color optimised for the sign, given the hue </p>
     */
    public static ChatColor getColorFromHue(Material signMaterial,short hue, boolean isHighlight) {
        float saturation = (hue != -1)? (float)getSaturationFromSignMaterial(signMaterial,isHighlight)/100 : 0f;
        float brightness = (float)getBrightnessFromSignMaterial(signMaterial,isHighlight)/100;

        return ColorConverter.colorToChatColor(java.awt.Color.getHSBColor((float)hue/360, saturation, brightness));
    }

    /**
     * Get a saturation from a sign material
     * @param signMaterial <p> A saturation from a signmaterial </p>
     * @param isHighlight <p> Whether this to take internal values from pointer/highlight or not </p>
     * @return <p> A saturation </p>
     */
    private static short getSaturationFromSignMaterial(Material signMaterial, boolean isHighlight) {
        if(isHighlight) {
            return signSaturationMap.get(signMaterial).getSecondValue();
        }
        return signSaturationMap.get(signMaterial).getFirstValue();
    }

    /**
     * Get a brightness from a sign material
     * @param signMaterial <p> A brightness from a signmaterial </p>
     * @param isHighlight <p> Whether this to take internal values from pointer/highlight or not </p>
     * @return <p> A brightness </p>
     */
    private static short getBrightnessFromSignMaterial(Material signMaterial, boolean isHighlight) {
        if(isHighlight) {
            return signBrightnessMap.get(signMaterial).getSecondValue();
        }
        return signBrightnessMap.get(signMaterial).getFirstValue();
    }

    /**
     * A stupid way to avoid localisation
     * @return <p> A map of all the hues for the flags </p>
     */
    public static  Map<PortalFlag, Short> getFlagColorHues() {
        return flagColorHues;
    }
}
