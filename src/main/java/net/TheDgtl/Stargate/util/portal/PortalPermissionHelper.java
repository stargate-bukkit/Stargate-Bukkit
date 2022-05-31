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
        List<Permission> permsList = defaultPortalPermCompile(portal, identifier, actor.getUniqueId().toString());
        if (portal.hasFlag(PortalFlag.PRIVATE) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
            permsList.add(Bukkit.getPluginManager().getPermission("sg.admin.bypass.private"));
        }
        Permission baseTypePermission = Bukkit.getPluginManager().getPermission(identifier + ".type");
        if (baseTypePermission != null) {
            if (portal.hasFlag(PortalFlag.FIXED)) {
                Permission fixedPerm = new Permission(identifier + ".type.fixed");
                fixedPerm.addParent(baseTypePermission, true);
                permsList.add(fixedPerm);
            }
            if (portal.hasFlag(PortalFlag.NETWORKED)) {
                Permission fixedPerm = new Permission(identifier + ".type.networked");
                fixedPerm.addParent(baseTypePermission, true);
                permsList.add(fixedPerm);
            }
        }
        return permsList;
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
        List<Permission> permList = defaultPortalPermCompile(portal, identifier, actor.getUniqueId().toString());
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
        List<Permission> permList = defaultPortalPermCompile(portal, identifier, actor.getUniqueId().toString());
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
        List<Permission> permList = defaultPortalPermCompile(entrance, identifier, actor.getUniqueId().toString());
        if (exit instanceof RealPortal) {
            permList.add(compileWorldPerm((RealPortal) exit, identifier));
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
            permList = defaultPortalPermCompile(entrance, identifier, target.getUniqueId().toString());
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
        return compileCustomNetworkPerm(permissionIdentifier, portal.getNetwork().getName());
    }

    /**
     * Compile the permission required for creating a custom network
     *
     * @param permissionIdentifier <p> The start of the permission, for example 'sg.create' </p>
     * @param netName              <p> The name of the custom network </p>
     * @return <p> The permission required for creating the network </p>
     */
    public static Permission compileCustomNetworkPerm(String permissionIdentifier, String netName) {
        Permission custom = new Permission(permissionIdentifier + ".network.custom." + netName);
        Permission parent = Bukkit.getPluginManager().getPermission(permissionIdentifier + ".network.custom");
        if (parent != null) {
            custom.addParent(parent, true);
            parent.recalculatePermissibles();
        }
        return custom;
    }

    /**
     * Compile all permissions that by default will be used related to the portals position in the world for any action
     *
     * @param portal               <p> The portal which location to check </p>
     * @param permissionIdentifier <p> The beginning of every permission node generated </p>
     */
    private static Permission compileWorldPerm(RealPortal portal, String permissionIdentifier) {
        PluginManager pm = Bukkit.getPluginManager();
        Permission parent = pm.getPermission(permissionIdentifier + ".world");
        World world = portal.getGate().getTopLeft().getWorld();
        if (world == null) {
            return null;
        }
        String permissionNode = permissionIdentifier + ".world." + world.getName();
        Permission worldPermission = new Permission(permissionNode);
        if (parent != null) {
            worldPermission.addParent(parent, true);
            parent.recalculatePermissibles();
        }

        return worldPermission;
    }


    /**
     * Compile all permissions that by default will be used related to gate designs for any action
     *
     * @param portal               <p> The portal which design to check </p>
     * @param permissionIdentifier <p> The beginning of every permission node generated </p>
     */
    private static Permission compileDesignPerm(RealPortal portal, String permissionIdentifier) {
        PluginManager pm = Bukkit.getPluginManager();
        Permission parent = pm.getPermission(permissionIdentifier + ".design");
        String permNode = permissionIdentifier + ".design." + portal.getGate().getFormat().getFileName();
        Permission design = new Permission(permNode);
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
     * @param portal         <p> The portal affected </p>
     * @param permIdentifier <p> The beginning of every permission node generated </p>
     * @param activatorUUID  <p> UUID of the activator </p>
     * @return <p> A list with related permissions </p>
     */
    private static List<Permission> defaultPortalPermCompile(RealPortal portal, String permIdentifier, String activatorUUID) {
        List<Permission> permList = new ArrayList<>(compileFlagPerms(portal, permIdentifier));
        permList.add(compileWorldPerm(portal, permIdentifier));
        permList.add(compileNetworkPerm(portal, permIdentifier, activatorUUID));
        permList.add(compileDesignPerm(portal, permIdentifier));
        return permList;
    }

}
