package org.sgrewritten.stargate;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

public class FakeStargate extends JavaPlugin implements StargateLogger {

    public FakeStargate()
    {
        super();
    }

    protected FakeStargate(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file)
    {
        super(loader, description, dataFolder, file);
    }
    
    @Override
    public void logMessage(Level priorityLevel, String message) {
        System.out.println(message);
    }

}
