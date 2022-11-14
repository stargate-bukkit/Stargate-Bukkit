package org.sgrewritten.stargate.network.portal.formatting;

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
     * The highlighting to use for personal networks
     */
    PERSONAL("{", "}"),

    /**
     * No highlighting at all. Just a workaround, really
     */
    NOTHING("", "");


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

    public static HighlightingStyle getHighlightType(String highlightedText) {
        for (HighlightingStyle highlight : HighlightingStyle.values()) {
            if (highlight == HighlightingStyle.NOTHING) {
                continue;
            }

            if (highlightedText.startsWith(highlight.prefix) && highlightedText.endsWith(highlight.suffix)) {
                return highlight;
            }
        }
        return NOTHING;
    }

    /**
     * Gets the plain name from a name with highlighting
     *
     * @param highlightedName <p>The highlighted name</p>
     * @return <p>The plain name</p>
     */
    public static String getNameFromHighlightedText(String highlightedName) {
        HighlightingStyle highlight = getHighlightType(highlightedName);
        return highlightedName.substring(highlight.prefix.length(),
                highlightedName.length() - highlight.suffix.length());
    }

}
