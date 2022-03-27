package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.action.DelayedAction;
import net.TheDgtl.Stargate.action.SupplierAction;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.event.StargatePortalEvent;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.manager.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * @author Thorinwasher
 * Work in progress, not used currently
 */
public class DraftTeleporter {

    private static final double LOOK_FOR_LEASHED_RADIUS = 15;
    private final Location destination;
    private final RealPortal origin;
    private final int cost;
    private final BlockFace destinationFace;
    private String teleportMessage;
    private final StargateLogger logger;
    private final BlockFace entranceFace;

    /**
     * Instantiate a manager for advanced teleportation between a portal and a location
     *
     * @param destination        <p>The destination location of this teleporter</p>
     * @param origin             <p>The origin portal the teleportation is originating from</p>
     * @param destinationFace    <p>The direction the destination's portal is facing</p>
     * @param entranceFace       <p>The direction the entrance portal is facing</p>
     * @param cost               <p>The cost of teleportation for any players</p>
     * @param teleportMessage    <p>The teleportation message to display if the teleportation is successful</p>
     * @param logger             <p>The logger used for logging messages</p>
     */
    public DraftTeleporter(Location destination, RealPortal origin, BlockFace destinationFace, BlockFace entranceFace,
                           int cost, String teleportMessage, StargateLogger logger) {
        // Center the destination in the destination block
        this.destination = destination.clone().add(new Vector(0.5, 0, 0.5));
        this.destinationFace = destinationFace;
        this.entranceFace = entranceFace;
        this.origin = origin;
        this.cost = cost;
        this.teleportMessage = teleportMessage;
        this.logger = logger;
    }

    /**
     * Teleports the given target entity
     *
     * @param target <p>The entity that is the target of this teleportation</p>
     */
    public void teleport(Entity target) {
        Entity baseEntity = target;
        while (baseEntity.getVehicle() != null) {
            baseEntity = baseEntity.getVehicle();
        }

        TeleportedEntityRelationDFS dfs = new TeleportedEntityRelationDFS((anyEntity) -> {
            //TODO: The access event should be called to allow add-ons cancelling or overriding the teleportation
            PermissionManager permissionManager = new PermissionManager(anyEntity);
            if (!hasPermission(anyEntity, permissionManager)) {
                teleportMessage = permissionManager.getDenyMessage();
                return false;
            }

            if (anyEntity instanceof Player && !Stargate.economyManager.has((Player) anyEntity, this.cost)) {
                teleportMessage = Stargate.languageManager.getErrorMessage(TranslatableMessage.LACKING_FUNDS);
                return false;
            }
            return true;
        }, getSurroundingLeashed(baseEntity));

        boolean shouldProceed = dfs.depthFirstSearch(null, target, true);

        Stargate.syncTickPopulator.addAction(new SupplierAction(() -> {
            unStackEntities(dfs.getEntitiesToTeleport());
            return true;
        }));

        Location modifiedDestination = calculateDestination(shouldProceed, baseEntity);
        modifiedDestination.getChunk().load();
        Stargate.syncTickPopulator.addAction(new SupplierAction(() -> {
            doTeleportation(dfs.getEntitiesToTeleport(), modifiedDestination, shouldProceed);
            stackEntities(dfs.getEntitiesToTeleport(), dfs.getLeashHolders(), dfs.getPassengerVehicles());
            return true;
        }));
    }

    private Vector getOffset(BlockFace destinationFacing, Entity target) {
        double targetWidth = target.getWidth();
        Vector offset = destinationFacing.getDirection();
        offset.multiply(Math.ceil((targetWidth + 1) / 2));
        return offset;
    }

