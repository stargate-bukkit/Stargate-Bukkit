package net.TheDgtl.Stargate.util.colors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.TheDgtl.Stargate.util.FileHelper;
import net.TheDgtl.Stargate.util.colors.ColorConverter;

/**
 * @author Thorin
 */
class ColorConverterTest {

    @Test
    void test() {
        for (DyeColor color : DyeColor.values()) {
            Assertions.assertNotNull(ColorConverter.getChatColorFromDyeColor(color));
            Material dye = ColorConverter.getMaterialFromDyeColor(color);
            Assertions.assertNotNull(dye);
            Assertions.assertEquals(color, ColorConverter.getDyeColorFromMaterial(dye));
        }
    }
}
