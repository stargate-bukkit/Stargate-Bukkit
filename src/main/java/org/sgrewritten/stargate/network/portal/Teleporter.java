package org.sgrewritten.stargate.network.portal;

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
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.DelayedAction;
import org.sgrewritten.stargate.action.SimpleAction;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.StargatePortalEvent;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.economy.EconomyAPI;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.property.NonLegacyMethod;
import org.sgrewritten.stargate.thread.SynchronousPopulator;
import org.sgrewritten.stargate.util.portal.TeleportationHelper;
import org.sgrewritten.stargate.vectorlogic.VectorUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
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
    private List<LivingEntity> nearbyLeashed;
    private LanguageManager languageManager;
    private StargateEconomyAPI economyManager;
    private Consumer<SimpleAction> registerAction;

    /**
     * Instantiate a manager for advanced teleportation between a portal and a location
     *
     * @param destination     <p>The destination location of this teleporter</p>
     * @param origin          <p>The origin portal the teleportation is originating from</p>
     * @param destinationFace <p>The direction the destination's portal is facing</p>
     * @param entranceFace    <p>The direction the entrance portal is facing</p>
     * @param cost            <p>The cost of teleportation for any players</p>
     * @param teleportMessage <p>The teleportation message to display if the teleportation is successful</p>
     * @param logger
     */
    public Teleporter(@NotNull RealPortal destination, RealPortal origin, BlockFace destinationFace,
            BlockFace entranceFace, int cost, String teleportMessage, LanguageManager languageManager,StargateEconomyAPI economyManager) {
        this(destination, origin, destinationFace, entranceFace, cost, teleportMessage, languageManager,
                economyManager, ((action) -> Stargate.addSynchronousTickAction(action)));
    }

    public Teleporter(@NotNull RealPortal destination, RealPortal origin, BlockFace destinationFace,
            BlockFace entranceFace, int cost, String teleportMessage, LanguageManager languageManager,
            StargateEconomyAPI economyManager, Consumer<SimpleAction> registerAction) {
        // Center the destination in the destination block
        this.exit = destination.getExit().clone().add(new Vector(0.5, 0, 0.5));
        this.destinationFace = destinationFace;
        this.origin = origin;
        this.destination = destination;
        this.rotation = VectorUtils.calculateAngleDifference(entranceFace, destinationFace);
        this.cost = cost;
        this.teleportMessage = teleportMessage;
        this.languageManager = Objects.requireNonNull(languageManager);
        this.economyManager = Objects.requireNonNull(economyManager);
        this.registerAction = Objects.requireNonNull(registerAction);
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
            StargatePermissionManager permissionManager = new StargatePermissionManager(anyEntity,languageManager);
            if (!hasPermission(anyEntity, permissionManager)) {
                teleportMessage = permissionManager.getDenyMessage();
                return false;
            }

            if (anyEntity instanceof Player) {
                if (economyManager.chargePlayer((Player) anyEntity, origin, this.cost)) {
                    playersToRefund.add((Player) anyEntity);
                } else {
                    teleportMessage = languageManager.getErrorMessage(TranslatableMessage.LACKING_FUNDS);
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
            String worldBorderInterfereMessage = languageManager.getErrorMessage(TranslatableMessage.OUTSIDE_WORLDBORDER);
            entitiesToTeleport.forEach((entity) -> entity.sendMessage(worldBorderInterfereMessage));
            return;
        }

        registerAction.accept(new SupplierAction(() -> {
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
            if (!economyManager.refundPlayer(player, this.origin, this.cost)) {
                Stargate.log(Level.WARNING, "Unable to refund player " + player + " " + this.cost);
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

        Stargate.log(Level.FINEST, "Trying to teleport surrounding leashed entities");
        teleportNearbyLeashedEntities(target, exit, rotation);
        Stargate.log(Level.FINEST, "Teleporting entity " + target + " to exit location " + exit);
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
            registerAction.accept(new DelayedAction(1,action));
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
                registerAction.accept(new SupplierAction(action));
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

            Stargate.log(Level.FINE, msg);
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
        if(!NonLegacyMethod.GET_FUEL.isImplemented()) {
            Stargate.log(Level.FINE, String.format("Unable to handle Furnace Minecart at %S --" +
                    " use Paper 1.17+ for this feature.", location));
            return;
        }
        //Remove fuel and velocity to force the powered minecart to stop
        int fuel = poweredMinecart.getFuel();
        poweredMinecart.setFuel(0);
        poweredMinecart.setVelocity(new Vector());

        //Teleport the powered minecart
        Stargate.log(Level.FINEST, "Teleporting Powered Minecart to " + exit);
        teleport(poweredMinecart, exit);
        poweredMinecart.setFuel(fuel);

        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Stargate.getInstance(), () -> {
            poweredMinecart.setVelocity(targetVelocity);
            double pushX = 1; //any new pushing direction
            double pushZ = 0;
            poweredMinecart.setPushX(pushX);
            poweredMinecart.setPushZ(pushZ);
        },1);
        registerAction.accept(new DelayedAction(1, () -> {
            //Re-apply fuel and velocity
            Stargate.log(Level.FINEST, "Setting new velocity " + targetVelocity);
            poweredMinecart.setVelocity(targetVelocity);

            //Use the paper-only methods for setting the powered minecart's actual push
            if (NonLegacyMethod.PUSH_X.isImplemented() && NonLegacyMethod.PUSH_Z.isImplemented()) {
                Vector direction = destinationFace.getDirection();
                double pushX = -direction.getBlockX();
                double pushZ = -direction.getBlockZ();
                Stargate.log(Level.FINEST, "Setting push: X = " + pushX + " Z = " + pushZ);
                NonLegacyMethod.PUSH_X.invoke(poweredMinecart, pushX);
                NonLegacyMethod.PUSH_Z.invoke(poweredMinecart, pushZ);
            } else {
                Stargate.log(Level.FINE, String.format("Unable to restore Furnace Minecart Momentum at %S --" +
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
            Stargate.log(Level.FINE, "Sending player teleport message" + teleportMessage);
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
