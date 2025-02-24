package org.sgrewritten.stargate.command;

import org.apache.commons.io.FileUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.database.property.StoredPropertiesAPI;
import org.sgrewritten.stargate.database.property.StoredProperty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;

public class CommandParity implements CommandExecutor {

    private final @NotNull StoredPropertiesAPI properties;
    private final boolean doParityUpgrades;
    private static final String MECHANICS_URL = "https://sgrewritten.org/downloadsgm";
    private static final String MECHANICS_NAME = "StargateMechanics";
    private static final String INTERFACES_URL = "https://sgrewritten.org/downloadsgi";
    private static final String INTERFACES_NAME = "StargateInterfaces";
    private static final String CUSTOMIZATIONS_URL = "https://sgrewritten.org/downloadsgc";
    private static final String CUSTOMIZATIONS_NAME = "StargateCustomizations";
    private static final String MAPPER_URL = "https://sgrewritten.org/downloadmapper";
    private static final String MAPPER_NAME = "StargateMapper";
    private static final String OLD_CONFIG_LOCATION = "debug/config.yml.old";
    private final File pluginFolder;
    private final LanguageManager languageManager;
    private final File oldConfig;
    private URL mapper;
    private URL mechanics;
    private URL interfaces;
    private URL customizations;

    CommandParity(@NotNull StoredPropertiesAPI properties, boolean doParityUpgrades, File pluginFolder, LanguageManager languageManager) {
        this(properties, doParityUpgrades, pluginFolder, MECHANICS_URL, INTERFACES_URL, CUSTOMIZATIONS_URL, MAPPER_URL, new File(pluginFolder, OLD_CONFIG_LOCATION), languageManager);
    }

    CommandParity(@NotNull StoredPropertiesAPI properties, boolean doParityUpgrades, File pluginFolder, String mechanics, String interfaces
            , String customizations, String mapper, File oldConfig, LanguageManager languageManager) {
        this.properties = Objects.requireNonNull(properties);
        this.doParityUpgrades = doParityUpgrades;
        this.pluginFolder = pluginFolder;
        this.languageManager = languageManager;
        this.oldConfig = oldConfig;
        try {
            this.mechanics = new URL(mechanics);
            this.interfaces = new URL(interfaces);
            this.customizations = new URL(customizations);
            this.mapper = new URL(mapper);
        } catch (MalformedURLException e) {
            Stargate.log(e);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        String nagAuthor = properties.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE);
        if (!(sender instanceof ConsoleCommandSender) || nagAuthor == null || !nagAuthor.equals("true")) {
            return false;
        }
        if (!doParityUpgrades) {
            properties.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, "false");
            Stargate.log(Level.INFO, " Rejected parity upgrades.");
            return true;
        }
        File pluginsFolder = pluginFolder.getParentFile();
        File mechanicsFile = determineDestinationJarName(mechanics, MECHANICS_NAME, pluginsFolder);
        File interfacesFile = determineDestinationJarName(interfaces, INTERFACES_NAME, pluginsFolder);
        File customizationsFile = determineDestinationJarName(customizations, CUSTOMIZATIONS_NAME, pluginsFolder);
        File mapperFile = determineDestinationJarName(mapper, MAPPER_NAME, pluginsFolder);
        sender.sendMessage(languageManager.getWarningMessage(TranslatableMessage.PARITY_UPGRADE).replace("%modules%",
                String.format("%s, %s, %s, %s",MECHANICS_NAME,INTERFACES_NAME,CUSTOMIZATIONS_NAME,MAPPER_NAME)));
        try {
            downloadPlugin(mechanics, mechanicsFile);
            downloadPlugin(interfaces, interfacesFile);
            downloadPlugin(customizations, customizationsFile);
            downloadPlugin(mapper, mapperFile);
            if (oldConfig.exists()) {
                File targetDir = new File(pluginsFolder, CUSTOMIZATIONS_NAME);
                if (!targetDir.exists() && !targetDir.mkdir()) {
                    throw new IOException("Could not create directory: " + targetDir);
                }
                FileUtils.copyFile(oldConfig, new File(targetDir, "old_stargate_config.yml"));
            }
        } catch (IOException e) {
            Stargate.log(e);
            Stargate.log(Level.WARNING, "Unable to download modules. It could be that they are not completed yet");
        }
        properties.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, false);
        return true;
    }

    private void downloadPlugin(URL source, File destination) throws IOException {
        if (destination.exists()) {
            return;
        }
        if (!destination.createNewFile()) {
            throw new IOException("Could not create new file: " + destination);
        }
        try (InputStream inputStream = source.openStream()) {
            try (FileOutputStream outputStream = new FileOutputStream(destination)) {
                inputStream.transferTo(outputStream);
            }
        }
    }

    private File determineDestinationJarName(URL url, String pluginName, File pluginsFolder) {
        String urlPath = url.getPath();
        String[] urlPathSplit = urlPath.split("-", 0);
        String fileName;
        int urlPathSplitLength = urlPathSplit.length;
        if (urlPathSplitLength > 2) {
            fileName = pluginName + "-" + urlPathSplit[urlPathSplitLength - 2] + "-" + urlPathSplit[urlPathSplitLength - 1];
        } else {
            fileName = pluginName + ".jar";
        }
        if (!fileName.endsWith(".jar")) {
            fileName = fileName + ".jar";
        }
        return new File(pluginsFolder, fileName);
    }

}
