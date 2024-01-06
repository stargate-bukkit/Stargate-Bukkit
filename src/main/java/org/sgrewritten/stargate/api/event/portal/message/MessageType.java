package org.sgrewritten.stargate.api.event.portal.message;


public enum MessageType {
    /**
     * Called whenever the portal has been destroyed
     */
    DESTROY,
    /**
     * Called during activation and the destination list is empty
     */
    DESTINATION_EMPTY,
    /**
     * Called whenever the user lacks perms
     */
    DENY,
    /**
     * Called whenever a portal has been created
     */
    CREATE,
    /**
     * Called whenever an entity is teleported
     */
    TELEPORT
}
