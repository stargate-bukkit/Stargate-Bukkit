package org.sgrewritten.stargate.exception;

import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * Generic exception for any exception that can directly provide a localized message
 */
public class LocalisedMessageException extends TranslatableException {


    private final String localizedMessage;
    private final transient Portal portal;
    private final MessageType messageType;

    /**
     * @param localizedMessage <p>The localized message of this exception</p>
     */
    public LocalisedMessageException(String localizedMessage) {
        this(localizedMessage, null, null);
    }

    /**
     * @param localizedMessage <p>the localized message of this exception</p>
     * @param portal           <p>The the portal of this exception</p>
     * @param messageType      <p>The message type of this exception</p>
     */
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

    /**
     * @return <p>The portal where this exception came from</p>
     */
    public @Nullable Portal getPortal() {
        return this.portal;
    }

    /**
     * @return <p>The type of this localized message</p>
     */
    public @Nullable MessageType getMessageType() {
        return this.messageType;
    }
}
