package net.TheDgtl.Stargate.util.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.property.BypassPermission;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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
    public static List<String> getAccessPermissions(RealPortal portal, Entity actor) {
        if (!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.use";
        List<String> permissionList = generateDefaultPortalPermissionList(portal, identifier);
        if (portal.hasFlag(PortalFlag.PRIVATE) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
            permissionList.add(BypassPermission.PRIVATE.getPermissionString());
        }
        if (portal.hasFlag(PortalFlag.FIXED)) {
            permissionList.add(identifier + ".type.fixed");
        }
        if (portal.hasFlag(PortalFlag.NETWORKED)) {
            permissionList.add(identifier + ".type.networked");
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
    public static List<String> getCreatePermissions(RealPortal portal, Entity actor) {
        if (!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.create";
        List<String> permList = generateDefaultPortalPermissionList(portal, identifier);
        if (portal.hasFlag(PortalFlag.PERSONAL_NETWORK) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
            permList.add(BypassPermission.PRIVATE.getPermissionString());
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
    public static List<String> getDestroyPermissions(RealPortal portal, Entity actor) {
        if (!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.destroy";
        List<String> permList = generateDefaultPortalPermissionList(portal, identifier);
        if (portal.hasFlag(PortalFlag.PERSONAL_NETWORK) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
            permList.add(BypassPermission.PRIVATE.getPermissionString());
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
    public static List<String> getOpenPermissions(RealPortal entrance, Portal exit, Entity actor) {
        if (!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.use";
        List<String> permList = generateDefaultPortalPermissionList(entrance, identifier);
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
    public static List<String> getTeleportPermissions(RealPortal entrance, Entity target) {
        String identifier = "sg.use";

        List<String> permList;
        if (entrance.hasFlag(PortalFlag.ALWAYS_ON)) {
            permList = generateDefaultPortalPermissionList(entrance, identifier);
        } else {
            permList = new ArrayList<>();
        }

        if (target instanceof Player) {
            Stargate.log(Level.FINER, " Generating teleport permissions for a player");
            if (!entrance.isOpenFor(target)) {
                Stargate.log(Level.FINER, " Adding the .follow constraint");
                permList.add(identifier + ".follow");
            }
            if (entrance.hasFlag(PortalFlag.PRIVATE) && !entrance.getOwnerUUID().equals(target.getUniqueId())) {
                Stargate.log(Level.FINER, " Adding a bypass constraint for private portals");
                permList.add(BypassPermission.PRIVATE.getPermissionString());
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
    private static List<String> compileFlagPerms(Portal portal, String permissionIdentifier) {
        List<String> permissionList = new ArrayList<>();
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
            permissionList.add(permissionIdentifier + ".type." + identifier);
        }
        return permissionList;
    }

    /**
     * Compile all permissions that by default will be used related to the portals network for any action
     *
     * @param portal               <p> The portal which location to check </p>
     * @param permissionIdentifier <p>The permission root node for this plugin</p>
     */
    private static String generateNetworkPermission(Portal portal, String permissionIdentifier) {
        if (portal.hasFlag(PortalFlag.PERSONAL_NETWORK)) {
            return permissionIdentifier + ".network.personal";
        }
        if (portal.getNetwork().getName().equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            return permissionIdentifier + ".network.default";
        }
        if (portal.getNetwork().getName().equals(ConfigurationHelper.getString(
                ConfigurationOption.LEGACY_BUNGEE_NETWORK))) {
            if (!portal.hasFlag(PortalFlag.BUNGEE)) {
                //A creation of a non-bungee portal on the legacy bungee network should never be allowed
                return "r5j4k2l4l7o9l7.j5k6k6k3kf03kv";
            } else {
                return null;
            }
        }
        return generateCustomNetworkPermission(permissionIdentifier, portal.getNetwork().getName());
    }

    /**
     * Generates the permission required for creating a custom network
     *
     * @param permissionRootNode <p>The root node (stargate/sg) of any generated permission</p>
     * @param networkName        <p>The name of the custom network</p>
     * @return <p>The permission required for creating the network</p>
     */
    public static String generateCustomNetworkPermission(String permissionRootNode, String networkName) {
        return permissionRootNode + ".network.custom." + networkName;
    }

    /**
     * Generates the permission required for using the given portal's world
     *
     * @param portal             <p>The portal to generate the permission for</p>
     * @param permissionRootNode <p>The root node (stargate/sg) of any generated permission</p>
     * @return <p>The permission required for the given portal's world</p>
     */
    private static String generateWorldPermission(RealPortal portal, String permissionRootNode) {
        World world = portal.getGate().getTopLeft().getWorld();
        if (world == null) {
            return null;
        }
        return permissionRootNode + ".world." + world.getName();
    }

    /**
     * Generates the permission required for using the given portal's design
     *
     * @param portal             <p>The portal which design to check</p>
     * @param permissionRootNode <p>The root node (stargate/sg) of any generated permission</p>
     * @return <p>The permission required for the given portal's design</p>
     */
    private static String generateDesignPermission(RealPortal portal, String permissionRootNode) {
        String designFileName = portal.getGate().getFormat().getFileName();
        return permissionRootNode + ".design." + designFileName.subSequence(0, designFileName.length() - 5); // remove .gate thing
    }

    /**
     * Compile the permissions that by default will be used for any action,
     * the only uniqueness depending on the action comes from the start of the string for each node
     *
     * @param portal             <p>The portal affected</p>
     * @param permissionRootNode <p>The beginning of every permission node generated</p>
     * @return <p>A list with related permissions</p>
     */
    private static List<String> generateDefaultPortalPermissionList(RealPortal portal, String permissionRootNode) {
        List<String> permissionList = new ArrayList<>(compileFlagPerms(portal, permissionRootNode));
        permissionList.add(generateWorldPermission(portal, permissionRootNode));
        permissionList.add(generateNetworkPermission(portal, permissionRootNode));
        permissionList.add(generateDesignPermission(portal, permissionRootNode));
        return permissionList;
    }

}
