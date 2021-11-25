package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

/**
 * The update checker is responsible for looking for new updates
 */
public final class UpdateChecker {

    private final static String APIResourceURL = "https://api.spigotmc.org/legacy/update.php?resource=97784";
    private final static String updateNotice = "A new update is available: %s (You are still on %s)";

    private UpdateChecker() {

    }

    /**
     * Checks if there's a new update available, and alerts the user if necessary
     */
    public static void checkForUpdate() {
        BukkitScheduler scheduler = Stargate.getInstance().getServer().getScheduler();
        scheduler.runTaskAsynchronously(Stargate.getInstance(), UpdateChecker::queryAPI);
    }

    /**
     * Queries the spigot API to check for a newer version, and informs the user
     */
    private static void queryAPI() {
        try {
            InputStream inputStream = new URL(APIResourceURL).openStream();
            BufferedReader reader = FileHelper.getBufferedReaderFromInputStream(inputStream);
            //There should only be one line of output
            String newVersion = reader.readLine();
            reader.close();

            String oldVersion = Stargate.getPluginVersion();
            //If there is a newer version, notify the user
            if (isVersionHigher(oldVersion, newVersion)) {
                Stargate.getConsoleLogger().log(Level.INFO, Stargate.getBackupString("prefix") +
                        getUpdateAvailableString(newVersion, oldVersion));
                Stargate.setUpdateAvailable(newVersion);
            }
        } catch (IOException e) {
            Stargate.debug("UpdateChecker", "Unable to get newest version.");
        }
    }

    /**
     * Gets the string to display to a user to alert about a new update
     *
     * @param newVersion <p>The new available plugin version</p>
     * @param oldVersion <p>The old (current) plugin version</p>
     * @return <p>The string to display</p>
     */
    public static String getUpdateAvailableString(String newVersion, String oldVersion) {
        return String.format(updateNotice, newVersion, oldVersion);
    }

    /**
     * Decides whether one version number is higher than another
     *
     * @param oldVersion <p>The old version to check</p>
     * @param newVersion <p>The new version to check</p>
     * @return <p>True if the new version is higher than the old one</p>
     */
    public static boolean isVersionHigher(String oldVersion, String newVersion) {
        String[] oldVersionParts = oldVersion.split("\\.");
        String[] newVersionParts = newVersion.split("\\.");
        int versionLength = Math.max(oldVersionParts.length, newVersionParts.length);
        for (int i = 0; i < versionLength; i++) {
            int oldVersionNumber = oldVersionParts.length > i ? Integer.parseInt(oldVersionParts[i]) : 0;
            int newVersionNumber = newVersionParts.length > i ? Integer.parseInt(newVersionParts[i]) : 0;
            if (newVersionNumber != oldVersionNumber) {
                return newVersionNumber > oldVersionNumber;
            }
        }
        return false;
    }

}
