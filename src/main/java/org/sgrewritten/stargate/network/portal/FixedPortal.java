package org.sgrewritten.stargate.network.portal;

import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.format.*;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import java.util.*;

/**
 * A portal with a fixed destination
 */
public class FixedPortal extends AbstractPortal {

    private final String destinationName;

    /**
     * Instantiates a new fixed portal
     *
     * @param network         <p>The network the portal belongs to</p>
     * @param name            <p>The name of the portal</p>
     * @param destinationName <p>The name of the destination portal</p>
     * @param flags           <p>The flags enabled for the portal</p>
     * @param gate            <p>The gate format used by this portal</p>
     * @param ownerUUID       <p>The UUID of the portal's owner</p>
     * @throws NameLengthException
     */
    public FixedPortal(Network network, String name, String destinationName, Set<PortalFlag> flags, Set<Character> unrecognisedFlags, GateAPI gate,
                       UUID ownerUUID, LanguageManager languageManager, StargateEconomyAPI economyAPI) throws NameLengthException {
        super(network, name, flags, unrecognisedFlags, gate, ownerUUID, languageManager, economyAPI);
        this.destinationName = destinationName;
        this.destination = network.getPortal(destinationName);
    }

    @Override
    public SignLine[] getDrawnControlLines() {
        SignLine[] lines = new SignLine[4];
        lines[0] =  new PortalLine(super.colorDrawer.formatPortalName(this, HighlightingStyle.MINUS_SIGN),this, SignLineType.THIS_PORTAL);
        lines[2] = new NetworkLine(super.colorDrawer.formatNetworkName(network, network.getHighlightingStyle()),network);
        Portal destination = getDestination();
        if (destination != null) {
            lines[1] = new PortalLine(super.colorDrawer.formatPortalName(destination, HighlightingStyle.LESSER_GREATER_THAN),getDestination(),SignLineType.DESTINATION_PORTAL);
            lines[3] = new TextLine();
        } else {
            lines[1] = new TextLine(super.colorDrawer.formatLine(destinationName),SignLineType.DESTINATION_PORTAL);
            lines[3] = new TextLine(super.colorDrawer.formatErrorLine(super.languageManager.getString(
                    TranslatableMessage.DISCONNECTED), HighlightingStyle.SQUARE_BRACKETS),SignLineType.ERROR);
        }
        return lines;
    }

    @Override
    public Portal getDestination() {
        return this.network.getPortal(destinationName);
    }

    @Override
    public String getDestinationName() {
        return destinationName;
    }

    @Override
    public void close(boolean force) {
        super.close(force);
        this.openFor = null;
    }

}