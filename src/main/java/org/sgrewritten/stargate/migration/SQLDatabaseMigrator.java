package org.sgrewritten.stargate.migration;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.StorageType;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.SQLQuery;
import org.sgrewritten.stargate.database.SQLQueryHandler;
import org.sgrewritten.stargate.util.ExceptionHelper;
import org.sgrewritten.stargate.util.FileHelper;
import org.sgrewritten.stargate.util.database.DatabaseHelper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class SQLDatabaseMigrator {

    private final @NotNull TableNameConfiguration nameConfiguration;
    private final @NotNull String sqlFilesPath;
    private final boolean interServerEnabled;
    private final @NotNull SQLDatabaseAPI database;

    public SQLDatabaseMigrator(@NotNull SQLDatabaseAPI database, @NotNull TableNameConfiguration nameConfiguration, @NotNull String sqlFilesPath, boolean interServerEnabled) {
        this.nameConfiguration = Objects.requireNonNull(nameConfiguration);
        this.sqlFilesPath = Objects.requireNonNull(sqlFilesPath);
        this.interServerEnabled = interServerEnabled;
        this.database = Objects.requireNonNull(database);

    }

    public void run() throws SQLException, IOException {
        try (Connection connection = database.getConnection()) {
            run(StorageType.LOCAL, connection);
            if (interServerEnabled) {
                run(StorageType.INTER_SERVER, connection);
            }
        }
    }

    private void run(StorageType type, Connection connection) throws SQLException, IOException {
        String path = sqlFilesPath + "/" + type.toString().toLowerCase();
        int count = 0;
        while (true) {
            boolean fallible = false;
            try {
                InputStream stream = FileHelper.getInputStreamForInternalFile(path + "/step" + count + ".sql");
                if (stream == null) {
                    fallible = true;
                    stream = FileHelper.getInputStreamForInternalFile(path + "/fallibleStep" + count + ".sql");
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
                if (!fallible) {
                    throw e;
                }
            }
        }
    }

    private void processQuery(final String queryString, Connection connection) throws SQLException {
        String newQueryString;
        if (ExceptionHelper.doesNotThrow(IllegalArgumentException.class, () -> SQLQuery.valueOf(queryString.trim()))) {
            newQueryString = SQLQueryHandler.getQuery(SQLQuery.valueOf(queryString.trim()), database.getDriver());
        } else {
            newQueryString = queryString + ";";
        }
        newQueryString = nameConfiguration.replaceKnownTableNames(newQueryString);
        DatabaseHelper.runStatement(connection.prepareStatement(newQueryString));
    }

}
