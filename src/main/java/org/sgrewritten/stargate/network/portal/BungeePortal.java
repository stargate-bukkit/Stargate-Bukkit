package org.sgrewritten.stargate.network.portal;

import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.gate.control.GateTextDisplayHandler;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkType;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.formatting.*;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.BungeeNameException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.util.NameHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A portal representing a legacy BungeeCord portal
 *
 * <p>This portal type uses several cheats to make the legacy BungeeCord logic work with the new database.</p>
 */
public class BungeePortal extends AbstractPortal {

    private final Network fakeNetwork;
    private final LegacyVirtualPortal targetPortal;
    private final String serverDestination;
    private final String bungeeString;

    /**
     * Instantiates a new Bungee Portal
     *
     * @param network           <p>A reference to the Legacy Bungee network</p>
     * @param name              <p>The name of the portal</p>
     * @param destination       <p>The destination of the portal</p>
     * @param destinationServer <p>The destination server to connect to</p>
     * @param flags             <p>The flags enabled for this portal</p>
     * @param gate              <p>The gate format used by this portal</p>
     * @param ownerUUID         <p>The UUID of this portal's owner</p>
     * @throws InvalidNameException       <p>If the portal name is invalid</p>
     * @throws BungeeNameException
     * @throws NameLengthException
     * @throws UnimplementedFlagException
     */
    public BungeePortal(Network network, String name, String destination, String destinationServer,
                        Set<PortalFlag> flags, Gate gate, UUID ownerUUID, LanguageManager languageManager, StargateEconomyAPI economyAPI) throws InvalidNameException, BungeeNameException, NameLengthException, UnimplementedFlagException {
        super(network, name, flags, gate, ownerUUID, languageManager, economyAPI);


        destination = NameHelper.getTrimmedName(destination);
        destinationServer = NameHelper.getTrimmedName(destinationServer);

        if (destination == null || destination.isEmpty() || destinationServer == null || destinationServer.isEmpty()) {
            throw new BungeeNameException("Lacking sign information for bungee portal", TranslatableMessage.BUNGEE_LACKING_SIGN_INFORMATION);
        }

        /*
         * Create a virtual portal that handles everything related
         * to moving the player to a different server. This is set
         * as destination portal, of course.
         *
         * Note that this is only used locally inside this portal
         * and can not be found (should not) in any network anywhere.
         */
        targetPortal = new LegacyVirtualPortal(destinationServer, destination, network,
                new HashSet<>(), ownerUUID);
        this.serverDestination = destinationServer;
        /*
         * CHEATS! we love cheats. This one helps to save the legacy bungee gate into sql table so that the
         * target server is stored as a replacement to network.
         */
        fakeNetwork = new LocalNetwork(destinationServer, NetworkType.CUSTOM);
        String possibleBungeeString = super.languageManager.getString(TranslatableMessage.BUNGEE_SIGN_LINE_4);
        bungeeString = (possibleBungeeString == null) ? "[PlaceHolder]" : possibleBungeeString;
    }

    /**
     * Gets the name of the legacy network used for all legacy BungeeCord portals
     *
     * @return <p>The name of the legacy network</p>
     */
    public static String getLegacyNetworkName() {
        return ConfigurationHelper.getString(ConfigurationOption.LEGACY_BUNGEE_NETWORK);
    }

    @Override
    public void drawControlMechanisms() {
        Stargate.log(Level.FINEST, "serverDestination = " + serverDestination);
        GateTextDisplayHandler display = super.getPortalTextDisplay();
        if (display == null) {
            return;
        }
        FormattableObject[] lines = new FormattableObject[4];
        lines[0] = new PortalFormattingObject(this, HighlightingStyle.MINUS_SIGN);
        lines[1] = new PortalFormattingObject(getDestination(), HighlightingStyle.LESSER_GREATER_THAN);
        lines[2] = new StringFormattableObject(serverDestination, HighlightingStyle.SQUARE_BRACKETS);
        lines[3] = new StringFormattableObject(bungeeString, HighlightingStyle.NOTHING);
        display.displayText(lines, this);
    }

    @Override
    public Portal getDestination() {
        return targetPortal;
    }

    @Override
    public String getDestinationName() {
        return targetPortal.getName();
    }

    @Override
    public Network getNetwork() {
        return fakeNetwork;
    }

}
