package org.sgrewritten.stargate.exception.name;

import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;

public class BungeeNameException extends TranslatableException {

    private TranslatableMessage translatableMessage;

    public BungeeNameException(String message, TranslatableMessage translatableMessage) {
        super(message);
        this.translatableMessage = translatableMessage;
    }

    @Override
    protected TranslatableMessage getTranslatableMessage() {
        return translatableMessage;
    }

}
