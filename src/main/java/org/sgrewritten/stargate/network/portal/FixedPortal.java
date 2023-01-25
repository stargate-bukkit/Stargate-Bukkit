package org.sgrewritten.stargate.network.portal;

import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

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
     * @throws InvalidNameException <p>If the portal name is invalid</p>
     * @throws NameLengthException
     */
    public FixedPortal(Network network, String name, String destinationName, Set<PortalFlag> flags, Gate gate,
                       UUID ownerUUID, LanguageManager languageManager, StargateEconomyAPI economyAPI) throws InvalidNameException, NameLengthException {
        super(network, name, flags, gate, ownerUUID, languageManager, economyAPI);
        this.destinationName = destinationName;
        this.destination = network.getPortal(destinationName);
    }

    @Override
    public void drawControlMechanisms() {
        String[] lines = new String[4];
        lines[0] = super.colorDrawer.formatPortalName(this, HighlightingStyle.MINUS_SIGN);
        lines[2] = !this.hasFlag(PortalFlag.HIDE_NETWORK) ? super.colorDrawer.formatNetworkName(network, network.getHighlightingStyle()) : "";
        Portal destination = getDestination();
        if (destination != null) {
            lines[1] = super.colorDrawer.formatPortalName(destination, HighlightingStyle.LESSER_GREATER_THAN);
        } else {
            lines[1] = super.colorDrawer.formatLine(destinationName);
            lines[3] = super.colorDrawer.formatErrorLine(super.languageManager.getString(
                    TranslatableMessage.DISCONNECTED), HighlightingStyle.SQUARE_BRACKETS);
        }
        getGate().drawControlMechanisms(lines, !hasFlag(PortalFlag.ALWAYS_ON));
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