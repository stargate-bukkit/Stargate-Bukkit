package org.sgrewritten.stargate.exception;

/**
 * An exception thrown when Stargate fails its initialization
 */
public class StargateInitializationException extends Exception {

    /**
     * Instantiates a new Stargate Initialization exception
     *
     * @param message <p>The message describing the cause of the exception</p>
     */
    public StargateInitializationException(String message) {
        super(message);
    }

    /**
     *
     * @param exception <p>The real cause of this exception</p>
     */
    public StargateInitializationException(Throwable exception) {
        super(exception);
    }

}
