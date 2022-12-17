package org.sgrewritten.stargate.exception.name;

import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;

public class NameConflictException extends TranslatableException{
    /**
     * 
     */
    private static final long serialVersionUID = -183581478633277966L;

    public NameConflictException(String message) {
        super(message);
    }

    @Override
    public TranslatableMessage getTranslatableMessage() {
        return TranslatableMessage.ALREADY_EXIST;
    }
}
