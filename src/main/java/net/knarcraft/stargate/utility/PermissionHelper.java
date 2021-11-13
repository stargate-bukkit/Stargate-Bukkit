package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.event.StargateAccessEvent;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.property.PortalOption;
import net.knarcraft.stargate.portal.teleporter.PlayerTeleporter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import static net.knarcraft.stargate.Stargate.getMaxNameNetworkLength;

/**
 * Helper class for deciding which actions a player is allowed to perform
 */
public final class PermissionHelper {

    private PermissionHelper() {

    }

    /**
     * Opens a portal if the given player is allowed to, and if the portal is not already open
     *
     * @param player <p>The player opening the portal</p>
     * @param portal <p>The portal to open</p>
     */
    public static void openPortal(Player player, Portal portal) {
        Portal destination = portal.getPortalActivator().getDestination();

        //For an always open portal, no action is necessary
        if (portal.getOptions().isAlwaysOn()) {
            return;
        }

        //Destination is invalid or the same portal. Send an error message
        if (destination == null || destination == portal) {
            if (!portal.getOptions().isSilent()) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("invalidMsg"));
            }
            return;
        }

        //Portal is already open
        if (portal.isOpen()) {
            //Close the portal if this player opened the portal
            if (portal.getActivePlayer() == player) {
                portal.getPortalOpener().closePortal(false);
            }
            return;
        }

        //Deny access if another player has activated the portal, and it's still in use
        if (!portal.getOptions().isFixed() && portal.getPortalActivator().isActive() &&
                portal.getActivePlayer() != player) {
            if (!portal.getOptions().isSilent()) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("denyMsg"));
            }
            return;
        }

        //Check if the player can use the private gate
        if (portal.getOptions().isPrivate() && !PermissionHelper.canUsePrivatePortal(player, portal)) {
            if (!portal.getOptions().isSilent()) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("denyMsg"));
            }
            return;
        }

        //Destination is currently in use by another player, blocking teleportation
        if (destination.isOpen() && !destination.getOptions().isAlwaysOn()) {
            if (!portal.getOptions().isSilent()) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("blockMsg"));
            }
            return;
        }

        //Open the portal
        portal.getPortalOpener().openPortal(player, false);
    }

    /**
     * Creates a StargateAccessEvent and gets the updated deny value
     *
     * <p>The event is used for other plugins to bypass the permission checks.</p>
     *
     * @param player <p>The player trying to use the portal</p>
     * @param portal <p>The portal the player is trying to use</p>
     * @param deny   <p>Whether the player's access has already been denied by a previous check</p>
     * @return <p>False if the player should be allowed through the portal</p>
     */
    public static boolean portalAccessDenied(Player player, Portal portal, boolean deny) {
        StargateAccessEvent event = new StargateAccessEvent(player, portal, deny);
        Stargate.getInstance().getServer().getPluginManager().callEvent(event);
        return event.getDeny();
    }

    /**
     * Checks whether a given user cannot travel between two portals
     *
     * @param player         <p>The player to check</p>
     * @param entrancePortal <p>The portal the user wants to enter</p>
     * @param destination    <p>The portal the user wants to exit from</p>
     * @return <p>False if the user is allowed to access the portal</p>
     */
    public static boolean cannotAccessPortal(Player player, Portal entrancePortal, Portal destination) {
        boolean deny = false;

        if (entrancePortal.getOptions().isBungee()) {
            if (!PermissionHelper.canAccessServer(player, entrancePortal.getCleanNetwork())) {
                //If the portal is a bungee portal, and the player cannot access the server, deny
                Stargate.debug("cannotAccessPortal", "Cannot access server");
                deny = true;
            }
        } else if (PermissionHelper.cannotAccessNetwork(player, entrancePortal.getCleanNetwork())) {
            //If the player does not have access to the network, deny
            Stargate.debug("cannotAccessPortal", "Cannot access network");
            deny = true;
        } else if (PermissionHelper.cannotAccessWorld(player, destination.getWorld().getName())) {
            //If the player does not have access to the portal's world, deny
            Stargate.debug("cannotAccessPortal", "Cannot access world");
            deny = true;
        }
        //Allow other plugins to override whether the player can access the portal
        return portalAccessDenied(player, entrancePortal, deny);
    }

    /**
     * Checks whether a player has the given permission
     *
     * <p>This is the same as player.hasPermission(), but this function allows for printing permission debugging info.</p>
     *
     * @param player     <p>The player to check</p>
     * @param permission <p>The permission to check</p>
     * @return <p>True if the player has the permission</p>
     */
    public static boolean hasPermission(Player player, String permission) {
        if (Stargate.getStargateConfig().isPermissionDebuggingEnabled()) {
            Stargate.debug("hasPerm::Permission(" + player.getName() + ")", permission + " => " +
                    player.hasPermission(permission));
        }
        return player.hasPermission(permission);
    }

    /**
     * Check if a player has been given a permission implicitly
     *
     * <p>This should be run if a player has a parent permission to check for the child permission. It is assumed the
     * player has the child permission unless it's explicitly set to false.</p>
     *
     * @param player     <p>The player to check</p>
     * @param permission <p>The permission to check</p>
     * @return <p>True if the player has the permission implicitly or explicitly</p>
     */
    public static boolean hasPermissionImplicit(Player player, String permission) {
        if (!player.isPermissionSet(permission)) {
            if (Stargate.getStargateConfig().isPermissionDebuggingEnabled()) {
                Stargate.debug("hasPermissionImplicit::Permission", permission + " => implicitly true");
            }
            return true;
        }
        if (Stargate.getStargateConfig().isPermissionDebuggingEnabled()) {
            Stargate.debug("hasPermissionImplicit::Permission", permission + " => " +
                    player.hasPermission(permission));
        }
        return player.hasPermission(permission);
    }

    /**
     * Checks whether a player can access the given world
     *
     * @param player <p>The player trying to access the world</p>
     * @param world  <p>The world the player is trying to access</p>
     * @return <p>False if the player should be allowed to access the world</p>
     */
    public static boolean cannotAccessWorld(Player player, String world) {
        //The player can access all worlds
        if (hasPermission(player, "stargate.world")) {
            //Check if the world permission has been explicitly denied
            return !hasPermissionImplicit(player, "stargate.world." + world);
        }
        //The player can access the destination world
        return !hasPermission(player, "stargate.world." + world);
    }

    /**
     * Checks whether a player can access the given network
     *
     * @param player  <p>The player to check</p>
     * @param network <p>The network to check</p>
     * @return <p>True if the player is denied from accessing the network</p>
     */
    public static boolean cannotAccessNetwork(Player player, String network) {
        //The player can access all networks
        if (hasPermission(player, "stargate.network")) {
            //Check if the world permission has been explicitly denied
            return !hasPermissionImplicit(player, "stargate.network." + network);
        }
        //Check if the player can access this network
        if (hasPermission(player, "stargate.network." + network)) {
            return false;
        }
        //Is able to create personal gates (Assumption is made they can also access them)
        String playerName = player.getName();
        if (playerName.length() > getMaxNameNetworkLength()) {
            playerName = playerName.substring(0, getMaxNameNetworkLength());
        }
        return !network.equals(playerName) || !hasPermission(player, "stargate.create.personal");
    }

    /**
     * Checks whether a player can access the given bungee server
     *
     * @param player <p>The player trying to teleport</p>
     * @param server <p>The server the player is trying to connect to</p>
     * @return <p>True if the player is allowed to access the given server</p>
     */
    public static boolean canAccessServer(Player player, String server) {
        //The player can access all servers
        if (hasPermission(player, "stargate.server")) {
            //Check if the server permission has been explicitly denied
            return hasPermissionImplicit(player, "stargate.server." + server);
        }
        //The player can access the destination server
        return hasPermission(player, "stargate.server." + server);
    }

    /**
     * Checks whether the given player can teleport the given stretch for free
     *
     * @param player <p>The player trying to teleport</p>
     * @param src    <p>The portal the player is entering</p>
     * @param dest   <p>The portal the player wants to teleport to</p>
     * @return <p>True if the player can travel for free</p>
     */
    public static boolean isFree(Player player, Portal src, Portal dest) {
        //This portal is free
        if (src.getOptions().isFree()) {
            return true;
        }
        //Player can use this portal for free
        if (hasPermission(player, "stargate.free.use")) {
            return true;
        }
        //Don't charge for free destinations unless specified in the config
        return dest != null && !Stargate.getEconomyConfig().chargeFreeDestination() && dest.getOptions().isFree();
    }

    /**
     * Checks whether the player can see this gate (Hidden property check)
     *
     * <p>This decides if the player can see the gate on the network selection screen</p>
     *
     * @param player <p>The player to check</p>
     * @param portal <p>The portal to check</p>
     * @return <p>True if the given player can see the given portal</p>
     */
    public static boolean canSeePortal(Player player, Portal portal) {
        //The portal is not hidden
        if (!portal.getOptions().isHidden()) {
            return true;
        }
        //The player can see all hidden portals
        if (hasPermission(player, "stargate.admin.hidden")) {
            return true;
        }
        //The player is the owner of the portal
        return portal.isOwner(player);
    }

    /**
     * Checks if the given player is allowed to use the given private portal
     *
     * @param player <p>The player trying to use the portal</p>
     * @param portal <p>The private portal used</p>
     * @return <p>True if the player is allowed to use the portal</p>
     */
    public static boolean canUsePrivatePortal(Player player, Portal portal) {
        //Check if the player is the owner of the gate
        if (portal.isOwner(player)) {
            return true;
        }
        //The player is an admin with the ability to use private gates
        return hasPermission(player, "stargate.admin.private");
    }

    /**
     * Checks if the given player has access to the given portal option
     *
     * @param player <p>The player trying to use the option</p>
     * @param option <p>The option the player is trying to use</p>
     * @return <p>True if the player is allowed to create a portal with the given option</p>
     */
    public static boolean canUseOption(Player player, PortalOption option) {
        return hasPermission(player, option.getPermissionString());
    }

    /**
     * Checks if the given player is allowed to create gates on the given network
     *
     * @param player  <p>The player trying to create a new gate</p>
     * @param network <p>The network the player is trying to create a gate on</p>
     * @return <p>True if the player is allowed to create the new gate</p>
     */
    public static boolean canCreateNetworkGate(Player player, String network) {
        //Check if the player is allowed to create a portal on any network
        if (hasPermission(player, "stargate.create.network")) {
            //Check if the network has been explicitly denied
            return hasPermissionImplicit(player, "stargate.create.network." + network);
        }
        //Check if the player is allowed to create on this specific network
        return hasPermission(player, "stargate.create.network." + network);
    }

    /**
     * Checks whether the given player is allowed to create a personal gate
     *
     * @param player <p>The player trying to create the new gate</p>
     * @return <p>True if the player is allowed</p>
     */
    public static boolean canCreatePersonalPortal(Player player) {
        return hasPermission(player, "stargate.create.personal");
    }

    /**
     * Checks if the given player can create a portal with the given gate layout
     *
     * @param player <p>The player trying to create a portal</p>
     * @param gate   <p>The gate type of the new portal</p>
     * @return <p>True if the player is allowed to create a portal with the given gate layout</p>
     */
    public static boolean canCreatePortal(Player player, String gate) {
        //Check if the player is allowed to create all gates
        if (hasPermission(player, "stargate.create.gate")) {
            //Check if the gate type has been explicitly denied
            return hasPermissionImplicit(player, "stargate.create.gate." + gate);
        }
        //Check if the player can create the specific gate type
        return hasPermission(player, "stargate.create.gate." + gate);
    }

    /**
     * Checks if the given player can destroy the given portal
     *
     * @param player <p>The player trying to destroy the portal</p>
     * @param portal <p>The portal to destroy</p>
     * @return <p>True if the player is allowed to destroy the portal</p>
     */
    public static boolean canDestroyPortal(Player player, Portal portal) {
        String network = portal.getCleanNetwork();

        //Use a special check for bungee portals
        if (portal.getOptions().isBungee()) {
            return hasPermission(player, "stargate.admin.bungee");
        }

        //Check if the player is allowed to destroy on all networks
        if (hasPermission(player, "stargate.destroy.network")) {
            //Check if the network has been explicitly denied
            return hasPermissionImplicit(player, "stargate.destroy.network." + network);
        }
        //Check if the player is allowed to destroy on the network
        if (hasPermission(player, "stargate.destroy.network." + network)) {
            return true;
        }
        //Check if personal portal and if the player is allowed to destroy it
        return portal.isOwner(player) && hasPermission(player, "stargate.destroy.personal");
    }

    /**
     * Decide of the player can teleport through a portal
     *
     * @param entrancePortal <p>The portal the player is entering from</p>
     * @param destination    <p>The destination of the portal the player is inside</p>
     * @param player         <p>The player wanting to teleport</p>
     * @param event          <p>The move event causing the teleportation</p>
     * @return <p>True if the player cannot teleport. False otherwise</p>
     */
    public static boolean playerCannotTeleport(Portal entrancePortal, Portal destination, Player player, PlayerMoveEvent event) {
        //No portal or not open
        if (entrancePortal == null || !entrancePortal.isOpen()) {
            return true;
        }

        //Not open for this player
        if (!entrancePortal.getPortalOpener().isOpenFor(player)) {
            if (!entrancePortal.getOptions().isSilent()) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("denyMsg"));
            }
            new PlayerTeleporter(entrancePortal, player).teleport(entrancePortal, event);
            return true;
        }

        //No destination
        if (!entrancePortal.getOptions().isBungee() && destination == null) {
            return true;
        }

        //Player cannot access portal
        if (PermissionHelper.cannotAccessPortal(player, entrancePortal, destination)) {
            if (!entrancePortal.getOptions().isSilent()) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("denyMsg"));
            }
            new PlayerTeleporter(entrancePortal, player).teleport(entrancePortal, event);
            entrancePortal.getPortalOpener().closePortal(false);
            return true;
        }

        //Player cannot pay for teleportation
        int cost = Stargate.getEconomyConfig().getUseCost(player, entrancePortal, destination);
        if (cost > 0) {
            return EconomyHelper.cannotPayTeleportFee(entrancePortal, player, cost);
        }
        return false;
    }

}
