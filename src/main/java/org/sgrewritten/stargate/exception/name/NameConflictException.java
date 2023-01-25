package org.sgrewritten.stargate.exception.name;

import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;

public class NameConflictException extends TranslatableException{
    /**
     * 
     */
    private static final long serialVersionUID = -183581478633277966L;
    private boolean isNetwork;

    public NameConflictException(String message, boolean isNetwork) {
        super(message);
        this.isNetwork = isNetwork;
    }

    @Override
    protected TranslatableMessage getTranslatableMessage() {
        return isNetwork ? TranslatableMessage.NET_CONFLICT : TranslatableMessage.GATE_ALREADY_EXIST;
    }
}
