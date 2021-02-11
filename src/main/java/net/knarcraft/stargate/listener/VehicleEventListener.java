package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Portal;
import net.knarcraft.stargate.Stargate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.List;

public class VehicleEventListener implements Listener {
    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!Stargate.handleVehicles) return;
        List<Entity> passengers = event.getVehicle().getPassengers();
        Vehicle vehicle = event.getVehicle();

        Portal portal = Portal.getByEntrance(event.getTo());
        if (portal == null || !portal.isOpen()) return;

        // We don't support vehicles in Bungee portals
        if (portal.isBungee()) return;

        if (!passengers.isEmpty() && passengers.get(0) instanceof Player) {
            /*
            Player player = (Player) passengers.get(0);
            if (!portal.isOpenFor(player)) {
                stargate.sendMessage(player, stargate.getString("denyMsg"));
                return;
            }

            Portal dest = portal.getDestination(player);
            if (dest == null) return;
            boolean deny = false;
            // Check if player has access to this network
            if (!canAccessNetwork(player, portal.getNetwork())) {
                deny = true;
            }

            // Check if player has access to destination world
            if (!canAccessWorld(player, dest.getWorld().getName())) {
                deny = true;
            }

            if (!canAccessPortal(player, portal, deny)) {
                stargate.sendMessage(player, stargate.getString("denyMsg"));
                portal.close(false);
                return;
            }

            int cost = stargate.getUseCost(player, portal, dest);
            if (cost > 0) {
                boolean success;
                if(portal.getGate().getToOwner()) {
                    if(portal.getOwnerUUID() == null) {
                        success = stargate.chargePlayer(player, portal.getOwnerUUID(), cost);
                    } else {
                        success = stargate.chargePlayer(player, portal.getOwnerName(), cost);
                    }
                } else {
                    success = stargate.chargePlayer(player, cost);
                }
                if(!success) {
                    // Insufficient Funds
                    stargate.sendMessage(player, stargate.getString("inFunds"));
                    portal.close(false);
                    return;
                }
                String deductMsg = stargate.getString("ecoDeduct");
                deductMsg = stargate.replaceVars(deductMsg, new String[] {"%cost%", "%portal%"}, new String[] {EconomyHandler.format(cost), portal.getName()});
                sendMessage(player, deductMsg, false);
                if (portal.getGate().getToOwner()) {
                    Player p;
                    if(portal.getOwnerUUID() != null) {
                        p = server.getPlayer(portal.getOwnerUUID());
                    } else {
                        p = server.getPlayer(portal.getOwnerName());
                    }
                    if (p != null) {
                        String obtainedMsg = stargate.getString("ecoObtain");
                        obtainedMsg = stargate.replaceVars(obtainedMsg, new String[] {"%cost%", "%portal%"}, new String[] {EconomyHandler.format(cost), portal.getName()});
                        stargate.sendMessage(p, obtainedMsg, false);
                    }
                }
            }

            stargate.sendMessage(player, stargate.getString("teleportMsg"), false);
            dest.teleport(vehicle);
            portal.close(false);
             */
        } else {
            Portal dest = portal.getDestination();
            if (dest == null) return;
            dest.teleport(vehicle);
        }
    }
}
