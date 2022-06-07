package net.TheDgtl.Stargate.listener;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.action.ConditionalDelayedAction;
import net.TheDgtl.Stargate.action.ConditionalRepeatedTask;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.manager.StargatePermissionManager;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.property.PluginChannel;
import net.TheDgtl.Stargate.util.BungeeHelper;
import net.TheDgtl.Stargate.util.ButtonHelper;
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
        RealPortal portal = Stargate.getRegistryStatic().getPortal(block.getLocation(), GateStructureType.CONTROL_BLOCK);
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
    private void handleRelevantClickEvent(Block block, RealPortal portal, PlayerInteractEvent event) {
        Material blockMaterial = block.getType();
        Player player = event.getPlayer();

        if (Tag.WALL_SIGNS.isTagged(blockMaterial)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && dyePortalSignText(event, portal)) {
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
        if (ButtonHelper.isButton(blockMaterial)) {
            portal.onButtonClick(event);
        }

    }

    /**
     * Tries to dye the text of a portals sign if the player is holding a dye and has enough permissions
     *
     * @param event  <p>The interact event causing this method to be triggered</p>
     * @param portal <p>The portal whose sign to apply dye to<p>
     * @return <p>True if the dye should be applied</p>
     */
    private boolean dyePortalSignText(PlayerInteractEvent event, RealPortal portal) {
        ItemStack item = event.getItem();
        if (!itemIsDye(item)) {
            return false;
        }

        StargatePermissionManager permissionManager = new StargatePermissionManager(event.getPlayer());
        boolean hasPermission = permissionManager.hasCreatePermissions(portal);
        if (!hasPermission) {
            event.getPlayer().sendMessage(permissionManager.getDenyMessage());
        }
        return hasPermission;
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
        if (!ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            return;
        }

        Player player = event.getPlayer();
        Portal destination = BungeeHelper.pullFromQueue(player.getName());

        if (destination != null) {
            destination.teleportHere(player, null);
        }

        if (!ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE)) {
            return;
        }

        //Gets the name of this server if it's still unknown
        if (!Stargate.knowsServerName) {
            Stargate.log(Level.FINEST, "First time player join");
            getBungeeServerName();
        }
    }

    /**
     * A stupid cheat to get serverName. A client is needed to get this data, hence
     * this stupid solution
     */
    private void getBungeeServerName() {
        //Action for loading bungee server id
        Supplier<Boolean> action = (() -> {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                dataOutputStream.writeUTF(PluginChannel.GET_SERVER.getChannel());
                Bukkit.getServer().sendPluginMessage(Stargate.getPlugin(Stargate.class), PluginChannel.BUNGEE.getChannel(),
                        byteArrayOutputStream.toByteArray());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
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
            Stargate.getStorageAPIStatic().startInterServerConnection();
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
