package net.knarcraft.stargate.portal.property;

/**
 * Each enum value represents one option a portal can have/use
 */
public enum PortalOption {

    /**
     * This option allows a portal to be hidden from others
     */
    HIDDEN('h', "stargate.option.hidden", 11),

    /**
     * This option allows a portal that's always on and does not need to be activated or opened each time
     */
    ALWAYS_ON('a', "stargate.option.alwayson", 12),

    /**
     * This option allows a portal that's private to the stargate's owner
     */
    PRIVATE('p', "stargate.option.private", 13),

    /**
     * This option allows a portal that's free even if stargates usually are not
     */
    FREE('f', "stargate.option.free", 15),

    /**
     * This option allows a portal where players exit through the back of the portal
     */
    BACKWARDS('b', "stargate.option.backwards", 16),

    /**
     * This option shows the gate in the network list even if it's always on
     */
    SHOW('s', "stargate.option.show", 17),

    /**
     * This option hides the network name on the sign
     */
    NO_NETWORK('n', "stargate.option.nonetwork", 18),

    /**
     * This option allows a portal where players teleport to a random exit portal in the network
     */
    RANDOM('r', "stargate.option.random", 19),

    /**
     * This option allows a portal to teleport to another server connected through BungeeCord
     */
    BUNGEE('u', "stargate.admin.bungee", 20),

    /**
     * This option allows a portal which does not display a teleportation message, for better immersion
     */
    SILENT('i', "stargate.option.silent", 21),

    /**
     * This option causes a fixed portal's sign to be removed after creation
     */
    NO_SIGN('e', "stargate.option.nosign", 22);

    private final char characterRepresentation;
    private final String permissionString;
    private final int saveIndex;

    /**
     * Instantiates a new portal options
     *
     * @param characterRepresentation <p>The character representation used on the sign to allow this option</p>
     * @param permissionString        <p>The permission necessary to use this option</p>
     */
    PortalOption(final char characterRepresentation, String permissionString, int saveIndex) {
        this.characterRepresentation = characterRepresentation;
        this.permissionString = permissionString;
        this.saveIndex = saveIndex;
    }

    /**
     * Gets the character representation used to enable this setting on the sign
     *
     * @return <p>The character representation of this option</p>
     */
    public char getCharacterRepresentation() {
        return this.characterRepresentation;
    }

    /**
     * Gets the permission necessary to use this option
     *
     * @return <p>The permission necessary for this option</p>
     */
    public String getPermissionString() {
        return this.permissionString;
    }

    /**
     * Gets the index of the save file this option is stored at
     *
     * @return <p>This option's save index</p>
     */
    public int getSaveIndex() {
        return this.saveIndex;
    }

}
