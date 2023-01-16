package org.sgrewritten.stargate.exception.name;

import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.exception.TranslatableException;

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
