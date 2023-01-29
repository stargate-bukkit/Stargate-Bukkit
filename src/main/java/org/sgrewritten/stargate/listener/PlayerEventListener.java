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
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.ConditionalDelayedAction;
import org.sgrewritten.stargate.action.ConditionalRepeatedTask;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.gate.structure.GateStructureType;
import org.sgrewritten.stargate.api.manager.BungeeManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.manager.BlockLoggingManager;
import org.sgrewritten.stargate.property.PluginChannel;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;
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
    private final @NotNull BlockLoggingManager loggingCompatability;

    public PlayerEventListener(@NotNull LanguageManager languageManager, @NotNull RegistryAPI registry, @NotNull BungeeManager bungeeManager, @NotNull BlockLoggingManager loggingCompatability) {
        this.languageManager = Objects.requireNonNull(languageManager);
        this.bungeeManager = Objects.requireNonNull(bungeeManager);
        this.registry = Objects.requireNonNull(registry);
        this.loggingCompatability = Objects.requireNonNull(loggingCompatability);
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
        if ((action == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) || this.clickIsBug(event)) {
            return;
        }

        RealPortal portal = registry.getPortal(block.getLocation(), GateStructureType.CONTROL_BLOCK);
        if (portal == null) {
            return;
        }
        if (portal.getGate().getPortalPosition(block.getLocation()).onBlockClick(event, portal)) {
            loggingCompatability.logPlayerInteractEvent(event);
        }
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
        Supplier<Boolean> action = (() -> {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                dataOutputStream.writeUTF(PluginChannel.GET_SERVER.getChannel());
                Bukkit.getServer().sendPluginMessage(Stargate.getPlugin(Stargate.class), PluginChannel.BUNGEE.getChannel(),
                        byteArrayOutputStream.toByteArray());
                return true;
            } catch (IOException e) {
                Stargate.log(e);
                return false;
            }
        });

        //Repeatedly try to load bungee server id until either the id is known, or no player is able to send bungee messages.
        Stargate.addSynchronousSecAction(new ConditionalRepeatedTask(action,
                () -> !((Stargate.knowsServerName()) || (1 > Bukkit.getServer().getOnlinePlayers().size()))));

        //Update the server name in the database once it's known
        updateServerName();
    }

    /**
     * Updates this server's name in the database if necessary
     */
    private void updateServerName() {
        ConditionalDelayedAction action = new ConditionalDelayedAction(() -> {
            try {
                Stargate.getStorageAPIStatic().startInterServerConnection();
            } catch (StorageWriteException e) {
                Stargate.log(e);
            }
            return true;
        }, Stargate::knowsServerName);
        Stargate.addSynchronousSecAction(action, true);
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
