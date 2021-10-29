package net.knarcraft.stargate.config;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockChangeRequest;
import net.knarcraft.stargate.listener.BungeeCordListener;
import net.knarcraft.stargate.portal.GateHandler;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.PortalRegistry;
import net.knarcraft.stargate.thread.BlockChangeThread;
import net.knarcraft.stargate.utility.FileHelper;
import net.knarcraft.stargate.utility.PortalFileHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.messaging.Messenger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * The stargate config is responsible for keeping track of all configuration values
 */
public final class StargateConfig {

    private final Queue<Portal> activePortalsQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Portal> openPortalsQueue = new ConcurrentLinkedQueue<>();
    private final HashSet<String> managedWorlds = new HashSet<>();

    private StargateGateConfig stargateGateConfig;
    private MessageSender messageSender;
    private final LanguageLoader languageLoader;
    private EconomyConfig economyConfig;
    private final Logger logger;

    private final String dataFolderPath;
    private String gateFolder;
    private String portalFolder;
    private String languageName = "en";

    private boolean debuggingEnabled = false;
    private boolean permissionDebuggingEnabled = false;

    /**
     * Instantiates a new stargate config
     *
     * @param logger <p>The logger to use for logging errors</p>
     */
    public StargateConfig(Logger logger) {
        this.logger = logger;

        dataFolderPath = Stargate.stargate.getDataFolder().getPath().replaceAll("\\\\", "/");
        portalFolder = dataFolderPath + "/portals/";
        gateFolder = dataFolderPath + "/gates/";
        languageLoader = new LanguageLoader(dataFolderPath + "/lang/");

        this.loadConfig();

        //Enable the required channels for Bungee support
        if (stargateGateConfig.enableBungee()) {
            startStopBungeeListener(true);
        }
    }

    /**
     * Finish the config setup by loading languages, gates and portals, and loading economy if vault is loaded
     */
    public void finishSetup() {
        //Set the chosen language and reload the language loader
        languageLoader.setChosenLanguage(languageName);
        languageLoader.reload();

        messageSender = new MessageSender(languageLoader);
        if (debuggingEnabled) {
            languageLoader.debug();
        }

        this.createMissingFolders();
        this.loadGates();
        this.loadAllPortals();

        //Set up vault economy if vault has been loaded
        setupVaultEconomy();
    }

    /**
     * Gets the queue of open portals
     *
     * <p>The open portals queue is used to close open portals after some time has passed</p>
     *
     * @return <p>The open portals queue</p>
     */
    public Queue<Portal> getOpenPortalsQueue() {
        return openPortalsQueue;
    }

    /**
     * Gets the queue of active portals
     *
     * <p>The active portals queue is used to de-activate portals after some time has passed</p>
     *
     * @return <p>The active portals queue</p>
     */
    public Queue<Portal> getActivePortalsQueue() {
        return activePortalsQueue;
    }

    /**
     * Gets whether debugging is enabled
     *
     * @return <p>Whether debugging is enabled</p>
     */
    public boolean isDebuggingEnabled() {
        return debuggingEnabled;
    }

    /**
     * Gets whether permission debugging is enabled
     *
     * @return <p>Whether permission debugging is enabled</p>
     */
    public boolean isPermissionDebuggingEnabled() {
        return permissionDebuggingEnabled;
    }

    /**
     * Gets the object containing economy config values
     *
     * @return <p>The object containing economy config values</p>
     */
    public EconomyConfig getEconomyConfig() {
        return this.economyConfig;
    }

    /**
     * Reloads all portals and files
     *
     * @param sender <p>The sender of the reload request</p>
     */
    public void reload(CommandSender sender) {
        //Unload all saved data
        unload();

        //Perform all block change requests to prevent mismatch if a gate's open-material changes. Changing the 
        // closed-material still requires a restart.
        BlockChangeRequest firstElement = Stargate.getBlockChangeRequestQueue().peek();
        while (firstElement != null) {
            BlockChangeThread.pollQueue();
            firstElement = Stargate.getBlockChangeRequestQueue().peek();
        }

        //Store the old enable bungee state in case it changes
        boolean oldEnableBungee = stargateGateConfig.enableBungee();

        //Load all data
        load();

        //Enable or disable the required channels for Bungee support
        if (oldEnableBungee != stargateGateConfig.enableBungee()) {
            startStopBungeeListener(stargateGateConfig.enableBungee());
        }

        messageSender.sendErrorMessage(sender, languageLoader.getString("reloaded"));
    }

