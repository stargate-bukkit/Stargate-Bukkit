package org.sgrewritten.stargate.property;

/**
 * How should portals with invalid structures be dealt with?
 */
public enum PortalValidity {

    /**
     * Ignore loading the portal if it has an invalid structure
     */
    IGNORE,

    /**
     * Repair the portal if it has an invalid structure
     */
    REPAIR,

    /**
     *Remove the portal from the database if it has an invalid structure
     */
    REMOVE;
}