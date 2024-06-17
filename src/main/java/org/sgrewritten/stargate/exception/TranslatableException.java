package org.sgrewritten.stargate.exception;

import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;

import java.io.Serial;

public abstract class TranslatableException extends Exception {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -7553564667058934584L;

    /**
     * @param message <p>The message displayed in stack traces</p>
     */
    protected TranslatableException(String message) {
        super(message);
    }

    /**
     * @return <p>A translatable message that can be used to determine the localized message of this exception</p>
     */
    protected abstract TranslatableMessage getTranslatableMessage();

    /**
     * @param manager <p>A language manager able to provide localized messages</p>
     * @return <p>The localized message of this exception</p>
     */
    public String getLocalisedMessage(LanguageManager manager) {
        return manager.getErrorMessage(getTranslatableMessage());
    }

}
