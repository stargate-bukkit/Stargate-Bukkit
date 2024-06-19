package org.sgrewritten.stargate.exception.name;

import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.exception.TranslatableException;

public class BungeeNameException extends TranslatableException {

    private final TranslatableMessage translatableMessage;

    /**
     *
     * @param message <p>The message to display on the stacktrace for this exception</p>
     * @param translatableMessage <p>The translatable message of this exception</p>
     */
    public BungeeNameException(String message, TranslatableMessage translatableMessage) {
        super(message);
        this.translatableMessage = translatableMessage;
    }

    @Override
    protected TranslatableMessage getTranslatableMessage() {
        return translatableMessage;
    }

}
