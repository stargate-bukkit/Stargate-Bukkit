package org.sgrewritten.stargate;

import java.util.logging.Level;

public class FakeStargateLogger implements StargateLogger{

    
    @Override
    public void logMessage(Level priorityLevel, String message) {
        System.out.println(message);
    }
}
