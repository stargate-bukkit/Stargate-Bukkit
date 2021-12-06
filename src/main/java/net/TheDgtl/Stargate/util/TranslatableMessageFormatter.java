package net.TheDgtl.Stargate.util;

/**
 * The translatable message formatter is responsible for formatting translatable messages
 *
 * <p>The translatable message formatter is mainly used for replacing placeholders in translatable message</p>
 */
public class TranslatableMessageFormatter {

    static private final String COST_INSERTION_IDENTIFIER = "%cost%";
    static private final String PORTAL_INSERTION_IDENTIFIER = "%portal%";
    static private final String WORLD_INSERTION_IDENTIFIER = "%world%";

    /**
     * Replaces the %cost% in a translatable message with the actual cost
     *
     * @param unformattedMessage <p>The unformatted message to format</p>
     * @param cost               <p>The cost to insert</p>
     * @return <p>The message with the cost placeholder replaced with the actual cost</p>
     */
    public static String compileCost(String unformattedMessage, int cost) {
        return unformattedMessage.replace(COST_INSERTION_IDENTIFIER, String.valueOf(cost));
    }

    /**
     * Replaces the %portal% in a translatable message with the relevant portal name
     *
     * @param unformattedMessage <p>The unformatted message to format</p>
     * @param portalName         <p>The portal name to replace the placeholder with</p>
     * @return <p>The message with the portal placeholder replaced with the relevant portal name</p>
     */
    public static String compilePortal(String unformattedMessage, String portalName) {
        return unformattedMessage.replace(PORTAL_INSERTION_IDENTIFIER, portalName);
    }

    public static String compileWorld(String unformattedMessage, String worldName) {
        return unformattedMessage.replace(WORLD_INSERTION_IDENTIFIER, worldName);
    }

}
