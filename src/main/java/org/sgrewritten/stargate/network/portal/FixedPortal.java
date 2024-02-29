package org.sgrewritten.stargate.network.portal;

import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.NetworkLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.PortalLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.TextLineData;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.name.NameLengthException;

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
    public FixedPortal(Network network, String name, String destinationName, Set<PortalFlag> flags, Set<Character> unrecognisedFlags, GateAPI gate, UUID ownerUUID, LanguageManager languageManager, StargateEconomyAPI economyAPI, String metaData) throws NameLengthException {
        super(network, name, flags, unrecognisedFlags, gate, ownerUUID, languageManager, economyAPI, metaData);
        this.destinationName = destinationName;
        this.destination = network.getPortal(destinationName);
    }

    @Override
    public LineData[] getDrawnControlLines() {
        LineData[] lines = new LineData[4];
        lines[0] = new PortalLineData(this, SignLineType.THIS_PORTAL);
        lines[2] = new NetworkLineData(network);
        if (destination != null) {
            lines[1] = new PortalLineData(destination, SignLineType.DESTINATION_PORTAL);
            lines[3] = new TextLineData();
        } else {
            lines[1] = new PortalLineData(destinationName, SignLineType.DESTINATION_PORTAL);
            lines[3] = new TextLineData(super.languageManager.getString(TranslatableMessage.DISCONNECTED), SignLineType.ERROR);
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