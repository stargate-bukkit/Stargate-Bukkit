package org.sgrewritten.stargate.database;

import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.GlobalPortalId;
import org.sgrewritten.stargate.network.portal.PortalFlag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;

/**
 * An executor for executing common queries
 */
public class SQLQueryExecutor {

    private final Connection connection;
    private final SQLQueryGenerator queryGenerator;

    /**
     * Instantiates an SQL query executor
     *
     * @param connection     <p>The database connection to use</p>
     * @param queryGenerator <p>The generator to use</p>
     */
    public SQLQueryExecutor(Connection connection, SQLQueryGenerator queryGenerator) {
        this.connection = connection;
        this.queryGenerator = queryGenerator;
    }

    /**
     * Executes the query for adding a new flag to a portal
     *
     * @param type           <p>The type of portal to add the flag to</p>
     * @param globalPortalId <p>The identifier for the portal to update</p>
     * @param flags          <p>The flags to add</p>
     * @throws SQLException <p>If unable to execute the query</p>
     */
    public void executeAddFlagRelation(StorageType type, GlobalPortalId globalPortalId, Set<PortalFlag> flags) throws SQLException {
        PreparedStatement flagStatement = queryGenerator.generateAddPortalFlagRelationStatement(connection, type);
        for (PortalFlag flag : flags) {
            char flagCharacter = flag.getCharacterRepresentation();
            queryGenerator.log(Level.FINER, "Adding flag " + flagCharacter + " to portal: " + globalPortalId);
            flagStatement.setString(1, globalPortalId.portalId());
            flagStatement.setString(2, globalPortalId.networkId());
            flagStatement.setString(3, String.valueOf(flagCharacter));
            flagStatement.execute();
        }
        flagStatement.close();
    }

}
