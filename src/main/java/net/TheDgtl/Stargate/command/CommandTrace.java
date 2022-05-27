package net.TheDgtl.Stargate.command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.property.CommandPermission;
import net.TheDgtl.Stargate.util.FileHelper;

public class CommandTrace implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(CommandPermission.TRACE.getPermissionNode())) {
            sender.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.DENY));
            return true;
        }
        String fileName = "stargate." + System.currentTimeMillis() + ".txt";
        File file = new File(Stargate.getInstance().getDataFolder(),fileName);
        BufferedWriter writer;
        try {
            writer = FileHelper.getBufferedWriter(file);
            writer.write(getConfiguration());
            writer.write("BukkitVersion: " + Bukkit.getServer().getBukkitVersion());
            writer.newLine();
            writer.write("UsingSpigot: " + String.valueOf(usingSpigot()));
            writer.newLine();
            writer.write("Stargate version: " + Stargate.getInstance().getDescription().getVersion());
            writer.newLine();
            
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
        sender.sendMessage( String.format("Instance data saved to location '%s'", file.getAbsolutePath()));
        
        return true;
    }

    
    private String getConfiguration() {
        ConfigurationOption[] excludeArray = {ConfigurationOption.BUNGEE_ADDRESS,ConfigurationOption.BUNGEE_DATABASE,ConfigurationOption.BUNGEE_PASSWORD,ConfigurationOption.BUNGEE_USERNAME,ConfigurationOption.BUNGEE_PORT};
        Set<ConfigurationOption> exclude = EnumSet.noneOf(ConfigurationOption.class);
        Collections.addAll(exclude, excludeArray);
        Set<ConfigurationOption> include = EnumSet.allOf(ConfigurationOption.class);
        include.removeAll(exclude);
        Configuration loadConfig = Stargate.getFileConfiguration();
        YamlConfiguration dumpConfig = new YamlConfiguration();
        for(ConfigurationOption option : include) {
            String node = option.getConfigNode();
            Stargate.log(Level.FINE, "Adding confignode " + node);
            dumpConfig.set(node, loadConfig.get(node));
        }
        return dumpConfig.saveToString();
    }
    
    private boolean usingSpigot() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
