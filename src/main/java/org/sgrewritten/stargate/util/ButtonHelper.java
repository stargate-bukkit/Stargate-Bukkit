package org.sgrewritten.stargate.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * A helper class for dealing with buttons
 */
public final class ButtonHelper {
    private static final List<Material> BUTTONS = List.copyOf(Tag.BUTTONS.getValues());
    private static final List<Material> CORAL_WALL_FANS = Stream.concat(Tag.WALL_CORALS.getValues().stream(), getDeadWallCoralWallFans()).toList();
    private static final Random RANDOM = new Random();

    private ButtonHelper() {

    }

    /**
     * Checks whether the given material is a valid button
     *
     * @param material <p>The material to check</p>
     * @return <p>True if the material is a button</p>
     */
    public static boolean isButton(Material material) {
        return BUTTONS.contains(material) || CORAL_WALL_FANS.contains(material);
    }


    /**
     * Gets the button material to use for this gate
     *
     * @param locationToGenerateButton <p>The location of the button to be generated</p>
     * @return <p>The button material to use for this gate</p>
     */
    public static Material getButtonMaterial(Location locationToGenerateButton) {
        Material typeToReplace = locationToGenerateButton.getBlock().getType();
        if (typeToReplace.isAir()) {
            return getOverWaterButtonMaterial();
        }
        if (typeToReplace == Material.WATER) {
            return getUnderWaterButtonMaterial();
        }
        BlockData blockData = locationToGenerateButton.getBlock().getBlockData();
        if (blockData instanceof Waterlogged waterlogged) {
            if (waterlogged.isWaterlogged()) {
                return getUnderWaterButtonMaterial();
            } else {
                return getOverWaterButtonMaterial();
            }
        }
        return getOverWaterButtonMaterial();
    }

    private static Material getOverWaterButtonMaterial() {
        return BUTTONS.get(RANDOM.nextInt(BUTTONS.size()));
    }

    private static Material getUnderWaterButtonMaterial() {
        return CORAL_WALL_FANS.get(RANDOM.nextInt(CORAL_WALL_FANS.size()));
    }

    private static Stream<Material> getDeadWallCoralWallFans() {
        return Stream.of(Material.DEAD_TUBE_CORAL_WALL_FAN, Material.DEAD_HORN_CORAL_WALL_FAN,
                Material.DEAD_FIRE_CORAL_WALL_FAN, Material.DEAD_BUBBLE_CORAL_WALL_FAN, Material.DEAD_BRAIN_CORAL_WALL_FAN);
    }
}
