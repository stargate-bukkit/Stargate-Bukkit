package net.knarcraft.stargate.listener;

import net.knarcraft.knarlib.util.UpdateChecker;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.Message;
import net.knarcraft.stargate.config.MessageSender;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalActivator;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.teleporter.PlayerTeleporter;
import net.knarcraft.stargate.portal.teleporter.VehicleTeleporter;
import net.knarcraft.stargate.utility.BungeeHelper;
import net.knarcraft.stargate.utility.MaterialHelper;
import net.knarcraft.stargate.utility.PermissionHelper;
import net.knarcraft.stargate.utility.TeleportHelper;
import net.knarcraft.stargate.utility.UUIDMigrationHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This listener listens to any player-related events related to stargates
 */
@SuppressWarnings("unused")
public class PlayerEventListener implements Listener {

    private static final Map<Player, Long> previousEventTimes = new HashMap<>();

    /**
     * This event handler handles detection of any player teleporting through a bungee gate
     *
     * @param event <p>The event to check for a teleporting player</p>
     */
    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        //Migrate player name to UUID if necessary
        UUIDMigrationHelper.migrateUUID(player);

        //Notify joining admins about the available update
        String availableUpdate = Stargate.getUpdateAvailable();
        if (availableUpdate != null && Stargate.getStargateConfig().alertAdminsAboutUpdates() &&
                player.hasPermission("stargate.admin")) {
            String updateMessage = UpdateChecker.getUpdateAvailableString(availableUpdate, Stargate.getPluginVersion());
            Stargate.getMessageSender().sendErrorMessage(player, updateMessage);
        }

        if (!Stargate.getGateConfig().enableBungee()) {
            return;
        }

        //Check if the player is waiting to be teleported to a stargate
        String destination = BungeeHelper.removeFromQueue(player.getUniqueId());
        if (destination == null) {
            return;
        }

