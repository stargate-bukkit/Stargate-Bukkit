package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;

import java.util.Map;

/**
 * Keeps track of all options for a given portal
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
            Stargate.debug("Portal", "Can not create a non-fixed always-on gate. Setting AlwaysOn = false");
        }

        if (this.isRandom() && !this.isAlwaysOn()) {
            this.options.put(PortalOption.ALWAYS_ON, true);
            Stargate.debug("Portal", "Gate marked as random, set to always-on");
        }
    }

    /**
     * Gets whether this portal is fixed
     *
     * <p>A fixed portal has only one destination which never changes. A fixed portal has a fixed destination, is a
     * random portal or is a bungee portal. A fixed portal is always open.</p>
     *
     * @return <p>Whether this gate is fixed</p>
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
     * @return <p>Whether this portal is always on</p>
     */
    public boolean isAlwaysOn() {
        return this.options.get(PortalOption.ALWAYS_ON);
    }

    /**
     * Gets whether this portal is hidden
     *
     * @return <p>Whether this portal is hidden</p>
     */
    public boolean isHidden() {
        return this.options.get(PortalOption.HIDDEN);
    }

    /**
     * Gets whether this portal is private
     *
     * @return <p>Whether this portal is private</p>
     */
    public boolean isPrivate() {
        return this.options.get(PortalOption.PRIVATE);
    }

    /**
     * Gets whether this portal is free
     *
     * @return <p>Whether this portal is free</p>
     */
    public boolean isFree() {
        return this.options.get(PortalOption.FREE);
    }

    /**
     * Gets whether this portal is backwards
     *
     * <p>A backwards portal is one where players exit through the back.</p>
     *
     * @return <p>Whether this portal is backwards</p>
     */
    public boolean isBackwards() {
        return this.options.get(PortalOption.BACKWARDS);
    }

    /**
     * Gets whether this portal is shown on the network even if it's always on
     *
     * @return <p>Whether portal gate is shown</p>
     */
    public boolean isShown() {
        return this.options.get(PortalOption.SHOW);
    }

    /**
     * Gets whether this portal shows no network
     *
     * @return <p>Whether this portal shows no network/p>
     */
    public boolean isNoNetwork() {
        return this.options.get(PortalOption.NO_NETWORK);
    }

    /**
     * Gets whether this portal goes to a random location on the network
     *
     * @return <p>Whether this portal goes to a random location</p>
     */
    public boolean isRandom() {
        return this.options.get(PortalOption.RANDOM);
    }

    /**
     * Gets whether this portal is a bungee portal
     *
     * @return <p>Whether this portal is a bungee portal</p>
     */
    public boolean isBungee() {
        return this.options.get(PortalOption.BUNGEE);
    }

}
