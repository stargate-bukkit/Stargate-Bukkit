package org.sgrewritten.stargate.exception.name;

import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.exception.TranslatableException;

import java.io.Serial;

public class NameConflictException extends TranslatableException {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -183581478633277966L;
    private final boolean isNetwork;

    /**
     * @param message <p>The message to display on the stacktrace for this exception</p>
     * @param isNetwork <p>Whether this is a network or portal name conflict</p>
     */
    public NameConflictException(String message, boolean isNetwork) {
        super(message);
        this.isNetwork = isNetwork;
    }

    @Override
    protected TranslatableMessage getTranslatableMessage() {
        return isNetwork ? TranslatableMessage.NET_CONFLICT : TranslatableMessage.GATE_ALREADY_EXIST;
    }
}