        Portal portal = PortalHandler.getBungeePortal(destination);
        if (portal == null) {
            Stargate.debug("PlayerJoin", "Error fetching destination portal: " + destination);
            return;
        }
        //Teleport the player to the stargate
        new PlayerTeleporter(portal, player).teleport(portal, null);
    }

    /**
     * This event handler detects if a player moves into a portal
     *
     * @param event <p>The player move event which was triggered</p>
     */
    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        if (event.isCancelled() || event.getTo() == null) {
            return;
        }

        BlockLocation fromLocation = new BlockLocation(event.getFrom().getBlock());
        BlockLocation toLocation = new BlockLocation(event.getTo().getBlock());
        Player player = event.getPlayer();

        //Check whether the event needs to be considered
        if (!isRelevantMoveEvent(event, player, fromLocation, toLocation)) {
            return;
        }
        Portal entrancePortal = PortalHandler.getByEntrance(toLocation);
        //Check an additional block away in case the portal is a bungee portal using END_PORTAL
        if (entrancePortal == null) {
            entrancePortal = PortalHandler.getByAdjacentEntrance(toLocation);
            // This should never realistically be null
            if (entrancePortal == null) {
                return;
            }
        }

        Portal destination = entrancePortal.getPortalActivator().getDestination(player);
        if (destination == null) {
            return;
        }

        Entity playerVehicle = player.getVehicle();
        //If the player is in a vehicle, but vehicle handling is disabled, just ignore the player
        if (playerVehicle == null || (playerVehicle instanceof LivingEntity &&
                Stargate.getGateConfig().handleVehicles())) {
            teleportPlayer(playerVehicle, player, entrancePortal, destination, event);
        }
    }

    /**
     * Teleports a player, also teleports the player's vehicle if it's a living entity
     *
     * @param playerVehicle  <p>The vehicle the player is currently sitting in</p>
     * @param player         <p>The player which moved</p>
     * @param entrancePortal <p>The entrance the player entered</p>
     * @param destination    <p>The destination of the entrance portal</p>
     * @param event          <p>The move event causing the teleportation to trigger</p>
     */
    private void teleportPlayer(@Nullable Entity playerVehicle, @NotNull Player player, @NotNull Portal entrancePortal,
                                @NotNull Portal destination, @NotNull PlayerMoveEvent event) {
        if (playerVehicle instanceof LivingEntity) {
            //Make sure any horses are properly tamed
            if (playerVehicle instanceof AbstractHorse horse && !horse.isTamed()) {
                horse.setTamed(true);
                horse.setOwner(player);
            }
            //Teleport the player's vehicle
            player.setVelocity(new Vector());
            new VehicleTeleporter(destination, (Vehicle) playerVehicle).teleportEntity(entrancePortal);
        } else {
            //Just teleport the player like normal
            new PlayerTeleporter(destination, player).teleportPlayer(entrancePortal, event);
        }
        if (!entrancePortal.getOptions().isSilent()) {
            Stargate.getMessageSender().sendSuccessMessage(player, Stargate.getString(Message.TELEPORTED));
        }
        entrancePortal.getPortalOpener().closePortal(false);
    }

    /**
     * Checks whether a player move event is relevant for this plugin
     *
     * @param event        <p>The player move event to check</p>
     * @param player       <p>The player which moved</p>
     * @param fromLocation <p>The location the player is moving from</p>
     * @param toLocation   <p>The location the player is moving to</p>
     * @return <p>True if the event is relevant</p>
     */
    private boolean isRelevantMoveEvent(@NotNull PlayerMoveEvent event, Player player,
                                        @NotNull BlockLocation fromLocation, @NotNull BlockLocation toLocation) {
        //Check to see if the player moved to another block
        if (fromLocation.equals(toLocation)) {
            return false;
        }

        //Check if the player moved from a portal
        Portal entrancePortal = PortalHandler.getByEntrance(toLocation);
        if (entrancePortal == null) {
            //Check an additional block away for BungeeCord portals using END_PORTAL as its material
            entrancePortal = PortalHandler.getByAdjacentEntrance(toLocation);
            if (entrancePortal == null || !entrancePortal.getOptions().isBungee() ||
                    !MaterialHelper.specifiersToMaterials(
                            entrancePortal.getGate().getPortalOpenMaterials()).contains(Material.END_PORTAL)) {
                return false;
            }
        }

        Portal destination = entrancePortal.getPortalActivator().getDestination(player);

        //Catch always open portals without a valid destination to prevent the user for being teleported and denied
        if (!entrancePortal.getOptions().isBungee() && destination == null) {
            return false;
        }

        //Decide if the anything stops the player from teleport
        if (PermissionHelper.playerCannotTeleport(entrancePortal, destination, player, event)) {
            return false;
        }

        //Decide if the user should be teleported to another bungee server
        if (entrancePortal.getOptions().isBungee()) {
            if (BungeeHelper.bungeeTeleport(player, entrancePortal, event) && !entrancePortal.getOptions().isSilent()) {
                Stargate.getMessageSender().sendSuccessMessage(player, Stargate.getString(Message.TELEPORTED));
            }
            return false;
        }

        //Make sure to check if the player has any leashed creatures, even though leashed teleportation is disabled
        return TeleportHelper.noLeashedCreaturesPreventTeleportation(player);
    }

    /**
     * This event handler detects if a player clicks a button or a sign
     *
     * @param event <p>The player interact event which was triggered</p>
     */
    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getHand() == null) {
                return;
            }
            // Handle right-click of a sign, button or other
            handleRightClickBlock(event, player, block, event.getHand());
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && block.getBlockData() instanceof WallSign) {
            //Handle left click of a wall sign
            handleSignClick(event, player, block, true);
        }
    }

    /**
     * This method handles left- or right-clicking of a sign
     *
     * @param event     <p>The event causing the click</p>
     * @param player    <p>The player clicking the sign</p>
     * @param block     <p>The block that was clicked</p>
     * @param leftClick <p>Whether the player performed a left click as opposed to a right click</p>
     */
    private void handleSignClick(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull Block block,
                                 boolean leftClick) {
        Portal portal = PortalHandler.getByBlock(block);
        if (portal == null) {
            return;
        }

        //Allow players with permissions to apply dye to signs
        if (dyeSign(event, player, portal)) {
            return;
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        if (leftClick) {
            //Cancel event in creative mode to prevent breaking the sign
            if (player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        } else {
            //Prevent usage of item in the player's hand (placing block and such)
            event.setUseItemInHand(Event.Result.DENY);
        }

        //Check if the user can use the portal
        if (cannotAccessPortal(player, portal)) {
            return;
        }

        //Cycle portal destination
        if ((!portal.isOpen()) && (!portal.getOptions().isFixed())) {
            PortalActivator destinations = portal.getPortalActivator();
            if (leftClick) {
                destinations.cycleDestination(player, -1);
            } else {
                destinations.cycleDestination(player);
            }
        }
    }

    /**
     * Tries to take care of a sign dye interaction
     *
     * @param event  <p>The triggered player interaction event</p>
     * @param player <p>The involved player</p>
     * @param portal <p>The involved portal</p>
     * @return <p>True if a sign was dyed</p>
     */
    private boolean dyeSign(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull Portal portal) {
        EquipmentSlot hand = event.getHand();
        // Check if the player is allowed to dye the sign
        if (hand == null || (!PermissionHelper.hasPermission(player, "stargate.admin.dye") &&
                !portal.isOwner(player))) {
            return false;
        }

        // Check if the player is holding an item
        ItemStack item = player.getInventory().getItem(hand);
        if (item == null) {
            return false;
        }

        String itemName = item.getType().toString();
        // Check if the player's item can be used to dye the sign
        if (itemName.endsWith("DYE") || itemName.endsWith("INK_SAC")) {
            event.setUseInteractedBlock(Event.Result.ALLOW);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Stargate.getInstance(), portal::drawSign, 1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if a player should be denied from accessing (using) a portal
     *
     * @param player <p>The player trying to access the portal</p>
     * @param portal <p>The portal the player is trying to use</p>
     * @return <p>True if the player should be denied</p>
     */
    private boolean cannotAccessPortal(@NotNull Player player, @NotNull Portal portal) {
        boolean deny = PermissionHelper.cannotAccessNetwork(player, portal.getCleanNetwork());

        if (PermissionHelper.portalAccessDenied(player, portal, deny)) {
            if (!portal.getOptions().isSilent()) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString(Message.ACCESS_DENIED));
            }
            return true;
        }
        return false;
    }

    /**
     * This method handles right-clicking of a sign or button belonging to a stargate
     *
     * @param event  <p>The event triggering the right-click</p>
     * @param player <p>The player doing the right-click</p>
     * @param block  <p>The block the player clicked</p>
     * @param hand   <p>The hand the player used to interact with the stargate</p>
     */
    private void handleRightClickBlock(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull Block block,
                                       @NotNull EquipmentSlot hand) {
        if (block.getBlockData() instanceof WallSign) {
            handleSignClick(event, player, block, false);
            return;
        }

        //Prevent a double click caused by a Spigot bug
        if (clickIsBug(event.getPlayer())) {
            return;
        }

        if (MaterialHelper.isButtonCompatible(block.getType())) {
            Portal portal = PortalHandler.getByBlock(block);
            if (portal == null) {
                return;
            }

            //Prevent the held item from being placed
            event.setUseItemInHand(Event.Result.DENY);
            event.setUseInteractedBlock(Event.Result.DENY);

            //Check if the user can use the portal
            if (cannotAccessPortal(player, portal)) {
                return;
            }

            PermissionHelper.openPortal(player, portal);
            if (portal.getPortalOpener().isOpenFor(player) && !MaterialHelper.isContainer(block.getType())) {
                event.setUseInteractedBlock(Event.Result.ALLOW);
            }
        } else {
            //Display information about the portal if it has no sign
            ItemStack heldItem = player.getInventory().getItem(hand);
            if (heldItem != null && (heldItem.getType().isAir() || !heldItem.getType().isBlock())) {
                displayPortalInfo(block, player);
            }
        }
    }

    /**
     * Displays information about a clicked portal
     *
     * <p>This will only display portal info if the portal has no sign and is not silent.</p>
     *
     * @param block  <p>The clicked block</p>
     * @param player <p>The player that clicked the block</p>
     */
    private void displayPortalInfo(@NotNull Block block, @NotNull Player player) {
        Portal portal = PortalHandler.getByBlock(block);
        if (portal == null) {
            return;
        }

        //Display portal information as a portal without a sign does not display any
        if (portal.getOptions().hasNoSign() && (!portal.getOptions().isSilent() || player.isSneaking())) {
            MessageSender sender = Stargate.getMessageSender();
            sender.sendSuccessMessage(player, ChatColor.GOLD + Stargate.getString(Message.PORTAL_INFO_TITLE));
            sender.sendSuccessMessage(player, Stargate.replacePlaceholders(Stargate.getString(Message.PORTAL_INFO_NAME),
                    "%name%", portal.getName()));
            sender.sendSuccessMessage(player, Stargate.replacePlaceholders(Stargate.getString(Message.PORTAL_INFO_DESTINATION),
                    "%destination%", portal.getDestinationName()));
            if (portal.getOptions().isBungee()) {
                sender.sendSuccessMessage(player, Stargate.replacePlaceholders(Stargate.getString(Message.PORTAL_INFO_SERVER),
                        "%server%", portal.getNetwork()));
            } else {
                sender.sendSuccessMessage(player, Stargate.replacePlaceholders(Stargate.getString(Message.PORTAL_INFO_NETWORK),
                        "%network%", portal.getNetwork()));
            }
        }
    }

    /**
     * This function decides if a right click of a block is caused by a Spigot bug
     *
     * <p>The Spigot bug currently makes every right click of some blocks trigger twice, causing the portal to close
     * immediately, or causing portal information printing twice. This fix should detect the bug without breaking
     * clicking once the bug is fixed.</p>
     *
     * @param player <p>The player performing the right-click</p>
     * @return <p>True if the click is a bug and should be cancelled</p>
     */
    private boolean clickIsBug(@NotNull Player player) {
        Long previousEventTime = previousEventTimes.get(player);
        if (previousEventTime != null && previousEventTime + 50 > System.currentTimeMillis()) {
            previousEventTimes.put(player, null);
            return true;
        }
        previousEventTimes.put(player, System.currentTimeMillis());
        return false;
    }

}
