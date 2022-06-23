package net.TheDgtl.Stargate.network.portal;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.minecart.PoweredMinecart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A somewhat altered DFS used to find entities involved in a teleportation
 */
public class TeleportedEntityRelationDFS {

    private final Function<Entity, Boolean> permissionFunction;
    private final List<LivingEntity> nearbyLeashedEntities;

    private final Map<Entity, List<LivingEntity>> leashHolders;
    private final Map<Entity, List<Entity>> passengerVehicles;
    private final Set<Entity> entitiesToTeleport;
    private boolean isBlockTeleportation = false;

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
        this.entitiesToTeleport = new HashSet<>();
    }

    /**
     * Gets the map between a living entity and its leash holder
     *
     * @return <p>The map of leash holders</p>
     */
    public Map<Entity, List<LivingEntity>> getLeashHolders() {
        return leashHolders;
    }

    /**
     * Gets the map between an entity and its vehicle
     *
     * @return <p>The map of vehicles</p>
     */
    public Map<Entity, List<Entity>> getPassengerVehicles() {
        return passengerVehicles;
    }

    /**
     * Gets all entities part of the teleportation
     *
     * @return <p>All entities part of the teleportation</p>
     */
    public Set<Entity> getEntitiesToTeleport() {
        return entitiesToTeleport;
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

        if(node instanceof PoweredMinecart) {
            this.isBlockTeleportation  = true;
        }
        
        List<Entity> passengers = node.getPassengers();
        for (Entity passenger : node.getPassengers()) {
            isSuccess &= depthFirstSearch(passenger);
        }
        passengerVehicles.put(node, passengers);

        List<LivingEntity> leashedEntities = new ArrayList<>();
        for (LivingEntity leashed : nearbyLeashedEntities) {
            if (leashed.getLeashHolder() == node) {
                leashedEntities.add(leashed);
                isSuccess &= depthFirstSearch(leashed);
            }
        }
        leashHolders.put(node, leashedEntities);
        return isSuccess;
    }

    public boolean isBlockTeleportation() {
        return this.isBlockTeleportation;
    }
}
