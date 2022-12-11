package org.sgrewritten.stargate.exception.name;

import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;

public class NameLengthException extends TranslatableException {

    /**
     * 
     */
    private static final long serialVersionUID = 3943182936599325569L;

    public NameLengthException(String message) {
        super(message);
    }

    @Override
    public TranslatableMessage getTranslatableMessage() {
        return TranslatableMessage.INVALID_NAME;
    }

}
