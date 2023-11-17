package org.sgrewritten.stargate.api.permission;

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
     * Gets the permission string for this protection bypass
     *
     * @return <p>The permission string for this bypass permission</p>
     */
    public String getPermissionString() {
        return permissionString;
    }

}
