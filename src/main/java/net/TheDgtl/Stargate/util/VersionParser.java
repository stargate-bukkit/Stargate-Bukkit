package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.ImportantVersion;
import net.TheDgtl.Stargate.Stargate;
import org.bukkit.Bukkit;

import java.util.logging.Level;

/**
 * A parser for parsing and checking the used bukkit version
 */
public class VersionParser {

    /**
     * Checks whether the used bukkit version is newer than the given important version
     *
     * @param version <p>The important version to check for</p>
     * @return <p>True if the bukkit version is newer than the given important version</p>
     */
    public static boolean bukkitIsNewerThan(ImportantVersion version) {
        String versionString = Bukkit.getServer().getBukkitVersion();
        Stargate.log(Level.FINER, versionString);
        String[] splitVersion = versionString.split("\\.");
        if (splitVersion[1].contains("-")) {
            splitVersion[1] = splitVersion[1].split("-")[0];
        }

        int release = Integer.parseInt(splitVersion[0]);
        int major = Integer.parseInt(splitVersion[1]);
        return version.isOlderThan(release, major);
    }

}
