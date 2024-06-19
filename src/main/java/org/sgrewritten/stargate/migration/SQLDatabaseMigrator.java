package org.sgrewritten.stargate.migration;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.SQLQuery;
import org.sgrewritten.stargate.database.SQLQueryHandler;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.util.ExceptionHelper;
import org.sgrewritten.stargate.util.FileHelper;
import org.sgrewritten.stargate.util.database.DatabaseHelper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Helps with running sql database migrations by reading sql files.
 * - Can read multiple queries in the same file
 * - Can parse already defined stargate SQL statements and run them (in the same file)
 */
public class SQLDatabaseMigrator {

    private final @NotNull TableNameConfiguration nameConfiguration;
    private final @NotNull String sqlFilesPath;
    private final boolean interServerEnabled;
    private final @NotNull SQLDatabaseAPI database;

    /**
     * @param database <p>A database api able to provide sql statements</p>
     * @param nameConfiguration <p>SQL table name config</p>
     * @param sqlFilesPath <p>The internal resource path to the SQL files to run</p>
     * @param interServerEnabled <p>Whether inter server portals are enabled</p>
     */
    public SQLDatabaseMigrator(@NotNull SQLDatabaseAPI database, @NotNull TableNameConfiguration nameConfiguration, @NotNull String sqlFilesPath, boolean interServerEnabled) {
        this.nameConfiguration = Objects.requireNonNull(nameConfiguration);
        this.sqlFilesPath = Objects.requireNonNull(sqlFilesPath);
        this.interServerEnabled = interServerEnabled;
        this.database = Objects.requireNonNull(database);

    }

    /**
     * Run multiple SQL scripts in specified directory (constructor)
     * @throws SQLException <p>Any sql exception</p>
     * @throws IOException <p>if unable to read the internal file</p>
     */
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
            boolean failAble = false;
            try {
                InputStream stream = FileHelper.getInputStreamForInternalFile(path + "/step" + count + ".sql");
                if (stream == null) {
                    failAble = true;
                    stream = FileHelper.getInputStreamForInternalFile(path + "/failAbleStep" + count + ".sql");
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
            } catch (SQLException | IOException e) {
                if (!failAble) {
                    throw e;
                }
            } finally {
                count++;
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
