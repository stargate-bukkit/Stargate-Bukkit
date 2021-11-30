package net.TheDgtl.Stargate.database;

/**
 * A config for keeping track of the names of all database tables in use
 *
 * @author Kristian
 */
public class TableNameConfig {

    private final String mainPrefix;
    private final String serverPrefix;
    private String portalTableName = "Portal";
    private String interPortalTableName = "InterPortal";
    private String flagTableName = "Flag";
    private String flagRelationTableName = "PortalFlagRelation";
    private String interFlagRelationTableName = "InterPortalFlagRelation";
    private String portalViewName = "PortalView";
    private String interPortalViewName = "InterPortalView";
    private String lastKnownNameTableName = "LastKnownName";
    private String serverInfoTableName = "ServerInfo";

    /**
     * Instantiates a new table config
     *
     * @param mainPrefix   <p>The main prefix used on all tables to prevent name collisions</p>
     * @param serverPrefix <p>The per-server prefix used to prevent name conflicts between non-shared tables</p>
     */
    public TableNameConfig(String mainPrefix, String serverPrefix) {
        this.mainPrefix = mainPrefix;
        this.serverPrefix = serverPrefix;
    }

    /**
     * Instantiates a new table config
     *
     * @param mainPrefix                 <p>The main prefix used on all tables to prevent name collisions</p>
     * @param serverPrefix               <p>The per-server prefix used to prevent name conflicts between non-shared tables</p>
     * @param portalTableName            <p>The name of the table storing portals</p>
     * @param interPortalTableName       <p>The name of the table storing inter-portals</p>
     * @param flagTableName              <p>The name of the table storing available portal-flags</p>
     * @param flagRelationTableName      <p>The name of the table storing set flags for normal portals</p>
     * @param interFlagRelationTableName <p>The name of the table storing set flags for inter-server portals</p>
     * @param portalViewName             <p>The name of the view used to get the full information about portals</p>
     * @param interPortalViewName        <p>The name of the view used to get the full information about inter-server portals</p>
     * @param lastKnownNameTableName     <p>The name of the table in which to store the last known names attached to UUIDs</p>
     * @param serverInfoTableName        <p>The name of the table in which to store information, such as names about the known
     *                                   servers in the network</p>
     */
    @SuppressWarnings("unused")
    public TableNameConfig(String mainPrefix, String serverPrefix, String portalTableName, String interPortalTableName,
                           String flagTableName, String flagRelationTableName, String interFlagRelationTableName,
                           String portalViewName, String interPortalViewName, String lastKnownNameTableName,
                           String serverInfoTableName) {
        this.mainPrefix = mainPrefix;
        this.serverPrefix = serverPrefix;
        this.portalTableName = portalTableName;
        this.interPortalTableName = interPortalTableName;
        this.flagTableName = flagTableName;
        this.flagRelationTableName = flagRelationTableName;
        this.interFlagRelationTableName = interFlagRelationTableName;
        this.portalViewName = portalViewName;
        this.interPortalViewName = interPortalViewName;
        this.lastKnownNameTableName = lastKnownNameTableName;
        this.serverInfoTableName = serverInfoTableName;
    }

    /**
     * Gets the name of the table storing portals
     *
     * @return <p>The name of the table storing portals</p>
     */
    public String getPortalTableName() {
        return mainPrefix + serverPrefix + portalTableName;
    }

    /**
     * Gets the name of the table storing inter-portals
     *
     * @return <p>The name of the table storing inter-portals</p>
     */
    public String getInterPortalTableName() {
        return mainPrefix + interPortalTableName;
    }

    /**
     * Gets the name of the table storing available portal-flags
     *
     * @return <p>The name of the table storing available portal-flags</p>
     */
    public String getFlagTableName() {
        return mainPrefix + flagTableName;
    }

    /**
     * Gets the name of the table storing set flags for normal portals
     *
     * @return <p>The name of the table storing set flags for normal portals</p>
     */
    public String getFlagRelationTableName() {
        return mainPrefix + serverPrefix + flagRelationTableName;
    }

    /**
     * Gets the name of the table storing set flags for inter-server portals
     *
     * @return <p>The name of the table storing set flags for inter-server portals</p>
     */
    public String getInterFlagRelationTableName() {
        return mainPrefix + interFlagRelationTableName;
    }

    /**
     * Gets the name of the view used to get the full information about portals
     *
     * @return <p>The name of the view used to get the full information about portals</p>
     */
    public String getPortalViewName() {
        return mainPrefix + serverPrefix + portalViewName;
    }

    /**
     * Gets the name of the view used to get the full information about inter-server portals
     *
     * @return <p>The name of the view used to get the full information about inter-server portals</p>
     */
    public String getInterPortalViewName() {
        return mainPrefix + interPortalViewName;
    }

    /**
     * Gets the name of the table in which to store the last known names attached to UUIDs
     *
     * @return <p>The name of the table in which to store the last known names attached to UUIDs</p>
     */
    public String getLastKnownNameTableName() {
        return mainPrefix + lastKnownNameTableName;
    }

    /**
     * Gets the name of the table in which to store information, such as names about the known servers in the network
     *
     * @return <p>The name of the table in which to store information, such as names about the known servers in the network</p>
     */
    public String getServerInfoTableName() {
        return mainPrefix + serverInfoTableName;
    }

}
