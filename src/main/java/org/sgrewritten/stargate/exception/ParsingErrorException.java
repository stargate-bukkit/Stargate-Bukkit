package org.sgrewritten.stargate.exception;

import java.io.Serial;

public class ParsingErrorException extends Exception {

    @Serial
    private static final long serialVersionUID = -8103799867513880231L;

    /**
     * Instantiates a new parsing error exception
     *
     * @param errorMessage <p>The error message describing the thrown exception</p>
     */
    public ParsingErrorException(String errorMessage) {
        super(errorMessage);
    }
}