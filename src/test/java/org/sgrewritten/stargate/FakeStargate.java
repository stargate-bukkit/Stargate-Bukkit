package org.sgrewritten.stargate;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.logging.Level;

public class FakeStargate extends JavaPlugin implements StargateLogger {

    public FakeStargate() {
        super();
    }

    protected FakeStargate(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void logMessage(Level priorityLevel, String message) {
        Stargate.log(priorityLevel, message);
    }

}
