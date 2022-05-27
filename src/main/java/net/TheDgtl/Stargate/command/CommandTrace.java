package net.TheDgtl.Stargate.command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.gate.GateFormatHandler;
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
            writer.write("BukkitInstance: " + Bukkit.getServer().getVersion());
            writer.newLine();
            writer.write("Stargate version: " + Stargate.getInstance().getDescription().getVersion());
            writer.newLine();
            writer.write("JavaVersion: " + System.getProperty("java.version"));
            writer.newLine();
            writer.write("OperatingSystem: " + System.getProperty("os.name"));
            writer.newLine();
            writer.write(getGates());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
        sender.sendMessage( String.format("Instance data saved to location '%s'", file.getAbsolutePath()));
        
        return true;
    }

    
    private String getGates() {
        Set<String> approvedGateFiles = GateFormatHandler.getAllGateFormatNames();
        File dir = new File(Stargate.getInstance().DATA_FOLDER, Stargate.getInstance().GATE_FOLDER);
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> name.endsWith(".gate")) : new File[0];
        String msg = "";
        for(File file : files) {
            msg = msg + "\n";
            msg = msg + file.getName() + "\n";
            msg = msg + "isValidDesign=" + approvedGateFiles.contains(file.getName())+ "\n";
            try {
                BufferedReader reader = FileHelper.getBufferedReader(file);
                Iterator<String> lines = reader.lines().iterator();
                while(lines.hasNext()) {
                    msg = msg + lines.next() + "\n";
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return msg;
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
}
