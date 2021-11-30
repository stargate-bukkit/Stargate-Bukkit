package net.TheDgtl.Stargate.util;

public class TranslatableMessageFormater {

    static private final String COST_INSERTION_IDENTIFIER = "%cost%";
    static private final String PORTAL_INSERTION_IDENTIFIER = "%portal%";
    
    /**
     * Inserts the cost into the unformated message on any %cost% string
     * @param messageType
     * @param cost
     * @return
     */
    public static String compileCost(String unformatedMessage, int cost) {
        return unformatedMessage.replace(COST_INSERTION_IDENTIFIER, String.valueOf(cost));
    }
    
    public static String compilePortal(String unformatedMessage, String portalName) {
        return unformatedMessage.replace(PORTAL_INSERTION_IDENTIFIER, portalName);
    }
}
