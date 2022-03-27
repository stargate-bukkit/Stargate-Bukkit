package net.TheDgtl.Stargate.network.portal;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A somewhat altered DFS used to find entities involved in a teleportation
 */
public class TeleportedEntityRelationDFS {

    private final Function<Entity, Boolean> permissionFunction;
    private final List<LivingEntity> nearbyLeashedEntities;

    private final Map<LivingEntity, Entity> leashHolders;
    private final Map<Entity, Entity> passengerVehicles;
    private final List<Entity> entitiesToTeleport;

    /**
     * Instantiates a new entity relation Depth-First-Search
     *
     * @param permissionFunction    <p>The permission function to use for checking permissions of an entity</p>
     * @param nearbyLeashedEntities <p>The nearby leashed entities to consider part of the teleportation</p>
     */
    public TeleportedEntityRelationDFS(Function<Entity, Boolean> permissionFunction,
                                       List<LivingEntity> nearbyLeashedEntities) {
        this.permissionFunction = permissionFunction;
        this.nearbyLeashedEntities = nearbyLeashedEntities;
        this.leashHolders = new HashMap<>();
        this.passengerVehicles = new HashMap<>();
        this.entitiesToTeleport = new ArrayList<>();
    }

    /**
     * Gets the map between a living entity and its leash holder
     *
     * @return <p>The map of leash holders</p>
     */
    public Map<LivingEntity, Entity> getLeashHolders() {
        return leashHolders;
    }

    /**
     * Gets the map between an entity and its vehicle
     *
     * @return <p>The map of vehicles</p>
     */
    public Map<Entity, Entity> getPassengerVehicles() {
        return passengerVehicles;
    }

    /**
     * Gets all entities part of the teleportation
     *
     * @return <p>All entities part of the teleportation</p>
     */
    public List<Entity> getEntitiesToTeleport() {
        return entitiesToTeleport;
    }

    /**
     * Does a deep first search
     *
     * @param node            <p>The entity to run the dept-first-search</p>
     * @param nodeIsPassenger <p>Whether the node is a passenger of the parent or leashed by the parent</p>
     * @return <p>If the teleportation should proceed</p>
     */
    public boolean depthFirstSearch(Entity parent, Entity node, boolean nodeIsPassenger) {
        if (!permissionFunction.apply(node)) {
            return false;
        }

        boolean success = true;
        if (!passengerVehicles.containsKey(node)) {
            for (Entity passenger : node.getPassengers()) {
                success &= depthFirstSearch(node, passenger, true);
            }
        }

        if (!leashHolders.containsKey((LivingEntity) node)) {
            for (LivingEntity leashed : nearbyLeashedEntities) {
                if (leashed.getLeashHolder() == node) {
                    success &= depthFirstSearch(node, leashed, false);
                }
            }
        }

        //Calling this at the end means we consider passengers before vehicles
        if (!entitiesToTeleport.contains(node)) {
            entitiesToTeleport.add(node);
        }
        if (nodeIsPassenger && parent != null) {
            passengerVehicles.put(node, parent);
        } else if (parent != null) {
            leashHolders.put((LivingEntity) node, parent);
        }
        return success;
    }

}
