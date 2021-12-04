package net.TheDgtl.Stargate.util;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * @author Thorin
 */
class ColorConverterTest {

    @Test
    void test() {
        for(DyeColor color : DyeColor.values()) {
            Assertions.assertTrue(ColorConverter.getChatColorFromDyeColor(color) != null);
            Material dye = ColorConverter.getMaterialFromDyeColor(color);
            Assertions.assertTrue(dye != null);
            Assertions.assertEquals(color,ColorConverter.getDyeColorFromMaterial(dye));
        }
    }

}
