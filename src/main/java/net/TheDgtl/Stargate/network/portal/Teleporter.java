package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Settings;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.actions.SupplierAction;
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

public class Teleporter {

    private static final double LOOK_FOR_LEASHED_RADIUS = 15;
    private final Location destination;
    private final RealPortal origin;
    private final int cost;
    private final double rotation;
    private final BlockFace destinationFace;
    private TranslatableMessage teleportMessage;
    private boolean checkPerms;

    /**
     * Instantiate a manager for advanced teleportation between a portal and a
     * location
     *
     * @param destination     location
     * @param origin          Portal origin
     * @param destinationFace Facing of exit point
     * @param entranceFace    The facing that the player entered
     * @param cost            of the teleportation for any players
     */
    public Teleporter(Location destination, RealPortal origin, BlockFace destinationFace, BlockFace entranceFace, int cost,
                      TranslatableMessage teleportMessage, boolean checkPerms) {
        // compensate so that the teleportation is centred in block
        this.destination = destination.clone().add(new Vector(0.5, 0, 0.5));
        this.destinationFace = destinationFace;
        this.origin = origin;
        this.rotation = calculateAngleChange(entranceFace, destinationFace);
        this.cost = cost;
        this.teleportMessage = teleportMessage;
        this.checkPerms = checkPerms;
    }

    public void teleport(Entity target) {
        /*
         * To teleport the whole vessel, regardless of what entity triggered the initial event
         */
        while (target.getVehicle() != null) {
            target = target.getVehicle();
        }


        double width = target.getWidth();
        Vector offset = destinationFace.getDirection();
        offset.multiply(Math.ceil((width + 1) / 2));
        destination.subtract(offset);

        betterTeleport(target, rotation);
    }

    /**
     * The {@link Entity#teleport(Entity)} method does not handle passengers / vehicles well. This method fixes that
     *
     * @param target
     * @param rotation
     */
    private void betterTeleport(Entity target, double rotation) {
        List<Entity> passengers = target.getPassengers();
        if (target.eject()) {
            Stargate.log(Level.FINEST, "Ejected all passengers");
            for (Entity passenger : passengers) {

                Supplier<Boolean> action = () -> {
                    betterTeleport(passenger, rotation);
                    target.addPassenger(passenger);
                    return true;
                };

                if (passenger instanceof Player) {
                    /*
                     * Delay action by one tick to avoid client issues
                     */
                    Stargate.syncTickPopulator.addAction(new DelayedAction(1, action));
                    continue;
                }
                Stargate.syncTickPopulator.addAction(new SupplierAction(action));
            }
        }

        if (origin == null) {
            destination.setDirection(destinationFace.getOppositeFace().getDirection());
            teleport(target, destination);
            return;
        }

        PermissionManager permissionManager = new PermissionManager(target);
        if (!hasPerm(target, permissionManager) && checkPerms) {
            target.sendMessage(permissionManager.getDenyMessage());
            //For non math guys: teleport entity to the exit of the portal it entered. Also turn the entity around 180 degrees
            teleport(target, origin.getExit(), Math.PI);
            return;
        }


        if (target instanceof Player && !charge((Player) target)) {
            target.sendMessage(Stargate.languageManager.getErrorMessage(TranslatableMessage.LACKING_FUNDS));
            teleport(target, origin.getExit(), 180);
            Player player = (Player) target;
            nearbyLeashedEntityTeleport(player, rotation);
            return;
        }

        if (target instanceof PoweredMinecart) {
            // TODO: NOT Currently implemented, does not seem to be an accessible way to rotate powered minecarts using spigot api
            return;
        }

        // To smooth the experienced for highly used portals, or entity teleportation
        if (!destination.getChunk().isLoaded()) {
            destination.getChunk().load();
        }

        teleport(target, destination, rotation);
    }

    private void nearbyLeashedEntityTeleport(Player holder, double rotation) {
        List<Entity> entities = holder.getNearbyEntities(LOOK_FOR_LEASHED_RADIUS, LOOK_FOR_LEASHED_RADIUS, LOOK_FOR_LEASHED_RADIUS);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && ((LivingEntity) entity).isLeashed() && ((LivingEntity) entity).getLeashHolder() == holder) {

                betterTeleport(entity, rotation);
            }
        }
    }

    private void teleport(Entity target, Location loc, double rotation) {
        Vector direction = target.getLocation().getDirection();
        Location exit = loc.setDirection(direction.rotateAroundY(rotation));
        Vector velocity = target.getVelocity();
        teleport(target, exit);

        Vector targetVelocity = velocity.rotateAroundY(rotation).multiply(Settings.getDouble(Setting.GATE_EXIT_SPEED_MULTIPLIER));
        target.setVelocity(targetVelocity);
    }

    private void teleport(Entity target, Location exitpoint) {
        target.teleport(exitpoint);
        if (origin != null && !origin.hasFlag(PortalFlag.SILENT))
            target.sendMessage(Stargate.languageManager.getMessage(teleportMessage));

    }

    private boolean charge(Player target) {
        if (origin.hasFlag(PortalFlag.PERSONAL_NETWORK))
            return Stargate.economyManager.chargePlayer(target, origin, cost);
        return Stargate.economyManager.chargeAndTax(target, cost);
    }

    private double calculateAngleChange(BlockFace originFacing, BlockFace destinationFacing) {
        if (originFacing != null) {
            Vector originGateDirection = originFacing.getDirection();
            return directionalAngleOperator(originGateDirection, destinationFacing.getDirection());
        }
        return -directionalAngleOperator(BlockFace.EAST.getDirection(), destinationFacing.getDirection());
    }

    private boolean hasPerm(Entity target, PermissionManager permissionManager) {
        StargatePortalEvent event = new StargatePortalEvent(target, origin);
        Bukkit.getPluginManager().callEvent(event);
        return (permissionManager.hasPermission(event) && !event.isCancelled());
    }

    /**
     * The {@link Vector#angle(Vector)} function is not directional, meaning if you exchange position with vectors,
     * there will be no difference in angle. The behaviour that is needed in some portal methods is for the angle to
     * change sign if the vectors change places.
     * <p>
     * NOTE: ONLY ACCOUNTS FOR Y AXIS ROTATIONS
     *
     * @param vector1 normalized
     * @param vector2 normalized
     * @return angle between the two vectors
     */
    private double directionalAngleOperator(Vector vector1, Vector vector2) {
        return Math.atan2(vector1.clone().crossProduct(vector2).getY(), vector1.dot(vector2));
    }
}