    /**
     * Un-loads all loaded data
     */
    private void unload() {
        //De-activate all currently active portals
        for (Portal activePortal : activePortalsQueue) {
            activePortal.getPortalActivator().deactivate();
        }
        //Force all portals to close
        closeAllOpenPortals();
        PortalHandler.closeAllPortals();

        //Clear queues and lists
        activePortalsQueue.clear();
        openPortalsQueue.clear();
        managedWorlds.clear();

        //Clear all loaded portals
        PortalRegistry.clearPortals();

        //Clear all loaded gates
        GateHandler.clearGates();
    }

    /**
     * Clears the set of managed worlds
     */
    public void clearManagedWorlds() {
        managedWorlds.clear();
    }

    /**
     * Gets a copy of the set of managed worlds
     *
     * @return <p>The managed worlds</p>
     */
    public Set<String> getManagedWorlds() {
        return new HashSet<>(managedWorlds);
    }

    /**
     * Adds a world to the managed worlds
     *
     * @param worldName <p>The name of the world to manage</p>
     */
    public void addManagedWorld(String worldName) {
        managedWorlds.add(worldName);
    }

    /**
     * Removes a world from the managed worlds
     *
     * @param worldName <p>The name of the world to stop managing</p>
     */
    public void removeManagedWorld(String worldName) {
        managedWorlds.remove(worldName);
    }

    /**
     * Loads all necessary data
     */
    private void load() {
        //Load the config from disk
        loadConfig();

        //Load all gates
        loadGates();

        //Load all portals
        loadAllPortals();

        //Update the language loader in case the loaded language changed
        languageLoader.setChosenLanguage(languageName);
        languageLoader.reload();
        if (debuggingEnabled) {
            languageLoader.debug();
        }

        //Load Economy support if enabled/clear if disabled
        reloadEconomy();
    }

    /**
     * Starts the listener for listening to BungeeCord messages
     */
    private void startStopBungeeListener(boolean start) {
        Messenger messenger = Bukkit.getMessenger();
        String bungeeChannel = "BungeeCord";

        if (start) {
            messenger.registerOutgoingPluginChannel(Stargate.stargate, bungeeChannel);
            messenger.registerIncomingPluginChannel(Stargate.stargate, bungeeChannel, new BungeeCordListener());
        } else {
            messenger.unregisterIncomingPluginChannel(Stargate.stargate, bungeeChannel);
            messenger.unregisterOutgoingPluginChannel(Stargate.stargate, bungeeChannel);
        }
    }

    /**
     * Reloads economy by enabling or disabling it as necessary
     */
    private void reloadEconomy() {
        EconomyConfig economyConfig = getEconomyConfig();
        if (economyConfig.isEconomyEnabled() && economyConfig.getEconomy() == null) {
            setupVaultEconomy();
        } else if (!economyConfig.isEconomyEnabled()) {
            economyConfig.disableEconomy();
        }
    }

    /**
     * Forces all open portals to close
     */
    public void closeAllOpenPortals() {
        for (Portal openPortal : openPortalsQueue) {
            openPortal.getPortalOpener().closePortal(false);
        }
    }

    /**
     * Loads all config values
     */
    public void loadConfig() {
        Stargate.stargate.reloadConfig();
        FileConfiguration newConfig = Stargate.stargate.getConfig();

        boolean isMigrating = false;
        if (newConfig.getString("lang") != null ||
                newConfig.getString("gates.integrity.ignoreEntrance") != null ||
                newConfig.getString("ignoreEntrance") != null) {
            migrateConfig(newConfig);
            isMigrating = true;
        }

        //Copy missing default values if any values are missing
        newConfig.options().copyDefaults(true);

        //Get the language name from the config
        languageName = newConfig.getString("language");

        //Get important folders from the config
        portalFolder = newConfig.getString("folders.portalFolder");
        gateFolder = newConfig.getString("folders.gateFolder");

        //Get enabled debug settings from the config
        debuggingEnabled = newConfig.getBoolean("debugging.debug");
        permissionDebuggingEnabled = newConfig.getBoolean("debugging.permissionDebug");

        //If users have an outdated config, assume they also need to update their default gates
        if (isMigrating) {
            GateHandler.writeDefaultGatesToFolder(gateFolder);
        }

        //Load all gate config values
        stargateGateConfig = new StargateGateConfig(newConfig);

        //Load all economy config values
        economyConfig = new EconomyConfig(newConfig);

        Stargate.stargate.saveConfig();
    }

