package net.TheDgtl.Stargate.listeners;

import net.TheDgtl.Stargate.Bypass;
import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.actions.SupplierAction;
import net.TheDgtl.Stargate.event.StargateCreateEvent;
import net.TheDgtl.Stargate.event.StargateDestroyEvent;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.EnumSet;
import java.util.function.Supplier;
import java.util.logging.Level;

public class BlockEventListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        Location loc = event.getBlock().getLocation();
        Portal portal = Network.getPortal(loc, GateStructureType.FRAME);
        if (portal != null) {
            int cost = Setting.getInteger(Setting.DESTROY_COST);
            StargateDestroyEvent dEvent = new StargateDestroyEvent(portal, event.getPlayer(), cost);
            Bukkit.getPluginManager().callEvent(dEvent);
            PermissionManager permissionManager = new PermissionManager(event.getPlayer());
            if (permissionManager.hasPerm(dEvent) && !dEvent.isCancelled()) {
                /*
                 * If setting charge free destination is false, destination portal is PortalFlag.Free and portal is of Fixed type
                 * or if player has override cost permission, do not collect money
                 */
                if (shouldChargePlayer(event.getPlayer(), portal) && !Stargate.economyManager.chargeAndTax(event.getPlayer(), dEvent.getCost())) {
                    event.getPlayer().sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.LACKING_FUNDS, true));
                    event.setCancelled(true);
                    return;
                }

                Supplier<Boolean> action = () -> {
                    String msg = Stargate.languageManager.getMessage(TranslatableMessage.DESTROY, false);
                    event.getPlayer().sendMessage(msg);

                    portal.destroy();
                    Stargate.log(Level.FINEST, "Broke the portal");
                    return true;
                };
                Stargate.syncTickPopulator.addAction(new SupplierAction(action));
                return;
            }
            event.setCancelled(true);
            return;
        }
        if (Network.getPortal(loc, new GateStructureType[]{GateStructureType.CONTROL_BLOCK, GateStructureType.IRIS}) != null) {
            event.setCancelled(true);
        }
    }

    private boolean shouldChargePlayer(Player player, IPortal portal) {
        if (player.hasPermission(Bypass.COST_CREATE.getPermissionString()))
            return false;

        return Setting.getBoolean(Setting.CHARGE_FREE_DESTINATION)
                || !portal.hasFlag(PortalFlag.FIXED)
                || !((Portal) portal).loadDestination().hasFlag(PortalFlag.FREE);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();
        Portal portal = Network.getPortal(loc, GateStructureType.IRIS);
        if(portal != null)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Block block = event.getBlock();
        if (!(block.getBlockData() instanceof WallSign))
            return;

        String[] lines = event.getLines();
        String network = lines[2];
        int cost = Setting.getInteger(Setting.CREATION_COST);
        Player player = event.getPlayer();
        EnumSet<PortalFlag> flags = PortalFlag.parseFlags(lines[3]);
        PermissionManager permissionManager = new PermissionManager(player);
        TranslatableMessage errorMessage = null;


        flags = permissionManager.returnAllowedFlags(flags);
        String finalNetworkName;
        Network selectedNet = null;
        try {
            finalNetworkName = compileNetworkName(network, flags, player, permissionManager);
            selectedNet = selectNetwork(finalNetworkName, flags);
        } catch (NameError e2) {
            errorMessage = e2.getErrorMessage();
        }

        try {
            IPortal portal = Portal.createPortalFromSign(selectedNet, lines, block, flags, event.getPlayer().getUniqueId());
            StargateCreateEvent sEvent = new StargateCreateEvent(event.getPlayer(), portal, lines, cost);


            Bukkit.getPluginManager().callEvent(sEvent);

            boolean hasPerm = permissionManager.hasPerm(sEvent);
            Stargate.log(Level.CONFIG, " player has perm = " + hasPerm);


            if (errorMessage != null) {
                player.sendMessage(Stargate.languageManager.getMessage(errorMessage, true));
                return;
            }

            if (sEvent.isCancelled() || !hasPerm) {
                Stargate.log(Level.CONFIG, " Event was cancelled due to perm or external cancellation");
                player.sendMessage(Stargate.languageManager.getMessage(permissionManager.getDenyMsg(), true));
                return;
            }

            if (shouldChargePlayer(player, portal) && !Stargate.economyManager.chargeAndTax(player, sEvent.getCost())) {
                player.sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.LACKING_FUNDS, true));
                return;
            }
            selectedNet.addPortal(portal, true);
            selectedNet.updatePortals();
            Stargate.log(Level.FINE, "A Gate format matches");
            player.sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.CREATE, false));
        } catch (NoFormatFound e) {
            Stargate.log(Level.FINE, "No Gate format matches");
        } catch (GateConflict e) {
            player.sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.GATE_CONFLICT, true));
        } catch (NameError e) {
            player.sendMessage(Stargate.languageManager.getMessage(e.getErrorMessage(), true));
        }
    }

    /**
     * Goes through some scenarios where the initial network name would need to be changed, and returns the modified network name
     *
     * @param initialNetworkName
     * @param flags              all the flags of the portal, this code has some side effects and might add some more flags
     * @param player
     * @param permissionManager
     * @return
     * @throws NameError
     */
    private String compileNetworkName(String initialNetworkName, EnumSet<PortalFlag> flags, Player player, PermissionManager permissionManager) throws NameError {
        if (initialNetworkName.endsWith("]") && initialNetworkName.startsWith("[")) {
            flags.add(PortalFlag.FANCY_INTER_SERVER);
            return initialNetworkName.substring(1, initialNetworkName.length() - 1);
        }

        if (initialNetworkName.endsWith("}") && initialNetworkName.startsWith("{")) {
            String possiblePlayername = initialNetworkName.substring(1, initialNetworkName.length() - 1);

            if (possiblePlayername != null) {
                flags.add(PortalFlag.PERSONAL_NETWORK);
                Player possiblePlayer = Bukkit.getPlayer(possiblePlayername);
                if (possiblePlayer != null)
                    return possiblePlayer.getUniqueId().toString();
            }
            throw new NameError(TranslatableMessage.INVALID_NAME);
        }

        if (!permissionManager.canCreateInNetwork(initialNetworkName) || initialNetworkName.trim().isEmpty()) {
            Stargate.log(Level.CONFIG, " Player does not have perms to create on current network. Replacing to default...");
            String defaultNet = Setting.getString(Setting.DEFAULT_NET);
            if (!permissionManager.canCreateInNetwork(defaultNet)) {
                Stargate.log(Level.CONFIG, " Player does not have perms to create on current network. Replacing to private...");
                flags.add(PortalFlag.PERSONAL_NETWORK);
                return player.getUniqueId().toString();
            }
            return defaultNet;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer possiblePersonalNetworkTarget = Bukkit.getOfflinePlayer(initialNetworkName);
        if ((possiblePersonalNetworkTarget != null) || flags.contains(PortalFlag.PRIVATE)) {
            flags.add(PortalFlag.PERSONAL_NETWORK);
            return possiblePersonalNetworkTarget.getUniqueId().toString();
        }

        if (flags.contains(PortalFlag.BUNGEE)) {
            return "§§§§§§#BUNGEE#§§§§§§";
        }
        return initialNetworkName;
    }

    private Network selectNetwork(String name, EnumSet<PortalFlag> flags) throws NameError {
        try {
            Stargate.factory.createNetwork(name, flags);
        } catch (NameError e1) {
            TranslatableMessage msg = e1.getErrorMessage();
            if (msg != null) {
                throw e1;
            }
        }
        return Stargate.factory.getNetwork(name, flags.contains(PortalFlag.FANCY_INTER_SERVER));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        // check if portal is affected, if so cancel
        if (Network.isInPortal(event.getBlocks(), GateStructureType.values()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        // check if portal is affected, if so cancel
        if (Network.isInPortal(event.getBlocks(), GateStructureType.values()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (Network.isInPortal(event.blockList(), GateStructureType.values()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        // check if water or lava is flowing into a gate entrance?
        // if so, cancel
        Block to = event.getToBlock();
        Block from = event.getBlock();
        if ((Network.getPortal(to.getLocation(), GateStructureType.IRIS) != null)
                || (Network.getPortal(from.getLocation(), GateStructureType.IRIS) != null))
            event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFormEvent event) {
        Location loc = event.getBlock().getLocation();
        Portal portal = Network.getPortal(loc, GateStructureType.IRIS);
        if(portal != null)
            event.setCancelled(true);
    }
}
