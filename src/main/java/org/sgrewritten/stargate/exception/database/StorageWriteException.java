package org.sgrewritten.stargate.exception.database;

import java.io.IOException;

public class StorageWriteException extends IOException  {
    
    /**
     * 
     */
    private static final long serialVersionUID = -4126887842322165859L;

    public StorageWriteException(Throwable cause) {
        super(cause);
    }
}
