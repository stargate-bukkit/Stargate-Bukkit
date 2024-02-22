package net.knarcraft.stargate.portal.teleporter;

import net.knarcraft.stargate.event.StargateEntityPortalEvent;
import net.knarcraft.stargate.portal.Portal;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * The portal teleporter takes care of the actual portal teleportation for any entities
 */
public class EntityTeleporter extends Teleporter {

    private final Entity teleportingEntity;

    /**
     * Instantiates a new portal teleporter
     *
     * @param targetPortal <p>The portal which is the target of the teleportation</p>
     */
    public EntityTeleporter(@NotNull Portal targetPortal, @NotNull Entity teleportingEntity) {
        super(targetPortal, teleportingEntity);
        this.teleportingEntity = teleportingEntity;
    }

    /**
     * Teleports an entity to this teleporter's portal
     *
     * @param origin <p>The portal the entity is teleporting from</p>
     * @return <p>True if the entity was teleported. False otherwise</p>
     */
    public boolean teleportEntity(@NotNull Portal origin) {
        return teleport(origin, new StargateEntityPortalEvent(teleportingEntity, origin, portal, exit));
    }

}
