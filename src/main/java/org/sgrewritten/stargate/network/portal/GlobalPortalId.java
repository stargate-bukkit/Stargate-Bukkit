package org.sgrewritten.stargate.network.portal;

/**
 * A class representing a portal's global identifier
 */
public record GlobalPortalId(String portalId, String networkId) {

    /**
     * Instantiates a new portal id
     *
     * @param portalId  <p>The portal's local identifier (name)</p>
     * @param networkId <p>The portal's network's identifier</p>
     */
    public GlobalPortalId {
    }

    /**
     * Gets the local id of this global id
     *
     * @return <p>The local portal id</p>
     */
    @Override
    public String portalId() {
        return this.portalId;
    }

    /**
     * Gets the network id of this global id
     *
     * @return <p>The network id</p>
     */
    @Override
    public String networkId() {
        return this.networkId;
    }

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
