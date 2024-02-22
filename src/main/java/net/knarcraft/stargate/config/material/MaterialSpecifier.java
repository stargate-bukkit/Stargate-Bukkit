package net.knarcraft.stargate.config.material;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * An interface describing a specifier for one or more Bukkit materials
 */
public interface MaterialSpecifier {

    /**
     * Gets the string representation of the material specifier
     *
     * <p>This is used when saving the value to a gate file</p>
     */
    @NotNull
    String asString();

    /**
     * Gets all the materials the specifier specifies
     *
     * <p>This is used when registering gate materials</p>
     */
    @NotNull
    Set<Material> asMaterials();

}
