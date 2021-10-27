package net.knarcraft.stargate.utility;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MaterialHelperTest {

    @BeforeAll
    public static void setUp() {
        MockBukkit.mock();
    }
    
    @Test
    public void isWallCoralTest() {
        Assertions.assertTrue(MaterialHelper.isWallCoral(Material.DEAD_BRAIN_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isWallCoral(Material.BRAIN_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isWallCoral(Material.DEAD_BUBBLE_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isWallCoral(Material.BUBBLE_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isWallCoral(Material.DEAD_FIRE_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isWallCoral(Material.FIRE_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isWallCoral(Material.DEAD_HORN_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isWallCoral(Material.HORN_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isWallCoral(Material.DEAD_TUBE_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isWallCoral(Material.TUBE_CORAL_WALL_FAN));
        
        Assertions.assertFalse(MaterialHelper.isWallCoral(Material.DEAD_TUBE_CORAL));
        Assertions.assertFalse(MaterialHelper.isWallCoral(Material.TUBE_CORAL));
        Assertions.assertFalse(MaterialHelper.isWallCoral(Material.TUBE_CORAL_BLOCK));
    }
    
    @Test
    public void isButtonCompatibleTest() {
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.DEAD_BRAIN_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.BRAIN_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.DEAD_BUBBLE_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.BUBBLE_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.DEAD_FIRE_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.FIRE_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.DEAD_HORN_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.HORN_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.DEAD_TUBE_CORAL_WALL_FAN));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.TUBE_CORAL_WALL_FAN));

        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.STONE_BUTTON));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.BIRCH_BUTTON));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.ACACIA_BUTTON));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.CRIMSON_BUTTON));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.OAK_BUTTON));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.DARK_OAK_BUTTON));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.JUNGLE_BUTTON));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.POLISHED_BLACKSTONE_BUTTON));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.SPRUCE_BUTTON));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.WARPED_BUTTON));

        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.BLACK_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.RED_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.GREEN_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.BLUE_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.YELLOW_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.CYAN_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.LIME_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.BROWN_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.GRAY_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.LIGHT_BLUE_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.LIGHT_GRAY_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.MAGENTA_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.ORANGE_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.PINK_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.PURPLE_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.WHITE_SHULKER_BOX));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.SHULKER_BOX));

        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.CHEST));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.ENDER_CHEST));
        Assertions.assertTrue(MaterialHelper.isButtonCompatible(Material.TRAPPED_CHEST));
        
        //Chek something random to make sure isButtonCompatible is not just "return true;"
        Assertions.assertFalse(MaterialHelper.isButtonCompatible(Material.OAK_LOG));
    }
    
}
