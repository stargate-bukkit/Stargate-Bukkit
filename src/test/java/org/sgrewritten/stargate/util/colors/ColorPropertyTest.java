package org.sgrewritten.stargate.util.colors;

import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.Stargate;

import java.util.logging.Level;

class ColorPropertyTest {

    @Test
    void signSBTest() {
        Material[] materialsToCheckFor = new Material[]{
                Material.ACACIA_WALL_SIGN,
                Material.BIRCH_WALL_SIGN,
                Material.CRIMSON_WALL_SIGN,
                Material.DARK_OAK_WALL_SIGN,
                Material.JUNGLE_WALL_SIGN,
                Material.OAK_WALL_SIGN,
                Material.SPRUCE_WALL_SIGN,
                Material.WARPED_WALL_SIGN
        };
        for (Material materialToCheckFor : materialsToCheckFor) {
            Stargate.log(Level.FINEST, materialToCheckFor.toString());
            Assertions.assertNotNull(ColorProperty.getColorFromHue(materialToCheckFor, (short) 0, false));
            Assertions.assertNotNull(ColorProperty.getColorFromHue(materialToCheckFor, (short) 0, true));
        }
    }

}
