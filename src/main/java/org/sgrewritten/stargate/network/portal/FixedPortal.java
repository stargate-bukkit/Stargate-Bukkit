package org.sgrewritten.stargate.network.portal;

import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.gate.control.GateTextDisplayHandler;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.formatting.*;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.Gate;

import java.util.Set;
import java.util.UUID;

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
    public FixedPortal(Network network, String name, String destinationName, Set<PortalFlag> flags, Gate gate, UUID ownerUUID, LanguageManager languageManager, StargateEconomyAPI economyAPI) throws NameLengthException {
        super(network, name, flags, gate, ownerUUID, languageManager, economyAPI);
        this.destinationName = destinationName;
        this.destination = network.getPortal(destinationName);
    }

    @Override
    public void drawControlMechanisms() {
        GateTextDisplayHandler display = super.getPortalTextDisplay();
        if (display == null) {
            return;
        }
        FormattableObject[] lines = new FormattableObject[4];
        lines[0] = new PortalFormattingObject(this, HighlightingStyle.MINUS_SIGN);
        lines[2] = new NetworkFormattingObject(network, this.hasFlag(PortalFlag.HIDE_NETWORK));
        Portal destination = getDestination();
        if (destination != null) {
            lines[1] = new PortalFormattingObject(destination, HighlightingStyle.LESSER_GREATER_THAN);
        } else {
            lines[1] = new StringFormattableObject(destinationName, HighlightingStyle.LESSER_GREATER_THAN);
            lines[3] = new StringFormattableObject(languageManager.getString(TranslatableMessage.DISCONNECTED), HighlightingStyle.SQUARE_BRACKETS);
        }
        display.displayText(lines);
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