package net.TheDgtl.Stargate.network.portal.formatting;

/**
 * The highlighting characters to use around a portal or network name
 */
public enum HighlightingStyle {

    /**
     * The highlighting to use around a portal's name
     */
    PORTAL("-", "-"),

    /**
     * The highlighting to use around a portal's destination
     */
    DESTINATION(">", "<"),

    /**
     * The highlighting to use around a normal network name
     */
    NETWORK("(", ")"),

    /**
     * The highlighting used for legacy bungee stargates' server
     */
    BUNGEE("[", "]"),

    /**
     * No highlighting at all. Just a workaround, really
     */
    NOTHING("", ""),

    /**
     * The highlighting to use for personal networks
     */
    PERSONAL("{", "}");

    private final String prefix;
    private final String suffix;

    /**
     * Instantiates a new highlighting style
     *
     * @param prefix <p>The prefix to prepend to the name</p>
     * @param suffix <p>The suffix to add to the name</p>
     */
    HighlightingStyle(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Gets the given name with the highlighting applied
     *
     * @param name <p>The name to highlight</p>
     * @return <p>The highlighted name</p>
     */
    public String getHighlightedName(String name) {
        return prefix + name + suffix;
    }

}
