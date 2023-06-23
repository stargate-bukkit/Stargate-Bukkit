package org.sgrewritten.stargate.api;

/**
 * An enum containing the different types a portal position can have
 */
public enum PositionType {

    /**
     * A sign that works as an interface for selecting destination and displaying portal information
     */
    SIGN,

    /**
     * A button that works as an interface for confirming destination or activating a portal
     */
    BUTTON,

    /**
     * A custom position type meant for addon use
     */
    CUSTOM;

}
