package org.sgrewritten.stargate.exception.name;

import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.exception.TranslatableException;

import java.io.Serial;

/**
 * The name error exception is thrown when a portal name is invalid for whatever reason
 */
public class InvalidNameException extends TranslatableException {

    @Serial
    private static final long serialVersionUID = -9187508162071170232L;

    public InvalidNameException(String message) {
        super(message);
    }

    @Override
    protected TranslatableMessage getTranslatableMessage() {
        return TranslatableMessage.INVALID_NAME;
    }
}