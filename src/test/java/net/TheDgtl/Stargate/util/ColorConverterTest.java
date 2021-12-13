package net.TheDgtl.Stargate.util;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
