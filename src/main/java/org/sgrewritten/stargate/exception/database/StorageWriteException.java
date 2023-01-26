package org.sgrewritten.stargate.exception.database;

import java.io.IOException;
import java.io.Serial;

/**
 * An exception thrown if unable to write to Stargate's storage
 */
public class StorageWriteException extends IOException {

    @Serial
    private static final long serialVersionUID = -4126887842322165859L;

    /**
     * Instantiates a new storage write exception
     *
     * @param cause <p>The underlying exception that caused this exception</p>
     */
    public StorageWriteException(Throwable cause) {
        super(cause);
    }

}
