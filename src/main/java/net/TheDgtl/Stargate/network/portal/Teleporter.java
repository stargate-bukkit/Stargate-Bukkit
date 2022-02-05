package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.actions.SupplierAction;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.event.StargatePortalEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * The teleporter that takes care of the actual teleportation
 */
public class Teleporter {

    private static final double LOOK_FOR_LEASHED_RADIUS = 15;
    private final Location destination;
    private final RealPortal origin;
    private final int cost;
    private final double rotation;
    private final BlockFace destinationFace;
    private final TranslatableMessage teleportMessage;
    private final boolean checkPermissions;

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
                      int cost, TranslatableMessage teleportMessage, boolean checkPermissions) {
        // Center the destination in the destination block
        this.destination = destination.clone().add(new Vector(0.5, 0, 0.5));
        this.destinationFace = destinationFace;
        this.origin = origin;
        this.rotation = calculateAngleDifference(entranceFace, destinationFace);
        this.cost = cost;
        this.teleportMessage = teleportMessage;
        this.checkPermissions = checkPermissions;
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

        double targetWidth = target.getWidth();
        Vector offset = destinationFace.getDirection();
        offset.multiply(Math.ceil((targetWidth + 1) / 2));
        destination.subtract(offset);

        betterTeleport(target, rotation);
    }

    /**
     * Teleports an entity with all its passengers and its vehicle
     *
     * <p>The {@link Entity#teleport(Entity)} method does not handle passengers / vehicles well. This method fixes
     * that.</p>
     *
     * @param target   <p>The entity to teleport</p>
     * @param rotation <p>The rotation to apply to teleported entities, relative to its existing rotation</p>
     */
    private void betterTeleport(Entity target, double rotation) {
        List<Entity> passengers = target.getPassengers();
        if (target.eject()) {
            Stargate.log(Level.FINER, "Ejected all passengers");
            teleportPassengers(target, passengers);
        }

        if (origin == null) {
            destination.setDirection(destinationFace.getOppositeFace().getDirection());
            teleport(target, destination);
            return;
        }

        PermissionManager permissionManager = new PermissionManager(target);
        if (!hasPermission(target, permissionManager) && checkPermissions) {
            target.sendMessage(permissionManager.getDenyMessage());
            /* For non math guys: teleport entity to the exit of the portal it entered. Also turn the entity around 180
             * degrees */
            teleport(target, origin.getExit(), Math.PI);
            return;
        }

        // Teleport player to the entrance portal if the player is unable to pay
        if (target instanceof Player && !charge((Player) target)) {
            target.sendMessage(Stargate.languageManager.getErrorMessage(TranslatableMessage.LACKING_FUNDS));
            teleport(target, origin.getExit(), 180);
            Player player = (Player) target;
            teleportNearbyLeashedEntities(player, rotation);
            return;
        }

        if (target instanceof PoweredMinecart) {
            return;
        }

        // To smooth the experienced for highly used portals, or entity teleportation
        if (!destination.getChunk().isLoaded()) {
            destination.getChunk().load();
        }

        teleport(target, destination, rotation);
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
                betterTeleport(passenger, rotation);
                target.addPassenger(passenger);
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
    private void teleportNearbyLeashedEntities(Player holder, double rotation) {
        List<Entity> entities = holder.getNearbyEntities(LOOK_FOR_LEASHED_RADIUS, LOOK_FOR_LEASHED_RADIUS,
                LOOK_FOR_LEASHED_RADIUS);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && ((LivingEntity) entity).isLeashed() &&
                    ((LivingEntity) entity).getLeashHolder() == holder) {
                betterTeleport(entity, rotation);
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
        teleport(target, exit);

        Vector targetVelocity = velocity.rotateAroundY(rotation).multiply(Settings.getDouble(
                Setting.GATE_EXIT_SPEED_MULTIPLIER));
        target.setVelocity(targetVelocity);
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
            target.sendMessage(Stargate.languageManager.getMessage(teleportMessage));
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
        StargatePortalEvent event = new StargatePortalEvent(target, origin);
        Bukkit.getPluginManager().callEvent(event);
        return (permissionManager.hasPermission(event) && !event.isCancelled());
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
