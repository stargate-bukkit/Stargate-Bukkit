package org.sgrewritten.stargate.colors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.property.NonLegacyClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

public class ColorRegistry {
    private ColorRegistry() {
        throw new IllegalStateException("Utility class");
    }

    public static final Map<DyeColor, ChatColor> TEXT_COLORS = loadColors(false, "/colors/colorTable.json");
    public static final Map<DyeColor, ChatColor> POINTER_COLORS = loadColors(true, "/colors/colorTable.json");
    public static final Map<StargateFlag, ChatColor> FLAG_COLORS = loadFlagColors("/colors/flagColorTable.json");
    public static final Map<ColorSelector, ChatColor> DEFAULT_COLORS = loadDefaultColors();
    public static org.bukkit.ChatColor LEGACY_SIGN_COLOR = org.bukkit.ChatColor.BLACK;
    public static DyeColor DEFAULT_DYE_COLOR = DyeColor.BLACK;

    private static Map<ColorSelector, ChatColor> loadDefaultColors() {
        Map<ColorSelector, ChatColor> output = new EnumMap<>(ColorSelector.class);
        output.put(ColorSelector.TEXT, ChatColor.BLACK);
        output.put(ColorSelector.POINTER, ChatColor.WHITE);
        return output;
    }

    private static Map<DyeColor, ChatColor> loadColors(boolean isPointer, @NotNull String fileName) {
        try (InputStream inputStream = Stargate.class.getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("Could not find internal file: " + fileName);
            }
            try (Reader reader = new InputStreamReader(inputStream)) {
                JsonElement jsonData = JsonParser.parseReader(reader);
                Map<DyeColor, ChatColor> output = new EnumMap<>(DyeColor.class);
                for (JsonElement jsonElement : jsonData.getAsJsonArray()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    DyeColor dyeColor = DyeColor.valueOf(jsonObject.get("color").getAsString());
                    String hexColor;
                    if (isPointer) {
                        hexColor = jsonObject.get("pointer").getAsString();
                    } else {
                        hexColor = jsonObject.get("text").getAsString();
                    }
                    output.put(dyeColor, ChatColor.of("#" + hexColor));
                }
                return Collections.unmodifiableMap(output);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<StargateFlag, ChatColor> loadFlagColors(@NotNull String fileName) {
        try (InputStream inputStream = Stargate.class.getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("Could not find internal file: " + fileName);
            }
            try (Reader reader = new InputStreamReader(inputStream)) {
                JsonElement jsonData = JsonParser.parseReader(reader);
                Map<StargateFlag, ChatColor> output = new EnumMap<>(StargateFlag.class);
                for (JsonElement jsonElement : jsonData.getAsJsonArray()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    DyeColor dyeColor = DyeColor.valueOf(jsonObject.get("dyeColor").getAsString());
                    StargateFlag portalFlag = StargateFlag.valueOf(jsonObject.get("flag").getAsString());
                    output.put(portalFlag, POINTER_COLORS.get(dyeColor));
                }
                return Collections.unmodifiableMap(output);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadDefaultColorsFromConfig() {
        try {
            if (!NonLegacyClass.CHAT_COLOR.isImplemented()) {
                Stargate.log(Level.INFO, "Default stargate coloring is not supported on your current server implementation");
                LEGACY_SIGN_COLOR = org.bukkit.ChatColor.valueOf(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_SIGN_COLOR).toUpperCase());
                return;
            }
            String defaultColorString = ConfigurationHelper.getString(ConfigurationOption.DEFAULT_SIGN_COLOR);
            DEFAULT_COLORS.put(ColorSelector.TEXT, ColorNameInterpreter.getDefaultTextColor(defaultColorString));
            DEFAULT_COLORS.put(ColorSelector.POINTER, ColorNameInterpreter.getDefaultPointerColor(defaultColorString));
            DEFAULT_DYE_COLOR = ColorConverter.getClosestDyeColor(DEFAULT_COLORS.get(ColorSelector.TEXT));
        } catch (IllegalArgumentException | NullPointerException e) {
            Stargate.log(e);
            Stargate.log(Level.WARNING, "Invalid colors for sign text. Using default colors instead...");
        }
    }

}
