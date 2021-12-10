package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.Stargate;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class VersionParser {
    public enum ImportantVersion {
        NO_CHATCOLOR_IMPLEMENTED(1, 15);
        private int release;
        private int major;

        private ImportantVersion(int release, int major) {
            this.release = release;
            this.major = major;
        }
    }

    public static boolean bukkitIsNewerThan(ImportantVersion version) {
        String versionString = Bukkit.getServer().getBukkitVersion();
        Stargate.log(Level.FINEST, versionString);
        String[] splitedVersion = versionString.split("\\.");
        return !(version.release > Integer.valueOf(splitedVersion[0])) && !(version.major >= Integer.valueOf(splitedVersion[1]));
    }
}
