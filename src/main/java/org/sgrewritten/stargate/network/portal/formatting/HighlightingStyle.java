package org.sgrewritten.stargate.network.portal.formatting;

/**
 * The highlighting characters to use around a portal or network name
 */
public enum HighlightingStyle {

    /**
     * The highlighting to use around a portal's name
     */
    MINUS_SIGN("-", "-"),

    /**
     * The highlighting to use around a portal's destination
     */
    LESSER_GREATER_THAN(">", "<"),

    /**
     * HighlightingStyle generally used for terminal gates
     */
    GREATER_LESSER_THAN("<", ">"),
    /**
     * The highlighting to use around a normal network name
     */
    ROUNDED_BRACKETS("(", ")"),

    /**
     * The highlighting used for the destination server of legacy bungee stargates
     */
    SQUARE_BRACKETS("[", "]"),

    /**
     * The highlighting to use for personal networks
     */
    CURLY_BRACKETS("{", "}"),

    /**
     * A variant of GREATER_LESSER_THAN
     */
    DOUBLE_GREATER_LESSER_THAN("«", "»"),

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

    public String getPrefix(){
        return this.prefix;
    }

    public String getSuffix(){
        return this.suffix;
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
