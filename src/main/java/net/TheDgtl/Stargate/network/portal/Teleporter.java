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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * The teleporter that takes care of the actual teleportation
 */
public class Teleporter {

    private static final double LOOK_FOR_LEASHED_RADIUS = 15;
    private Location destination;
    private final RealPortal origin;
    private final int cost;
    private double rotation;
    private final BlockFace destinationFace;
    boolean hasPermission;
    private String teleportMessage;
    private Set<Entity> teleportedEntities = new HashSet<>();
    private final StargateLogger logger;
    private List<LivingEntity> nearbyLeashed;

    /**
     * Instantiate a manager for advanced teleportation between a portal and a location
     *
     * @param destination      <p>The destination location of this teleporter</p>
     * @param origin           <p>The origin portal the teleportation is originating from</p>
     * @param destinationFace  <p>The direction the destination's portal is facing</p>
     * @param entranceFace     <p>The direction the entrance portal is facing</p>
     * @param cost             <p>The cost of teleportation for any players</p>
     * @param teleportMessage  <p>The teleportation message to display if the teleportation is successful</p>
     * @param checkPermissions <p>Whether to check, or totally ignore permissions</p>
     */
    public Teleporter(Location destination, RealPortal origin, BlockFace destinationFace, BlockFace entranceFace,
                      int cost, String teleportMessage, StargateLogger logger) {
        // Center the destination in the destination block
        this.destination = destination.clone().add(new Vector(0.5, 0, 0.5));
        this.destinationFace = destinationFace;
        this.origin = origin;
        this.rotation = calculateAngleDifference(entranceFace, destinationFace);
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
        // Teleport the whole vessel, regardless of what entity triggered the initial event
        while (target.getVehicle() != null) {
            target = target.getVehicle();
        }
        final Entity baseEntity = target;


        nearbyLeashed = getNearbyLeashedEntities(baseEntity);
        
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
        }, nearbyLeashed);
        
        hasPermission = dfs.depthFirstSearch(baseEntity);
        if(!hasPermission) {
            rotation = Math.PI;
            if(origin != null) {
                destination = origin.getExit().add(new Vector(0.5, 0, 0.5));
            } else {
                destination = baseEntity.getLocation();
            }
        }

        Vector offset = getOffset(baseEntity);
        destination.subtract(offset);
        
        Stargate.syncTickPopulator.addAction(new SupplierAction(() -> {
            betterTeleport(baseEntity, rotation);
            return true;
        }));
    }
    
    private Vector getOffset(Entity baseEntity) {
        if(hasPermission) {
            return getOffsettFromFacing(baseEntity,destinationFace);
        }
        if(origin != null) {
            return getOffsettFromFacing(baseEntity,origin.getGate().getFacing().getOppositeFace());
        }
        return new Vector();
    }
    
    private Vector getOffsettFromFacing(Entity baseEntity, BlockFace facing) {
        Vector offset = facing.getDirection();
        double targetWidth = baseEntity.getWidth();
        offset.multiply(Math.ceil((targetWidth + 1) / 2));
        return offset;
    }

    private List<LivingEntity> getNearbyLeashedEntities(Entity origin) {
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
     * Teleports an entity with all its passengers and its vehicle
     *
     * <p>The {@link Entity#teleport(Entity)} method does not handle passengers / vehicles well. This method fixes
     * that.</p>
     *
     * @param target   <p>The entity to teleport</p>
     * @param rotation <p>The rotation to apply to teleported entities, relative to its existing rotation</p>
     * @return <p>If the teleportation was successfull</p>
     */
    private boolean betterTeleport(Entity target, double rotation) {
        if (teleportedEntities.contains(target)) {
            return true;
        }
        teleportedEntities.add(target);
        List<Entity> passengers = target.getPassengers();
        if (target.eject()) {
            Stargate.log(Level.FINER, "Ejected all passengers");
            teleportPassengers(target, passengers);
        }

        if (origin == null) {
            destination.setDirection(destinationFace.getOppositeFace().getDirection());
            teleport(target, destination);
            return true;
        }

        // To smooth the experienced for highly used portals, or entity teleportation
        if (!destination.getChunk().isLoaded()) {
            destination.getChunk().load();
        }

        logger.logMessage(Level.FINEST, "Trying to teleport surrounding leashed entities");
        teleportNearbyLeashedEntities(target, rotation);
        teleport(target, destination, rotation);
        return true;
    }

    /**
     * Teleports all passengers of an entity
     *
     * @param target     <p>The target to teleport</p>
     * @param passengers <p>The ejected passengers of the target entity</p>
     */
    private void teleportPassengers(Entity target, List<Entity> passengers) {
        for (Entity passenger : passengers) {
            Supplier<Boolean> action = () -> {
                if (betterTeleport(passenger, rotation)) {
                    target.addPassenger(passenger);
                }
                return true;
            };

            if (passenger instanceof Player) {
                // Delay action by one tick to avoid client issues
                Stargate.syncTickPopulator.addAction(new DelayedAction(1, action));
                continue;
            }
            Stargate.syncTickPopulator.addAction(new SupplierAction(action));
        }
    }

    /**
     * Looks for any entities in a 15-block radius and teleports them to the holding player
     *
     * @param holder   <p>The player that may hold entities in a leash</p>
     * @param rotation <p>The rotation to apply to teleported leashed entities, relative to its existing rotation</p>
     */
    private void teleportNearbyLeashedEntities(Entity holder, double rotation) {
        if (!ConfigurationHelper.getBoolean(ConfigurationOption.HANDLE_LEASHES)) {
            return;
        }
        for (LivingEntity entity : nearbyLeashed) {
            if (entity.isLeashed() &&  entity.getLeashHolder() == holder) {
                Supplier<Boolean> action = () -> {
                    ((LivingEntity) entity).setLeashHolder(null);
                    if (betterTeleport(entity, rotation)) {
                        ((LivingEntity) entity).setLeashHolder(holder);
                    }

                    return true;
                };
                Stargate.syncTickPopulator.addAction(new SupplierAction(action));
            }
        }
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

        if (target instanceof Player) {
            Player player = (Player) target;
            String msg = "Teleporting player %s to %s";
            msg = String.format(msg, player.getName(), location.toString());
            if (this.origin != null) {
                msg = msg + "from portal %s in network %s";
                msg = String.format(msg, origin.getName(), origin.getNetwork().getName());
            }

            logger.logMessage(Level.FINE, msg);
        }

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
                try {
                    Method setPushX = PoweredMinecart.class.getMethod("setPushX", double.class);
                    Method setPushZ = PoweredMinecart.class.getMethod("setPushZ", double.class);
                    setPushX.invoke(poweredMinecart, -location.getDirection().getBlockX());
                    setPushZ.invoke(poweredMinecart, -location.getDirection().getBlockZ());

                } catch (NoSuchMethodException ignored) {
                    logger.logMessage(Level.FINE, String.format("Unable to restore Furnace Minecart Momentum at %S -- use Paper 1.18.2+ for this feature.", location.toString()));
                } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
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
            logger.logMessage(Level.FINE, "Sending player teleport message" + teleportMessage);
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
     * Checks whether the given entity has the required permissions for performing the teleportation
     *
     * @param target            <p>The entity to check permissions of</p>
     * @param permissionManager <p>The permission manager to use for checking for relevant permissions</p>
     * @return <p>True if the entity has the required permissions for performing the teleportation</p>
     */
    private boolean hasPermission(Entity target, PermissionManager permissionManager) {
        if(origin == null) {
           // TODO origin == null means interserver teleportation. Make a permission check for this or something?
           return true;
        } 
        StargatePortalEvent event = new StargatePortalEvent(target, origin);
        Bukkit.getPluginManager().callEvent(event);
        return (permissionManager.hasTeleportPermissions(origin) && !event.isCancelled());
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

}
