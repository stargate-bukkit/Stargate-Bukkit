package net.TheDgtl.Stargate.network.portal;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * A somewhat altered DFS used to find entities involved in a teleportation
 */
public class TeleportedEntityRelationDFS {

    private final Function<Entity, Boolean> permissionFunction;
    private final List<LivingEntity> nearbyLeashedEntities;
    private final Set<Entity> entitiesToTeleport;

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
        this.entitiesToTeleport = new HashSet<>();
    }

    /**
     * Does a deep first search
     *
     * @param node <p>The entity to run the dept-first-search</p>
     * @return <p>If the teleportation should proceed</p>
     */
    public boolean depthFirstSearch(Entity node) {
        //Note that full tree has to be explored to check if anything is an instance of PoweredMinecart
        boolean isSuccess = permissionFunction.apply(node);
        // Should be here to avoid checking the same node twice
        if (entitiesToTeleport.contains(node)) {
            return true;
        }
        entitiesToTeleport.add(node);

        //Recurse through all passengers
        for (Entity passenger : node.getPassengers()) {
            isSuccess &= depthFirstSearch(passenger);
        }

        //Recurse through all entities held in a leash by the node
        for (LivingEntity leashed : nearbyLeashedEntities) {
            if (leashed.getLeashHolder() == node) {
                isSuccess &= depthFirstSearch(leashed);
            }
        }
        return isSuccess;
    }

    /**
     * Gets all entities found during the last call to depthFirstSearch
     *
     * @return <p>All entities found during the search</p>
     */
    public Set<Entity> getEntitiesToTeleport() {
        return entitiesToTeleport;
    }

}
