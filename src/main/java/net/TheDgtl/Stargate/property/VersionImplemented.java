package net.TheDgtl.Stargate.property;

/**
 * An enum that contains versions that implemented various Spigot features
 */
public enum VersionImplemented {

    /**
     * The version that added chat color
     */
    CHAT_COLOR("net.md_5.bungee.api.ChatColor");

    private boolean isImplemented;

    /**
     * Instantiates a new version implemented
     *
     * @param classToCheckFor <p> A class which is specific for the version implemented </p>
     */
    VersionImplemented(String classToCheckFor) {
        try {
            Class.forName(classToCheckFor);
            isImplemented = true;
        } catch (ClassNotFoundException ignored) {
            isImplemented = false;
        }
    }


    /**
     * Checks whether the version this feature was implemented is imlemented in the given bukkit instance
     *
     * @return <p> True if the feature is implemented</p>
     */
    public boolean getIsImplemented() {
        return isImplemented;
    }
}
