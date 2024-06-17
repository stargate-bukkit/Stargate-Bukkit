package org.sgrewritten.stargate.exception.name;

import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.exception.TranslatableException;

import java.io.Serial;

public class NameLengthException extends TranslatableException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 3943182936599325569L;

    /**
     * @param message <p>The message to display on the stacktrace for this exception</p>
     */
    public NameLengthException(String message) {
        super(message);
    }

    @Override
    protected TranslatableMessage getTranslatableMessage() {
        return TranslatableMessage.INVALID_NAME_LENGTH;
    }

}
