package net.TheDgtl.Stargate.util;

import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.Tag;

import net.TheDgtl.Stargate.Stargate;

/**
 * A helper class for dealing with buttons
 */
public final class ButtonHelper {

    private static final Material DEFAULT_BUTTON = Material.STONE_BUTTON;
    private static final Material DEFAULT_WATER_BUTTON = Material.DEAD_TUBE_CORAL_WALL_FAN;
    
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

    


    /**
     * Gets the button material to use for this gate
     *
     * @return <p>The button material to use for this gate</p>
     */
    public static Material getButtonMaterial(Material portalClosedMaterial) {
        //TODO: Add support for using solid blocks as the gate-closed material for underwater portals
        switch (portalClosedMaterial) {
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
                return DEFAULT_BUTTON;
            case WATER:
                return DEFAULT_WATER_BUTTON;
            default:
                Stargate.log(Level.FINE, portalClosedMaterial.name() +
                        " is currently not supported as a portal closed material");
                return DEFAULT_BUTTON;
        }
    }
}
