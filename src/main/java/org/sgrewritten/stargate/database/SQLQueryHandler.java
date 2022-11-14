package org.sgrewritten.stargate.database;

import org.sgrewritten.stargate.util.FileHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A handler which keeps track of all queries and query variations
 */
public class SQLQueryHandler {

    private static final Map<SQLQuery, Map<DatabaseDriver, String>> queries = new HashMap<>();

    static {
        //Load all queries from the query files
        parseSQLQueries(readQueryFiles(getSQLQueryFiles()));
    }

    /**
     * Gets the given SQL query
     *
     * <p>This will return a query for the given database driver, or SQLite if the SQLite syntax is valid</p>
     *
     * @param sqlQuery       <p>The SQL query to get</p>
     * @param databaseDriver <p>The database driver to get the query for</p>
     * @return <p>A query valid for the given database driver</p>
     */
    public static String getQuery(SQLQuery sqlQuery, DatabaseDriver databaseDriver) {
        Map<DatabaseDriver, String> queryVariations = queries.get(sqlQuery);
        if (queryVariations.containsKey(databaseDriver)) {
            return queryVariations.get(databaseDriver);
        } else {
            return getQuery(sqlQuery);
        }
    }

    /**
     * Gets the given SQL query
     *
     * <p>This will return a query compatible with SQLite</p>
     *
     * @param sqlQuery <p>The SQL query to get</p>
     * @return <p>The query</p>
     */
    private static String getQuery(SQLQuery sqlQuery) {
        Map<DatabaseDriver, String> query = queries.get(sqlQuery);
        if (query == null) {
            return null;
        } else {
            return query.get(DatabaseDriver.SQLITE);
        }
    }

    /**
     * Gets all unique query files which need to be read
     *
     * @return <p>All unique query files</p>
     */
    private static Set<String> getSQLQueryFiles() {
        Set<String> queryFiles = new HashSet<>();
        for (DatabaseDriver driver : DatabaseDriver.values()) {
            if (!driver.getQueryFile().isEmpty()) {
                queryFiles.add(driver.getQueryFile());
            }
        }
        return queryFiles;
    }

    /**
     * Reads the given query files
     *
     * @param queryFiles <p>The query files to read</p>
     * @return <p>The read queries with the file name as key</p>
     */
    private static Map<String, Map<String, String>> readQueryFiles(Set<String> queryFiles) {
        Map<String, Map<String, String>> readQueryFiles = new HashMap<>();
        for (String queryFile : queryFiles) {
            //Prevent duplicate reading as with MySQL and MariaDB
            if (readQueryFiles.containsKey(queryFile)) {
                continue;
            }
            Map<String, String> readValues = new HashMap<>();
            FileHelper.readInternalFileToMap("/database/" + queryFile, readValues);
            readQueryFiles.put(queryFile, readValues);
        }
        return readQueryFiles;
    }

    /**
     * Parses the given read query files
     *
     * <p>This method registers all queries in the read query files to the queries variable.</p>
     *
     * @param readQueryFiles <p>The read query files</p>
     */
    private static void parseSQLQueries(Map<String, Map<String, String>> readQueryFiles) {
        for (DatabaseDriver databaseDriver : DatabaseDriver.values()) {
            //Skip any drivers without valid query files
            if (!readQueryFiles.containsKey(databaseDriver.getQueryFile())) {
                continue;
            }
            Map<String, String> readQueries = readQueryFiles.get(databaseDriver.getQueryFile());
            for (String query : readQueries.keySet()) {
                SQLQuery sqlQuery = SQLQuery.valueOf(query);
                String queryString = readQueries.get(query);
                if (!queries.containsKey(sqlQuery)) {
                    queries.put(sqlQuery, new HashMap<>());
                }
                queries.get(sqlQuery).put(databaseDriver, queryString);
            }
        }
    }

}
