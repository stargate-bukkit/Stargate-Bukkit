package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.config.material.BukkitMaterialSpecifier;
import net.knarcraft.stargate.config.material.BukkitTagSpecifier;
import net.knarcraft.stargate.config.material.MaterialSpecifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public static boolean isWallCoral(@NotNull Material material) {
        //Unfortunately, there is no tag for dead wall corals, so they need to be checked manually
        return Tag.WALL_CORALS.isTagged(material) ||
                material.equals(Material.DEAD_BRAIN_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_BUBBLE_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_FIRE_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_HORN_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_TUBE_CORAL_WALL_FAN);
    }

    /**
     * Checks whether the given material is a container
     *
     * @param material <p>The material to check</p>
     * @return <p>True if the material is a container</p>
     */
    public static boolean isContainer(@NotNull Material material) {
        return Tag.SHULKER_BOXES.isTagged(material) || material == Material.CHEST ||
                material == Material.TRAPPED_CHEST || material == Material.ENDER_CHEST;
    }

    /**
     * Checks whether the given material can be used as a button
     *
     * @param material <p>The material to check</p>
     * @return <p>True if the material can be used as a button</p>
     */
    public static boolean isButtonCompatible(@NotNull Material material) {
        return Tag.BUTTONS.isTagged(material) || isWallCoral(material) || isContainer(material);
    }

    @NotNull
    public static String specifiersToString(@NotNull List<MaterialSpecifier> specifiers) {
        List<String> names = new ArrayList<>();
        for (MaterialSpecifier specifier : specifiers) {
            names.add(specifier.asString());
        }
        return String.join(",", names);
    }

    /**
     * Converts a list of material specifiers to a set of materials
     *
     * @param specifiers <p>The material specifiers to convert</p>
     * @return <p>The materials the specifiers represent</p>
     */
    @NotNull
    public static Set<Material> specifiersToMaterials(@NotNull List<MaterialSpecifier> specifiers) {
        Set<Material> output = new HashSet<>();

        for (MaterialSpecifier specifier : specifiers) {
            output.addAll(specifier.asMaterials());
        }

        return output;
    }

    /**
     * Parses all materials and material tags found in the input string
     *
     * @param input <p>The input string to parse</p>
     * @return <p>All material specifiers found</p>
     */
    @NotNull
    public static List<MaterialSpecifier> parseTagsAndMaterials(@NotNull String input) {
        List<MaterialSpecifier> specifiers = new ArrayList<>();

        // Nothing to parse
        if (input.isBlank()) {
            return specifiers;
        }

        String[] parts;
        if (input.contains(",")) {
            parts = input.split(",");
        } else {
            parts = new String[]{input};
        }

        for (String part : parts) {
            MaterialSpecifier materialSpecifier = parseTagOrMaterial(part.trim());
            if (materialSpecifier != null) {
                specifiers.add(materialSpecifier);
            }
        }

        return specifiers;
    }

    @Nullable
    private static MaterialSpecifier parseTagOrMaterial(@NotNull String input) {
        if (input.startsWith("#")) {
            String tagString = input.replaceFirst("#", "").toLowerCase();
            Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(tagString), Material.class);
            if (tag != null) {
                return new BukkitTagSpecifier(tag);
            }
        } else {
            Material material = Material.matchMaterial(input);
            if (material != null) {
                return new BukkitMaterialSpecifier(material);
            }
        }

        return null;
    }

}
