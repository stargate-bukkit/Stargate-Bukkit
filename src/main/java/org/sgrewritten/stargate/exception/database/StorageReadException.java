package org.sgrewritten.stargate.exception.database;

import java.io.IOException;

public class StorageReadException extends IOException {
    /**
     *
     */
    private static final long serialVersionUID = 8947780361329530499L;

    public StorageReadException(Throwable cause) {
        super(cause);
    }
}
