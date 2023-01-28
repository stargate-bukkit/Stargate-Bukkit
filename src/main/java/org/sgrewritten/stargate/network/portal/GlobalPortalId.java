package org.sgrewritten.stargate.network.portal;

/**
 * A class representing a portal's global identifier
 *
 * @param portalId  <p>The locally unique id of a portal</p>
 * @param networkId <p>The id of the network the portal belongs to</p>
 */
public record GlobalPortalId(String portalId, String networkId) {

    /**
     * Gets a global portal id from the given portal
     *
     * @param portal <p>The portal to get the id for</p>
     * @return <p>The portal's id</p>
     */
    public static GlobalPortalId getFromPortal(Portal portal) {
        return new GlobalPortalId(portal.getId(), portal.getNetwork().getId());
    }

    @Override
    public String toString() {
        return "{Network: " + networkId() + ", Portal: " + portalId() + "}";
    }

}
