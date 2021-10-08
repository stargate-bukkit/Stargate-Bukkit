package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.event.StargateAccessEvent;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalOption;
import org.bukkit.entity.Player;

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
        Portal destination = portal.getDestination();

        //Always-open gate -- Do nothing
        if (portal.getOptions().isAlwaysOn()) {
            return;
        }

        //Random gate -- Do nothing
        if (portal.getOptions().isRandom()) {
            return;
        }

        //Invalid destination
        if ((destination == null) || (destination == portal)) {
            Stargate.sendErrorMessage(player, Stargate.getString("invalidMsg"));
            return;
        }

        //Gate is already open
        if (portal.isOpen()) {
            // Close if this player opened the gate
            if (portal.getActivePlayer() == player) {
                portal.close(false);
            }
            return;
        }

        //Gate that someone else is using -- Deny access
        if ((!portal.getOptions().isFixed()) && portal.isActive() && (portal.getActivePlayer() != player)) {
            Stargate.sendErrorMessage(player, Stargate.getString("denyMsg"));
            return;
        }

        //Check if the player can use the private gate
        if (portal.getOptions().isPrivate() && !PermissionHelper.canPrivate(player, portal)) {
            Stargate.sendErrorMessage(player, Stargate.getString("denyMsg"));
            return;
        }

        //Destination blocked
        if ((destination.isOpen()) && (!destination.getOptions().isAlwaysOn())) {
            Stargate.sendErrorMessage(player, Stargate.getString("blockMsg"));
            return;
        }

        //Open gate
        portal.open(player, false);
    }

    /**
     * Creates a StargateAccessPortal and gives the result
     *
     * <p>The event is used for other plugins to bypass the permission checks</p>
     *
     * @param player <p>The player trying to use the portal</p>
     * @param portal <p>The portal the player is trying to use</p>
     * @param deny   <p>Whether the player's access has already been denied by a check</p>
     * @return <p>False if the player should be allowed through the portal</p>
     */
    public static boolean cannotAccessPortal(Player player, Portal portal, boolean deny) {
        StargateAccessEvent event = new StargateAccessEvent(player, portal, deny);
        Stargate.server.getPluginManager().callEvent(event);
        return event.getDeny();
    }

    /**
     * Checks whether a given user cannot travel between two portals
     *
     * @param player         <p>The player to check</p>
     * @param entrancePortal <p>The portal the user wants to enter</p>
     * @param destination    <p>The portal the user wants to exit</p>
     * @return <p>False if the user is allowed to access the portal</p>
     */
    public static boolean cannotAccessPortal(Player player, Portal entrancePortal, Portal destination) {
        boolean deny = false;
        // Check if player has access to this server for Bungee gates
        if (entrancePortal.getOptions().isBungee() && !PermissionHelper.canAccessServer(player, entrancePortal.getNetwork())) {
            Stargate.debug("cannotAccessPortal", "Cannot access server");
            deny = true;
        } else if (PermissionHelper.cannotAccessNetwork(player, entrancePortal.getNetwork())) {
            Stargate.debug("cannotAccessPortal", "Cannot access network");
            deny = true;
        } else if (!entrancePortal.getOptions().isBungee() && PermissionHelper.cannotAccessWorld(player, destination.getWorld().getName())) {
            Stargate.debug("cannotAccessPortal", "Cannot access world");
            deny = true;
        }
        return cannotAccessPortal(player, entrancePortal, deny);
    }

    /**
     * Checks whether a player has the given permission
     *
     * <p>This is the same as player.hasPermission(), but this function allows for printing permission debugging info.</p>
     *
     * @param player <p>The player to check</p>
     * @param perm   <p>The permission to check</p>
     * @return <p>True if the player has the permission</p>
     */
    public static boolean hasPermission(Player player, String perm) {
        if (Stargate.permissionDebuggingEnabled) {
            Stargate.debug("hasPerm::SuperPerm(" + player.getName() + ")", perm + " => " + player.hasPermission(perm));
        }
        return player.hasPermission(perm);
    }

    /**
     * Check a deep permission, this will check to see if the permissions is defined for this use
     *
     * <p>If using Permissions it will return the same as hasPerm. If using SuperPerms will return true if the node
     * isn't defined, or the value of the node if it is</p>
     *
     * @param player     <p>The player to check</p>
     * @param permission <p>The permission to check</p>
     * @return <p>True if the player has the permission or it is not set</p>
     */
    public static boolean hasPermDeep(Player player, String permission) {
        if (!player.isPermissionSet(permission)) {
            if (Stargate.permissionDebuggingEnabled) {
                Stargate.debug("hasPermDeep::SuperPerm", permission + " => true");
            }
            return true;
        }
        if (Stargate.permissionDebuggingEnabled) {
            Stargate.debug("hasPermDeep::SuperPerms", permission + " => " + player.hasPermission(permission));
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
        // Can use all stargate player features or access all worlds
        if (hasPermission(player, "stargate.use") || hasPermission(player, "stargate.world")) {
            // Do a deep check to see if the player lacks this specific world node
            return !hasPermDeep(player, "stargate.world." + world);
        }
        // Can access dest world
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
        // Can user all stargate player features, or access all networks
        if (hasPermission(player, "stargate.use") || hasPermission(player, "stargate.network")) {
            // Do a deep check to see if the player lacks this specific network node
            return !hasPermDeep(player, "stargate.network." + network);
        }
        //Check if the player can access this network
        if (hasPermission(player, "stargate.network." + network)) {
            return false;
        }
        //Is able to create personal gates (Assumption is made they can also access them)
        String playerName = player.getName();
        if (playerName.length() > 11) {
            playerName = playerName.substring(0, 11);
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
        //Can user all stargate player features, or access all servers
        if (hasPermission(player, "stargate.use") || hasPermission(player, "stargate.servers")) {
            //Do a deep check to see if the player lacks this specific server node
            return hasPermDeep(player, "stargate.server." + server);
        }
        //Can access this server
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
        // This gate is free
        if (src.getOptions().isFree()) {
            return true;
        }
        // Player gets free use
        if (hasPermission(player, "stargate.free") || hasPermission(player, "stargate.free.use")) {
            return true;
        }
        // Don't charge for free destination gates
        return dest != null && !EconomyHandler.chargeFreeDestination && dest.getOptions().isFree();
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
        // The gate is not hidden
        if (!portal.getOptions().isHidden()) {
            return true;
        }
        // The player is an admin with the ability to see hidden gates
        if (hasPermission(player, "stargate.admin") || hasPermission(player, "stargate.admin.hidden")) {
            return true;
        }
        // The player is the owner of the gate
        return portal.isOwner(player);
    }

    /**
     * Checks if the given player is allowed to use the given private portal
     *
     * @param player <p>The player trying to use the portal</p>
     * @param portal <p>The private portal used</p>
     * @return <p>True if the player is allowed to use the portal</p>
     */
    public static boolean canPrivate(Player player, Portal portal) {
        //Check if the player is the owner of the gate
        if (portal.isOwner(player)) {
            return true;
        }
        //The player is an admin with the ability to use private gates
        return hasPermission(player, "stargate.admin") || hasPermission(player, "stargate.admin.private");
    }

    /**
     * Checks if the given player has access to the given portal option
     *
     * @param player <p>The player trying to use the option</p>
     * @param option <p>The option the player is trying to use</p>
     * @return <p>True if the player is allowed to create a portal with the given option</p>
     */
    public static boolean canUseOption(Player player, PortalOption option) {
        //Check if the player can use all options
        if (hasPermission(player, "stargate.option") || option == PortalOption.BUNGEE) {
            return true;
        }
        //Check if they can use this specific option
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
        //Check for general create
        if (hasPermission(player, "stargate.create")) {
            return true;
        }
        //Check for all network create permission
        if (hasPermission(player, "stargate.create.network")) {
            // Do a deep check to see if the player lacks this specific network node
            return hasPermDeep(player, "stargate.create.network." + network);
        }
        //Check for this specific network
        return hasPermission(player, "stargate.create.network." + network);

    }

    /**
     * Checks whether the given player is allowed to create a personal gate
     *
     * @param player <p>The player trying to create the new gate</p>
     * @return <p>True if the player is allowed</p>
     */
    public static boolean canCreatePersonalGate(Player player) {
        //Check for general create
        if (hasPermission(player, "stargate.create")) {
            return true;
        }
        //Check for personal
        return hasPermission(player, "stargate.create.personal");
    }

    /**
     * Checks if the given player can create a portal with the given gate layout
     *
     * @param player <p>The player trying to create a portal</p>
     * @param gate   <p>The gate type of the new portal</p>
     * @return <p>True if the player is allowed to create a portal with the given gate layout</p>
     */
    public static boolean canCreateGate(Player player, String gate) {
        //Check for general create
        if (hasPermission(player, "stargate.create")) {
            return true;
        }
        //Check for all gate create permissions
        if (hasPermission(player, "stargate.create.gate")) {
            // Do a deep check to see if the player lacks this specific gate node
            return hasPermDeep(player, "stargate.create.gate." + gate);
        }
        //Check for this specific gate
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
        String network = portal.getNetwork();
        //Check for general destroy
        if (hasPermission(player, "stargate.destroy")) {
            return true;
        }
        //Check for all network destroy permission
        if (hasPermission(player, "stargate.destroy.network")) {
            //Do a deep check to see if the player lacks permission for this network node
            return hasPermDeep(player, "stargate.destroy.network." + network);
        }
        //Check for this specific network
        if (hasPermission(player, "stargate.destroy.network." + network)) {
            return true;
        }
        //Check for personal gate
        return portal.isOwner(player) && hasPermission(player, "stargate.destroy.personal");
    }

}
