package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.event.StargateEntityPortalEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * The portal teleporter takes care of the actual portal teleportation for any entities
 */
public class EntityTeleporter extends Teleporter {

    private final Entity teleportingEntity;

    /**
     * Instantiates a new portal teleporter
     *
     * @param portal <p>The portal which is the target of the teleportation</p>
     */
    public EntityTeleporter(Portal portal, Entity teleportingEntity) {
        super(portal);
        this.teleportingEntity = teleportingEntity;
    }

    /**
     * Teleports an entity to this teleporter's portal
     *
     * @param origin <p>The portal the entity is teleporting from</p>
     * @return <p>True if the entity was teleported. False otherwise</p>
     */
    public boolean teleport(Portal origin) {
        Location traveller = teleportingEntity.getLocation();
        Location exit = getExit(teleportingEntity, traveller);

        //Rotate the entity to face out from the portal
        adjustRotation(exit);

        //Call the StargateEntityPortalEvent to allow plugins to change destination
        if (!origin.equals(portal)) {
            exit = triggerEntityPortalEvent(origin, exit);
            if (exit == null) {
                return false;
            }
        }

        //Load chunks to make sure not to teleport to the void
        loadChunks();

        teleportingEntity.teleport(exit);
        return true;
    }

    /**
     * Triggers the entity portal event to allow plugins to change the exit location
     *
     * @param origin <p>The origin portal teleported from</p>
     * @param exit   <p>The exit location to teleport the entity to</p>
     * @return <p>The location the entity should be teleported to, or null if the event was cancelled</p>
     */
    protected Location triggerEntityPortalEvent(Portal origin, Location exit) {
        StargateEntityPortalEvent stargateEntityPortalEvent = new StargateEntityPortalEvent(teleportingEntity, origin,
                portal, exit);
        Stargate.getInstance().getServer().getPluginManager().callEvent(stargateEntityPortalEvent);
        //Teleport is cancelled. Teleport the entity back to where it came from just for sanity's sake
        if (stargateEntityPortalEvent.isCancelled()) {
            new EntityTeleporter(origin, teleportingEntity).teleport(origin);
            return null;
        }
        return stargateEntityPortalEvent.getExit();
    }
}
