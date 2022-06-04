package net.TheDgtl.Stargate.util;

import org.bukkit.Material;
import org.bukkit.Tag;

/**
 * A helper class for dealing with buttons
 */
public final class ButtonHelper {

    private ButtonHelper() {

    }

    /**
     * Checks whether the given material is a valid button
     *
     * @param material <p>The material to check</p>
     * @return <p>True if the material is a button</p>
     */
    public static boolean isButton(Material material) {
        return Tag.BUTTONS.isTagged(material) || Tag.WALL_CORALS.isTagged(material) ||
                material == Material.DEAD_TUBE_CORAL_WALL_FAN ||
                material == Material.DEAD_HORN_CORAL_WALL_FAN ||
                material == Material.DEAD_FIRE_CORAL_WALL_FAN ||
                material == Material.DEAD_BUBBLE_CORAL_WALL_FAN ||
                material == Material.DEAD_BRAIN_CORAL_WALL_FAN;
    }

}
