package net.TheDgtl.Stargate.util.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * A permission helper for dealing with portal permissions
 */
public final class PortalPermissionHelper {

    private PortalPermissionHelper() {

    }

    /**
     * Generate access permissions
     *
     * @param portal <p> The portal to access </p>
     * @param actor  <p> The entity to get related permissions </p>
     * @return <p> A list with related permissions </p>
     */
    public static List<Permission> getAccessPermissions(RealPortal portal, Entity actor) {
        if (!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.use";
        List<Permission> permissionList = generateDefaultPortalPermissionList(portal, identifier, actor.getUniqueId().toString());
        if (portal.hasFlag(PortalFlag.PRIVATE) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
            permissionList.add(Bukkit.getPluginManager().getPermission("sg.admin.bypass.private"));
        }
        Permission baseTypePermission = Bukkit.getPluginManager().getPermission(identifier + ".type");
        if (baseTypePermission != null) {
            if (portal.hasFlag(PortalFlag.FIXED)) {
                Permission fixedPerm = new Permission(identifier + ".type.fixed");
                fixedPerm.addParent(baseTypePermission, true);
                permissionList.add(fixedPerm);
            }
            if (portal.hasFlag(PortalFlag.NETWORKED)) {
                Permission fixedPerm = new Permission(identifier + ".type.networked");
                fixedPerm.addParent(baseTypePermission, true);
                permissionList.add(fixedPerm);
            }
        }
        return permissionList;
    }

    /**
     * Generate create permissions
     *
     * @param portal <p> The portal to create </p>
     * @param actor  <p> The entity to get related permissions </p>
     * @return <p> A list with related permissions </p>
     */
    public static List<Permission> getCreatePermissions(RealPortal portal, Entity actor) {
        if (!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.create";
        List<Permission> permList = generateDefaultPortalPermissionList(portal, identifier, actor.getUniqueId().toString());
        if (portal.hasFlag(PortalFlag.PERSONAL_NETWORK) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
            permList.add(Bukkit.getPluginManager().getPermission("sg.admin.bypass.private"));
        }
        return permList;
    }

    /**
     * Generate destroy permissions
     *
     * @param portal <p> The portal to destroy </p>
     * @param actor  <p> The entity to get related permissions </p>
     * @return <p> A list with related permissions </p>
     */
    public static List<Permission> getDestroyPermissions(RealPortal portal, Entity actor) {
        if (!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.destroy";
        List<Permission> permList = generateDefaultPortalPermissionList(portal, identifier, actor.getUniqueId().toString());
        if (portal.hasFlag(PortalFlag.PERSONAL_NETWORK) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
            permList.add(Bukkit.getPluginManager().getPermission("sg.admin.bypass.private"));
        }
        return permList;
    }

    /**
     * Generate open permissions
     *
     * @param entrance <p> The portal to open </p>
     * @param exit     <p> The destination portal </p>
     * @param actor    <p> the entity to check permissions for </p>
     * @return <p> A list with related permissions </p>
     */
    public static List<Permission> getOpenPermissions(RealPortal entrance, Portal exit, Entity actor) {
        if (!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.use";
        List<Permission> permList = generateDefaultPortalPermissionList(entrance, identifier, actor.getUniqueId().toString());
        if (exit instanceof RealPortal) {
            permList.add(generateWorldPermission((RealPortal) exit, identifier));
        }
        return permList;
    }


    /**
     * Generate teleport permissions
     *
     * @param entrance <p> The portal to teleport from </p>
     * @param target   <p> The entity to check permissions on </p>
     * @return <p> A list with related permissions </p>
     */
    public static List<Permission> getTeleportPermissions(RealPortal entrance, Entity target) {
        String identifier = "sg.use";

        List<Permission> permList;
        if (entrance.hasFlag(PortalFlag.ALWAYS_ON)) {
            permList = generateDefaultPortalPermissionList(entrance, identifier, target.getUniqueId().toString());
        } else {
            permList = new ArrayList<>();
        }


        if (target instanceof Player) {
            Stargate.log(Level.FINER, " Generating teleport permissions for a player");
            if (!entrance.isOpenFor(target)) {
                Stargate.log(Level.FINER, " Adding the .follow constraint");
                permList.add(Bukkit.getPluginManager().getPermission(identifier + ".follow"));
            }
            if (entrance.hasFlag(PortalFlag.PRIVATE) && !entrance.getOwnerUUID().equals(target.getUniqueId())) {
                Stargate.log(Level.FINER, " Adding a bypass constraint for private portals");
                permList.add(Bukkit.getPluginManager().getPermission("sg.admin.bypass.private"));
            }
        }

        return permList;
    }

    /**
     * Compile all permissions that by default will be used related to the portal flags for any action
     *
     * @param portal               <p> The portal which location to check </p>
     * @param permissionIdentifier <p> The beginning of every permission node generated </p>
     * @return <p> A list with related permissions </p>
     */
    private static List<Permission> compileFlagPerms(Portal portal, String permissionIdentifier) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        List<Permission> permissionList = new ArrayList<>();
        Set<PortalFlag> flags = PortalFlag.parseFlags(portal.getAllFlagsString());
        for (PortalFlag flag : flags) {
            String identifier;
            switch (flag) {
                case FIXED:
                case NETWORKED:
                case PERSONAL_NETWORK:
                case IRON_DOOR:
                    continue;
                default:
                    identifier = String.valueOf(flag.getCharacterRepresentation()).toLowerCase();
                    break;
            }
            permissionList.add(pluginManager.getPermission(permissionIdentifier + ".type." + identifier));
        }
        return permissionList;
    }

    /**
     * Compile all permissions that by default will be used related to the portals network for any action
     *
     * @param portal               <p> The portal which location to check </p>
     * @param permissionIdentifier <p>The permission root node for this plugin</p>
     * @param activatorUUID        <p> The uuid of the acting entity </p>
     */
    private static Permission compileNetworkPerm(Portal portal, String permissionIdentifier, String activatorUUID) {
        //TODO: activatorUUID is never used
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (portal.hasFlag(PortalFlag.PERSONAL_NETWORK)) {
            return pluginManager.getPermission(permissionIdentifier + ".network.personal");
        }
        if (portal.getNetwork().getName().equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            return pluginManager.getPermission(permissionIdentifier + ".network.default");
        }
        return generateCustomNetworkPermission(permissionIdentifier, portal.getNetwork().getName());
    }

    /**
     * Generates the permission required for creating a custom network
     *
     * @param permissionRootNode <p>The root node (stargate/sg) of any generated permission</p>
     * @param networkName              <p>The name of the custom network</p>
     * @return <p>The permission required for creating the network</p>
     */
    public static Permission generateCustomNetworkPermission(String permissionRootNode, String networkName) {
        Permission custom = new Permission(permissionRootNode + ".network.custom." + networkName);
        Permission parent = Bukkit.getPluginManager().getPermission(permissionRootNode + ".network.custom");
        if (parent != null) {
            custom.addParent(parent, true);
            parent.recalculatePermissibles();
        }
        return custom;
    }

    /**
     * Generates the permission required for using the given portal's world
     *
     * @param portal               <p>The portal to generate the permission for</p>
     * @param permissionRootNode <p>The root node (stargate/sg) of any generated permission</p>
     * @return <p>The permission required for the given portal's world</p>
     */
    private static Permission generateWorldPermission(RealPortal portal, String permissionRootNode) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Permission parent = pluginManager.getPermission(permissionRootNode + ".world");
        World world = portal.getGate().getTopLeft().getWorld();
        if (world == null) {
            return null;
        }
        String permissionNode = permissionRootNode + ".world." + world.getName();
        Permission worldPermission = new Permission(permissionNode);
        if (parent != null) {
            worldPermission.addParent(parent, true);
            parent.recalculatePermissibles();
        }

        return worldPermission;
    }

    /**
     * Generates the permission required for using the given portal's design
     *
     * @param portal               <p>The portal which design to check</p>
     * @param permissionRootNode <p>The root node (stargate/sg) of any generated permission</p>
     * @return <p>The permission required for the given portal's design</p>
     */
    private static Permission generateDesignPermission(RealPortal portal, String permissionRootNode) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Permission parent = pluginManager.getPermission(permissionRootNode + ".design");
        String permissionNode = permissionRootNode + ".design." + portal.getGate().getFormat().getFileName();
        Permission design = new Permission(permissionNode);
        if (parent != null) {
            design.addParent(parent, true);
            parent.recalculatePermissibles();
        }
        return design;
    }

    /**
     * Compile the permissions that by default will be used for any action,
     * the only uniqueness depending on the action comes from the start of the string for each node
     *
     * @param portal         <p>The portal affected</p>
     * @param permissionRootNode <p>The beginning of every permission node generated</p>
     * @param activatorUUID  <p>UUID of the activator</p>
     * @return <p>A list with related permissions</p>
     */
    private static List<Permission> generateDefaultPortalPermissionList(RealPortal portal, String permissionRootNode, 
                                                                        String activatorUUID) {
        List<Permission> permissionList = new ArrayList<>(compileFlagPerms(portal, permissionRootNode));
        permissionList.add(generateWorldPermission(portal, permissionRootNode));
        permissionList.add(compileNetworkPerm(portal, permissionRootNode, activatorUUID));
        permissionList.add(generateDesignPermission(portal, permissionRootNode));
        return permissionList;
    }

}
