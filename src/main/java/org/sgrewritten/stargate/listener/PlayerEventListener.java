package org.sgrewritten.stargate.listener;

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
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.manager.BungeeManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.colors.ColorConverter;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.manager.BlockLoggingManager;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.thread.task.StargateGlobalTask;
import org.sgrewritten.stargate.util.ButtonHelper;
import org.sgrewritten.stargate.util.MessageUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * A listener for relevant player events such as right- or left-clicking
 */
public class PlayerEventListener implements Listener {

    private static long eventTime;
    private static PlayerInteractEvent previousEvent;
    private final @NotNull LanguageManager languageManager;
    private final @NotNull BungeeManager bungeeManager;
    private final @NotNull RegistryAPI registry;
    private final @NotNull BlockLoggingManager loggingCompatibility;
    private final StorageAPI storageAPI;

    /**
     * @param languageManager <p>A localized message provider</p>
     * @param registry <p>A registry containing all portal information</p>
     * @param bungeeManager <p>A manager that deals with bungee related messages</p>
     * @param loggingCompatibility <p>Block logger interface</p>
     * @param storageAPI <p>An interface to the database containing all info about portals</p>
     */
    public PlayerEventListener(@NotNull LanguageManager languageManager, @NotNull RegistryAPI registry, @NotNull BungeeManager bungeeManager, @NotNull BlockLoggingManager loggingCompatibility, StorageAPI storageAPI) {
        this.languageManager = Objects.requireNonNull(languageManager);
        this.bungeeManager = Objects.requireNonNull(bungeeManager);
        this.registry = Objects.requireNonNull(registry);
        this.loggingCompatibility = Objects.requireNonNull(loggingCompatibility);
        this.storageAPI = storageAPI;
    }

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
        if ((action == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) || clickIsBug(event)) {
            return;
        }

        PortalPosition portalPosition = registry.getPortalPosition(block.getLocation());
        if (portalPosition != null) {
            handleRelevantClickEvent(block, portalPosition, event);
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isNonInteractablePortalPart(block.getType()) && registry.getPortal(block.getLocation()) != null) {
            event.setCancelled(true);
        }
    }

    private boolean isNonInteractablePortalPart(Material type) {
        if (Tag.ANVIL.isTagged(type)) {
            return true;
        }
        return Material.RESPAWN_ANCHOR == type;
    }

    /**
     * Handles a right-click event that is known to be relevant
     *
     * @param block <p>The block that was interacted with</p>
     * @param event <p>The player interact event to handle</p>
     */
    private void handleRelevantClickEvent(Block block, PortalPosition portalPosition, PlayerInteractEvent event) {
        Material blockMaterial = block.getType();
        Player player = event.getPlayer();
        RealPortal portal = portalPosition.getPortal();
        if (portal == null) {
            Stargate.log(Level.WARNING, "Improper use of unregistered PortalPositions");
            return;
        }

        if (Tag.WALL_SIGNS.isTagged(blockMaterial)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && dyePortalSignText(event, portal)) {
                portal.setSignColor(ColorConverter.getDyeColorFromMaterial(event.getMaterial()), registry.getPortalPosition(block.getLocation()));
                event.setUseInteractedBlock(Event.Result.ALLOW);
                return;
            }
            loggingCompatibility.logPlayerInteractEvent(event);
            event.setUseInteractedBlock(Event.Result.DENY);
            if (portal.isOpenFor(player)) {
                Stargate.log(Level.FINEST, "Player name=" + player.getName());
                portal.getBehavior().onSignClick(event);
                return;
            }
        }
        if (ButtonHelper.isButton(blockMaterial)) {
            portal.getBehavior().onButtonClick(event);
            loggingCompatibility.logPlayerInteractEvent(event);
            event.setUseInteractedBlock(Event.Result.DENY);
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

        StargatePermissionManager permissionManager = new StargatePermissionManager(event.getPlayer(), languageManager);
        boolean hasPermission = permissionManager.hasCreatePermissions(portal);
        if (!hasPermission) {
            String message = permissionManager.getDenyMessage();
            MessageUtils.sendMessageFromPortal(portal, event.getPlayer(), message, MessageType.DENY);
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
        Material glowInkSac = Material.matchMaterial("GLOW_INK_SAC");
        if (glowInkSac != null && item.getType() == glowInkSac) {
            return true;
        }
        return (itemName.contains("DYE") || item.getType() == Material.INK_SAC);
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
        Portal destination = bungeeManager.pullFromQueue(player.getName());

        if (destination != null) {
            destination.teleportHere(player, null);
        }

        if (!ConfigurationHelper.getBoolean(ConfigurationOption.USING_REMOTE_DATABASE)) {
            return;
        }

        //Gets the name of this server if it's still unknown
        if (!Stargate.knowsServerName()) {
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
        new StargateGlobalTask() {
            @Override
            public void run() {
                if (Bukkit.getServer().getOnlinePlayers().isEmpty()) {
                    return;
                }
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                    dataOutputStream.writeUTF(PluginChannel.GET_SERVER.getChannel());
                    Bukkit.getServer().sendPluginMessage(Stargate.getInstance(), PluginChannel.BUNGEE.getChannel(),
                            byteArrayOutputStream.toByteArray());
                } catch (IOException e) {
                    Stargate.log(e);
                }
                this.cancel();
            }
        }.runTaskTimer(0, 20);


        //Update the server name in the database once it's known
        updateServerName();
    }

    /**
     * Updates this server's name in the database if necessary
     */
    private void updateServerName() {
        new StargateGlobalTask(true) {
            @Override
            public void run() {
                try {
                    storageAPI.startInterServerConnection();
                } catch (StorageWriteException e) {
                    Stargate.log(e);
                }
            }
        }.runNow();
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
    private static boolean clickIsBug(PlayerInteractEvent event) {
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
