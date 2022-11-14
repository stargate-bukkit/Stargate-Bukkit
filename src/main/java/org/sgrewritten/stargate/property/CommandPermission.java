package org.sgrewritten.stargate.property;

/**
 * A representation of the permissions required for executing commands
 */
public enum CommandPermission {

    /**
     * The permission necessary for executing the reload command
     */
    RELOAD("sg.admin.reload"),

    /**
     * The permission necessary for executing the about command
     */
    ABOUT("sg.info.help"),

    /**
     * The permission necessary for executing the trace command
     */
    TRACE("sg.admin.trace"),

    /**
     * The permission necessary for executing the version command
     */
    VERSION("sg.info.version");

    private final String permissionNode;

    /**
     * Instantiates a new command permission
     *
     * @param permissionNode <p>The permission node belonging to this command permission</p>
     */
    CommandPermission(String permissionNode) {
        this.permissionNode = permissionNode;
    }

    /**
     * Gets the permission node related to this permission
     *
     * @return <p>The permission node related to this permission</p>
     */
    public String getPermissionNode() {
        return permissionNode;
    }

}
