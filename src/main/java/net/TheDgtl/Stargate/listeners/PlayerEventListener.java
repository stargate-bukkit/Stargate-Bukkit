package net.TheDgtl.Stargate.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.PluginChannel;
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Settings;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.actions.ConditionalRepeatedTask;
import net.TheDgtl.Stargate.actions.SimpleAction;
import net.TheDgtl.Stargate.actions.SupplierAction;
import net.TheDgtl.Stargate.event.StargateCreateEvent;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.logging.Level;

public class PlayerEventListener implements Listener {
    private static long eventTime;
    private static PlayerInteractEvent previousEvent;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null)
            return;
        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking())
            return;
        if (this.clickIsBug(event, block)) {
            return;
        }

        // TODO material optimisation?
        Portal portal = Network.getPortal(block.getLocation(), GateStructureType.CONTROL_BLOCK);
        if (portal == null) {
            return;
        }
        Material blockMat = block.getType();
        if ((action == Action.RIGHT_CLICK_BLOCK)) {
            // Cancel item use
        }

        Player player = event.getPlayer();

        if (Tag.WALL_SIGNS.isTagged(blockMat)) {
            if (portal.isOpenFor(player)) {
                Stargate.log(Level.FINEST, "Player name=" + player.getName());
                portal.onSignClick(event);
            }
            return;
        }
        if (Tag.BUTTONS.isTagged(blockMat) || (blockMat == Material.DEAD_TUBE_CORAL_WALL_FAN)) {
            portal.onButtonClick(event);
            return;
        }

        Stargate.log(Level.WARNING, "This should never be triggered, an unknown glitch is occurring");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Settings.getBoolean(Setting.USING_BUNGEE))
            return;

        if (!Stargate.knowsServerName) {
            Stargate.log(Level.FINEST, "First time player join");
            getBungeeServerName();
            updateInterServerPortals();
        }

        Player player = event.getPlayer();
        IPortal destination = Stargate.pullFromQueue(player.getName());

        if (destination == null) {
            return;
        }
        destination.teleportHere(player, null);
    }

    /**
     * A stupid cheat to get serverName. A client is needed to get this data, hence
     * this stupid solution
     */
    private void getBungeeServerName() {
        /*
         * Action for loading bungee server id
         */
        Supplier<Boolean> action = (() -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(PluginChannel.GET_SERVER.getChannel());
            Bukkit.getServer().sendPluginMessage(Stargate.getPlugin(Stargate.class), PluginChannel.BUNGEE.getChannel(),
                    out.toByteArray());
            return true;
        });

        /*
         * Repeatedly try to load bungee server id until either the id is known, or no player is able to send bungee messages.
         */
        Stargate.syncSecPopulator.addAction(new ConditionalRepeatedTask(action,
                () -> !((Stargate.knowsServerName) || (1 > Bukkit.getServer().getOnlinePlayers().size()))));
    }

    private void updateInterServerPortals() {
        SimpleAction action = new SupplierAction(() -> {
            try {
                Stargate.factory.startInterServerConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        });
        Stargate.syncSecPopulator.addAction(action, true);

    }

    /**
     * This function decides if a right click of a block is caused by a Spigot bug
     *
     * <p>The Spigot bug currently makes every right click of some blocks trigger twice, causing the portal to close
     * immediately, or causing portal information printing twice. This fix should detect the bug without breaking
     * clicking once the bug is fixed.</p>
     *
     * @param event <p>The event causing the right click</p>
     * @param block <p>The block to check</p>
     * @return <p>True if the click is a bug and should be cancelled</p>
     */
    private boolean clickIsBug(PlayerInteractEvent event, Block block) {
        if (previousEvent != null &&
                event.getPlayer() == previousEvent.getPlayer() && eventTime + 15 > System.currentTimeMillis()) {
            previousEvent = null;
            eventTime = 0;
            return true;
        }
        previousEvent = event;
        eventTime = System.currentTimeMillis();
        return false;
    }
}
