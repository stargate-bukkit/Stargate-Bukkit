package net.TheDgtl.Stargate.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.TheDgtl.Stargate.*;
import net.TheDgtl.Stargate.actions.ConditionalDelayedAction;
import net.TheDgtl.Stargate.actions.ConditionalRepeatedTask;
import net.TheDgtl.Stargate.event.StargateCreateEvent;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.util.ColorConverter;
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

/**
 * A listener for relevant player events such as right- or left-clicking
 */
public class PlayerEventListener implements Listener {

    private static long eventTime;
    private static PlayerInteractEvent previousEvent;

    /**
     * Listens for and handles any relevant interaction events such as sign or button interaction
     *
     * @param event <p>The triggered player interact event</p>
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Action action = event.getAction();
        if ((action == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) || this.clickIsBug(event)) {
            return;
        }

        // TODO material optimisation?
        Portal portal = Network.getPortal(block.getLocation(), GateStructureType.CONTROL_BLOCK);
        if (portal == null) {
            return;
        }

        handleRelevantClickEvent(block, portal, event);
    }

    /**
     * Handles a right-click event that is known to be relevant
     *
     * @param block  <p>The block that was interacted with</p>
     * @param portal <p>The portal the block belongs to</p>
     * @param event  <p>The player interact event to handle</p>
     */
    private void handleRelevantClickEvent(Block block, Portal portal, PlayerInteractEvent event) {
        Material blockMaterial = block.getType();
        Player player = event.getPlayer();

        if (Tag.WALL_SIGNS.isTagged(blockMaterial)) {
            if (dyePortalSignText(event, portal)) {
                portal.setSignColor(ColorConverter.getDyeColorFromMaterial(event.getMaterial()));
                event.setUseInteractedBlock(Event.Result.ALLOW);
                return;
            }
            event.setUseInteractedBlock(Event.Result.DENY);
            if (portal.isOpenFor(player)) {
                Stargate.log(Level.FINEST, "Player name=" + player.getName());
                portal.onSignClick(event);
                return;
            }
        }
        if (Tag.BUTTONS.isTagged(blockMaterial) || (blockMaterial == Material.DEAD_TUBE_CORAL_WALL_FAN)) {
            portal.onButtonClick(event);
            return;
        }
        Stargate.log(Level.WARNING, "This should never be triggered, an unknown glitch is occurring");

    }

    /**
     * Tries to dye the text of a portals sign if the player is holding a dye and has enough permissions
     *
     * @param event  <p>The interact event causing this method to be triggered</p>
     * @param portal <p>The portal whose sign to apply dye to<p>
     * @return <p>True if the dye should be applied</p>
     */
    private boolean dyePortalSignText(PlayerInteractEvent event, Portal portal) {
        ItemStack item = event.getItem();
        PermissionManager permissionManager = new PermissionManager(event.getPlayer());
        StargateCreateEvent colorSignPermission = new StargateCreateEvent(event.getPlayer(), portal, new String[]{""},
                0);
        return itemIsDye(item) && permissionManager.hasPermission(colorSignPermission);
    }

    /**
     * Checks if the given item stack is a type of dye
     *
     * @param item <p>The item to check</p>
     * @return <p>True if the item stack is a type of dye</p>
     */
    private boolean itemIsDye(ItemStack item) {
        if (item == null) {
            return false;
        }
        String itemName = item.getType().toString();
        return (itemName.contains("DYE") || itemName.contains("GLOW_INK_SAC"));
    }

    /**
     * Listens for any player join events that might be relevant for BungeeCord
     *
     * @param event <p>The triggered player join event</p>
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Settings.getBoolean(Setting.USING_BUNGEE))
            return;

        //Gets the name of this server if it's still unknown
        if (!Stargate.knowsServerName) {
            Stargate.log(Level.FINEST, "First time player join");
            getBungeeServerName();
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
        //Action for loading bungee server id
        Supplier<Boolean> action = (() -> {
            //TODO: Replace this with a stable method
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(PluginChannel.GET_SERVER.getChannel());
            Bukkit.getServer().sendPluginMessage(Stargate.getPlugin(Stargate.class), PluginChannel.BUNGEE.getChannel(),
                    out.toByteArray());
            return true;
        });

        //Repeatedly try to load bungee server id until either the id is known, or no player is able to send bungee messages.
        Stargate.syncSecPopulator.addAction(new ConditionalRepeatedTask(action,
                () -> !((Stargate.knowsServerName) || (1 > Bukkit.getServer().getOnlinePlayers().size()))));

        //Update the server name in the database once it's known
        updateServerName();
    }

    /**
     * Updates this server's name in the database if necessary
     */
    private void updateServerName() {
        ConditionalDelayedAction action = new ConditionalDelayedAction(() -> {
            try {
                Stargate.factory.startInterServerConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }, () -> Stargate.knowsServerName);
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
     * @return <p>True if the click is a bug and should be cancelled</p>
     */
    private boolean clickIsBug(PlayerInteractEvent event) {
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
