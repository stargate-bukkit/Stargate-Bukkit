package org.sgrewritten.stargate.exception.database;

import java.io.IOException;
import java.io.Serial;

/**
 * An exception thrown if unable to read from Stargate's storage
 */
public class StorageReadException extends IOException {

    @Serial
    private static final long serialVersionUID = 8947780361329530499L;

    /**
     * Instantiates a new storage read exception
     *
     * @param cause <p>The underlying exception that caused this exception</p>
     */
    public StorageReadException(Throwable cause) {
        super(cause);
    }

}
