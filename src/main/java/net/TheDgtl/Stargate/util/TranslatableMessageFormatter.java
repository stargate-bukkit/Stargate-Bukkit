package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.network.portal.PortalFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The translatable message formatter is responsible for formatting translatable messages
 *
 * <p>The translatable message formatter is mainly used for replacing placeholders in translatable message</p>
 */
public class TranslatableMessageFormatter {

    static private final String COST_INSERTION_IDENTIFIER = "%cost%";
    static private final String PORTAL_INSERTION_IDENTIFIER = "%portal%";
    static private final String WORLD_INSERTION_IDENTIFIER = "%world%";
    static private final String NET_NAME_IDENTIFIER = "%network%";
    static private final String FLAGS_NAME_IDENTIFIER = "%flags%";
    static private final String VERSION_IDENTIFIER = "%version%";

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

    public static String compileNetwork(String unformattedMessage, String netName) {
        return unformattedMessage.replace(NET_NAME_IDENTIFIER, netName);
    }

    public static String compileFlags(String unformatedMessage, Set<PortalFlag> dissallowedFlags) {
        String flagsString = compileFlagsString(new ArrayList<>(dissallowedFlags));
        return unformatedMessage.replace(FLAGS_NAME_IDENTIFIER, flagsString);
    }

    public static String compileFlagsString(List<PortalFlag> dissallowedFlags) {
        String chracterRepresentation = dissallowedFlags.get(0).getCharacterRepresentation().toString();
        if (dissallowedFlags.size() < 2)
            return chracterRepresentation;
        if (dissallowedFlags.size() == 2)
            return chracterRepresentation + "&" + compileFlagsString(dissallowedFlags.subList(1, dissallowedFlags.size()));
        return chracterRepresentation + "," + compileFlagsString(dissallowedFlags.subList(1, dissallowedFlags.size()));
    }

    public static String compileVersion(String unformatedMessage, String version) {
        return unformatedMessage.replace(VERSION_IDENTIFIER, version);
    }

}
