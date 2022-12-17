package org.sgrewritten.stargate.exception;

import org.sgrewritten.stargate.formatting.TranslatableMessage;

public abstract class TranslatableException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -7553564667058934584L;

    
    public TranslatableException(String message) {
        super(message);
    }
    
    public abstract TranslatableMessage getTranslatableMessage();
}
