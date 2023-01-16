package org.sgrewritten.stargate.exception.name;

import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.exception.TranslatableException;

public class BungeeNameException extends TranslatableException{

    private TranslatableMessage translatableMessage;

    public BungeeNameException(String message, TranslatableMessage translatableMessage) {
        super(message);
        this.translatableMessage = translatableMessage;
    }

    @Override
    public TranslatableMessage getTranslatableMessage() {
        return translatableMessage;
    }

}
