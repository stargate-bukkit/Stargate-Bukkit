package net.TheDgtl.Stargate.database;

import net.TheDgtl.Stargate.StargateLogger;

import java.util.logging.Level;

public class FakeStargate implements StargateLogger {
    @Override
    public void logMessage(Level priorityLevel, String message) {
        System.out.println(message);
    }


}
