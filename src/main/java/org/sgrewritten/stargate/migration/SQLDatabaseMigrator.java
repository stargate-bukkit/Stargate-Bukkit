package org.sgrewritten.stargate.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.TableNameConfiguration;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.util.FileHelper;

public class SQLDatabaseMigrator {
    private Connection connection;
    private @NotNull TableNameConfiguration nameConfiguration;
    private @NotNull File sqlFilesPath;
    private boolean interServerEnabled;

    public SQLDatabaseMigrator(@NotNull SQLDatabaseAPI database, @NotNull TableNameConfiguration nameConfiguration,@NotNull File sqlFilesPath,boolean interServerEnabled) throws SQLException {
        this.connection = database.getConnection();
        assert !this.connection.isClosed() : "Connection was closed";
        this.nameConfiguration = Objects.requireNonNull(nameConfiguration);
        this.sqlFilesPath = Objects.requireNonNull(sqlFilesPath);
        this.interServerEnabled = interServerEnabled;
        
    }
    
    public void run() throws SQLException {
        run(StorageType.LOCAL);
        if(interServerEnabled) {
            run(StorageType.INTER_SERVER);
        }
    }
    
    private void run(StorageType type) throws SQLException {
        File path = new File(sqlFilesPath, type.toString().toLowerCase());
        int count = 0;
        while (true) {
            InputStream stream = FileHelper.getInputStreamForInternalFile(new File(path, count + ".sql").getPath());
            if (stream == null) {
                break;
            }
            BufferedReader reader = FileHelper.getBufferedReaderFromInputStream(stream);
            Stargate.log(Level.INFO,reader.toString());
            
            
            count++;
            
            
        }
        connection.close();
    }
}
