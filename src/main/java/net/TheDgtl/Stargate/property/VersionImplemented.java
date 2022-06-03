package net.TheDgtl.Stargate.property;

/**
 * An enum that contains versions that implemented various Spigot features
 */
public enum VersionImplemented {

    /**
     * The version that added chat color as non enum object
     */
    CHAT_COLOR("net.md_5.bungee.api.ChatColor", "of", String.class);

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

    VersionImplemented(String classToCheckFor, String methodInClassToCheckFor, Class<?>... parameterTypes) {
        try {
            Class<?> aClass = Class.forName(classToCheckFor);
            aClass.getMethod(methodInClassToCheckFor, parameterTypes);
            isImplemented = true;
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
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
