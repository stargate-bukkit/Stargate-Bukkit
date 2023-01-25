package org.sgrewritten.stargate.exception.name;

import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;

/**
 * The name error exception is thrown when a portal name is invalid for whatever reason
 */
public class InvalidNameException extends TranslatableException {

    private static final long serialVersionUID = -9187508162071170232L;

    public InvalidNameException(String message) {
        super(message);
    }

    @Override
    protected TranslatableMessage getTranslatableMessage() {
        return TranslatableMessage.INVALID_NAME;
    }
}