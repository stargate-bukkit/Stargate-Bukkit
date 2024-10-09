package net.knarcraft.stargate.config.material;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A specifier for a Bukkit material
 */
public class BukkitMaterialSpecifier implements MaterialSpecifier {

    private final Material material;

    /**
     * Instantiates a new material specifier
     *
     * @param material <p>The material to specify</p>
     */
    public BukkitMaterialSpecifier(@NotNull Material material) {
        this.material = material;
    }

    @Override
    @NotNull
    public String asString() {
        return this.material.name();
    }

    @Override
    @NotNull
    public Set<Material> asMaterials() {
        return Set.of(this.material);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BukkitMaterialSpecifier bukkitMaterialSpecifier)) {
            return false;
        }
        return this.material == bukkitMaterialSpecifier.material;
    }

    @Override
    public int hashCode() {
        return material.hashCode();
    }

}
