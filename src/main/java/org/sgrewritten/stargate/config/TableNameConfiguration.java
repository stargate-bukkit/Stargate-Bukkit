package org.sgrewritten.stargate.config;

import java.util.HashMap;
import java.util.Map;

/**
 * A config for keeping track of the names of all database tables in use
 *
 * @author Kristian
 */
public class TableNameConfiguration {

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
    private String positionTypeTableName = "PortalPositionType";
    private String portalPositionTableName = "PortalPosition";
    private String interPortalPositionTableName = "InterPortalPosition";
    private String portalPositionIndexTableName = "portalPositionIndex";
    private String interPortalPositionIndexTableName = "interPortalPositionIndex";
    private final Map<String, String> prefixedTableNames;

    /**
     * Instantiates a new table config
     *
     * @param mainPrefix   <p>The main prefix used on all tables to prevent name collisions</p>
     * @param serverPrefix <p>The per-server prefix used to prevent name conflicts between non-shared tables</p>
     */
    public TableNameConfiguration(String mainPrefix, String serverPrefix) {
        this.mainPrefix = mainPrefix;
        this.serverPrefix = serverPrefix;
        this.prefixedTableNames = getPrefixedTableNamesMap();
    }

    /**
     * Instantiates a new table config
     *
     * @param mainPrefix                        <p>The main prefix used on all tables to prevent name collisions</p>
     * @param serverPrefix                      <p>The per-server prefix used to prevent name conflicts between non-shared tables</p>
     * @param portalTableName                   <p>The name of the table storing portals</p>
     * @param interPortalTableName              <p>The name of the table storing inter-portals</p>
     * @param flagTableName                     <p>The name of the table storing available portal-flags</p>
     * @param flagRelationTableName             <p>The name of the table storing set flags for normal portals</p>
     * @param interFlagRelationTableName        <p>The name of the table storing set flags for inter-server portals</p>
     * @param portalViewName                    <p>The name of the view used to get the full information about portals</p>
     * @param interPortalViewName               <p>The name of the view used to get the full information about inter-server portals</p>
     * @param lastKnownNameTableName            <p>The name of the table in which to store the last known names attached to UUIDs</p>
     * @param serverInfoTableName               <p>The name of the table in which to store information, such as names about the known
     *                                          servers in the network</p>
     * @param positionTypeTableName             <p>The name of the table storing position types</p>
     * @param portalPositionTableName           <p>The name of the table storing portal positions</p>
     * @param interPortalPositionTableName      <p>The name of the table storing inter-server portal positions</p>
     * @param portalPositionIndexTableName      <p>The name of the table indexing the types of portal positions</p>
     * @param interPortalPositionIndexTableName <p>The name of the table indexing the types of inter-server portal positions</p>
     */
    @SuppressWarnings("unused")
    public TableNameConfiguration(String mainPrefix, String serverPrefix, String portalTableName,
                                  String interPortalTableName, String flagTableName, String flagRelationTableName,
                                  String interFlagRelationTableName, String portalViewName, String interPortalViewName,
                                  String lastKnownNameTableName, String serverInfoTableName, String positionTypeTableName,
                                  String portalPositionTableName, String interPortalPositionTableName, String portalPositionIndexTableName,
                                  String interPortalPositionIndexTableName) {
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
        this.positionTypeTableName = positionTypeTableName;
        this.portalPositionTableName = portalPositionTableName;
        this.interPortalPositionTableName = interPortalPositionTableName;
        this.portalPositionIndexTableName = portalPositionIndexTableName;
        this.interPortalPositionIndexTableName = interPortalPositionIndexTableName;
        this.prefixedTableNames = getPrefixedTableNamesMap();
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

    /**
     * Gets the name of the table used to store the available position types
     *
     * @return <p>The name of the table used to store available position types</p>
     */
    public String getPositionTypeTableName() {
        return mainPrefix + positionTypeTableName;
    }

    /**
     * Gets the name of the table used to store portal positions
     *
     * @return <p>The name of the table used to store portal positions</p>
     */
    public String getPortalPositionTableName() {
        return mainPrefix + serverPrefix + portalPositionTableName;
    }

    /**
     * Gets the name of the table used to store inter-portal positions
     *
     * @return <p>The name of the table used to store inter-portal positions</p>
     */
    public String getInterPortalPositionTableName() {
        return mainPrefix + interPortalPositionTableName;
    }

    /**
     * Gets the name of the index for the portal position table
     *
     * @return <p>The name of the index for the portal position table</p>
     */
    public String getPortalPositionIndexTableName() {
        return mainPrefix + serverPrefix + portalPositionIndexTableName;
    }

    /**
     * Gets the name of the index for the inter-server portal position table
     *
     * @return <p>The name of the index for the inter-portal position table</p>
     */
    public String getInterPortalPositionIndexTableName() {
        return mainPrefix + interPortalPositionIndexTableName;
    }

    /**
     * Replaces known table name keys with their proper name values
     *
     * @param query <p>The input query string to replace in</p>
     * @return <p>The query string with keys replaced</p>
     */
    public String replaceKnownTableNames(String query) {
        for (String key : prefixedTableNames.keySet()) {
            query = query.replace("{" + key + "}", prefixedTableNames.get(key));
        }
        return query;
    }

    /**
     * Gets the map between table name placeholders and the correct prefixed table names
     *
     * @return <p>The map between table name placeholders and the correct prefixed table names</p>
     */
    private Map<String, String> getPrefixedTableNamesMap() {
        Map<String, String> output = new HashMap<>();
        output.put("Portal", this.getPortalTableName());
        output.put("PortalView", this.getPortalViewName());
        output.put("Flag", this.getFlagTableName());
        output.put("PortalFlagRelation", this.getFlagRelationTableName());
        output.put("InterPortal", this.getInterPortalTableName());
        output.put("InterPortalView", this.getInterPortalViewName());
        output.put("InterPortalFlagRelation", this.getInterFlagRelationTableName());
        output.put("LastKnownName", this.getLastKnownNameTableName());
        output.put("ServerInfo", this.getServerInfoTableName());
        output.put("PositionType", this.getPositionTypeTableName());
        output.put("PortalPosition", this.getPortalPositionTableName());
        output.put("InterPortalPosition", this.getInterPortalPositionTableName());
        output.put("PortalPositionIndex", this.getPortalPositionIndexTableName());
        output.put("InterPortalPositionIndex", this.getInterPortalPositionIndexTableName());
        return output;
    }

}
