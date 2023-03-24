package net.knarcraft.stargate.portal.property;

import net.knarcraft.stargate.Stargate;

import java.util.Map;

/**
 * Keeps track of all options for one portal
 */
public class PortalOptions {

    private final Map<PortalOption, Boolean> options;
    private boolean isFixed;

    /**
     * Instantiates a new portal options object
     *
     * @param options        <p>All options to keep track of</p>
     * @param hasDestination <p>Whether the portal has a fixed destination</p>
     */
    public PortalOptions(Map<PortalOption, Boolean> options, boolean hasDestination) {
        this.options = options;

        isFixed = hasDestination || this.isRandom() || this.isBungee();

        if (this.isAlwaysOn() && !isFixed) {
            this.options.put(PortalOption.ALWAYS_ON, false);
            Stargate.debug("PortalOptions", "Can not create a non-fixed always-on gate. Setting AlwaysOn = false");
        }

        if ((this.isRandom() || this.isBungee()) && !this.isAlwaysOn()) {
            this.options.put(PortalOption.ALWAYS_ON, true);
            Stargate.debug("PortalOptions", "Gate marked as random or bungee, set to always-on");
        }

        if (this.hasNoSign() && !this.isFixed) {
            this.options.put(PortalOption.NO_SIGN, false);
            Stargate.debug("PortalOptions", "Gate marked with no sign, but not fixed. Setting NoSign = false");
        }
    }

    /**
     * Gets whether this portal is fixed
     *
     * <p>A fixed portal is a portal for which the player cannot choose destination. A portal with a set destination, a
     * random portal and bungee portals are fixed. While the player has no choice regarding destinations, a fixed gate
     * may still need to be activated if not set to always on.</p>
     *
     * @return <p>Whether this portal is fixed</p>
     */
    public boolean isFixed() {
        return this.isFixed;
    }

    /**
     * Sets whether this portal is fixed
     *
     * @param fixed <p>Whether this gate should be fixed</p>
     */
    public void setFixed(boolean fixed) {
        this.isFixed = fixed;
    }

    /**
     * Gets whether this portal is always on
     *
     * <p>An always on portal is always open for everyone, and always uses the open-block. It never needs to be
     * activated or opened manually.</p>
     *
     * @return <p>Whether this portal is always on</p>
     */
    public boolean isAlwaysOn() {
        return this.options.get(PortalOption.ALWAYS_ON);
    }

    /**
     * Gets whether this portal is hidden
     *
     * <p>A hidden portal will be hidden on a network for everyone but admins and the portal owner. In other words,
     * when selecting a destination using a portal's sign, hidden gates will only be available in the list for the
     * owner and players with the appropriate permission.</p>
     *
     * @return <p>Whether this portal is hidden</p>
     */
    public boolean isHidden() {
        return this.options.get(PortalOption.HIDDEN);
    }

    /**
     * Gets whether this portal is private
     *
     * <p>A private portal can only be opened by the owner and players with the appropriate permission. A private gate
     * is not hidden unless the hidden option is also enabled.</p>
     *
     * @return <p>Whether this portal is private</p>
     */
    public boolean isPrivate() {
        return this.options.get(PortalOption.PRIVATE);
    }

    /**
     * Gets whether this portal is free
     *
     * <p>A free portal is exempt from any fees which would normally occur from using the portal. It does nothing if
     * economy is disabled.</p>
     *
     * @return <p>Whether this portal is free</p>
     */
    public boolean isFree() {
        return this.options.get(PortalOption.FREE);
    }

    /**
     * Gets whether this portal is backwards
     *
     * <p>A backwards portal is one where players exit through the back. It's important to note that the exit is
     * mirrored, not rotated, when exiting backwards.</p>
     *
     * @return <p>Whether this portal is backwards</p>
     */
    public boolean isBackwards() {
        return this.options.get(PortalOption.BACKWARDS);
    }

    /**
     * Gets whether this portal is shown on the network even if it's always on
     *
     * <p>Normally, always-on portals are not selectable on a network, but enabling this option allows the portal to be
     * shown.</p>
     *
     * @return <p>Whether portal gate is shown</p>
     */
    public boolean isShown() {
        return this.options.get(PortalOption.SHOW);
    }

    /**
     * Gets whether this portal shows no network
     *
     * <p>Enabling the no network option allows the portal's network to be hidden for whatever reason. If allowing
     * normal players to create portals, this can be used to prevent random users from connecting gates to
     * "protected networks".</p>
     *
     * @return <p>Whether this portal shows no network/p>
     */
    public boolean isNoNetwork() {
        return this.options.get(PortalOption.NO_NETWORK);
    }

    /**
     * Gets whether this portal goes to a random location on the network
     *
     * <p>A random portal is always on and will teleport to a random destination within the same network.</p>
     *
     * @return <p>Whether this portal goes to a random location</p>
     */
    public boolean isRandom() {
        return this.options.get(PortalOption.RANDOM);
    }

    /**
     * Gets whether this portal is a bungee portal
     *
     * <p>A bungee portal is able to teleport to a portal on another server. It works differently from other portals as
     * it does not have a network, but instead the network line specifies the same of the server it connects to.</p>
     *
     * @return <p>Whether this portal is a bungee portal</p>
     */
    public boolean isBungee() {
        return this.options.get(PortalOption.BUNGEE);
    }

    /**
     * Gets whether this portal is silent
     *
     * <p>A silent portal does not output anything to the chat when teleporting. This option is mainly useful to keep
     * the immersion during teleportation (for role-playing servers or similar).</p>
     *
     * @return <p>Whether this portal is silent</p>
     */
    public boolean isSilent() {
        return this.options.get(PortalOption.SILENT);
    }

    /**
     * Gets whether this portal has no sign
     *
     * <p>An always-on portal is allowed to not have a sign as it will never be interacted with anyway.</p>
     *
     * @return <p>Whether this portal has no sign</p>
     */
    public boolean hasNoSign() {
        return this.options.get(PortalOption.NO_SIGN);
    }

}
