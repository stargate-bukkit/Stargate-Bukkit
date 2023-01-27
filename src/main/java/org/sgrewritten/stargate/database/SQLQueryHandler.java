package org.sgrewritten.stargate.database;

import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.util.FileHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * A handler which keeps track of all queries and query variations
 */
public class SQLQueryHandler {

    private static final Map<SQLQuery, Map<DatabaseDriver, String>> queries = new HashMap<>();

    static {
        //Load all queries from the query files
        parseSQLQueries(readQueryFiles(getSQLQueryFolders()));
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
     * Gets all unique query folders which need to be read
     *
     * @return <p>All unique query files</p>
     */
    private static Set<String> getSQLQueryFolders() {
        Set<String> queryFiles = new HashSet<>();
        for (DatabaseDriver driver : DatabaseDriver.values()) {
            if (!driver.getQueryFolder().isEmpty()) {
                queryFiles.add(driver.getQueryFolder());
            }
        }
        return queryFiles;
    }

    /**
     * Reads the given query folders
     *
     * @param queryFolders <p>The query folders to read</p>
     * @return <p>The read queries with the file name as key</p>
     */
    private static Map<String, Map<String, String>> readQueryFiles(Set<String> queryFolders) {
        Map<String, Map<String,String>> readQueryFiles = new HashMap<>();
        for(String folder : queryFolders) {
            try {
                readQueryFiles.put(folder, readQueryFilesFromFolder(folder));
            } catch (IOException | URISyntaxException e) {
                Stargate.log(e);
            }
        }
        return readQueryFiles;
    }
    
    private static Map<String, String> readQueryFilesFromFolder(String folder) throws IOException, URISyntaxException {
        final Map<String, String> queries = new HashMap<>();
        String fullFolder = "/database/" + folder;
        List<Path> walk = FileHelper.listFilesOfInternalDirectory(fullFolder);

        walk.forEach((path) -> {
            if (!path.toString().endsWith(".sql")) {
                return;
            }
            try {
                String query = FileHelper.readStreamToString(FileHelper.getInputStreamForInternalFile(fullFolder + "/" + path.getFileName().toString()));
                queries.put(path.getFileName().toString().replaceAll(".sql$", ""), query);
            } catch (IOException e) {
                Stargate.log(e);
            }
        });
        return queries;
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
            if (!readQueryFiles.containsKey(databaseDriver.getQueryFolder())) {
                continue;
            }
            Map<String, String> readQueries = readQueryFiles.get(databaseDriver.getQueryFolder());
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
