package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.actions.PopulatorAction;
import net.TheDgtl.Stargate.event.StargatePortalEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.logging.Level;

public class Teleporter {
    private Location destination;
    private Portal origin;
    private int cost;
    private double rotation;
    private BlockFace destinationFace;

    /**
     * Instantiate a manager for advanced teleportation between a portal and a location
     *
     * @param destination     location
     * @param origin          Portal origin
     * @param destinationFace Facing of exit point
     * @param entranceFace    The facing that the player entered
     * @param cost            of the teleportation for any players
     */
    public Teleporter(Location destination, Portal origin, BlockFace destinationFace, BlockFace entranceFace, int cost) {
        //compensate so that the teleportation is centred in block
        this.destination = destination.clone().add(new Vector(0.5, 0, 0.5));
        this.destinationFace = destinationFace;
        this.origin = origin;
        this.rotation = calculateAngleChange(entranceFace, destinationFace);
        this.cost = cost;
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
     * @param loc
     */
    private void betterTeleport(Entity target, double rotation) {
        List<Entity> passengers = target.getPassengers();
        if (target.eject()) {
            Stargate.log(Level.FINEST, "Ejected all passengers");
            for (Entity passenger : passengers) {

                PopulatorAction action = new PopulatorAction() {

                    @Override
                    public void run(boolean forceEnd) {
                        betterTeleport(passenger, rotation);
                        target.addPassenger(passenger);
                    }

                    @Override
                    public boolean isFinished() {
                        return true;
                    }
                };
                if (passenger instanceof Player) {
                    /*
                     * Delay action by one tick to avoid client issues
                     */
                    Stargate.syncTickPopulator.addAction(new DelayedAction(1, action));
                    continue;
                }
                Stargate.syncTickPopulator.addAction(action);
            }
        }

        if (!hasPerm(target)) {
            target.sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.DENY, true));
            origin.teleportHere(target, origin);
            return;
        }


        if (target instanceof Player && !charge((Player) target)) {
            target.sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.LACKING_FUNDS, true));
            teleport(target, origin.getExit(), 180);
            return;
        }

        if (target instanceof PoweredMinecart) {
            // TODO: NOT Currently implemented, does not seem to be a accessible way to fix this using spigot api
            return;
        }

        teleport(target, destination, rotation);
    }

    private void teleport(Entity target, Location loc, double rotation) {
        Vector direction = target.getLocation().getDirection();
        Location exit = loc.setDirection(direction.rotateAroundY(rotation));
        Vector velocity = target.getVelocity();
        target.teleport(exit);
        Vector targetVelocity = velocity.rotateAroundY(rotation).multiply(Setting.getDouble(Setting.GATE_EXIT_SPEED_MULTIPLIER));
        target.setVelocity(targetVelocity);
    }

    private boolean charge(Player target) {
        if (origin.hasFlag(PortalFlag.PERSONAL_NETWORK))
            return Stargate.economyManager.chargePlayer((Player) target, Bukkit.getOfflinePlayer(origin.getOwnerUUID()), cost);
        else
            return Stargate.economyManager.chargeAndTax((Player) target, cost);
    }

    private double calculateAngleChange(BlockFace originFacing, BlockFace destinationFacing) {
        if (originFacing != null) {
            Vector originGateDirection = originFacing.getDirection();
            return directionalAngleOperator(originGateDirection, destinationFacing.getDirection());
        } else {
            return 0;
        }
    }

    private boolean hasPerm(Entity target) {
        StargatePortalEvent event = new StargatePortalEvent(target, origin);
        Bukkit.getPluginManager().callEvent(event);
        PermissionManager permissionManager = new PermissionManager(target);
        return (permissionManager.hasPerm(event) && !event.isCancelled());
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
