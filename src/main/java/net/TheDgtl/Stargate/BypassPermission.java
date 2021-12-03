package net.TheDgtl.Stargate;

import org.bukkit.entity.Player;

import java.util.EnumSet;

/**
 * This enum represents the different permissions used to bypass various protections
 */
public enum BypassPermission {

    /**
     * The ability to bypass the cost of using stargates
     */
    COST_USE("sg.admin.bypass.cost.use"),

    /**
     * The ability to bypass the cost of creating stargates
     */
    COST_CREATE("sg.admin.bypass.cost.create"),

    /**
     * The ability to bypass the cost of destroying stargates
     */
    COST_DESTROY("sg.admin.bypass.cost.destroy"),

    /**
     * The ability to bypass the protection of private stargates
     */
    PRIVATE("sg.admin.bypass.private"),

    /**
     * The ability to bypass the protection of hidden stargates
     */
    HIDDEN("sg.admin.bypass.hidden"),

    /**
     * The ability to bypass the max "gates per network" limit
     */
    GATE_LIMIT("sg.admin.bypass.gatelimit");


    private final String permissionString;

    /**
     * Instantiates a new protection bypass
     *
     * @param permissionString <p>The permission string used for this protection bypass</p>
     */
    BypassPermission(String permissionString) {
        this.permissionString = permissionString;
    }

    /**
     * Gets all protections the given player is allowed to bypass
     *
     * @param player <p>The player to check</p>
     * @return <p>All protection the player can bypass</p>
     */
    public EnumSet<BypassPermission> getProtectionBypasses(Player player) {
        //TODO: Use or remove this method
        EnumSet<BypassPermission> allowedBypassPermissions = EnumSet.noneOf(BypassPermission.class);
        for (BypassPermission bypassPermission : values()) {
            if (player.hasPermission(bypassPermission.permissionString)) {
                allowedBypassPermissions.add(bypassPermission);
            }
        }
        return allowedBypassPermissions;
    }

    /**
     * Gets the permission string for this protection bypass
     *
     * @return <p>The permission string for this bypass permission</p>
     */
    public String getPermissionString() {
        return permissionString;
    }
}
