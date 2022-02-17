package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.NetworkAPI;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;

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
     * @param ownerUUID       <p>The UUID of the portal's owner</p>
     * @throws NameErrorException <p>If the portal name is invalid</p>
     */
    public FixedPortal(NetworkAPI network, String name, String destinationName, Set<PortalFlag> flags, Gate gate,
                       UUID ownerUUID, StargateLogger logger) throws NameErrorException {
        super(network, name, flags, gate, ownerUUID, logger);
        this.destinationName = destinationName;
        this.destination = network.getPortal(destinationName);
    }

    @Override
    public void drawControlMechanisms() {
        String[] lines = new String[4];
        lines[0] = super.colorDrawer.formatPortalName(this, HighlightingStyle.PORTAL);
        lines[2] = !this.hasFlag(PortalFlag.HIDE_NETWORK) ? super.colorDrawer.formatLine(this.network.getHighlightedName()) : "";
        Portal destination = loadDestination();
        if (destination != null) {
            lines[1] = super.colorDrawer.formatPortalName(loadDestination(), HighlightingStyle.DESTINATION);
        } else {
            lines[1] = super.colorDrawer.formatLine(destinationName);
            lines[3] = super.colorDrawer.formatErrorLine(Stargate.languageManager.getString(
                    TranslatableMessage.DISCONNECTED), HighlightingStyle.BUNGEE);
        }
        getGate().drawControlMechanisms(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    @Override
    public Portal loadDestination() {
        Portal destination = this.network.getPortal(destinationName);
        if (destination == null) {
            destination = new InvalidPortal(destinationName);
        }
        return destination;
    }
    
    @Override
    public void close(boolean force) {
        super.close(force);
        this.openFor = null;
    }

}