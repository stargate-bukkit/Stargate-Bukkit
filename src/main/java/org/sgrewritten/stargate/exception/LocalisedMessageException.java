package org.sgrewritten.stargate.exception;

import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.network.portal.Portal;

public class LocalisedMessageException extends TranslatableException {


    private final String localizedMessage;
    private final transient Portal portal;
    private final MessageType messageType;

    public LocalisedMessageException(String localizedMessage) {
        this(localizedMessage, null, null);
    }

    public LocalisedMessageException(String localizedMessage, @Nullable Portal portal, @Nullable MessageType messageType) {
        super(localizedMessage);
        this.localizedMessage = localizedMessage;
        this.portal = portal;
        this.messageType = messageType;
    }

    @Override
    protected TranslatableMessage getTranslatableMessage() {
        return null;
    }

    @Override
    public String getLocalisedMessage(LanguageManager manager) {
        return this.localizedMessage;
    }

    public @Nullable Portal getPortal() {
        return this.portal;
    }

    public @Nullable MessageType getMessageType() {
        return this.messageType;
    }
}
