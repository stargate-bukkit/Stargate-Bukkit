package org.sgrewritten.stargate.exception;

import org.sgrewritten.stargate.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;

public abstract class TranslatableException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -7553564667058934584L;


    public TranslatableException(String message) {
        super(message);
    }

    protected abstract TranslatableMessage getTranslatableMessage();

    public String getLocalisedMessage(LanguageManager manager) {
        return manager.getErrorMessage(getTranslatableMessage());
    }

    ;
}
