package net.TheDgtl.Stargate.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.TheDgtl.Stargate.Stargate;

public class MiscDatabaseAPI implements MiscStorageAPI{

    private Database database;
    private SQLQueryGenerator sqlQueryGenerator;

    public MiscDatabaseAPI(Database database, SQLQueryGenerator sqlQueryGenerator){
        this.database = database;
        this.sqlQueryGenerator = sqlQueryGenerator;
    }

    @Override
    public void startInterServerConnection() {
        try {
            Connection conn = database.getConnection();
            PreparedStatement statement = sqlQueryGenerator.generateUpdateServerInfoStatus(conn, Stargate.getServerUUID(),
                    Stargate.getServerName());
            statement.execute();
            statement.close();
            conn.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
    
    @Override
    public void addFlagType(char flagChar) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addPortalPositionType(String portalPositionTypeName) {
        // TODO Auto-generated method stub
        
    }
}
