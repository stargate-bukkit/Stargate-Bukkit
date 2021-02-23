package net.knarcraft.stargate.utility;

import org.bukkit.entity.Entity;

/**
 * This helper class helps with entity properties not immediately available
 */
public final class EntityHelper {

    private EntityHelper() {

    }

    /**
     * Gets the max size of an entity along its x and z axis
     *
     * <p>This function gets the ceiling of the max size of an entity, thus calculating the smallest square needed to
     * contain the entity.</p>
     *
     * @param entity <p>The entity to get max size for</p>
     * @return <p></p>
     */
    public static double getEntityMaxSize(Entity entity) {
        return Math.ceil((float) Math.max(entity.getBoundingBox().getWidthX(), entity.getBoundingBox().getWidthZ()));
    }

}
