package org.sgrewritten.stargate.network;

/**
 * Used for specifying the authority of a network creation
 */
public enum CreationAuthority {
    /**
     * Config-level change or addon specified
     */
    FORCED,
    
    /**
     * Explicitly specifying name, for example with brackets
     */
    EXCPLICIT,
    
    /**
     * Implicitly specifying name, plugin assumes type
     */
    IMPLICIT;
}
