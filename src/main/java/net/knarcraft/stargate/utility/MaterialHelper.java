package net.knarcraft.stargate.utility;

import org.bukkit.Material;
import org.bukkit.Tag;

/**
 * This class helps decide properties of materials not already present in the Spigot API
 */
public final class MaterialHelper {

    private MaterialHelper() {

    }

    /**
     * Checks whether the given material is a dead or alive wall coral
     *
     * @param material <p>The material to check</p>
     * @return <p>True if the material is a wall coral</p>
     */
    public static boolean isWallCoral(Material material) {
        return Tag.WALL_CORALS.isTagged(material) ||
                material.equals(Material.DEAD_BRAIN_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_BUBBLE_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_FIRE_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_HORN_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_TUBE_CORAL_WALL_FAN);
    }

    /**
     * Checks whether the given material can be used as a button
     *
     * @param material <p>The material to check</p>
     * @return <p>True if the material can be used as a button</p>
     */
    public static boolean isButtonCompatible(Material material) {
        return Tag.BUTTONS.isTagged(material) || isWallCoral(material) || Tag.SHULKER_BOXES.isTagged(material) ||
                material == Material.CHEST;
    }

}
