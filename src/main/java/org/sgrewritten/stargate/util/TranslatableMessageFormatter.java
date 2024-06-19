package org.sgrewritten.stargate.util;

import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.network.NetworkType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The translatable message formatter is responsible for formatting translatable messages
 *
 * <p>The translatable message formatter is mainly used for replacing placeholders in translatable message</p>
 */
public final class TranslatableMessageFormatter {

    private static final String COST_INSERTION_IDENTIFIER = "%cost%";
    private static final String PORTAL_INSERTION_IDENTIFIER = "%portal%";
    private static final String WORLD_INSERTION_IDENTIFIER = "%world%";
    private static final String NETWORK_NAME_IDENTIFIER = "%network%";
    private static final String FLAGS_NAME_IDENTIFIER = "%flags%";
    private static final String VERSION_IDENTIFIER = "%version%";

    private TranslatableMessageFormatter() {

    }

    /**
     * Replaces the %cost% in a string with the given cost
     *
     * @param unformattedMessage <p>The unformatted message to format</p>
     * @param cost               <p>The cost to insert</p>
     * @return <p>The message with the cost placeholder replaced with the given cost</p>
     */
    public static String formatCost(String unformattedMessage, double cost) {
        return unformattedMessage.replace(COST_INSERTION_IDENTIFIER, String.valueOf(cost));
    }

    /**
     * Replaces the %portal% in a string with the given portal name
     *
     * @param unformattedMessage <p>The unformatted message to format</p>
     * @param portalName         <p>The portal name to replace the placeholder with</p>
     * @return <p>The message with the portal placeholder replaced with the given portal name</p>
     */
    public static String formatPortal(String unformattedMessage, String portalName) {
        return unformattedMessage.replace(PORTAL_INSERTION_IDENTIFIER, portalName);
    }

    /**
     * Replaces the %world% in a string with the given world name
     *
     * @param unformattedMessage <p>The unformatted message to format</p>
     * @param worldName          <p>The world name to replace the placeholder with</p>
     * @return <p>The message with the world placeholder replaced with the given world name</p>
     */
    public static String formatWorld(String unformattedMessage, String worldName) {
        return unformattedMessage.replace(WORLD_INSERTION_IDENTIFIER, worldName);
    }

    /**
     * Replaces the %network% in a string with the given network name
     *
     * @param unformattedMessage <p>The unformatted message to format</p>
     * @param networkName        <p>The network name to replace the placeholder with</p>
     * @return <p>The message with the network name placeholder replaced with the given network name</p>
     */
    public static String formatNetwork(String unformattedMessage, String networkName) {
        return unformattedMessage.replace(NETWORK_NAME_IDENTIFIER, networkName);
    }

    /**
     * Replaces the %flags% in a string with the given set of flags
     *
     * @param unformattedMessage <p>The unformatted message to format</p>
     * @param flags              <p>The flags to replace the placeholder with</p>
     * @return <p>The message with the flags placeholder replaced with the given flags</p>
     */
    public static String formatFlags(String unformattedMessage, Set<PortalFlag> flags) {
        String flagsString = formatFlagsString(new ArrayList<>(flags));
        return unformattedMessage.replace(FLAGS_NAME_IDENTIFIER, flagsString);
    }

    /**
     * Replaces the %version% in a string with the given version
     *
     * @param unformattedMessage <p>The unformatted message to format</p>
     * @param version            <p>The version to replace the placeholder with</p>
     * @return <p>The message with the version placeholder replaced with the given version</p>
     */
    public static String formatVersion(String unformattedMessage, String version) {
        return unformattedMessage.replace(VERSION_IDENTIFIER, version);
    }

    /**
     * Formats a list of flags to a string
     *
     * @param flags <p>The flags to format</p>
     * @return <p>A string listing the flags</p>
     */
    private static String formatFlagsString(List<PortalFlag> flags) {
        String characterRepresentation = String.valueOf(flags.get(0).getCharacterRepresentation());
        if (flags.size() < 2) {
            return characterRepresentation;
        } else if (flags.size() == 2) {
            return characterRepresentation + "&" + formatFlagsString(flags.subList(1, flags.size()));
        } else {
            return characterRepresentation + "," + formatFlagsString(flags.subList(1, flags.size()));
        }
    }

    /**
     * Format the {@link TranslatableMessage#UNIMPLEMENTED_CONFLICT}
     *
     * @param interServer     <p> The inter-server network at conflict </p>
     * @param local           <p> The local network at conflict (none if null) </p>
     * @param languageManager <p> A languageManager </p>
     * @return <p> A formated {@link TranslatableMessage#UNIMPLEMENTED_CONFLICT} message
     */
    public static String formatUnimplementedConflictMessage(Network interServer, @Nullable Network local, LanguageManager languageManager) {
        String initialMessage = languageManager.getWarningMessage(TranslatableMessage.UNIMPLEMENTED_CONFLICT);
        NetworkType localType = (local == null) ? interServer.getType() : local.getType();

        String localTypeString = languageManager.getString(localType.getTerminology());
        String interServerTypeString = languageManager.getString(interServer.getType().getTerminology()) + " " + languageManager.getString(TranslatableMessage.FANCY_INTERSERVER);
        return initialMessage.replace("%name%", interServer.getName()).replace("%type1%", interServerTypeString.toLowerCase()).replace("%type2%", localTypeString.toLowerCase());
    }

    public static String formatNetworkType(String message, NetworkType type, LanguageManager languageManager) {
        return message.replace("%type%", languageManager.getString(type.getTerminology()));
    }
}
