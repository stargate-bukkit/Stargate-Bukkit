package net.TheDgtl.Stargate.database;

public interface MiscStorageAPI {

    /**
     * "Starts" the inter-server connection by setting this server's portals as online
     */
    void startInterServerConnection();
    
    /**
     * Add a new flagtype
     * 
     * @param flagChar
     */
    void addFlagType(char flagChar);
    
    /**
     * Add a new type of portalPosition
     * 
     * @param portalPositionTypeName
     */
    void addPortalPositionType(String portalPositionTypeName);
}
