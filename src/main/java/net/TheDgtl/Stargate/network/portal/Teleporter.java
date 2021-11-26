package net.TheDgtl.Stargate.network.portal;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.Bypass;
import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.actions.PopulatorAction;
import net.TheDgtl.Stargate.event.StargatePortalEvent;

public class Teleporter {
    private Location destination;
    private BlockFace destinationFace;
    private BlockFace entranceFace;
    private Portal origin;
    private int cost;
    private double rotation;

    /**
     * Instantiate a manager for advanced teleportation between portals
     * @param destination
     * @param 
     * @param destinationFace
     * @param entranceFace
     */
    public Teleporter(Location destination, Portal origin, BlockFace destinationFace, BlockFace entranceFace, int cost){
        this.destination = destination;
        this.origin = origin;
        this.destinationFace = destinationFace;
        this.entranceFace = entranceFace;
        this.rotation = calculateAngleChange(entranceFace,destinationFace);
        this.cost = cost;
    }
    
    public void teleport(Entity target) {
        betterTeleport(target,rotation);
    }
    
    /**
     * The {@link Entity#teleport(Entity)} method does not handle passengers / vehicles well. This method fixes that
     * @param target
     * @param loc
     */
    private void betterTeleport(Entity target, double rotation) {
        /*
         * To teleport the whole vessel, regardless of what entity triggered the initial event
         */
        if(target.getVehicle() != null) {
            betterTeleport(target.getVehicle(),rotation);
            return;
        }
        
        List<Entity> passengers = target.getPassengers();
        if(target.eject()) {
            Stargate.log(Level.FINEST, "Ejected all passangers");
            for(Entity passenger : passengers) {
                
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
                if(passenger instanceof Player) {
                    /*
                     * Delay action by one tick to avoid client issues
                     */
                    Stargate.syncTickPopulator.addAction(new DelayedAction(1,action));
                    continue;
                }
                Stargate.syncTickPopulator.addAction(action);
            }
        }
        
        if(!hasPerm(target)) {
            target.sendMessage(Stargate.langManager.getMessage(LangMsg.DENY, true));
            origin.teleportHere(target,origin.getGate().getFacing());
            return;
        }
        
        
        if(target instanceof Player && !charge((Player)target)) {
            target.sendMessage(Stargate.langManager.getMessage(LangMsg.LACKING_FUNDS, true));
            teleport(target, origin.getExit(), 180);
            return;
        }
        
        teleport(target, destination ,rotation);
    }
    
    private void teleport(Entity target, Location loc, double rotation){
        Vector direction = target.getLocation().getDirection();
        Location exit = loc.setDirection(direction.rotateAroundY(rotation));
        Vector velocity = target.getVelocity();
        target.teleport(exit);
        target.setVelocity(velocity.rotateAroundY(rotation));
    }
    
    private boolean charge(Player target) {
        if(target.hasPermission(Bypass.COST_USE.getPerm()))
            return true;
        
        if(origin.hasFlag(PortalFlag.PERSONAL_NETWORK)) 
            return Stargate.economyManager.chargePlayer((Player) target, Bukkit.getOfflinePlayer(origin.getOwnerUUID()), cost);
        else
            return Stargate.economyManager.chargeAndTax((Player) target, cost);
    }
    
    private double calculateAngleChange(BlockFace originFacing, BlockFace destinationFacing) {
        if(originFacing != null) {
            Vector originGateDirection =  originFacing.getDirection();
            return directionalAngleOperator(originGateDirection, destinationFacing.getDirection());
        } else {
            return 0;
        }
    }
    
    private boolean hasPerm(Entity target) {
        StargatePortalEvent event = new StargatePortalEvent(target, origin);
        Bukkit.getPluginManager().callEvent(event);
        PermissionManager mngr = new PermissionManager(target);
        return (!mngr.hasPerm(event) || event.isCancelled());
    }
    
    /**
     * The {@link Vector#angle(Vector)} function is not directional, meaning if you exchange position with vectors,
     * there will be no difference in angle. The behaviour that is needed in some portal methods is for the angle to
     * change sign if the vectors change places.
     * 
     * NOTE: ONLY ACCOUNTS FOR Y AXIS ROTATIONS
     * @param vector1 normalized
     * @param vector2 normalized
     * @return angle between the two vectors
     */
    private double directionalAngleOperator(Vector vector1, Vector vector2) {
        return Math.atan2(vector1.clone().crossProduct(vector2).getY(), vector1.dot(vector2));
    }
}
