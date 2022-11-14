package org.sgrewritten.stargate;

import java.util.logging.Level;

/**
 * A logger for the stargate plugin
 */
public interface StargateLogger {

    /**
     * Logs a message to the console
     *
     * @param priorityLevel <p>The priority level for the logged message</p>
     * @param message       <p>The message to log</p>
     */
    void logMessage(Level priorityLevel, String message);

}
