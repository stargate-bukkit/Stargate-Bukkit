package net.TheDgtl.Stargate.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;

public class PortalPermissionHelper {
    /**
     * Compile all permissions that by default will be used related to the portal flags for any action
     * @param portal                <p> The portal which location to check </p>
     * @param permissionIdentifier  <p> The beginning of every permission node generated </p>
     * @return
     */
    static private List<Permission> compileFlagPerms(Portal portal, String permIdentifier) {
        PluginManager pm = Bukkit.getPluginManager();
        List<Permission> permList = new ArrayList<>();
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
            permList.add(pm.getPermission(permIdentifier + ".type." + identifier));
        }
        return permList;
    }
    
    /**
     * Compile all permissions that by default will be used related to the portals network for any action
     * @param portal                <p> The portal which location to check </p>
     * @param permissionIdentifier  <p> The beginning of every permission node generated </p>
     * @param activatorUUID             <p> The uuid of the acting entity </p>
     * @return
     */
    static private Permission compileNetworkPerm(Portal portal, String permIdentifier, String activatorUUID) {
        PluginManager pm = Bukkit.getPluginManager();
        if (portal.hasFlag(PortalFlag.PERSONAL_NETWORK)) {
            return pm.getPermission(permIdentifier + ".network.personal");
        }
        if (portal.getNetwork().getName().equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            return pm.getPermission(permIdentifier + ".network.default");
        }
        Permission custom = new Permission(permIdentifier + ".network.custom." + portal.getNetwork().getName());
        Permission parent = pm.getPermission(permIdentifier + ".network.custom");
        if (parent != null) {
            custom.addParent(parent, true);
        }
        return custom;
    }
    
    /**
     * Compile all permissions that by default will be used related to the portals position in the world for any action
     * @param portal                <p> The portal which location to check </p>
     * @param permissionIdentifier  <p> The beginning of every permission node generated </p>
     * @return
     */
    static private Permission compileWorldPerm(RealPortal portal,String permissionIdentifier) {
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
        }
        return worldPermission;
    }
    
    
    /**
     * Compile all permissions that by default will be used related to gate designs for any action
     * @param portal        <p> The portal which design to check </p>
     * @param permIdentifier    <p> The beginning of every permission node generated </p>
     * @return
     */
    static private Permission compileDesignPerm(RealPortal portal, String permIdentifier) {
        PluginManager pm = Bukkit.getPluginManager();
        Permission parent = pm.getPermission(permIdentifier + ".design");
        String permNode = permIdentifier + ".design." + portal.getGate().getFormat().getFileName();
        Permission design = new Permission(permNode);
        if (parent != null) {
            design.addParent(parent, true);
        }
        return design;
    }
    
    /**
     * Compile the permissions that by default will be used for any action,
     *  the only uniqueness depending on the action comes from the start of the string for each node
     * @param portal        <p> The portal affected </p>
     * @param permIdentifier    <p> The beginning of every permission node generated </p>
     * @param activatorUUID     <p> UUID of the activator </p>
     * @return
     */
    static private List<Permission> defaultPortalPermCompile(RealPortal portal, String permIdentifier, String activatorUUID) {
        List<Permission> permList = new ArrayList<>(compileFlagPerms(portal,permIdentifier));
        permList.add(compileWorldPerm(portal,permIdentifier));
        permList.add(compileNetworkPerm(portal,permIdentifier, activatorUUID));
        permList.add(compileDesignPerm(portal,permIdentifier));
        return permList;
    }
    
    static public List<Permission> getAccessPermissions(RealPortal portal, Entity actor){
        if(!(actor instanceof Player)) {
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
                Permission fixedPerm = new Permission(identifier + ".type.non-fixed");
                fixedPerm.addParent(baseTypePermission, true);
                permsList.add(fixedPerm);
            }
        }
        return permsList;
    }
    
    static public List<Permission> getCreatePermissions(RealPortal portal, Entity actor){
        if(!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.create";
        List<Permission> permList = defaultPortalPermCompile(portal, identifier, actor.getUniqueId().toString());
        if (portal.hasFlag(PortalFlag.PERSONAL_NETWORK) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
            permList.add(Bukkit.getPluginManager().getPermission("sg.admin.bypass.private"));
        }
        return permList;
    }
    
    static public List<Permission> getDestroyPermissions(RealPortal portal, Entity actor){
        if(!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.destroy";
        List<Permission> permList = defaultPortalPermCompile(portal, identifier, actor.getUniqueId().toString());
        if (portal.hasFlag(PortalFlag.PERSONAL_NETWORK) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
            permList.add(Bukkit.getPluginManager().getPermission("sg.admin.bypass.private"));
        }
        return permList;
    }
    
    static public List<Permission> getOpenPermissions(RealPortal entrance, Portal exit, Entity actor) {
        if(!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.use";
        List<Permission> permList = defaultPortalPermCompile(entrance, identifier, actor.getUniqueId().toString());
        if (exit instanceof RealPortal) {
            permList.add(compileWorldPerm((RealPortal) exit, identifier));
        }
        return permList;
    }
    
    static public List<Permission> getTeleportPermissions(RealPortal entrance, Entity target){
        String identifier = "sg.use";
        List<Permission> permList = new ArrayList<>();
        if (target instanceof Player) {
            if (!entrance.isOpenFor(target)) {
                permList.add(Bukkit.getPluginManager().getPermission(identifier + ".follow"));
            }
            if (entrance.hasFlag(PortalFlag.PRIVATE) && !entrance.getOwnerUUID().equals(target.getUniqueId())) {
                permList.add(Bukkit.getPluginManager().getPermission("sg.admin.bypass.private"));
            }
        }

        return permList;
    }
}
