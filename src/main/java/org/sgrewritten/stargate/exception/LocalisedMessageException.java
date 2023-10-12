package org.sgrewritten.stargate.exception;

import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.event.portal.StargateSendMessagePortalEvent;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.formatting.TranslatableMessage;

public class LocalisedMessageException extends TranslatableException {


    private final String localizedMessage;
    private final Portal portal;
    private final StargateSendMessagePortalEvent.MessageType messageType;

    public LocalisedMessageException(String localizedMessage) {
        this(localizedMessage, null, null);
    }

    public LocalisedMessageException(String localizedMessage, @Nullable Portal portal, @Nullable StargateSendMessagePortalEvent.MessageType messageType) {
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

    public @Nullable Portal getPortal(){
        return this.portal;
    }

    public @Nullable StargateSendMessagePortalEvent.MessageType getMessageType(){
        return this.messageType;
    }
}
