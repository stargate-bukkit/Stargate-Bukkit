package net.TheDgtl.Stargate.command;

public enum CommandPermission {
    RELOAD("sg.admin.reload"), INFO_ABOUT("sg.info.help"), INFO_VERSION("sg.info.version");

    private final String permissionNode;

    CommandPermission(String permissionNode) {
        this.permissionNode = permissionNode;
    }

    public String getPermissionNode() {
        return permissionNode;
    }
}
