package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.teleporter.EntityTeleporter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class with methods for various teleportation tasks
 *
 * <p>The teleport helper mainly helps with passengers and leashed creatures</p>
 */
public final class TeleportHelper {

    private TeleportHelper() {

    }

    /**
     * Checks whether a player has leashed creatures that block the teleportation
     *
     * @param player <p>The player trying to teleport</p>
     * @return <p>False if the player has leashed any creatures that cannot go through the portal</p>
     */
    public static boolean noLeashedCreaturesPreventTeleportation(Player player) {
        //Find any nearby leashed entities to teleport with the player
        List<Creature> nearbyCreatures = getLeashedCreatures(player);

        //Disallow creatures with passengers to prevent smuggling
        for (Creature creature : nearbyCreatures) {
            if (!creature.getPassengers().isEmpty()) {
                return false;
            }
        }
        //TODO: Improve this to account for any players sitting on any of the lead creatures

        //If it's enabled, there is no problem
        if (Stargate.getGateConfig().handleLeashedCreatures()) {
            return true;
        } else {
            return nearbyCreatures.isEmpty();
        }
    }

    /**
     * Gets all creatures leashed by a player within the given range
     *
     * @param player <p>The player to check</p>
     * @return <p>A list of all creatures the player is holding in a leash (lead)</p>
     */
    public static List<Creature> getLeashedCreatures(Player player) {
        List<Creature> leashedCreatures = new ArrayList<>();
        //Find any nearby leashed entities to teleport with the player
        List<Entity> nearbyEntities = player.getNearbyEntities(15, 15, 15);
        //Teleport all creatures leashed by the player to the portal the player is to exit from
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Creature creature && creature.isLeashed() && creature.getLeashHolder() == player) {
                leashedCreatures.add(creature);
            }
        }
        return leashedCreatures;
    }

    /**
     * Teleports and adds a passenger to an entity
     *
     * <p>Teleportation of living vehicles is really buggy if you wait between the teleportation and passenger adding,
     * but there needs to be a delay between teleporting the vehicle and teleporting and adding the passenger.</p>
     *
     * @param targetVehicle <p>The entity to add the passenger to</p>
     * @param passenger     <p>The passenger to teleport and add</p>
     * @param exitDirection <p>The direction of any passengers exiting the stargate</p>
     */
    public static void teleportAndAddPassenger(Entity targetVehicle, Entity passenger, Vector exitDirection) {
        if (!passenger.teleport(targetVehicle.getLocation().clone().setDirection(exitDirection))) {
            Stargate.debug("handleVehiclePassengers", "Failed to teleport passenger" + passenger);
        }
        if (!targetVehicle.addPassenger(passenger)) {
            Stargate.debug("handleVehiclePassengers", "Failed to add passenger" + passenger);
        }
    }

    /**
     * Ejects, teleports and adds all passengers to the target entity
     *
     * @param passengers   <p>The passengers to handle</p>
     * @param entity       <p>The entity the passengers should be put into</p
     * @param origin       <p>The portal the entity teleported from</p>
     * @param target       <p>The portal the entity is teleporting to</p>
     * @param exitRotation <p>The rotation of any passengers exiting the stargate</p>
     */
    public static void handleEntityPassengers(List<Entity> passengers, Entity entity, Portal origin, Portal target,
                                              Vector exitRotation) {
        for (Entity passenger : passengers) {
            List<Entity> passengerPassengers = passenger.getPassengers();
            if (!passengerPassengers.isEmpty()) {
                Stargate.debug("Teleporter::handleEntityPassengers", "Found the entities: " +
                        passengerPassengers + " as passengers of " + entity);
            }
            if (passenger.eject()) {
                //Teleport any passengers of the passenger
                handleEntityPassengers(passengerPassengers, passenger, origin, target, exitRotation);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(Stargate.getInstance(), () -> {
                if (passenger instanceof Player player) {
                    //Teleport any creatures leashed by the player in a 15-block range
                    teleportLeashedCreatures(player, origin, target);
                }
                TeleportHelper.teleportAndAddPassenger(entity, passenger, exitRotation);
            }, passenger instanceof Player ? 6 : 0);
        }
    }

    /**
     * Teleports any creatures leashed by the player
     *
     * <p>Will return false if the teleportation should be aborted because the player has leashed creatures that
     * aren't allowed to be teleported with the player.</p>
     *
     * @param player <p>The player which is teleported</p>
     * @param origin <p>The portal the player is teleporting from</p>
     * @param target <p>The portal the player is teleporting to</p>
     */
    public static void teleportLeashedCreatures(Player player, Portal origin, Portal target) {
        //If this feature is disabled, just return
        if (!Stargate.getGateConfig().handleLeashedCreatures()) {
            return;
        }
        BukkitScheduler scheduler = Bukkit.getScheduler();

        //Find any nearby leashed entities to teleport with the player
        List<Creature> nearbyEntities = TeleportHelper.getLeashedCreatures(player);

        //Teleport all creatures leashed by the player to the portal the player is to exit from
        for (Creature creature : nearbyEntities) {
            creature.setLeashHolder(null);
            scheduler.scheduleSyncDelayedTask(Stargate.getInstance(), () -> {
                new EntityTeleporter(target, creature).teleportEntity(origin);
                scheduler.scheduleSyncDelayedTask(Stargate.getInstance(), () -> creature.setLeashHolder(player), 6);
            }, 2);
        }
    }

    /**
     * Checks whether a list of entities or any of their passengers contains any non-players
     *
     * @param entities <p>The list of entities to check</p>
     * @return <p>True if at least one entity is not a player</p>
     */
    public static boolean containsNonPlayer(List<Entity> entities) {
        for (Entity entity : entities) {
            if (!(entity instanceof Player) || containsNonPlayer(entity.getPassengers())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a list of entities of their passengers contains at least one player
     *
     * @param entities <p>The list of entities to check</p>
     * @return <p>True if at least one player is present among the passengers</p>
     */
    public static boolean containsPlayer(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity instanceof Player || containsPlayer(entity.getPassengers())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all players recursively from a list of entities
     *
     * @param entities <p>The entities to check for players</p>
     * @return <p>The found players</p>
     */
    public static List<Player> getPlayers(List<Entity> entities) {
        List<Player> players = new ArrayList<>(5);
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                players.add((Player) entity);
            }
            players.addAll(getPlayers(entity.getPassengers()));
        }
        return players;
    }

}
