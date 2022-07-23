package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.action.DelayedAction;
import net.TheDgtl.Stargate.action.SupplierAction;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.event.StargatePortalEvent;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.manager.StargatePermissionManager;
import net.TheDgtl.Stargate.property.NonLegacyMethod;
import net.TheDgtl.Stargate.util.portal.TeleportationHelper;
import net.TheDgtl.Stargate.vectorlogic.VectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

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
    private static final Set<Entity> boatsTeleporting = new HashSet<>();

    private Location exit;
    private final RealPortal origin;
    private final RealPortal destination;
    private final int cost;
    private double rotation;
    private final BlockFace destinationFace;
    boolean hasPermission;
    private String teleportMessage;
    private final Set<Entity> teleportedEntities = new HashSet<>();
    private final StargateLogger logger;
    private List<LivingEntity> nearbyLeashed;

    /**
     * Instantiate a manager for advanced teleportation between a portal and a location
     *
     * @param destination     <p>The destination location of this teleporter</p>
     * @param origin          <p>The origin portal the teleportation is originating from</p>
     * @param destinationFace <p>The direction the destination's portal is facing</p>
     * @param entranceFace    <p>The direction the entrance portal is facing</p>
     * @param cost            <p>The cost of teleportation for any players</p>
     * @param teleportMessage <p>The teleportation message to display if the teleportation is successful</p>
     */
    public Teleporter(@NotNull RealPortal destination, RealPortal origin, BlockFace destinationFace, BlockFace entranceFace,
                      int cost, String teleportMessage, StargateLogger logger) {
        // Center the destination in the destination block
        this.exit = destination.getExit().clone().add(new Vector(0.5, 0, 0.5));
        this.destinationFace = destinationFace;
        this.origin = origin;
        this.destination = destination;
        this.rotation = VectorUtils.calculateAngleDifference(entranceFace, destinationFace);
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

        List<Player> playersToRefund = new ArrayList<>();

        TeleportedEntityRelationDFS dfs = new TeleportedEntityRelationDFS((anyEntity) -> {
            StargatePermissionManager permissionManager = new StargatePermissionManager(anyEntity);
            if (!hasPermission(anyEntity, permissionManager)) {
                teleportMessage = permissionManager.getDenyMessage();
                return false;
            }

            if (anyEntity instanceof Player) {
                if (Stargate.getEconomyManager().chargePlayer((Player) anyEntity, origin, this.cost)) {
                    playersToRefund.add((Player) anyEntity);
                } else {
                    teleportMessage = Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.LACKING_FUNDS);
                    return false;
                }
            }
            return true;
        }, nearbyLeashed);


        hasPermission = dfs.depthFirstSearch(baseEntity);
        Set<Entity> entitiesToTeleport = dfs.getEntitiesToTeleport();
        //Check if already is teleporting and prevent entity to teleporting again
        for (Entity entityToTeleport : entitiesToTeleport) {
            if (boatsTeleporting.contains(entityToTeleport)) {
                return;
            }
        }
        entitiesToTeleport.forEach((entity) -> {
            if (entity instanceof Boat) {
                boatsTeleporting.add(entity);
            }
        });


        if (!hasPermission) {
            refundPlayers(playersToRefund);
            rotation = Math.PI;
            if (origin != null) {
                exit = origin.getExit().add(new Vector(0.5, 0, 0.5));
            } else {
                exit = baseEntity.getLocation();
            }
        }

        Vector offset = getOffset(baseEntity);
        exit.subtract(offset);
        //Cancel teleportation if outside world-border
        World world = exit.getWorld();
        if (world != null && !world.getWorldBorder().isInside(exit)) {
            String worldBorderInterfereMessage = Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.OUTSIDE_WORLDBORDER);
            entitiesToTeleport.forEach((entity) -> entity.sendMessage(worldBorderInterfereMessage));
            return;
        }

        Stargate.addSynchronousTickAction(new SupplierAction(() -> {
            betterTeleport(baseEntity, exit, rotation);
            return true;
        }));
    }

    /**
     * Refunds the teleportation cost to the given list of users
     *
     * @param playersToRefund <p>The players to refund</p>
     */
    private void refundPlayers(List<Player> playersToRefund) {
        for (Player player : playersToRefund) {
            if (!Stargate.getEconomyManager().refundPlayer(player, this.origin, this.cost)) {
                logger.logMessage(Level.WARNING, "Unable to refund player " + player + " " + this.cost);
            }
        }
    }

    private Vector getOffset(Entity baseEntity) {
        if (hasPermission) {
            return getOffsetFromFacing(baseEntity, destinationFace);
        }
        if (origin != null) {
            return getOffsetFromFacing(baseEntity, origin.getGate().getFacing().getOppositeFace());
        }
        return new Vector();
    }

    private Vector getOffsetFromFacing(Entity baseEntity, BlockFace facing) {
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
     */
    private void betterTeleport(Entity target, Location exit, double rotation) {
        if (teleportedEntities.contains(target)) {
            return;
        }
        teleportedEntities.add(target);
        List<Entity> passengers = target.getPassengers();
        if (target.eject()) {
            Stargate.log(Level.FINER, "Ejected all passengers");
            teleportPassengers(target, exit, passengers);
        }

        if (origin == null) {
            exit.setDirection(destinationFace.getOppositeFace().getDirection());
            teleport(target, exit);
            return;
        }

        // To smooth the experienced for highly used portals, or entity teleportation
        if (!exit.getChunk().isLoaded()) {
            exit.getChunk().load();
        }

        logger.logMessage(Level.FINEST, "Trying to teleport surrounding leashed entities");
        teleportNearbyLeashedEntities(target, exit, rotation);
        logger.logMessage(Level.FINEST, "Teleporting entity " + target + " to exit location " + exit);
        teleport(target, exit, rotation);
    }

    /**
     * Teleports all passengers of an entity
     *
     * @param target     <p>The target to teleport</p>
     * @param passengers <p>The ejected passengers of the target entity</p>
     */
    private void teleportPassengers(Entity target, Location exit, List<Entity> passengers) {
        for (Entity passenger : passengers) {
            Supplier<Boolean> action = () -> {
                betterTeleport(passenger, exit, rotation);
                target.addPassenger(passenger);
                return true;
            };

            if (passenger instanceof Player) {
                // Delay action by one tick to avoid client issues
                Stargate.addSynchronousTickAction(new DelayedAction(1, action));
                continue;
            }
            Stargate.addSynchronousTickAction(new SupplierAction(action));
        }
    }

    /**
     * Looks for any entities in a 15-block radius and teleports them to the holding player
     *
     * @param holder   <p>The player that may hold entities in a leash</p>
     * @param rotation <p>The rotation to apply to teleported leashed entities, relative to its existing rotation</p>
     */
    private void teleportNearbyLeashedEntities(Entity holder, Location exit, double rotation) {
        if (!ConfigurationHelper.getBoolean(ConfigurationOption.HANDLE_LEASHES)) {
            return;
        }
        for (LivingEntity entity : nearbyLeashed) {
            final Location modifiedExit;
            if (exit.getWorld() != entity.getWorld()) {
                modifiedExit = TeleportationHelper.findViableSpawnLocation(entity, destination);
            } else {
                modifiedExit = exit;
            }
            if (entity.isLeashed() && entity.getLeashHolder() == holder) {
                Supplier<Boolean> action = () -> {
                    entity.setLeashHolder(null);
                    betterTeleport(entity, modifiedExit, rotation);
                    entity.setLeashHolder(holder);
                    return true;
                };
                Stargate.addSynchronousTickAction(new SupplierAction(action));
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
            msg = String.format(msg, player.getName(), location);
            if (this.origin != null) {
                msg = msg + "from portal %s in network %s";
                msg = String.format(msg, origin.getName(), origin.getNetwork().getName());
            }

            logger.logMessage(Level.FINE, msg);
        }

        if (target instanceof PoweredMinecart) {
            teleportPoweredMinecart((PoweredMinecart) target, targetVelocity, location);
        } else {
            teleport(target, exit);
            target.setVelocity(targetVelocity);
        }
    }

    /**
     * Teleports a powered minecart using necessary workarounds
     *
     * @param poweredMinecart <p>The powered Minecart to teleport</p>
     * @param targetVelocity  <p>The velocity to add to the powered Minecart upon exiting</p>
     * @param location        <p>The location to teleport the powered minecart to</p>
     */
    private void teleportPoweredMinecart(PoweredMinecart poweredMinecart, Vector targetVelocity, Location location) {
        //Remove fuel and velocity to force the powered minecart to stop
        int fuel = poweredMinecart.getFuel();
        poweredMinecart.setFuel(0);
        poweredMinecart.setVelocity(new Vector());

        //Teleport the powered minecart
        logger.logMessage(Level.FINEST, "Teleporting Powered Minecart to " + exit);
        teleport(poweredMinecart, exit);
        poweredMinecart.setFuel(fuel);

        Stargate.addSynchronousTickAction(new DelayedAction(1, () -> {
            //Re-apply fuel and velocity
            logger.logMessage(Level.FINEST, "Setting new velocity " + targetVelocity);
            poweredMinecart.setVelocity(targetVelocity);

            //Use the paper-only methods for setting the powered minecart's actual push
            if (NonLegacyMethod.PUSH_X.isImplemented() && NonLegacyMethod.PUSH_Z.isImplemented()) {
                Vector direction = destinationFace.getDirection();
                double pushX = -direction.getBlockX();
                double pushZ = -direction.getBlockZ();
                logger.logMessage(Level.FINEST, "Setting push: X = " + pushX + " Z = " + pushZ);
                NonLegacyMethod.PUSH_X.invoke(poweredMinecart, pushX);
                NonLegacyMethod.PUSH_Z.invoke(poweredMinecart, pushZ);
            } else {
                logger.logMessage(Level.FINE, String.format("Unable to restore Furnace Minecart Momentum at %S --" +
                        " use Paper 1.18.2+ for this feature.", location));
            }
            return true;
        }));
    }

    /**
     * Performs the final, actual teleportation
     *
     * @param target    <p>The target entity to teleport</p>
     * @param exitPoint <p>The exit location to teleport the entity to</p>
     */
    private void teleport(Entity target, Location exitPoint) {
        target.teleport(exitPoint);
        boatsTeleporting.remove(target);
        if (origin != null && !origin.hasFlag(PortalFlag.SILENT)) {
            logger.logMessage(Level.FINE, "Sending player teleport message" + teleportMessage);
            target.sendMessage(teleportMessage);
        }
    }


    /**
     * Checks whether the given entity has the required permissions for performing the teleportation
     *
     * @param target            <p>The entity to check permissions of</p>
     * @param permissionManager <p>The permission manager to use for checking for relevant permissions</p>
     * @return <p>True if the entity has the required permissions for performing the teleportation</p>
     */
    private boolean hasPermission(Entity target, StargatePermissionManager permissionManager) {
        if (origin == null) {
            // TODO origin == null means inter-server teleportation. Make a permission check for this or something?
            return true;
        }
        boolean hasPermission = permissionManager.hasTeleportPermissions(origin);
        StargatePortalEvent event = new StargatePortalEvent(target, origin, destination, exit);
        Bukkit.getPluginManager().callEvent(event);
        return hasPermission;
    }


}
