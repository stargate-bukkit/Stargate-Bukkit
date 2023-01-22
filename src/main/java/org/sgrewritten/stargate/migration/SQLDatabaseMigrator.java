package org.sgrewritten.stargate.migration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.SQLQuery;
import org.sgrewritten.stargate.database.SQLQueryHandler;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.util.ExceptionHelper;
import org.sgrewritten.stargate.util.FileHelper;
import org.sgrewritten.stargate.util.database.DatabaseHelper;

public class SQLDatabaseMigrator {
    private @NotNull TableNameConfiguration nameConfiguration;
    private @NotNull String sqlFilesPath;
    private boolean interServerEnabled;
    private @NotNull SQLDatabaseAPI database;

    public SQLDatabaseMigrator(@NotNull SQLDatabaseAPI database, @NotNull TableNameConfiguration nameConfiguration,@NotNull String sqlFilesPath,boolean interServerEnabled) throws SQLException {
        this.nameConfiguration = Objects.requireNonNull(nameConfiguration);
        this.sqlFilesPath = Objects.requireNonNull(sqlFilesPath);
        this.interServerEnabled = interServerEnabled;
        this.database = Objects.requireNonNull(database);
        
    }
    
    public void run() throws SQLException, IOException {
        Connection connection = null;
        try {
            connection = database.getConnection();
            run(StorageType.LOCAL,connection);
            if (interServerEnabled) {
                run(StorageType.INTER_SERVER,connection);
            }
        } finally {
            if(connection != null) {
                connection.close();
            }
        }
    }
    
    private void run(StorageType type, Connection connection) throws SQLException, IOException {
        String path = sqlFilesPath + "/" + type.toString().toLowerCase();
        int count = 0;
        while (true) {
            boolean failable = false;
            try {
                InputStream stream = FileHelper
                        .getInputStreamForInternalFile(path + "/step" + count + ".sql");
                if (stream == null) {
                    failable = true;
                    stream = FileHelper.getInputStreamForInternalFile(path + "/failableStep" + count + ".sql");
                }
                if (stream == null) {
                    break;
                }
                String queriesString = FileHelper.readStreamToString(stream);
                String[] queriesStringList = queriesString.split(";");
                for (int i = 0; i < queriesStringList.length - 1; i++) {
                    String queryString = queriesStringList[i];
                    processQuery(queryString, connection);
                }
                count++;
            } catch (SQLException | IOException e) {
                if (!failable) {
                    throw e;
                }
            }
        }
    }
    
    private void processQuery(final String queryString, Connection connection) throws SQLException {
        String newQueryString;
        if(ExceptionHelper.doesNotThrow(IllegalArgumentException.class, () -> SQLQuery.valueOf(queryString.trim()))) {
            newQueryString = SQLQueryHandler.getQuery(SQLQuery.valueOf(queryString.trim()),database.getDriver());
        } else {
            newQueryString = queryString + ";";
        }
        newQueryString = nameConfiguration.replaceKnownTableNames(newQueryString);
        Stargate.log(Level.INFO,"Running query:\n " + newQueryString);
        DatabaseHelper.runStatement(connection.prepareStatement(newQueryString));
    }
}
