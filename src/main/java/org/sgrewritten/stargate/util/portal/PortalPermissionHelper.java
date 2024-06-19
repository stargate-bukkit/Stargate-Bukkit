package org.sgrewritten.stargate.util.portal;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.permission.BypassPermission;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.portal.VirtualPortal;

import java.util.*;
import java.util.logging.Level;

/**
 * A permission helper for dealing with portal permissions
 */
public final class PortalPermissionHelper {

    private PortalPermissionHelper() {
        throw new IllegalStateException("Utility class");
    }

    private static final String SG_USE = "sg.use";
    private static final String USE_PERSONAL_OTHER = "sg.use.personal.other";

    /**
     * Generate access permissions
     *
     * @param portal <p> The portal to access </p>
     * @param actor  <p> The entity to get related permissions </p>
     * @return <p> A list with related permissions </p>
     */
    public static List<String> getAccessPermissions(RealPortal portal, Entity actor) {
        if (!(actor instanceof Player player)) {
            return new ArrayList<>();
        }
        List<String> permissionList = generateDefaultPortalPermissionList(portal, SG_USE);
        if (portal.hasFlag(StargateFlag.PRIVATE) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
            permissionList.add(BypassPermission.PRIVATE.getPermissionString());
        }
        if (portal.hasFlag(StargateFlag.FIXED)) {
            permissionList.add(SG_USE + ".type.fixed");
        }
        if (portal.hasFlag(StargateFlag.NETWORKED)) {
            permissionList.add(SG_USE + ".type.networked");
        }
        if (portal.getNetwork().getType() == NetworkType.PERSONAL) {
            UUID networkOwnerUUID = UUID.fromString(portal.getNetwork().getId());
            if (!networkOwnerUUID.equals(player.getUniqueId())) {
                permissionList.add(USE_PERSONAL_OTHER + "." + networkOwnerUUID.toString().toLowerCase(Locale.ROOT));
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
    public static List<String> getCreatePermissions(RealPortal portal, Entity actor) {
        if (!(actor instanceof Player)) {
            return new ArrayList<>();
        }
        String identifier = "sg.create";
        List<String> permList = generateDefaultPortalPermissionList(portal, identifier);
        if (portal.hasFlag(StargateFlag.PERSONAL_NETWORK) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
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
        if (portal.hasFlag(StargateFlag.PERSONAL_NETWORK) && !actor.getUniqueId().equals(portal.getOwnerUUID())) {
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
        if (!(actor instanceof Player player)) {
            return new ArrayList<>();
        }
        List<String> permList = generateDefaultPortalPermissionList(entrance, SG_USE);
        if (exit instanceof VirtualPortal virtualPortal) {
            String server = virtualPortal.getServer();
            permList.add(SG_USE + ".server." + server);
        }
        if (exit instanceof RealPortal realPortal) {
            permList.add(generateWorldPermission(realPortal, SG_USE));
        }

        if (entrance.getNetwork().getType() == NetworkType.PERSONAL) {
            UUID networkOwnerUUID = UUID.fromString(entrance.getNetwork().getId());
            if (!networkOwnerUUID.equals(player.getUniqueId())) {
                permList.add(USE_PERSONAL_OTHER + "." + networkOwnerUUID.toString().toLowerCase(Locale.ROOT));
            }
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
        String identifier = SG_USE;

        List<String> permList;
        if (entrance.hasFlag(StargateFlag.ALWAYS_ON)) {
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
            if (entrance.hasFlag(StargateFlag.PRIVATE) && !entrance.getOwnerUUID().equals(target.getUniqueId())) {
                Stargate.log(Level.FINER, " Adding a bypass constraint for private portals");
                permList.add(BypassPermission.PRIVATE.getPermissionString());
            }
            if (entrance.getNetwork().getType() == NetworkType.PERSONAL) {
                UUID networkOwnerUUID = UUID.fromString(entrance.getNetwork().getId());
                if (!networkOwnerUUID.equals(target.getUniqueId())) {
                    permList.add(USE_PERSONAL_OTHER + "." + networkOwnerUUID.toString().toLowerCase(Locale.ROOT));
                }
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
        Set<StargateFlag> flags = StargateFlag.parseFlags(portal.getAllFlagsString());
        for (StargateFlag flag : flags) {
            String identifier;
            if (flag.isInternalFlag()) {
                continue;
            }
            identifier = String.valueOf(flag.getCharacterRepresentation()).toLowerCase();
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
        if (portal.hasFlag(StargateFlag.PERSONAL_NETWORK)) {
            return permissionIdentifier + ".network.personal";
        }
        if (portal.getNetwork().getName().equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            return permissionIdentifier + ".network.default";
        }
        if (portal.getNetwork().getName().equals(ConfigurationHelper.getString(
                ConfigurationOption.LEGACY_BUNGEE_NETWORK))) {
            if (!portal.hasFlag(StargateFlag.LEGACY_INTERSERVER)) {
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