    private void stackEntities(List<Entity> entitiesToTeleport, Map<LivingEntity, Entity> leashHolders,
                               Map<Entity, Entity> passengerVehicles) {
        for (Entity entity : entitiesToTeleport) {
            Supplier<Boolean> action = () -> {
                //Adds the passenger to its previous vehicle if necessary
                if (passengerVehicles.containsKey(entity)) {
                    passengerVehicles.get(entity).addPassenger(entity);
                }
                
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (leashHolders.containsKey(livingEntity)) {
                        livingEntity.setLeashHolder(leashHolders.get(livingEntity));
                    }
                }
                return true;
            };
            if (entity instanceof Player) {
                Stargate.syncTickPopulator.addAction(new DelayedAction(1, action));
            } else {
                Stargate.syncTickPopulator.addAction(new SupplierAction(action));
            }
        }
    }

    private void unStackEntities(List<Entity> entitiesToTeleport) {
        for (Entity entity : entitiesToTeleport) {
            entity.leaveVehicle();
            if (entity instanceof LivingEntity && ((LivingEntity) entity).isLeashed()) {
                ((LivingEntity) entity).setLeashHolder(null);
            }
        }
    }

    private Location calculateDestination(boolean shouldProceed, Entity baseEntity) {
        if (!shouldProceed && origin != null) {
            Vector offset = getOffset(destinationFace, baseEntity);
            return origin.getExit().clone().subtract(offset);
        }
        Vector offset = getOffset(destinationFace, baseEntity);
        return this.destination.clone().subtract(offset);
    }

    private void doTeleportation(List<Entity> entitiesToTeleport, Location destination, boolean shouldProceed) {
        double rotation;
        logger.logMessage(Level.FINE, String.format("Teleporting entities to destination %s", destination.toString()));
        if (!shouldProceed && origin != null) {
            rotation = Math.PI;

        } else {
            rotation = calculateAngleDifference(entranceFace, destinationFace);
        }

        for (Entity entity : entitiesToTeleport) {
            teleport(entity, destination, rotation);
            if (entity instanceof Player && cost != 0 && charge((Player) entity)) {
                entity.sendMessage(teleportMessage);
            }
        }
    }


    private List<LivingEntity> getSurroundingLeashed(Entity origin) {
        List<Entity> surroundingEntities = origin.getNearbyEntities(LOOK_FOR_LEASHED_RADIUS, LOOK_FOR_LEASHED_RADIUS,
                LOOK_FOR_LEASHED_RADIUS);
        List<LivingEntity> surroundingLeashedEntities = new ArrayList<>();
        for (Entity entity : surroundingEntities) {
            if (entity instanceof LivingEntity && ((LivingEntity) entity).isLeashed()) {
                surroundingLeashedEntities.add((LivingEntity) entity);
            }
        }
        return surroundingLeashedEntities;
    }

    /**
     * Teleports the given target to the given location
     *
     * @param target   <p>The target entity to teleport</p>
     * @param location <p>The location to teleport the entity to</p>
     * @param rotation <p>The rotation to apply to teleported leashed entities, relative to its existing rotation</p>
     */
    private void teleport(Entity target, Location location, double rotation) {
        Vector direction = target.getLocation().getDirection();
        Location exit = location.setDirection(direction.rotateAroundY(rotation));

        Vector velocity = target.getVelocity();
        Vector targetVelocity = velocity.rotateAroundY(rotation).multiply(ConfigurationHelper.getDouble(
                ConfigurationOption.GATE_EXIT_SPEED_MULTIPLIER));
        if (target instanceof PoweredMinecart) {
            //A workaround for powered minecarts
            PoweredMinecart poweredMinecart = (PoweredMinecart) target;
            int fuel = poweredMinecart.getFuel();
            poweredMinecart.setFuel(0);
            Bukkit.getScheduler().runTaskLater(Stargate.getInstance(), () -> {
                poweredMinecart.setVelocity(new Vector());
                teleport(poweredMinecart, exit);
                poweredMinecart.setFuel(fuel);
                poweredMinecart.setVelocity(targetVelocity);
            }, 1);
        } else {
            teleport(target, exit);
            target.setVelocity(targetVelocity);
        }
    }

    /**
     * Performs the final, actual teleportation
     *
     * @param target    <p>The target entity to teleport</p>
     * @param exitPoint <p>The exit location to teleport the entity to</p>
     */
    private void teleport(Entity target, Location exitPoint) {
        target.teleport(exitPoint);
        if (origin != null && !origin.hasFlag(PortalFlag.SILENT)) {
            target.sendMessage(teleportMessage);
        }
    }

    /**
     * Charges the given player as necessary
     *
     * @param target <p>The target player to charge</p>
     * @return <p>True if all necessary transactions were successfully completed</p>
     */
    private boolean charge(Player target) {
        if (origin.hasFlag(PortalFlag.PERSONAL_NETWORK)) {
            return Stargate.economyManager.chargePlayer(target, origin, cost);
        } else {
            return Stargate.economyManager.chargeAndTax(target, cost);
        }
    }

    /**
     * Calculates the relative angle difference between two block faces
     *
     * @param originFace      <p>The block face the origin portal is pointing towards</p>
     * @param destinationFace <p>The block face the destination portal is pointing towards</p>
     * @return <p>The angle difference between the two block faces</p>
     */
    private double calculateAngleDifference(BlockFace originFace, BlockFace destinationFace) {
        if (originFace != null) {
            Vector originGateDirection = originFace.getDirection();
            return directionalAngleOperator(originGateDirection, destinationFace.getDirection());
        } else {
            return -directionalAngleOperator(BlockFace.EAST.getDirection(), destinationFace.getDirection());
        }
    }

    /**
     * Gets the angle between two vectors
     *
     * <p>The {@link Vector#angle(Vector)} function is not directional, meaning if you exchange position with vectors,
     * there will be no difference in angle. The behaviour that is needed in some portal methods is for the angle to
     * change sign if the vectors change places.
     * <p>
     * NOTE: ONLY ACCOUNTS FOR Y AXIS ROTATIONS</p>
     *
     * @param vector1 <p>The first vector</p>
     * @param vector2 <p>The second vector</p>
     * @return <p>The angle between the two vectors</p>
     */
    private double directionalAngleOperator(Vector vector1, Vector vector2) {
        return Math.atan2(vector1.clone().crossProduct(vector2).getY(), vector1.dot(vector2));
    }

    /**
     * Checks whether the given entity has the required permissions for performing the teleportation
     *
     * @param target            <p>The entity to check permissions of</p>
     * @param permissionManager <p>The permission manager to use for checking for relevant permissions</p>
     * @return <p>True if the entity has the required permissions for performing the teleportation</p>
     */
    private boolean hasPermission(Entity target, PermissionManager permissionManager) {
        StargatePortalEvent event = new StargatePortalEvent(target, origin);
        Bukkit.getPluginManager().callEvent(event);
        return (permissionManager.hasPermission(event)) && !event.isCancelled();
    }

}
