package net.TheDgtl.Stargate.util;

public class TranslatableMessageFormatter {

    static private final String COST_INSERTION_IDENTIFIER = "%cost%";
    static private final String PORTAL_INSERTION_IDENTIFIER = "%portal%";

    /**
     * Inserts the cost into the un-formatted message on any %cost% string
     *
     * @param unformattedMessage
     * @param cost
     * @return
     */
    public static String compileCost(String unformattedMessage, int cost) {
        return unformattedMessage.replace(COST_INSERTION_IDENTIFIER, String.valueOf(cost));
    }

    public static String compilePortal(String unformattedMessage, String portalName) {
        return unformattedMessage.replace(PORTAL_INSERTION_IDENTIFIER, portalName);
    }
}
