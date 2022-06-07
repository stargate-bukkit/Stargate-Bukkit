package net.TheDgtl.Stargate.command;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.gate.GateFormatHandler;
import net.TheDgtl.Stargate.property.CommandPermission;
import net.TheDgtl.Stargate.util.FileHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

public class CommandTrace implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission(CommandPermission.TRACE.getPermissionNode())) {
            sender.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.DENY));
            return true;
        }
        String fileName = "stargate." + System.currentTimeMillis() + ".txt";
        File file = new File(Stargate.getInstance().getDataFolder(), fileName);
        BufferedWriter writer;
        try {
            writer = FileHelper.getBufferedWriter(file, false);
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
        sender.sendMessage(String.format("Instance data saved to location '%s'", file.getAbsolutePath()));

        return true;
    }

    private String getGates() {
        Set<String> approvedGateFiles = GateFormatHandler.getAllGateFormatNames();
        File dir = new File(Stargate.getInstance().DATA_FOLDER, Stargate.getInstance().GATE_FOLDER);
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> name.endsWith(".gate")) : new File[0];
        StringBuilder stringBuilder = new StringBuilder();
        if (files == null) {
            return stringBuilder.toString();
        }
        for (File file : files) {
            stringBuilder.append("\n");
            stringBuilder.append(file.getName()).append("\n");
            stringBuilder.append("isValidDesign=").append(approvedGateFiles.contains(file.getName())).append("\n");
            try {
                BufferedReader reader = FileHelper.getBufferedReader(file);
                Iterator<String> lines = reader.lines().iterator();
                while (lines.hasNext()) {
                    stringBuilder.append(lines.next()).append("\n");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }


    private String getConfiguration() {
        ConfigurationOption[] excludeArray = {ConfigurationOption.BUNGEE_ADDRESS, ConfigurationOption.BUNGEE_DATABASE, ConfigurationOption.BUNGEE_PASSWORD, ConfigurationOption.BUNGEE_USERNAME, ConfigurationOption.BUNGEE_PORT};
        Set<ConfigurationOption> exclude = EnumSet.noneOf(ConfigurationOption.class);
        Collections.addAll(exclude, excludeArray);
        Set<ConfigurationOption> include = EnumSet.allOf(ConfigurationOption.class);
        include.removeAll(exclude);
        Configuration loadConfig = Stargate.getFileConfiguration();
        YamlConfiguration dumpConfig = new YamlConfiguration();
        for (ConfigurationOption option : include) {
            String node = option.getConfigNode();
            Stargate.log(Level.FINE, "Adding config-node " + node);
            dumpConfig.set(node, loadConfig.get(node));
        }
        return dumpConfig.saveToString();
    }
}
