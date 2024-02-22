package net.knarcraft.stargate.utility;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * This helper class helps with entity properties not immediately available
 */
public final class EntityHelper {

    private EntityHelper() {

    }

    /**
     * Gets the max size of an entity along its x and z axis
     *
     * <p>This function gets the ceiling of the max size of an entity, thus calculating the smallest box, using whole
     * blocks as unit, needed to contain the entity. Assuming n is returned, an (n x n) box is needed to contain the
     * entity.</p>
     *
     * @param entity <p>The entity to get max size for</p>
     * @return <p>The max size of the entity</p>
     */
    public static int getEntityMaxSizeInt(@NotNull Entity entity) {
        return (int) Math.ceil((float) getEntityMaxSize(entity));
    }

    /**
     * Gets the max size of an entity along its x and z axis
     *
     * @param entity <p>The entity to get max size for</p>
     * @return <p>The max size of the entity</p>
     */
    public static double getEntityMaxSize(@NotNull Entity entity) {
        return Math.max(entity.getBoundingBox().getWidthX(), entity.getBoundingBox().getWidthZ());
    }

}
