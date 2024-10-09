package net.knarcraft.stargate.config.material;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A specifier for a Bukkit material tag
 */
public class BukkitTagSpecifier implements MaterialSpecifier {

    private final Tag<Material> tag;

    /**
     * Instantiates a new tag specifier
     *
     * @param tag <p>The tag to specify</p>
     */
    public BukkitTagSpecifier(@NotNull Tag<Material> tag) {
        this.tag = tag;
    }

    @Override
    public @NotNull String asString() {
        return "#" + this.tag.getKey().toString().replaceFirst("minecraft:", "");
    }

    @Override
    public @NotNull Set<Material> asMaterials() {
        return this.tag.getValues();
    }


    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BukkitTagSpecifier bukkitMaterialSpecifier)) {
            return false;
        }
        return this.tag == bukkitMaterialSpecifier.tag;
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

}