    /**
     * Gets the object containing configuration values regarding gates
     *
     * @return <p>Gets the gate config</p>
     */
    public StargateGateConfig getStargateGateConfig() {
        return stargateGateConfig;
    }

    /**
     * Loads all available gates
     */
    public void loadGates() {
        GateHandler.loadGates(gateFolder);
        Stargate.logInfo(String.format("Loaded %s gate layouts", GateHandler.getGateCount()));
    }

    /**
     * Changes all configuration values from the old name to the new name
     *
     * @param newConfig <p>The config to read from and write to</p>
     */
    private void migrateConfig(FileConfiguration newConfig) {
        //Save the old config just in case something goes wrong
        try {
            newConfig.save(dataFolderPath + "/config.yml.old");
        } catch (IOException e) {
            Stargate.debug("Stargate::migrateConfig", "Unable to save old backup and do migration");
            e.printStackTrace();
            return;
        }

        //Read all available config migrations
        Map<String, String> migrationFields;
        try {
            migrationFields = FileHelper.readKeyValuePairs(FileHelper.getBufferedReaderFromInputStream(
                    FileHelper.getInputStreamForInternalFile("/config-migrations.txt")));
        } catch (IOException e) {
            Stargate.debug("Stargate::migrateConfig", "Unable to load config migration file");
            e.printStackTrace();
            return;
        }

        //Replace old config names with the new ones
        for (String key : migrationFields.keySet()) {
            if (newConfig.contains(key)) {
                String newPath = migrationFields.get(key);
                Object oldValue = newConfig.get(key);
                if (!newPath.trim().isEmpty()) {
                    newConfig.set(newPath, oldValue);
                }
                newConfig.set(key, null);
            }
        }
    }

    /**
     * Loads economy from Vault
     */
    private void setupVaultEconomy() {
        EconomyConfig economyConfig = getEconomyConfig();
        if (economyConfig.setupEconomy(Stargate.getPluginManager()) && economyConfig.getEconomy() != null) {
            String vaultVersion = economyConfig.getVault().getDescription().getVersion();
            Stargate.logInfo(Stargate.replaceVars(Stargate.getString("vaultLoaded"), "%version%", vaultVersion));
        }
    }

    /**
     * Loads all portals in all un-managed worlds
     */
    public void loadAllPortals() {
        for (World world : Stargate.stargate.getServer().getWorlds()) {
            if (!managedWorlds.contains(world.getName())) {
                PortalFileHelper.loadAllPortals(world);
                managedWorlds.add(world.getName());
            }
        }
    }

    /**
     * Creates missing folders
     */
    private void createMissingFolders() {
        File newPortalDir = new File(portalFolder);
        if (!newPortalDir.exists()) {
            if (!newPortalDir.mkdirs()) {
                logger.severe("Unable to create portal directory");
            }
        }
        File newFile = new File(portalFolder, Stargate.stargate.getServer().getWorlds().get(0).getName() + ".db");
        if (!newFile.exists() && !newFile.getParentFile().exists()) {
            if (!newFile.getParentFile().mkdirs()) {
                logger.severe("Unable to create portal database folder: " + newFile.getParentFile().getPath());
            }
        }
    }

    /**
     * Gets the folder all portals are stored in
     *
     * @return <p>The portal folder</p>
     */
    public String getPortalFolder() {
        return portalFolder;
    }

    /**
     * Gets the folder storing gate files
     *
     * <p>The returned String path is the full path to the folder</p>
     *
     * @return <p>The folder storing gate files</p>
     */
    public String getGateFolder() {
        return gateFolder;
    }

    /**
     * Gets the sender for sending messages to players
     *
     * @return <p>The sender for sending messages to players</p>
     */
    public MessageSender getMessageSender() {
        return messageSender;
    }

    /**
     * Gets the language loader containing translated strings
     *
     * @return <p>The language loader</p>
     */
    public LanguageLoader getLanguageLoader() {
        return languageLoader;
    }
}
