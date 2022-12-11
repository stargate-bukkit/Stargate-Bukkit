package org.sgrewritten.stargate.exception.name;

/**
 * The name error exception is thrown when a portal name is invalid for whatever reason
 */
public class InvalidNameException extends Exception {

    private static final long serialVersionUID = -9187508162071170232L;

    public InvalidNameException(String message) {
        super(message);
    }
}