package org.sgrewritten.stargate.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.property.CommandPermission;
import org.sgrewritten.stargate.util.FileHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class CommandTrace implements CommandExecutor {

    private final Stargate stargate;


    public CommandTrace(@NotNull Stargate stargate) {
        this.stargate = Objects.requireNonNull(stargate);
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 1) {
            return false;
        }
        if (!sender.hasPermission(CommandPermission.TRACE.getPermissionNode())) {
            sender.sendMessage(stargate.getLanguageManager().getErrorMessage(TranslatableMessage.DENY));
            return true;
        }
        File directory = new File(stargate.getDataFolder(), "debug");
        if (!directory.exists() && !directory.mkdir()) {
            sender.sendMessage(ChatColor.RED + "Unable to create the debug directory. Make sure permissions for " +
                    "the Stargate folder are correct. Cannot continue.");
            return true;
        }
        String fileName = "stargate." + System.currentTimeMillis() + ".txt";
        File file = new File(directory, fileName);
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
            Stargate.log(e);
            return true;
        }
        sender.sendMessage(String.format("Instance data saved to location '%s'", file.getAbsolutePath()));

        return true;
    }

    private String getGates() {
        File dir = new File(Stargate.getInstance().getAbsoluteDataFolder(), ConfigurationHelper.getString(ConfigurationOption.GATE_FOLDER));
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> (name.endsWith(".gate") || name.endsWith(".gate.invalid"))) : new File[0];
        StringBuilder stringBuilder = new StringBuilder();
        if (files == null) {
            return stringBuilder.toString();
        }
        for (File file : files) {
            stringBuilder.append("\n");
            stringBuilder.append(file.getName()).append("\n");
            try {
                BufferedReader reader = FileHelper.getBufferedReader(file);
                Iterator<String> lines = reader.lines().iterator();
                while (lines.hasNext()) {
                    stringBuilder.append(lines.next()).append("\n");
                }
            } catch (IOException e) {
                Stargate.log(e);
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
