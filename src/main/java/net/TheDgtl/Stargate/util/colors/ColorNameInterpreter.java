package net.TheDgtl.Stargate.util.colors;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.DyeColor;

import net.TheDgtl.Stargate.util.FileHelper;
import net.md_5.bungee.api.ChatColor;

public class ColorNameInterpreter {

    /**
     * Determine a possible color based out of a string
     * Accepts: CSS color codes, DyeColors, ChatColors, java.awt.Colors, hex, rgb, rgba, hsb
     * @param color <p> A valid color string </p>
     * @return <p> A color </p>
     */
    public static ChatColor getColor(String color) {
        try {
            return getColorFromColorCode(color);
        } catch(IllegalArgumentException | NullPointerException ignored) {}
        
        try {
            return getColorFromRGBCode(color);
        } catch(IllegalArgumentException ignored) {}
        
        try {
            return getColorFromHexCode(color);
        } catch(IllegalArgumentException ignored) {}
        
        try {
            return getColorFromHSBCode(color);
        } catch(IllegalArgumentException ignored) {}
        
        throw new IllegalArgumentException();
    }
    
    /**
     * Look through all types of color enums and other color codes (including css codes) to determine a color
     * @param color <p> A possible colorcode </p>
     * @return <p> The color of that colorcode </p>
     * @throws IllegalArgumentException <p> If no colorcode could be matched </p> 
     */
    private static ChatColor getColorFromColorCode(String color) throws IllegalArgumentException{
        try {
            return ChatColor.valueOf(color.toUpperCase());
        } catch(IllegalArgumentException ignored) {}
        
        try {
            DyeColor dyeColor = DyeColor.valueOf(color.toUpperCase());
            return ColorConverter.getChatColorFromDyeColor(dyeColor);
        } catch(IllegalArgumentException ignored) {}
        
        try {
            return org.bukkit.ChatColor.valueOf(color.toUpperCase()).asBungee();
        } catch(IllegalArgumentException ignored) {}
        
        try {
            java.awt.Color javaColor = java.awt.Color.getColor(color.toUpperCase());
            return ColorConverter.colorToChatColor(javaColor);
        } catch(NullPointerException ignored) {}

        try {
        Map<String,String> otherColorCodes = new HashMap<>();
        FileHelper.readInternalFileToMap("/colors/colorCodes.properties", otherColorCodes);
        return getColorFromHexCode(otherColorCodes.get(color.toLowerCase()));
        } catch(NullPointerException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Determine a color from a hexcode
     * @param hexCode <p> Any valid hexcode </p>
     * @return <p> The color of that hexcode </p>
     * @throws IllegalArgumentException <p> If the hexcode is not valid </p>
     */
    private static ChatColor getColorFromHexCode(String hexCode) throws IllegalArgumentException{
        if(hexCode.startsWith("HEX(")) {
            hexCode = "#" + hexCode.substring(4,hexCode.length()-1);
        }
        if(!hexCode.startsWith("#")) {
            hexCode = "#" + hexCode;
        }
        // account for 3 digit long hex, such as #FFF
        if(hexCode.length() < 5) {
            hexCode = "#" + hexCode.charAt(1) + hexCode.charAt(1) +hexCode.charAt(2) +hexCode.charAt(2) +hexCode.charAt(3) +hexCode.charAt(3);
        }
        return ChatColor.of(hexCode);
    }
    
    /**
     * Determine the color from any given rgb code
     * @param rgbCode <p> A valid rgb code </p>
     * @return <p> The color of that rgb code </p>
     * @throws IllegalArgumentException <p> If the rgb code is not valid </p>
     */
    private static ChatColor getColorFromRGBCode(String rgbCode) throws IllegalArgumentException{
        if(!(rgbCode.startsWith("(") || rgbCode.toLowerCase().startsWith("rgb"))) {
            throw new IllegalArgumentException();
        }
        String rgbArgumentsString = rgbCode.substring(rgbCode.indexOf("(")+1, rgbCode.length() - 1);
        String[] values = rgbArgumentsString.replace(" ", "") .split("(,|:)");
        // Check only the 3 first values, as those are the only ones related to hue (for
        // example rgb in rgba)
        return ChatColor.of(String.format("#%02X%02X%02X", Integer.valueOf(values[0]),
                Integer.valueOf(values[1]), Integer.valueOf(values[2])));
    }
    
    /**
     * Determine the color from a hsb code
     * @param hsbCode <p> A hsb code </p>
     * @return <p> The color of that hsb code </p>
     * @throws IllegalArgumentException <p> If the hsb code is not valid </p>
     */
    private static ChatColor getColorFromHSBCode(String hsbCode) throws IllegalArgumentException{
        if(!hsbCode.toLowerCase().startsWith("hsb(")) {
            throw new IllegalArgumentException();
        }
        String hsbArgumentsString = hsbCode.substring(hsbCode.indexOf("(")+1, hsbCode.length() - 1);
        String[] values = hsbArgumentsString.replace(" ", "") .split("(,|:)");
        
        try {
            java.awt.Color color = java.awt.Color.getHSBColor((float) Integer.valueOf(values[0])/360,
                    (float) Integer.valueOf(values[1])/100, (float) Integer.valueOf(values[2])/100);
            return ColorConverter.colorToChatColor(color);
        } catch(IllegalArgumentException e) {
            java.awt.Color color = java.awt.Color.getHSBColor((float) Integer.valueOf(values[0])/360,
                    Float.valueOf(values[1]), Float.valueOf(values[2]));
            return ColorConverter.colorToChatColor(color);
        }
    }
}
